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

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient
import org.lightningj.paywall.lightninghandler.LightningHandler
import org.lightningj.paywall.springboot2.paymenthandler.ArticleData
import org.lightningj.paywall.springboot2.paymenthandler.ArticleDataRepository
import org.lightningj.paywall.springboot2.paymenthandler.DemoPaymentDataRepository
import org.lightningj.paywall.springboot2.paymenthandler.DemoPaymentHandler
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.OrderRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * TODO
 *
 * @author philip 2019-04-12
 *
 */
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/test_application.properties")
class LocalPaymentFlowIntegrationSpec extends Specification {

    @LocalServerPort
    int randomServerPort

    @Autowired
    ArticleDataRepository articleDataRepository

    @Autowired
    DemoPaymentDataRepository demoPaymentDataRepository

    @Autowired
    DemoPaymentHandler demoPaymentHandler

    @Autowired
    LightningHandler lightningHandler


    @Shared RESTClient restClient

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
    }

    // TODO Test both payPerRequest or without

    // Next step set up article inventory.
    // Next step is to start implementing filter with bells and wisels

    // Test all edge cases

    // How to test clock?


    def "Verify successful local paymentflow with Json and pay per request set to false."(){
        when: // First call end-point to receive
        println "Port: " + randomServerPort
        def resp = restClient.get( path: '/demo')
        then:

        resp.status == 402
        resp.contentType == "application/json"
        verifyJsonInvoiceResponse(resp.data)

//        when: // Then call the check settlement controller
//        resp = restClient.get( path: resp.data.checkSettlementLink, [])

        // TODO Exception

    }


    //Thread.sleep(130000)
    //def resp = restClient.get( path: '/demo' ,headers: ["Accept":"application/xml"])

    private void verifyJsonInvoiceResponse(Map jsonData, Map expectedData = [:]){
        assert jsonData.type == "invoice"
        assert jsonData.preImageHash != null
        assert jsonData.bolt11Invoice == "lntb10u1pwt6nk9pp59rulenhfxs7qcq867kfs3mx3pyehp5egjwa8zggaymp56kxr2hrsdqqcqzpgsn2swaz4q47u0dee8fsezqnarwlcjdhvdcdnv6avecqjldqx75yya7z8lw45qzh7jd9vgkwu38xeec620g4lsd6vstw8yrtkya96prsqru5vqa"
        assert jsonData.description == "Some description"
        assert jsonData.invoiceAmount.value == 10
        assert jsonData.invoiceAmount.currencyCode == "BTC"
        assert jsonData.invoiceAmount.magnetude == "NONE"
        assert jsonData.nodeInfo.publicKeyInfo == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa"
        assert jsonData.nodeInfo.nodeAddress == "10.10.10.10"
        assert jsonData.nodeInfo.nodePort == 5735
        assert jsonData.nodeInfo.mainNet == true
        assert jsonData.nodeInfo.connectString == "03978f437e05f64b36fa974b415049e6c36c0163b0af097bab3eb3642501055efa@10.10.10.10:5735"
        assert jsonData.token != null
        assert jsonData.invoiceDate == "2019-01-03T12:12:12.000+0000"
        assert jsonData.invoiceExpireDate == "2019-01-03T13:12:12.000+0000"
        assert jsonData.payPerRequest == expectedData.payPerRequest != null ? expectedData.payPerRequest.toString() : false
        assert jsonData.requestPolicyType == "WITH_BODY"
        assert jsonData.checkSettlementLink == "/paywall/api/checkSettlement"
        assert jsonData.qrLink == "/paywall/genqrcode"
    }



}
