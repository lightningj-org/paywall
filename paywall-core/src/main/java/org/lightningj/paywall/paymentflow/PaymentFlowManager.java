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
package org.lightningj.paywall.paymentflow;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.io.IOException;

/**
 * PaymentFlowManager is in charge of creating payment flows
 * for a given PaymentRequired annotation and managing all related sub-components such as TokenGenerator
 * and LightningHandler etc.
 *
 * Created by Philip Vendil on 2018-12-20.
 */
public interface PaymentFlowManager {

    /**
     * Method to create a new instance of related mode of PaymentFlow initialized with
     * current state in the payment flow. Usually when initiating a new payment flow or
     * checking if a current payment flow is settled.
     *
     * @param paymentRequired the related annotation for the resource needing payment.
     * @param request the HTTP request to parse JWT token from.
     * @return a new instance of related payment flow.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    PaymentFlow getPaymentFlowByAnnotation(PaymentRequired paymentRequired, CachableHttpServletRequest request) throws InternalErrorException, IOException, TokenException;

    /**
     * Method to create a new instance of related mode of PaymentFlow initialized with
     * current state in the payment flow.
     * <p>
     *     This method should be called in states that doesn't have access to the
     *     PaymentRequired annotation, such as status controllers and similar.
     * </p>
     * @param request the HTTP request to parse JWT token from.
     * @param expectedTokenType the expected type of JWT token.
     * @return a new instance of related payment flow.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    PaymentFlow getPaymentFlowFromToken(CachableHttpServletRequest request, ExpectedTokenType expectedTokenType) throws IllegalArgumentException, InternalErrorException, IOException, TokenException;
}
