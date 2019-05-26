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

import groovyx.net.http.RESTClient
import org.lightningj.paywall.util.BCUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification

import static org.lightningj.paywall.spring.controller.GenerateQRCodeController.*

/**
 * Integration test for testing QR Code Generation Controller.
 *
 * @author philip 2019-04-12
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/test_application.properties")
class QRCodeGenerationIntegrationSpec extends Specification {

    @LocalServerPort
    int randomServerPort

    @Shared RESTClient restClient

    static final String data = "lnbc30u1pwtvwpvpp5hvsr5ch6qsyp6wx7ek2sya2gy9lakzvc4fj96ez3kf4p4cu3gzasdp62phkcmr0vejk2epwvdhk6gpdypcxz7fqw3hjqen9v4jzqcmgd93kketwwvxqzjccqp2rzjqwac3nxyg3f5mfa4ke9577c4u8kvkx8pqtdsusqdfww0aymk823x6zyc0gqqqngqqqqqqq8mqqqqqqcqjqed7f8atc09d362xvmpryujfwjcycgar7lvlem5ulttsgh9eay58hhvwjqmqveqxjwk8gf4r9w0dux0prknclxz7euetkjnk9563022cpruw4jn"

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup() {
        restClient = new RESTClient( "http://localhost:${randomServerPort}" )
    }


    def "Generate a valid QR Code Generation Request with standared size"(){
        when:
        // Test with default heigth and width
        def resp1 = restClient.get( path: "/paywall/genqrcode", query: [
                (PARAMETER_DATA) : data
        ] )

        int image1Size = resp1.data.bytes.length
        then:

        resp1.status == 200
        resp1.contentType == "image/png"
        image1Size != 0
        resp1.containsHeader("Content-Length")

        when: // Generate a valid QR Code Generation Request with custom size
        def resp2 = restClient.get( path: "/paywall/genqrcode", query: [
                (PARAMETER_DATA) : data,
                (PARAMETER_WIDTH) : 2000,
                (PARAMETER_HEIGHT) : 2000

        ] )

        int image2Size = resp2.data.bytes.length
        then:
        resp2.status == 200
        resp2.contentType == "image/png"
        image2Size > image1Size
        resp2.containsHeader("Content-Length")
    }

    def "Generate a valid QR Code Generation Request with custom size"(){
        when:
        // Test with default heigth and width
        def resp = restClient.get( path: "/paywall/genqrcode", query: [
                (PARAMETER_DATA) : data,
                (PARAMETER_WIDTH) : 1000

        ] )

        then:

        resp.status == 200
        resp.contentType == "image/png"
        resp.data.bytes.length != 0
        resp.containsHeader("Content-Length")
    }

    def "Generate that QR Code Generation Request returns 400 for missing 'd' parameter"(){
        setup:
        def respStatus
        def respText
        restClient.handler.failure = {resp, data ->
            resp.setData(data)
            respStatus = resp.status
            respText = data.message
        }
        when:
        def failresp
        // Test with missing 'd' parameter
        restClient.get( path: "/paywall/genqrcode")

        then:
        respStatus == 400
        respText == "Invalid Request: Invalid request, parameter 'd' is required."
    }

    def "Generate that QR Code Generation Request returns 400 for invalid integer for 'w' parameter"(){
        setup:
        def respStatus
        def respText
        restClient.handler.failure = {resp, data ->
            resp.setData(data)
            respStatus = resp.status
            respText = data.message
        }
        when:
        def failresp
        // Test with missing 'd' parameter
        restClient.get( path: "/paywall/genqrcode", query: [
                (PARAMETER_DATA) : data,
                (PARAMETER_WIDTH) : "invalid",
                (PARAMETER_HEIGHT) : 2000

        ] )

        then:
        respStatus == 400
        respText == "Invalid Request: Invalid parameter 'w', should be an integer, not invalid"
    }

    def "Generate that QR Code Generation Request returns 400 for invalid integer for 'h' parameter"(){
        setup:
        def respStatus
        def respText
        restClient.handler.failure = {resp, data ->
            resp.setData(data)
            respStatus = resp.status
            respText = data.message
        }
        when:
        def failresp
        // Test with missing 'd' parameter
        restClient.get( path: "/paywall/genqrcode", query: [
                (PARAMETER_DATA) : data,
                (PARAMETER_WIDTH) : 2000,
                (PARAMETER_HEIGHT) : "invalid"

        ] )

        then:
        respStatus == 400
        respText == "Invalid Request: Invalid parameter 'h', should be an integer, not invalid"
    }

}
