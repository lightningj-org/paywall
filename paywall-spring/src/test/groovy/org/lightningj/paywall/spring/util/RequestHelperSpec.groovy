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
package org.lightningj.paywall.spring.util

import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

import static org.lightningj.paywall.spring.util.RequestHelper.RequestType.*
/**
 * Unit tests for RequestHelper
 *
 * @author philip 2019-02-13
 */
class RequestHelperSpec extends Specification {

    RequestHelper helper = new RequestHelper()

    def "Verify that request ending with .xml results in RequestType.XML"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/someuri.xml")
        expect:
        helper.getRequestType(request, JSON) == XML
    }

    def "Verify that request ending with .json results in RequestType.JSON"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/someuri.json")
        expect:
        helper.getRequestType(request, null) == JSON
    }

    @Unroll
    def "Verify that request with contentType #contentType results in #expected"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/someuri")
        request.setContentType(contentType)
        expect:
        helper.getRequestType(request,null) == expected
        where:
        contentType           | expected
        " " +XML.contentType  | XML
        JSON.contentType      | JSON
    }

    @Unroll
    def "Verify that request with accept header #contentType results in #expected"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/someuri")
        request.addHeader(RequestHelper.HEADER_ACCEPT,contentType)
        expect:
        helper.getRequestType(request,null) == expected
        where:
        contentType           | expected
        " "+XML.contentType   | XML
        " "+JSON.contentType  | JSON
    }

    @Unroll
    def "Verify that request with RequestType has MediaType #expected"(){
        expect:
        requestType.mediaType == expected
        where:
        requestType                | expected
        JSON                       | MediaType.APPLICATION_JSON
        XML                        | MediaType.APPLICATION_XML
    }

    def "Verify that default value is returned if nothing matches"(){
        setup:
        MockHttpServletRequest request = new MockHttpServletRequest("GET","/someuri")
        expect:
        helper.getRequestType(request,null) == null
    }
}
