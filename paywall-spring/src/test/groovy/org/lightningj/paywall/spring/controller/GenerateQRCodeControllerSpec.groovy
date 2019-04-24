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
package org.lightningj.paywall.spring.controller

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.spring.PaywallProperties
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import java.util.logging.Logger

import static GenerateQRCodeController.*


/**
 * Unit test for GenerateQRCodeController.
 * <p>
 *     This unit tests only tests help methods, see integration test for main controller.
 * </p>
 *
 * Created by Philip Vendil on 2019-04-16.
 */
class GenerateQRCodeControllerSpec extends Specification {

    GenerateQRCodeController controller = new GenerateQRCodeController()

    PaywallProperties paywallProperties = new PaywallProperties()

    def "Verify that getData returns parameter 'd' if set"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        request.addParameter(PARAMETER_DATA,"lnbc30u1pwtvwpvpp5hvsr5ch6qsyp6wx7ek2sya2gy9lakzvc4fj96ez3kf4p4cu3gzasdp62phkcmr0vejk2epwvdhk6gpdypcxz7fqw3hjqen9v4jzqcmgd93kketwwvxqzjccqp2rzjqwac3nxyg3f5mfa4ke9577c4u8kvkx8pqtdsusqdfww0aymk823x6zyc0gqqqngqqqqqqq8mqqqqqqcqjqed7f8atc09d362xvmpryujfwjcycgar7lvlem5ulttsgh9eay58hhvwjqmqveqxjwk8gf4r9w0dux0prknclxz7euetkjnk9563022cpruw4jn")
        expect:
        controller.getData(request) == "lnbc30u1pwtvwpvpp5hvsr5ch6qsyp6wx7ek2sya2gy9lakzvc4fj96ez3kf4p4cu3gzasdp62phkcmr0vejk2epwvdhk6gpdypcxz7fqw3hjqen9v4jzqcmgd93kketwwvxqzjccqp2rzjqwac3nxyg3f5mfa4ke9577c4u8kvkx8pqtdsusqdfww0aymk823x6zyc0gqqqngqqqqqqq8mqqqqqqcqjqed7f8atc09d362xvmpryujfwjcycgar7lvlem5ulttsgh9eay58hhvwjqmqveqxjwk8gf4r9w0dux0prknclxz7euetkjnk9563022cpruw4jn"
    }

    def "Verify that getData throws MissingServletRequestParameterException if parameter 'd' is not set"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        when:
        controller.getData(request)
        then:
        def e = thrown IllegalArgumentException
        e.message == "Invalid request, parameter 'd' is required."
    }

    def "Verify that getParameterWithDefaultAsInt returns parameter if set"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        request.addParameter(PARAMETER_WIDTH,"120")
        paywallProperties.qrCodeDefaultWidth = "150"
        expect:
        controller.getParameterWithDefaultAsInt(request,PARAMETER_WIDTH,PaywallProperties.QR_CODE_DEFAULT_WIDTH,paywallProperties.qrCodeDefaultWidth) == 120
    }

    def "Verify that getParameterWithDefaultAsInt throws "(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        request.addParameter(PARAMETER_WIDTH,"nonint")
        paywallProperties.qrCodeDefaultWidth = "150"
        when:
        controller.getParameterWithDefaultAsInt(request,PARAMETER_WIDTH,PaywallProperties.QR_CODE_DEFAULT_WIDTH,paywallProperties.qrCodeDefaultWidth)
        then:
        def e = thrown IllegalArgumentException
        e.message == "Invalid parameter 'w', should be an integer, not nonint"
    }

    def "Verify that getParameterWithDefaultAsInt returns default value if no parameter is set"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        paywallProperties.qrCodeDefaultWidth = "150"
        expect:
        controller.getParameterWithDefaultAsInt(request,PARAMETER_WIDTH,PaywallProperties.QR_CODE_DEFAULT_WIDTH,paywallProperties.qrCodeDefaultWidth) == 150
    }

    def "Verify that getParameterWithDefaultAsInt throws internal server error and logs error if default value setting is not an integer"(){
        setup:
        controller.log = Mock(Logger)
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/test")
        paywallProperties.qrCodeDefaultWidth = "nonint"
        when:
        controller.getParameterWithDefaultAsInt(request,PARAMETER_WIDTH,PaywallProperties.QR_CODE_DEFAULT_WIDTH,paywallProperties.qrCodeDefaultWidth)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error in server when generating QR code."
        1 * controller.log.severe("Internal error in paywall configuration, setting 'paywall.qrcode.width.default' must be an integer not nonint.")
    }
}
