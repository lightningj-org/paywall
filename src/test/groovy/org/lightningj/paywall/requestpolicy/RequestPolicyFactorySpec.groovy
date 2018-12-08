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

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.annotations.PaymentRequired
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.RequestData
import org.lightningj.paywall.web.CachableHttpServletRequest
import spock.lang.Specification

import java.time.Instant

/**
 * Unit tests for RequestPolicyFactory
 *
 * Created by philip on 2018-10-28.
 */
class RequestPolicyFactorySpec extends Specification {

    RequestPolicyFactory factory = new RequestPolicyFactory()

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def "Verify that correct built in request policy is returned"(){
        expect:
        factory.getRequestPolicy(findAnnotation("callUrlAndMethod")) instanceof UrlAndMethod
        factory.getRequestPolicy(findAnnotation("callUrlMethodANDParameters")) instanceof UrlMethodAndParameters
        factory.getRequestPolicy(findAnnotation("callWithBody")) instanceof WithBody
    }

    def "Verify that custom request policies are generated properly"(){
        when:
        def custom1 = factory.getRequestPolicy(findAnnotation("callWithCustom1"))
        def custom2 = factory.getRequestPolicy(findAnnotation("callWithCustom2"))
        then:
        custom1 instanceof CustomRequestPolicy1
        custom2 instanceof CustomRequestPolicy2
        factory.customPolicies.size() == 2
        factory.customPolicies[CustomRequestPolicy1] != null
        factory.customPolicies[CustomRequestPolicy2] != null
    }

    def "Verify that custom request policy with no implementation class specified throws InternalErrorException"(){
        when:
        factory.getRequestPolicy(findAnnotation("callWithInvalidCustom1"))
        then:
        def e = thrown InternalErrorException
        e.message == "Error in PaymentRequired annotation, class path to custom RequestPolicy implementation is required for RequestPolicyType CUSTOM."
    }

    def "Verify that custom request policy with invalid implementation class specified throws InternalErrorException"(){
        when:
        factory.getRequestPolicy(findAnnotation("callWithInvalidCustom2"))
        then:
        def e = thrown InternalErrorException
        e.message == 'Error constructing custom request policy: class org.lightningj.paywall.requestpolicy.RequestPolicyFactorySpec$InvalidCustomRequestPolicy, message: org.lightningj.paywall.requestpolicy.RequestPolicyFactorySpec$InvalidCustomRequestPolicy cannot be cast to org.lightningj.paywall.requestpolicy.RequestPolicy'
        e.cause != null
    }

    private findAnnotation(String method){
        return AnnotationTest.class.getMethod(method).annotations[0]
    }

    static class AnnotationTest{

        @PaymentRequired(articleId = "notused", requestPolicy = RequestPolicyType.URL_AND_METHOD)
        void callUrlAndMethod(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.URL_METHOD_AND_PARAMETERS)
        void callUrlMethodANDParameters(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.WITH_BODY)
        void callWithBody(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.CUSTOM, customPolicy = CustomRequestPolicy1)
        void callWithCustom1(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.CUSTOM, customPolicy = CustomRequestPolicy2)
        void callWithCustom2(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.CUSTOM)
        void callWithInvalidCustom1(){}

        @PaymentRequired(articleId= "notused", requestPolicy = RequestPolicyType.CUSTOM, customPolicy = InvalidCustomRequestPolicy)
        void callWithInvalidCustom2(){}
    }

    static class CustomRequestPolicy1 implements RequestPolicy{

        @Override
        RequestData significantRequestDataDigest(CachableHttpServletRequest request) throws IllegalArgumentException, IOException, InternalErrorException {
            return new RequestData(byte[0],null)
        }
    }

    static class CustomRequestPolicy2 implements RequestPolicy{

        @Override
        RequestData significantRequestDataDigest(CachableHttpServletRequest request) throws IllegalArgumentException, IOException, InternalErrorException {
            return new RequestData(byte[0], null)
        }
    }
    static class InvalidCustomRequestPolicy{

    }

}
