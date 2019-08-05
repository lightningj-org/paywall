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

import java.time.Instant;

/**
 * Interface defining the complete set of fields in a payment data and can
 * be implemented for best performance of the system.
 * <p>
 * If the payment contains all fields necessary in a payment flow it will
 * not perform look-ups of invoices in LightningHandler to the same extent which
 * may boost performance.
 *
 * @see MinimalPaymentData
 * @see PerRequestPaymentData
 * @see StandardPaymentData
 *
 * Created by Philip Vendil on 2018-12-10.
 */
public interface FullPaymentData extends StandardPaymentData, PerRequestPaymentData {

    /**
     *
     * @return the bolt11 lightning invoice displayed to the end user before
     * paying and invoice.
     */
    String getBolt11Invoice();

    /**
     *
     * @param bolt11Invoice the bolt11 lightning invoice displayed to the end user before
     * paying and invoice.
     */
    void setBolt11Invoice(String bolt11Invoice);

    /**
     *
     * @return the valid from timestamp used in generated settlement tokens. If null is
     * no valid from used, only validUntil.
     */
    Instant getSettlementValidFrom();

    /**
     *
     * @param settlementValidFrom the valid from timestamp used in generated settlement tokens. If null is
     * no valid from used, only validUntil.
     */
    void setSettlementValidFrom(Instant settlementValidFrom);

    /**
     * The settlement expire date sets the timestamp when a generated settlement token should expire. If
     * not set will a settlementDuration be used, and if that is also null will default duration be set.
     * This field is useful if a settlement should be valid the entire day or month.
     * <p>
     * If settlement expire date is set it has precedence over settlementDuration.
     * </p>
     * <p>
     *     <b>Important:</b> Data in this field is only set to instruct the settlement token generator of expiration date.
     *     the actual settlement date is not updated in this field.
     * </p>
     * @see StandardPaymentData#getSettlementDuration()
     *
     * @return the date the settlement will expire used to indicate how long a settlement token should be valid.
     * If null will default settlement validity be used. If settlementExpireDate is set it will override
     * the settlement duration value.
     */
    Instant getSettlementExpireDate();

    /**
     * The settlement expire date sets the timestamp when a generated settlement token should expire. If
     * not set will a settlementDuration be used, and if that is also null will default duration be set. In FullPaymentData it is also possible
     * This field is useful if a settlement should be valid the entire day or month.
     * <p>
     * If settlement expire date is set it has precedence over settlementDuration.
     * </p>
     * <p>
     *     <b>Important:</b> Data in this field is only set to instruct the settlement token generator of expiration date.
     *     the actual settlement date is not updated in this field.
     * </p>
     * @see StandardPaymentData#getSettlementDuration()
     *
     * @param settlementExpireDate the date the settlement will expire used to indicate how long a settlement token should be valid.
     * If null will default settlement validity be used. If settlementExpireDate is set it will override
     * the settlement duration value.
     */
    void setSettlementExpireDate(Instant settlementExpireDate);

}
