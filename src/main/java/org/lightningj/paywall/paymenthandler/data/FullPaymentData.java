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

import org.lightningj.paywall.vo.amount.Amount;

import java.time.Instant;

/**
 * TODO
 *
 * Created by Philip Vendil on 2018-12-10.
 */
public interface FullPaymentData extends StandardPaymentData {


    String getBolt11Invoice();

    void setBolt11Invoice(String bolt11Invoice);

    Instant getSettlementValidFrom();

    void setSettlementValidFrom(Instant settlementValidFrom);



}
