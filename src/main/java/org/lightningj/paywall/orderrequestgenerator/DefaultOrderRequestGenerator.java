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
package org.lightningj.paywall.orderrequestgenerator;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.vo.PaymentOption;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Default Order request generator that parses the values from
 * PaymentRequired annotation without any modifications.
 *
 * @see PaymentRequired
 * Created by Philip Vendil on 2018-10-29.
 */
public class DefaultOrderRequestGenerator implements OrderRequestGenerator {

    /**
     * Default Magnitude Calculator takes the parameter units from the
     * PaymentRequired annotation (default 1)
     *
     * @param paymentRequired the related annotation.
     * @param request         the HTTP request related to the call.
     * @return the number of units that should be debited to the PaymentHandler
     * @throws InternalErrorException if problem occurred generated order request data due to internal miss configuration.
     */
    @Override
    public OrderRequest generate(PaymentRequired paymentRequired, CachableHttpServletRequest request) throws InternalErrorException{
        if(paymentRequired.articleId().equals("")){
            throw new InternalErrorException("Internal error in DefaultOrderRequestGenerator, error in PaymentRequired annotation, article id is mandatory.");
        }
        return new OrderRequest(paymentRequired.articleId(), paymentRequired.units(), convertPaymentOptions(paymentRequired));
    }

    /**
     * Help method to convert all PaymentRequired payment options into value objects.
     * @param paymentRequired the related annotation.
     * @return list of value object representation of PaymentOptions.
     */
    protected List<PaymentOption> convertPaymentOptions(PaymentRequired paymentRequired){
        List<PaymentOption> retval = new ArrayList<>();
        for(int i =0; i<paymentRequired.paymentOptions().length;i++){
            retval.add(new PaymentOption(paymentRequired.paymentOptions()[i]));
        }
        return retval;
    }
}
