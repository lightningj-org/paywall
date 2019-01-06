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

import org.lightningj.paywall.AlreadyExecutedException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.currencyconverter.InvalidCurrencyException;
import org.lightningj.paywall.tokengenerator.TokenException;

import java.io.IOException;

/**
 * Interface of external methods of a payment flow. A Payment flow should be created
 * by a PaymentFlowManager only.
 *
 * @see LocalPaymentFlow
 * @see CentralLightningHandlerPaymentFlow
 *
 * Created by Philip Vendil on 2019-01-01.
 */
public interface PaymentFlow {

    /**
     * Method that should determine if the current state of payment flow. Should
     * for instance return false if valid settlement is included in the request.
     *
     * @return true if payment is currently required in for related HTTP request.
     * @throws AlreadyExecutedException if related payment is pay per request and is already executed.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    boolean isPaymentRequired() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException;

    /**
     * Method to create and order and optionally an invoice (depending on system setup).
     *
     * @return a value object containing a payment or invoice JWT Token and optionally and invoice.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws InvalidCurrencyException if problems occurred converting the currency in the order to the one
     * used in the invoice.
     * @throws TokenException if problems occurred generating or validating related JWT Token.
     */
    RequestPaymentResult requestPayment() throws IllegalArgumentException, IOException, InternalErrorException, InvalidCurrencyException, TokenException;

    /**
     * Method to check if related payment is settled by the end user.
     *
     * @return true if settled.
     * @throws AlreadyExecutedException if related payment is pay per request and is already executed.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    boolean isSettled() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException;

    /**
     * Method to retrieve a settlement and generate a settlement token.
     *
     * @return a value object containing the settlement and the related settlement token.
     * @throws AlreadyExecutedException if related payment is pay per request and is already executed.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     * @throws TokenException if problem occurred generating the settlement token.
     */
    SettlementResult getSettlement() throws AlreadyExecutedException, IllegalArgumentException, IOException, InternalErrorException, TokenException;

    /**
     * Method that should indicate if related payment flow is for one request only or valid
     * over a given period.
     *
     * @return true if related payment is for one request only.
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    boolean isPayPerRequest() throws IllegalArgumentException, IOException, InternalErrorException;

    /**
     * Method that should be called by a filter or equivalent after successful execution of
     * payed request and the related payment flow is payPerRequest.
     *
     * @throws IllegalArgumentException if user specified parameters (used by the constructor) was invalid.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred processing the method.
     */
    void markAsExecuted() throws IllegalArgumentException, IOException, InternalErrorException;

}
