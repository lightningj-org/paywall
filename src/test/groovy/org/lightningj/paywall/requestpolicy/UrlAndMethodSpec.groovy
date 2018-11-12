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
package org.lightningj.paywall.requestpolicy

import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.web.CachableHttpServletRequest
import spock.lang.Specification

/**
 * Unit tests for UrlAndMethod
 *
 * Created by Philip Vendil on 2018-10-27.
 */
class UrlAndMethodSpec extends Specification {

    def policy = new UrlAndMethod()
    def request = Mock(CachableHttpServletRequest)

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def "Verify that URL and Method are used for aggregation"(){
        when:
        byte[] result = policy.significantRequestDataDigest(request)
        then:
        result.length == 32
        1 * request.getMethod() >> { return "POST"}
        1 * request.getRequestURL() >> { return new StringBuffer("http://somehost/test")}
        when:
        byte[] result2 = policy.significantRequestDataDigest(request)
        then:
        result != result2
        result2.length == 32
        1 * request.getMethod() >> { return "GET"}
        1 * request.getRequestURL() >> { return new StringBuffer("http://somehost/test")}
        when:
        byte[] result3 = policy.significantRequestDataDigest(request)
        then:
        result != result3
        result3.length == 32
        1 * request.getMethod() >> { return "POST"}
        1 * request.getRequestURL() >> { return new StringBuffer("http://somehost/test2")}
    }
}
