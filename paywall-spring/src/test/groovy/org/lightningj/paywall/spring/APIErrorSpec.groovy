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

import org.lightningj.paywall.tokengenerator.TokenException
import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * Unit tests for APIError
 */
class APIErrorSpec extends Specification {

    def "Verify that constructors and toString works properly"(){
        expect:
        new APIError(HttpStatus.TEMPORARY_REDIRECT, "Some Temporary Redirect", "Some Constructed Message").toString() == """APIError{status=307 TEMPORARY_REDIRECT, message='Some Temporary Redirect', errors=[Some Constructed Message], reason=null}"""
        new APIError(HttpStatus.TEMPORARY_REDIRECT, "Some Temporary Redirect", ["Some Constructed Message1","Some Constructed Message2"]).toString() == """APIError{status=307 TEMPORARY_REDIRECT, message='Some Temporary Redirect', errors=[Some Constructed Message1, Some Constructed Message2], reason=null}"""
    }

    def "Verify getters and setters"(){
        setup:
        def apiError = new APIError(HttpStatus.TEMPORARY_REDIRECT, "Some Temporary Redirect", "Some Constructed Message")
        expect:
        apiError.getStatus() == HttpStatus.TEMPORARY_REDIRECT
        apiError.getMessage() == "Some Temporary Redirect"
        apiError.getErrors().size() == 1

        when:
        apiError = new APIError()
        apiError.setStatus(HttpStatus.TEMPORARY_REDIRECT)
        apiError.setMessage("Some Temporary Redirect")
        apiError.setErrors(["Some Constructed Message"])
        apiError.setReason(TokenException.Reason.EXPIRED)
        then:
        apiError.getStatus() == HttpStatus.TEMPORARY_REDIRECT
        apiError.getMessage() == "Some Temporary Redirect"
        apiError.getErrors().size() == 1
        apiError.getReason() == TokenException.Reason.EXPIRED
    }
}
