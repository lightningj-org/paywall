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
import org.lightningj.paywall.web.CachableHttpServletRequest;

/**
 * Interface calculating the number of units that should be debited
 * for each call having the PaymentRequired annotation.
 *
 * Created by philip on 2018-10-29.
 */
public interface OrderRequestGenerator {

    /**
     * Method that should populate a new OrderRequest to initiate a
     * payment flow using the PaymentRequired annotation and the
     * related HTTP request.
     * @param paymentRequired the related annotation.
     * @param request the HTTP request related to the call.
     * @return a new OrderRequest.
     * @throws IllegalArgumentException if user supplied data was invalid to generate order request.
     * @throws InternalErrorException if problem occurred generated order request data due to internal miss configuration.
     */
    OrderRequest generate(PaymentRequired paymentRequired, CachableHttpServletRequest request) throws IllegalArgumentException, InternalErrorException;
}
