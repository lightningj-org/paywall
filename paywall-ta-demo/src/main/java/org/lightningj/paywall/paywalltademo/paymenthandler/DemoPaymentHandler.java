/*
 * ***********************************************************************
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
package org.lightningj.paywall.paywalltademo.paymenthandler;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.LightningHandlerContext;
import org.lightningj.paywall.paymenthandler.PaymentEventType;
import org.lightningj.paywall.paymenthandler.data.PaymentData;
import org.lightningj.paywall.paywalltademo.paymenthandler.ArticleData;
import org.lightningj.paywall.paywalltademo.paymenthandler.ArticleDataRepository;
import org.lightningj.paywall.paywalltademo.paymenthandler.DemoPerRequestPaymentData;
import org.lightningj.paywall.paywalltademo.paymenthandler.DemoPerRequestPaymentDataRepository;
import org.lightningj.paywall.spring.SpringPaymentHandler;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.OrderRequest;
import org.lightningj.paywall.vo.amount.BTC;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

/**
 * Demo implementation of a Payment Handler extending SpringPaymentHandler.
 *
 * It creates DemoPaymentData that implements the PerRequestPaymentData interface. (In order to demonstrate
 * support for both request that's valid for a period of time and for a specific request.)
 * by first looking up the article id order (generated by the @PaymentRequired annotation).
 * Then checks the price of the article in ArticleData table to calculate the ordered amount.
 *
 * It also implements the lookup by preImageHash method and update payment data methods by calling
 * related methods in the DemoPaymentDataRepository.
 *
 * @author philip 2019-08-14
 */
@ComponentScan("org.lightningj.paywall.spring")
@Component("paymentHandler")
public class DemoPaymentHandler extends SpringPaymentHandler {

    @Autowired
    DemoPerRequestPaymentDataRepository demoPaymentDataRepository;

    @Autowired
    ArticleDataRepository articleDataRepository;

    /**
     * Method called after initialization of bean.
     *
     * Contains bootstrap of article database.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // Important call afterPropertiesSet from SpringPaymentHandler
        super.afterPropertiesSet();

        ArticleData articleData1 = articleDataRepository.findByArticleId("tademo1");
        if(articleData1 == null){
            articleData1 = new ArticleData();
            articleData1.setArticleId("tademo1");
            articleData1.setPrice(10);
            articleDataRepository.save(articleData1);
        }
    }

    /**
     * Method that should generate a new PaymentData for a given order request.
     * This is the first call in a payment flow and the implementation should
     * look up the order amount from the article id, units and other options in
     * the order request.
     * <p>
     * The generated PaymentData should be at least MinimalPaymentData with preImageHash
     * and orderedAmount set.
     * <p>
     * It is recommended that the PaymentData is persisted in this call but could
     * be skipped for performance in certain payment flows.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @param orderRequest the specification of the payment data that should be created calculated
     *                     from data in the PaymentRequired annotation.
     * @return a newly generated PaymentData signaling a new payment flow used to
     * create an Order value object.
     * @throws IOException            if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred generating new payment data.
     */
    @Override
    protected PaymentData newPaymentData(byte[] preImageHash, OrderRequest orderRequest) throws IOException, InternalErrorException {
        try{
            DemoPerRequestPaymentData demoPaymentData = new DemoPerRequestPaymentData();
            demoPaymentData.setPreImageHash(preImageHash);
            demoPaymentData.setPayPerRequest(orderRequest.isPayPerRequest());

            long orderPrice = findArticleById(orderRequest.getArticleId()).getPrice() * orderRequest.getUnits(); // Price in satoshis.
            demoPaymentData.setOrderAmount(new BTC(orderPrice));

            demoPaymentDataRepository.save(demoPaymentData);
            return demoPaymentData;
        }catch(Exception e){
            if(e instanceof InternalErrorException){
                throw e;
            }
            throw new InternalErrorException("Error occurred saving DemoPaymentData to database: " + e.getMessage(),e);
        }
    }

    /**
     * Method to lookup a payment data in the payment handler.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @return return related payment data or null if not found.
     * @throws InternalErrorException if internal exception occurred fetching related payment data.
     */
    @Override
    protected PaymentData findPaymentData(byte[] preImageHash) throws InternalErrorException {
        try{
          return demoPaymentDataRepository.findByPreImageHash(Base58.encodeToString(preImageHash));
        }catch(Exception e){
          throw new InternalErrorException("Error occurred fetching DemoPaymentData from database: " + e.getMessage(),e);
        }
    }

    /**
     * Method called on update events about a given payment data. This could be when
     * the payment is added as invoice in LND and contains complementary data or when
     * the invoice was settled and contains settled flag set and settled amount and date
     * (depending on the type of PaymentData used in PaymentHandler).
     * <p>
     * The related payment data (using preImageHash as unique identifier) is automatically
     * looked up and the implementing method should at least persist the updated data.
     *
     * @param type        the type of event such as INVOICE_CREATED or INVOICE_SETTLED.
     * @param paymentData the payment data to update and persist.
     * @param context     the latest known state of the lightning handler.  Null if no known state exists.
     * @throws InternalErrorException if internal exception occurred updating related payment data.
     */
    @Override
    protected void updatePaymentData(PaymentEventType type, PaymentData paymentData, LightningHandlerContext context) throws InternalErrorException {
        try {
            assert paymentData instanceof DemoPerRequestPaymentData;
            demoPaymentDataRepository.save((DemoPerRequestPaymentData) paymentData);
        }catch(Exception e){
            throw new InternalErrorException("Error occurred updating DemoPaymentData to database: " + e.getMessage(),e);
        }
    }

    private ArticleData findArticleById(String articleId) throws InternalErrorException{
        ArticleData articleData = articleDataRepository.findByArticleId(articleId);
        if(articleData == null){
            throw new InternalErrorException("Internal error creating payment data, article id " + articleId + " doesn't exist in database.");
        }
        return articleData;
    }

}
