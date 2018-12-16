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
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.paymenthandler.data.FullPaymentData;
import org.lightningj.paywall.paymenthandler.data.MinimalPaymentData;
import org.lightningj.paywall.paymenthandler.data.PaymentData;
import org.lightningj.paywall.paymenthandler.data.StandardPaymentData;
import org.lightningj.paywall.util.Base64Utils;
import org.lightningj.paywall.vo.Invoice;
import org.lightningj.paywall.vo.NodeInfo;
import org.lightningj.paywall.vo.Order;
import org.lightningj.paywall.vo.Settlement;
import org.lightningj.paywall.vo.amount.CryptoAmount;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

/**
 * Class containing helper methods to convert between
 * payment data and related value objects such as Order,Invoice and Settlement.
 *
 * Created by Philip Vendil on 2018-12-11.
 */
public class PaymentDataConverter {

    private LightningHandler lightningHandler;
    private Duration defaultSettlementValidity;
    private Duration defaultInvoiceValidity;

    private Clock clock = Clock.systemDefaultZone();
    /**
     * Default constructor.
     *
     * @param lightningHandler the used LightningHandler.
     * @param defaultSettlementValidity the default settlement validity used
     *                                  if no settlement validity is used in the
     *                                  configured payment data.
     * @param defaultInvoiceValidity the default invoice validity used
     *                               if no settlement validity is used in the
     *                               configured payment data.
     */
    public PaymentDataConverter(LightningHandler lightningHandler,
                                Duration defaultSettlementValidity,
                                Duration defaultInvoiceValidity){
      this.lightningHandler = lightningHandler;
      this.defaultSettlementValidity = defaultSettlementValidity;
      this.defaultInvoiceValidity = defaultInvoiceValidity;
    }

    /**
     * Method to convert at paymentData into an Order value object.
     * @param paymentData the payment data to convert into order.
     * @return the converted order value object.
     * @throws InternalErrorException if payment data is invalid.
     */
    Order convertToOrder(PaymentData paymentData) throws InternalErrorException {
        basicCheck(paymentData);
        Order order = new Order();
        MinimalPaymentData minimalPaymentData = (MinimalPaymentData) paymentData;
        order.setPreImageHash(minimalPaymentData.getPreImageHash());
        order.setOrderAmount(minimalPaymentData.getOrderAmount());
        if(paymentData instanceof StandardPaymentData){
            StandardPaymentData standardPaymentData = (StandardPaymentData) paymentData;
            order.setDescription(standardPaymentData.getDescription());
            if(standardPaymentData.getInvoiceExpireDate() == null) {
                order.setExpireDate(clock.instant().plus(defaultInvoiceValidity));
            }else{
                order.setExpireDate(standardPaymentData.getInvoiceExpireDate());
            }
        }else{
            order.setExpireDate(clock.instant().plus(defaultInvoiceValidity));
        }
        return order;
    }

    /**
     * Method to convert a payment data into an invoice. If payment data isn't a FullPaymentData
     * it will fetch the invoice from the LightningHandler to get the complete data.
     * @param paymentData the payment data to convert into an invoice.
     * @return the converted invoice.
     * @throws InternalErrorException if payment was invalid or problems occurred converting the related
     * invoice.
     * @throws IOException if communication problems occurred with LightningHandler.
     */
    Invoice convertToInvoice(PaymentData paymentData) throws InternalErrorException, IOException{
        basicCheck(paymentData);
        if(paymentData instanceof FullPaymentData){
            FullPaymentData pd = (FullPaymentData) paymentData;

            NodeInfo nodeInfo = lightningHandler.getNodeInfo();
            CryptoAmount invoiceAmoint = pd.getInvoiceAmount();
            return new Invoice(pd.getPreImageHash(),
                    pd.getBolt11Invoice(),
                    pd.getDescription(),
                    invoiceAmoint,
                    nodeInfo,
                    pd.getInvoiceExpireDate(),
                    pd.getInvoiceDate(),
                    pd.isSettled(),
                    pd.getSettledAmount(),
                    pd.getSettlementDate());

        }else{
            byte[] preImageHash = ((MinimalPaymentData) paymentData).getPreImageHash();
            Invoice invoice = lightningHandler.lookupInvoice(preImageHash);
            if(invoice == null){
                throw new InternalErrorException("Internal error converting payment data into invoice, invoice with preImageHash " + Base64Utils.encodeBase64String(preImageHash) + " not found by LightningHandler.");
            }
            return invoice;
        }
    }

    /**
     * Method to convert a paymentData into a settlement value object if related invoice
     * if includeInvoice set to true.
     * @param paymentData the paymentData to convert into a settlement value object.
     * @param includeInvoice set to true if related invoice should be included.
     * @return a generated settlement value object.
     * @throws InternalErrorException if payment was invalid or problems occurred converting the related
     * invoice.
     * @throws IOException if communication problems occurred with LightningHandler.
     */
    Settlement convertToSettlement(PaymentData paymentData, boolean includeInvoice) throws InternalErrorException, IOException{
        basicCheck(paymentData);
        Settlement settlement = new Settlement();
        if(paymentData instanceof MinimalPaymentData){
            MinimalPaymentData mpd = (MinimalPaymentData) paymentData;

            settlement.setPreImageHash(mpd.getPreImageHash());
            if(!(paymentData instanceof StandardPaymentData)){
                settlement.setValidUntil(clock.instant().plus(defaultSettlementValidity));
            }
        }
        if(paymentData instanceof StandardPaymentData){
            StandardPaymentData spd = (StandardPaymentData) paymentData;
            if(spd.getSettlementExpireDate() == null){
                settlement.setValidUntil(clock.instant().plus(defaultSettlementValidity));
            }else {
                settlement.setValidUntil(spd.getSettlementExpireDate());
            }
        }
        if(paymentData instanceof FullPaymentData){
            FullPaymentData fpd = (FullPaymentData) paymentData;
            settlement.setValidFrom(fpd.getSettlementValidFrom());
        }
        if(includeInvoice){
            settlement.setInvoice(convertToInvoice(paymentData));
        }
        return settlement;
    }


    /**
     * Help method to check that payment data has isSettled flag set to true.
     * @param paymentData the paymentData to check.
     * @return true if paymentData has isSettled flag set to true.
     * @throws InternalErrorException if payment data is invalid.
     */
    public boolean isSettled(PaymentData paymentData) throws InternalErrorException{
        basicCheck(paymentData);
        return ((MinimalPaymentData) paymentData).isSettled();
    }

    /**
     * Method to populate all fields from an invoice to a payment data depending on it's type.
     * It is assumed that preImageHash is already set in payment data.
     *
     * @param invoice the invoice from lightning handler.
     * @param paymentData the payment data to populate.
     * @throws InternalErrorException if payment data is invalid.
     */
    void populatePaymentDataFromInvoice(Invoice invoice, PaymentData paymentData)  throws InternalErrorException{
        basicCheck(paymentData);
        if(paymentData instanceof MinimalPaymentData){
            MinimalPaymentData minimalPaymentData = (MinimalPaymentData) paymentData;
            minimalPaymentData.setSettled(invoice.isSettled());
        }
        if(paymentData instanceof StandardPaymentData){
            StandardPaymentData standardPaymentData = (StandardPaymentData) paymentData;
            standardPaymentData.setDescription(invoice.getDescription());
            standardPaymentData.setInvoiceAmount(invoice.getInvoiceAmount());
            standardPaymentData.setInvoiceDate(invoice.getInvoiceDate());
            standardPaymentData.setInvoiceExpireDate(invoice.getExpireDate());
            standardPaymentData.setSettledAmount(invoice.getSettledAmount());
            standardPaymentData.setSettlementDate(invoice.getSettlementDate());
        }
        if(paymentData instanceof FullPaymentData){
            FullPaymentData fullPaymentData = (FullPaymentData) paymentData;
            fullPaymentData.setBolt11Invoice(invoice.getBolt11Invoice());
        }
    }

    /**
     * Method to check if payment data is not null and at least implements
     * MinimalPaymentData.
     *
     * @param paymentData the payment data to check.
     * @throws InternalErrorException if payment data was invalid.
     */
    private void basicCheck(PaymentData paymentData) throws InternalErrorException{
        if(paymentData == null){
            throw new InternalErrorException("Internal error converting PaymentData, PaymentData cannot be null.");
        }
        if(!(paymentData instanceof MinimalPaymentData)){
            throw new InternalErrorException("Internal error converting PaymentData, check that PaymentData object at least inherits MinimalPaymentData.");
        }
    }
}
