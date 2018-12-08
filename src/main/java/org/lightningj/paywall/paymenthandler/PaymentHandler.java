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
package org.lightningj.paywall.paymenthandler;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.LightningHandlerContext;
import org.lightningj.paywall.vo.InvoiceData;
import org.lightningj.paywall.vo.OrderData;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.vo.SettlementData;

import java.io.IOException;

/**
 * TODO
 * Created by Philip Vendil on 2018-12-04.
 */
public interface PaymentHandler {


    OrderData createOrder(byte[] preImageHash, OrderRequest orderRequest) throws IOException,InternalErrorException;// TODO check

    InvoiceData lookupInvoice(byte[] preImageHash) throws IOException,InternalErrorException;

    SettlementData checkSettlement(byte[] preImageHash) throws IOException,InternalErrorException;


    void registerListener(PaymentListener listener) throws InternalErrorException;

    void unregisterListener(PaymentListener listener) throws InternalErrorException;

    LightningHandlerContext getLightningHandlerContext() throws IOException,InternalErrorException;
}
