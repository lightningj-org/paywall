/*
 *************************************************************************
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
package org.lightningj.paywall.btcpayserver

import io.grpc.netty.shaded.io.netty.buffer.ByteBuf
import io.grpc.netty.shaded.io.netty.buffer.Unpooled
import io.grpc.netty.shaded.io.netty.channel.ChannelFutureListener
import io.grpc.netty.shaded.io.netty.channel.ChannelHandler
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext
import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler
import io.grpc.netty.shaded.io.netty.handler.codec.http.DefaultFullHttpResponse
import io.grpc.netty.shaded.io.netty.handler.codec.http.FullHttpRequest
import io.grpc.netty.shaded.io.netty.handler.codec.http.FullHttpResponse
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpHeaderNames
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpMethod
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpObject
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpVersion
import io.grpc.netty.shaded.io.netty.util.CharsetUtil
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager
import org.lightningj.paywall.keymgmt.DummyKeyManagerInstance
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.TestWebServer
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

import static org.lightningj.paywall.btcpayserver.BTCPayServerHTTPSender.METHOD.*

/**
 * Unit tests for BTCPayServerHTTPSender
 *
 * Created by philip on 2018-10-15.
 */
class BTCPayServerHTTPSenderSpec extends Specification {

    @Shared AsymmetricKeyManager keyManager

    BTCPayServerHTTPSender sender
    TestHandler handler = new TestHandler()

    @Shared int port

    def setupSpec(){
        BCUtils.installBCProvider()
        keyManager =  DummyKeyManagerInstance.commonInstance
        port = findFreePort()
    }

    def setup(){
        sender = new BTCPayServerHTTPSender("http://localhost:${port}",keyManager)
    }

    def "Verify that sending contains the correct headers"(){
        setup:
        TestWebServer webServer = new TestWebServer(port,handler)
        webServer.startup()
        Thread.sleep(500)
        when: // Test sending signed messages is correct
        handler.init(HttpResponseStatus.OK,"""{"test1":"value1"}""","application/json")
        byte[] response = sender.send(POST,"/someendpoint","""{"requestdata1":"value1"}""".getBytes("UTF-8"),
                true,["param1":"value1","param2":"value2"])
        String respString = new String(response,"UTF-8")
        then:
        respString == """{"test1":"value1"}"""
        handler.uri == "/someendpoint?param1=value1&param2=value2"
        handler.headers["x-accept-version"] == "2.0.0"
        handler.headers["x-identity"] != null
        handler.headers["x-signature"] != null
        handler.headers["Content-Type"] == "application/json"
        handler.method.name() == "POST"
        handler.recievedData == """{"requestdata1":"value1"}"""

        when:  // Test unsending signed messages is correct
        handler.init(HttpResponseStatus.OK,"""{"test1":"value1"}""","application/json")
        response = sender.send(PUT,"/someendpoint","""{"requestdata1":"value1"}""".getBytes("UTF-8"),
                false)
        respString = new String(response,"UTF-8")
        then:
        respString == """{"test1":"value1"}"""
        handler.uri == "/someendpoint"
        handler.headers["x-accept-version"] == "2.0.0"
        handler.headers["x-identity"] == null
        handler.headers["x-signature"] == null
        handler.headers["Content-Type"] == "application/json"
        handler.method.name() == "PUT"
        handler.recievedData == """{"requestdata1":"value1"}"""

        when:  // Test get signed messages is correct
        handler.init(HttpResponseStatus.OK,"""{"test1":"value1"}""","application/json")
        response = sender.send(GET,"/someendpoint",
                false)
        respString = new String(response,"UTF-8")
        then:
        respString == """{"test1":"value1"}"""
        handler.uri == "/someendpoint"
        handler.headers["x-accept-version"] == "2.0.0"
        handler.headers["x-identity"] == null
        handler.headers["x-signature"] == null
        handler.headers["Content-Type"] == "application/json"
        handler.method.name() == "GET"
        handler.recievedData == """"""

        when:  // That invalid response code throws correct error
        handler.init(HttpResponseStatus.BAD_REQUEST,"""{"test1":"value1"}""","application/json")
        sender.send(GET,"/someendpoint",
                false)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Error communicating with BTC Pay Server: Bad Request"

        cleanup:
        webServer.shutdown()
    }


    @Unroll
    def "Verify constructQueryString generates expected parameter string"(){
        expect:
        sender.constructQueryString(queryParams) == expected
        where:
        queryParams                            | expected
        null                                   | ""
        [:]                                    | ""
        ["key1":"value1"]                      | "?key1=value1"
        ["key1":"value1","keyåäö2":"valueåäl"] | "?key1=value1&key%C3%A5%C3%A4%C3%B62=value%C3%A5%C3%A4l"
    }

    @ChannelHandler.Sharable
    private static class TestHandler extends SimpleChannelInboundHandler<HttpObject> {

        private TestHandler(){
        }

        private HttpResponseStatus responseCode
        private String responseMessage
        private String responseContentType


        String recievedData
        String uri
        Map headers = [:]
        HttpMethod method

        void init(HttpResponseStatus responseCode, String responseMessage, String responseContentType="plain/text"){
            this.responseCode = responseCode
            this.responseMessage = responseMessage
            this.responseContentType = responseContentType

            recievedData = null
            uri = null
            method = null
            headers.clear()
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest req = (FullHttpRequest) msg
                for(String name : req.headers().names()){
                    headers[name] = req.headers().get(name)
                }
                recievedData = msg.content().toString(StandardCharsets.UTF_8)
                uri = req.uri()
                method = req.method()
                ByteBuf content = Unpooled.copiedBuffer(responseMessage, CharsetUtil.UTF_8)
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseCode, content,false)
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, responseContentType)
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
            }

        }

        @Override
        void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush()
        }
    }

    static int findFreePort() {
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            socket.setReuseAddress(true)
            int port = socket.getLocalPort()
            return port
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port for unit test.")
    }
}
