/*
 * ***********************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public License   *
 *  (LGPL-3.0-or-later)                                                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.springboot2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.spring.APIError
import org.lightningj.paywall.spring.PaywallProperties
import org.lightningj.paywall.spring.response.SettlementResponse
import org.lightningj.paywall.spring.websocket.WebSocketSettledPaymentHandler
import org.lightningj.paywall.springboot2.paymenthandler.*
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.Base58
import org.lightningj.paywall.web.HTTPConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.TestPropertySource
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.lang.reflect.Type
import java.security.SecureRandom
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

import static org.lightningj.paywall.spring.websocket.PaywallWebSocketConfig.CHECK_SETTLEMENT_QUEUE_PREFIX

/**
 * Functional test of PaywallInterceptor and LocalPaymentFlow. It runs
 * defined test scienarios against running web service with a mocked LightningHandler.
 *
 * @author philip 2019-04-12
 *
 */
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/test_application.properties")
class LocalWebSocketIntegrationSpec extends Specification {

    @LocalServerPort
    int randomServerPort

    @Autowired
    ArticleDataRepository articleDataRepository

    @Autowired
    DemoMinimalPaymentDataRepository demoPaymentDataRepository

    @Autowired
    DemoPaymentHandler demoPaymentHandler

    @Autowired
    LightningHandler lightningHandler

    @Autowired
    TokenGenerator tokenGenerator

    @Autowired
    PaywallProperties paywallProperties

    @Autowired
    WebSocketSettledPaymentHandler webSocketSettledPaymentHandler


    @Shared RESTClient restClient

    String baseurl

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup() {

        restClient = new RESTClient( "http://localhost:${randomServerPort}" )

        restClient.handler.failure = {
            resp, data ->
                resp.data = data
                return resp
        }

        if (articleDataRepository.findByArticleId("abc123") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abc123"
            articleData.price = 10
            articleDataRepository.save(articleData)
        }
        if (articleDataRepository.findByArticleId("abcPayPerRequest") == null) {
            ArticleData articleData = new ArticleData()
            articleData.articleId = "abcPayPerRequest"
            articleData.price = 15
            articleDataRepository.save(articleData)
        }

        baseurl = "ws://localhost:${randomServerPort}"
    }

    def "Verify successful web socket payment flow where settlement is done after WebSocket is connected."(){
        setup:
        def handler = new CheckSettlementStompFrameHandler()
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/demo' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        when:

        // subscribe to channel
        def websocket = subscribe(invoice, handler)

        then:
        // Verify that no message was returned, and WebSocket is waiting
        waitForMessage(handler,1, TimeUnit.SECONDS) == null

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        def settlement = waitForMessage(handler,1, TimeUnit.SECONDS)

        then:
        settlement != null
        verifyCheckSettlementResponse(settlement,invoice,[expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/demo', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "DemoService, test3!"

        when:
        // Verify that it is possible to use the token multiple times until if not pay per request.
        resp = get(uri: '/demo', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "DemoService, test3!"

        when:
        // Verify that disconnect, releases related resources
        websocket.unsubscribe(websocket.getSubscriptionHeaders())
        then:
        webSocketSettledPaymentHandler.paymentListenerMap.size() == 0

    }

    def "Verify successful web socket payment flow where settlement is already settled when WebSocket connects."(){
        setup:
        def handler = new CheckSettlementStompFrameHandler()
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/demo' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))
        when:

        // subscribe to channel
        def websocket = subscribe(invoice,handler)

        then:
        // Verify that message was returned.
        def settlement = waitForMessage(handler,1, TimeUnit.SECONDS)

        then:
        settlement != null
        verifyCheckSettlementResponse(settlement,invoice,[expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/demo', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "DemoService, test3!"

    }

    def "Verify error is thrown if queue name preImageHash doesn't match JWT Token."(){
        setup:
        def handler = new CheckSettlementStompFrameHandler()
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/demo' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        when:

        // subscribe to channel
        def websocket = subscribe(invoice, handler, CHECK_SETTLEMENT_QUEUE_PREFIX + Base58.encodeToString("abc".bytes))

        then:
        // Verify that api error was returned
        APIError error = waitForMessage(handler,1, TimeUnit.SECONDS)

        then:
        error != null
        verifyAPIError(error, [statusCode:400,
                               message: "Invalid Request: Token preImageHash doesn't match WebSocket name.",
                               errors: ["Token preImageHash doesn't match WebSocket name."]])

    }

    def "Verify web socket reconnect reuses existing listener"(){
        setup:
        def handler = new CheckSettlementStompFrameHandler()
        when: // First call end-point to receive invoice, and check that payment object
        // have been created in database.
        println "Starting Test Setup on Port: " + randomServerPort

        def resp = get(uri: '/demo' )
        then:
        resp.status == 402
        resp.contentType == "application/json"
        def invoice = resp.data
        // Check that invoice response has correct json
        verifyJsonInvoiceResponse(invoice)
        // Verify that payment data was created in database.
        verifyPaymentData(invoice,demoPaymentHandler.findPaymentData(Base58.decode(resp.data.preImageHash)))
        when:

        // subscribe to channel
        def websocket1 = subscribe(invoice, handler)
        Thread.sleep(100)
        then:
        webSocketSettledPaymentHandler.paymentListenerMap.size() == 1

        when:
        def websocket2 = subscribe(invoice, handler)

        then:
        // Verify that no message was returned, and WebSocket is waiting
        waitForMessage(handler, 1, TimeUnit.SECONDS) == null
        webSocketSettledPaymentHandler.paymentListenerMap.size() == 1

        when:
        // Simulate lightning invoice is payed in lightning handler.
        lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))

        def settlement = waitForMessage(handler, 1, TimeUnit.SECONDS)

        then:
        settlement != null
        verifyCheckSettlementResponse(settlement,invoice,[expectSettled: true])

        when:
        // Verify that with settled token it is possible to perform call.
        resp = get(uri: '/demo', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
        then:
        resp.status == 200
        resp.data.id != null
        resp.data.content == "DemoService, test3!"

    }


    def "Long run 10 parallel payment workflows."(){
        setup:
        lightningHandler.invoiceValidity = Duration.of(1, ChronoUnit.MINUTES)
        when:
        List testFlows = []
        List threads = []
        (1..10).each {
            TestFlow tf = new TestFlow(it)
            Thread thread = new Thread(tf)
            thread.start()
            threads << thread
            testFlows << tf
        }

        threads.each { Thread it ->
            it.join()
        }
        then:
        testFlows.each { TestFlow it ->
            assert it.success
        }
    }


    @Ignore// This test is disabled by default since it takes a long time, run if needed.
    def "Long run verify removal of expired payment listener "(){
        setup:
        lightningHandler.invoiceValidity = Duration.of(1, ChronoUnit.MINUTES)
        when:
        for(int i=1;i<1000;i++){
            def handler = new CheckSettlementStompFrameHandler()
            def resp = get(uri: '/demo' )
            assert resp.status == 402
            def invoice = resp.data

            WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()))
            stompClient.setMessageConverter(new StringMessageConverter())
            StompSession stompSession = stompClient.connect(baseurl + invoice.checkSettlementWebSocketEndpoint, new StompSessionHandlerAdapter() {
            }).get(1, TimeUnit.MINUTES)

            StompHeaders stompHeaders = new StompHeaders()
            stompHeaders.setDestination(invoice.checkSettlementWebSocketQueue)


            stompHeaders.set("token",invoice.token)

            def websocket = stompSession.subscribe(stompHeaders, handler)

            waitForMessage(handler,500, TimeUnit.MILLISECONDS)
            stompSession.disconnect()

            println("Processed: ${i}, paymentHandler ${demoPaymentHandler.paymentEventBus.listeners.size()}, listeners ${webSocketSettledPaymentHandler.paymentListenerMap.size()}, expired: ${webSocketSettledPaymentHandler.expiringListeners.size()}")
        }
        then:
        true
    }

    private def get(Map m){
        URIBuilder urlBuilder = new URIBuilder(m.uri)
        return restClient.get(path: urlBuilder.path, query: urlBuilder.query, headers: m.headers)
    }

    private void verifyJsonInvoiceResponse(Map jsonData, Map expectedData = [:]){
        assert jsonData.type == "invoice"
        assert jsonData.preImageHash != null
        assert jsonData.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert jsonData.description == "Some description"
        assert jsonData.invoiceAmount.value == (expectedData.expectedValue != null ? expectedData.expectedValue : 10)
        assert jsonData.invoiceAmount.currencyCode == "BTC"
        assert jsonData.invoiceAmount.magnetude == "NONE"
        assert jsonData.nodeInfo.publicKeyInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa"
        assert jsonData.nodeInfo.nodeAddress == "10.10.10.10"
        assert jsonData.nodeInfo.nodePort == 5735
        assert jsonData.nodeInfo.mainNet == true
        assert jsonData.nodeInfo.connectString == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        assert jsonData.token != null
        assert jsonData.invoiceDate != null
        assert jsonData.invoiceExpireDate != null
        assert jsonData.payPerRequest == (expectedData.payPerRequest != null ? expectedData.payPerRequest : false)
        assert jsonData.requestPolicyType == "WITH_BODY"
        assert jsonData.checkSettlementLink == "/paywall/api/checkSettlement?pwir=" + URLEncoder.encode(jsonData.token, "UTF-8")
        assert jsonData.qrLink == "/paywall/genqrcode?d=" + URLEncoder.encode(jsonData.bolt11Invoice,"UTF-8")
        assert jsonData.checkSettlementWebSocketEndpoint == "/paywall/api/websocket/checksettlement"
        assert jsonData.checkSettlementWebSocketQueue == "/queue/paywall/checksettlement/" + jsonData.preImageHash
    }

    private void verifyCheckSettlementResponse(SettlementResponse resp, Map invoice, Map expectedData = [:]){
        assert resp.type == "settlement"
        boolean expectSettled =  expectedData.expectSettled != null ? expectedData.expectSettled : false
        assert resp.settled == expectSettled
        assert resp.status == "OK"
        if(expectSettled){
            assert resp.preImageHash == invoice.preImageHash
            assert resp.token != null
            assert resp.settlementValidUntil != null
            assert resp.payPerRequest == (expectedData.expectPayPerRequest != null ? expectedData.expectPayPerRequest : false)
        }else{
            assert resp.preImageHash == null
            assert resp.token == null
            assert resp.settlementValidUntil == null
            assert resp.payPerRequest == null
        }
        assert resp.settlementValidFrom == null
    }

    private void verifyAPIError(APIError error,  Map expectedData){
        assert error.status.value() == expectedData.statusCode
        assert error.message == expectedData.message
        assert error.errors == expectedData.errors
    }


    private void verifyPaymentData(Map jsonData, DemoFullPaymentData data, Map expectedData = [:]){
        assert data.id > 0
        assert data.preImageHash == Base58.decode(jsonData.preImageHash)
        assert data.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert data.description == "Some description"
        assert data.orderAmount.value > 0
        assert data.orderAmount.value == jsonData.invoiceAmount.value
        assert data.invoiceAmount.value == data.orderAmount.value
        assert data.settled == (expectedData.expectSettled != null ? expectedData.expectSettled : false)
        assert data.invoiceDate != null
        assert data.invoiceExpireDate != null
        if(data.settled){
            assert data.settledAmount.value == data.orderAmount.value
            assert data.settlementDate != null
        }
        assert data.payPerRequest == (expectedData.expectPayPerRequest!= null ? expectedData.expectPayPerRequest : false)
        assert data.executed == (expectedData.expectExecuted!= null ? expectedData.expectExecuted : false)
    }

    private Object waitForMessage(CheckSettlementStompFrameHandler handler, long timeout, TimeUnit timeUnit){

        try{
            Object retval
            String message = handler.completableFuture.get(timeout, timeUnit)
            ObjectMapper mapper = new ObjectMapper()

            JsonNode jsonNode = mapper.readTree(message)
            if(jsonNode.get("status").asText() == "OK"){
                retval = mapper.readValue(message, SettlementResponse.class)
            }else{
                retval = mapper.readValue(message, APIError.class)
            }
            return retval
        }catch(TimeoutException){
            return null
        }
    }

    private StompSession.Subscription subscribe(Map invoice, CheckSettlementStompFrameHandler handler, String queueName = null){
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()))
        stompClient.setMessageConverter(new StringMessageConverter())
        StompSession stompSession = stompClient.connect(baseurl + invoice.checkSettlementWebSocketEndpoint, new StompSessionHandlerAdapter() {
        }).get(1, TimeUnit.MINUTES)

        StompHeaders stompHeaders = new StompHeaders()
        if(queueName == null) {
            stompHeaders.setDestination(invoice.checkSettlementWebSocketQueue)
        }else{
            stompHeaders.setDestination(queueName)
        }

        stompHeaders.set("token",invoice.token)

        return stompSession.subscribe(stompHeaders, handler)
    }



    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class CheckSettlementStompFrameHandler implements StompFrameHandler {
        CompletableFuture<Object> completableFuture  = new CompletableFuture<>();

        @Override
        Type getPayloadType(StompHeaders stompHeaders) {
            return String.class
        }

        @Override
        void handleFrame(StompHeaders stompHeaders, Object o) {
            completableFuture.complete( o.toString())
        }
    }

    SecureRandom random = new SecureRandom()
    private class TestFlow implements Runnable{

        boolean success=false
        int threadNumber
        RESTClient restClient
        private TestFlow(int threadNumber){
            this.threadNumber = threadNumber

            restClient = new RESTClient( "http://localhost:${randomServerPort}" )

            restClient.handler.failure = {
                resp, data ->
                    resp.data = data
                    return resp
            }

        }

        @Override
        void run() {
            println "Starting thread: " + threadNumber
            for(int i=1;i<100;i++){
                def handler = new CheckSettlementStompFrameHandler()
                def resp = get(uri: '/demo' )
                assert resp.status == 402
                def invoice = resp.data

                WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()))
                stompClient.setMessageConverter(new StringMessageConverter())
                StompSession stompSession = stompClient.connect(baseurl + invoice.checkSettlementWebSocketEndpoint, new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.MINUTES)

                StompHeaders stompHeaders = new StompHeaders()
                stompHeaders.setDestination(invoice.checkSettlementWebSocketQueue)


                stompHeaders.set("token",invoice.token)

                lightningHandler.simulateSettleInvoice(Base58.decode(invoice.preImageHash))
                stompSession.subscribe(stompHeaders, handler)

                def settlement = waitForMessage(handler,500, TimeUnit.MILLISECONDS)
                stompSession.disconnect()

                resp = get(uri: '/demo', headers: [(HTTPConstants.HEADER_PAYMENT): settlement.token ])
                assert resp.status == 200

                Thread.sleep(random.nextInt(100))

                println("Processed: ${threadNumber}:${i}, paymentHandler ${demoPaymentHandler.paymentEventBus.listeners.size()}, listeners ${webSocketSettledPaymentHandler.paymentListenerMap.size()}, expired: ${webSocketSettledPaymentHandler.expiringListeners.size()}")
            }
            success = true
        }

        private def get(Map m){
            URIBuilder urlBuilder = new URIBuilder(m.uri)
            return restClient.get(path: urlBuilder.path, query: urlBuilder.query, headers: m.headers)
        }
    }
}
