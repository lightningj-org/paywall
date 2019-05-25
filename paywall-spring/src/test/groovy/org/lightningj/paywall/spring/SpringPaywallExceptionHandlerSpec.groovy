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
package org.lightningj.paywall.spring

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.spring.util.PaywallRuntimeException
import org.lightningj.paywall.spring.util.RequestHelper
import org.lightningj.paywall.tokengenerator.TokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for SpringPaywallExceptionHandler
 *
 * @author philip 2019-02-19
 */
class SpringPaywallExceptionHandlerSpec extends Specification {

    SpringPaywallExceptionHandler handler = new SpringPaywallExceptionHandler()
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/someuri.json")
    MockHttpServletResponse response = new MockHttpServletResponse()

    def "Verify that handleException without any customized error message uses message from exception, if exception is not internal error."() {
        when:
        ResponseEntity<Object> result = handler.handleException(request, response, new PaywallRuntimeException(new IllegalArgumentException("Test Message")))
        then:
        result.statusCode == HttpStatus.BAD_REQUEST
        result.headers.get("Content-Type")[0] == "application/json"
        result.body.status == HttpStatus.BAD_REQUEST
        result.body.message == "Invalid Request: Test Message"
        result.body.errors.size() == 1
        result.body.errors[0] == "Test Message"
    }

    def "Verify that handleException with one customized error message includes customized message"() {
        when:
        ResponseEntity<Object> result = handler.handleException(request, response, new PaywallRuntimeException(new IllegalArgumentException("Test Message")), "Some custom message")
        then:
        result.statusCode == HttpStatus.BAD_REQUEST
        result.headers.get("Content-Type")[0] == "application/json"
        result.body.status == HttpStatus.BAD_REQUEST
        result.body.message == "Invalid Request: Test Message"
        result.body.errors.size() == 1
        result.body.errors[0] == "Some custom message"
    }

    def "Verify that handleException with a list of customized error messages includes all customized messages"() {
        when:
        ResponseEntity<Object> result = handler.handleException(request, response, new PaywallRuntimeException(new TestException()), ["Some custom message1", "Some custom message2"])
        then:
        result.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        result.headers.get("Content-Type")[0] == "application/json"
        result.body.status == HttpStatus.INTERNAL_SERVER_ERROR
        result.body.message == "Internal Server Error"
        result.body.errors.size() == 2
        result.body.errors[0] == "Some custom message1"
        result.body.errors[1] == "Some custom message2"
    }

    def "Verify that handleException for websocket without any customized error message uses message from exception, if exception is not internal error."() {
        when:
        ResponseEntity<Object> result = handler.handleException(RequestHelper.RequestType.JSON, new PaywallRuntimeException(new IllegalArgumentException("Test Message")))
        then:
        result.statusCode == HttpStatus.BAD_REQUEST
        result.headers.get("Content-Type")[0] == "application/json"
        result.body.status == HttpStatus.BAD_REQUEST
        result.body.message == "Invalid Request: Test Message"
        result.body.errors.size() == 1
        result.body.errors[0] == "Test Message"
    }

    @Unroll
    def "Verify that http response code #status and prefix #prefix is returned for exception of type #exceptionName"() {
        when:
        ResponseEntity<Object> result = handler.handleException(request, response, new PaywallRuntimeException(exception))
        then:
        result.statusCode == status
        result.body.message.startsWith(prefix)
        where:
        exception                                                 | exceptionName              | status                           | prefix
        new IOException("test")                                   | "IOException"              | HttpStatus.SERVICE_UNAVAILABLE   | "Internal Communication Problems"
        new InternalErrorException("test")                        | "InternalErrorException"   | HttpStatus.INTERNAL_SERVER_ERROR | "Internal Server Error"
        new IllegalArgumentException("test")                      | "IllegalArgumentException" | HttpStatus.BAD_REQUEST           | "Invalid Request"
        new TokenException("test", TokenException.Reason.EXPIRED) | "TokenException"           | HttpStatus.UNAUTHORIZED          | "JWT Token Problem"
        new TestException()                                       | "TestException"            | HttpStatus.INTERNAL_SERVER_ERROR | "Internal Server Error"
    }


    def "Verify that reason code is set of TokenException is thrown"() {
        when:
        ResponseEntity<Object> result = handler.handleException(request, response, new PaywallRuntimeException(new TokenException("SomeMessage", TokenException.Reason.EXPIRED)))
        then:
        result.body.reason == TokenException.Reason.EXPIRED
    }

    static class TestException extends Exception {

        TestException() {
            super("Message")
        }

        @Override
        String getLocalizedMessage() {
            return "Some Localized Message"
        }
    }
}
