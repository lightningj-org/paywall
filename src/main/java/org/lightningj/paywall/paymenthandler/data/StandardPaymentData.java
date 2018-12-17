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

import org.lightningj.paywall.vo.amount.CryptoAmount;

import java.time.Instant;

/**
 * A Standard PaymentData contains most of the needed data fields to be stored
 * in a normal lightning related invoicing solution.
 *
 * If system should support payPerRequest payments should the implementing data
 * also implement the PerRequestPaymentData.
 *
 * @see PerRequestPaymentData
 * @see MinimalPaymentData
 *
 * Created by Philip Vendil on 2018-12-10.
 */
public interface StandardPaymentData extends MinimalPaymentData {

    /**
     *
     * @return a short description of the payment used in the lightning invoice and might
     * be displayed to the end user.
     */
    String getDescription();

    /**
     *
     * @param description a short description of the payment used in the lightning invoice and might
     * be displayed to the end user.
     */
    void setDescription(String description);

    /**
     *
     * @return the amount set in the lightning invoice, this is the same as orderAmount if
     * the same currency is used in order as in lightning invoice, otherwise is the currency
     * converted before creating the invoice in LightningHandler and the actual invoiced amount
     * is specified here.
     */
    CryptoAmount getInvoiceAmount();

    /**
     *
     * @param invoiceAmount the amount set in the lightning invoice, this is the same as orderAmount if
     * the same currency is used in order as in lightning invoice, otherwise is the currency
     * converted before creating the invoice in LightningHandler and the actual invoiced amount
     * is specified here.
     */
    void setInvoiceAmount(CryptoAmount invoiceAmount);

    /**
     *
     * @return the date the invoice was created in LightningHandler.
     */
    Instant getInvoiceDate();

    /**
     *
     * @param invoiceDate the date the invoice was created in LightningHandler.
     */
    void setInvoiceDate(Instant invoiceDate);

    /**
     *
     * @return the date a generated invoice should expire, this value will be used
     * when creating invoice in LightningHandler. If null will default invoice validity
     * be used to calculate an expire date automatically.
     */
    Instant getInvoiceExpireDate();

    /**
     *
     * @param invoiceExpireDate the date a generated invoice should expire, this value will be used
     * when creating invoice in LightningHandler. If null will default invoice validity
     * be used to calculate an expire date automatically.
     */
    void setInvoiceExpireDate(Instant invoiceExpireDate);

    /**
     *
     * @return the amount that was settled in the LightningHandlers supported crypto currency.
     * Should be equal to invoiceAmount if fully settled. Null if invoice isn't settled yet.
     */
    CryptoAmount getSettledAmount();

    /**
     *
     * @param settledAmount the amount that was settled in the LightningHandlers supported crypto currency.
     * Should be equal to invoiceAmount if fully settled. Null if invoice isn't settled yet.
     */
    void setSettledAmount(CryptoAmount settledAmount);

    /**
     *
     * @return the timestamp the invoice was settled in LightningHandler. Null if not settled yet.
     */
    Instant getSettlementDate();

    /**
     *
     * @param settlementDate the timestamp the invoice was settled in LightningHandler. Null if not settled yet.
     */
    void setSettlementDate(Instant settlementDate);

    /**
     *
     * @return the date the settlement will expire used to indicate how long a settlement token should be valid.
     * If null will default settlement validity be used.
     */
    Instant getSettlementExpireDate();

    /**
     *
     * @param settlementDate the date the settlement will expire used to indicate how long a settlement token should be valid.
     * If null will default settlement validity be used.
     */
    void setSettlementExpireDate(Instant settlementDate);

    // TODO Mark as executed. Onetime token.

}
