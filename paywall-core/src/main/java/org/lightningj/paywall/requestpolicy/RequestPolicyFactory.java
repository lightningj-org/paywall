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
package org.lightningj.paywall.requestpolicy;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;

import java.util.HashMap;
import java.util.Map;

/**
 * RequestPolicyFactory is in charge if delivering the correct
 * RequestPolicy implementation specified in the PaymentRequired annotation.
 *
 * Created by Philip Vendil on 2018-10-28.
 */
public class RequestPolicyFactory {


    private RequestPolicy urlAndMethod = new UrlAndMethod();
    private RequestPolicy urlMethodAndParameters = new UrlMethodAndParameters();
    private RequestPolicy withBody = new WithBody();

    private Map<Class,RequestPolicy> customPolicies = new HashMap<>();

    /**
     * Method that returns a RequestPolicy instance that is specified
     * in the given PaymentRequired annotation.
     * @param paymentRequired the related PaymentRequired annotation.
     * @return related RequestPolicy, never null.
     * @throws InternalErrorException if problems occurred creating the related RequestPolicy object.
     */
    public RequestPolicy getRequestPolicy(PaymentRequired paymentRequired) throws InternalErrorException{
        switch (paymentRequired.requestPolicy()){
            case URL_AND_METHOD:
                return urlAndMethod;
            case URL_METHOD_AND_PARAMETERS:
                return urlMethodAndParameters;
            case WITH_BODY:
                return withBody;
            case CUSTOM:
            default:
                break;
        }
        return getCustomPolicy(paymentRequired);
    }


    private RequestPolicy getCustomPolicy(PaymentRequired paymentRequired) throws InternalErrorException{
        if(paymentRequired.customPolicy() == NoCustomRequestPolicy.class){
            throw new InternalErrorException("Error in PaymentRequired annotation, class path to custom RequestPolicy implementation is required for RequestPolicyType CUSTOM.");
        }

        RequestPolicy customPolicy = customPolicies.get(paymentRequired.customPolicy());
        if(customPolicy == null){
            try {
                customPolicy = (RequestPolicy) paymentRequired.customPolicy().newInstance();
                customPolicies.put(paymentRequired.customPolicy(),customPolicy);
            }catch(Exception e){
                throw new InternalErrorException("Error constructing custom request policy: " + paymentRequired.customPolicy() + ", message: " + e.getMessage(),e);
            }
        }
        return customPolicy;
    }
}
