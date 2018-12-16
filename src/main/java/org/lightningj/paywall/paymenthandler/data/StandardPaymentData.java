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
public interface StandardPaymentData extends MinimalPaymentData {

    String getDescription();

    void setDescription(String description);

    Amount getInvoiceAmount(); // TODO

    void setInvoiceAmount(Amount invoiceAmount);

    Instant getInvoiceDate();

    void setInvoiceDate(Instant invoiceDate);

    Instant getInvoiceExpireDate();

    void setInvoiceExpireDate(Instant invoiceExpireDate);

    Amount getSettledAmount(); // TODO

    void setSettledAmount(Amount settledAmount);

    Instant getSettlementDate();

    void setSettlementDate(Instant settlementDate);

    Instant getSettlementExpireDate();

    void setSettlementExpireDate(Instant settlementDate);

}
