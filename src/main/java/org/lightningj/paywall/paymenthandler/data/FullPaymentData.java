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
 *
 * If the payment contains all fields necessary in a payment flow it will
 * not perform lookups of invoices in LightningHandler to the same extent which
 * may boost performance.
 *
 * Created by Philip Vendil on 2018-12-10.
 */
public interface FullPaymentData extends StandardPaymentData {

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

}
