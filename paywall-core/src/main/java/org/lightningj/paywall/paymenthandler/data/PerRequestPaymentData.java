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
package org.lightningj.paywall.paymenthandler.data;

/**
 * Special interface for payment workflow that should support pay per request
 * billing. All implementing class must also at least implement MinimalPaymentData.
 * <p>
 * Contains two fields: payPerRequest indicating if related invoice is only valid
 * for one request and executed which is a flag set to true after a request have
 * actually been processed (and related payment had pay per request to true) so
 * it cannot be processed again.
 *
 * @see MinimalPaymentData
 *
 * Created by Philip Vendil on 2018-12-17.
 */
public interface PerRequestPaymentData {

    /**
     *
     * @return flag indicating that this payment is for one request only. The implementation
     * can take the payPerRequest flag from the order request as guidance, but it is the PaymentHandler
     * that ultimately decides if payPerRequest should be set.
     */
    boolean isPayPerRequest();

    /**
     *
     * @param payPerRequest flag indicating that this payment is for one request only. The implementation
     * can take the payPerRequest flag from the order request as guidance, but it is the PaymentHandler
     * that ultimately decides if payPerRequest should be set.
     */
    void setPayPerRequest(boolean payPerRequest);

    /**
     *
     * @return true if related request have been executed, is set after successful processing
     * if a payed call and used to indicate that it cannot be processed again.
     */
    boolean isExecuted();

    /**
     *
     * @param executed true if related request have been executed, is set after successful processing
     * if a payed call and used to indicate that it cannot be processed again.
     */
    void setExecuted(boolean executed);
}
