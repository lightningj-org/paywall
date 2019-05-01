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
package org.lightningj.paywall.springboot2.paymenthandler

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.tokengenerator.TokenGenerator
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.OrderRequest
import org.lightningj.paywall.vo.amount.BTC
import org.lightningj.paywall.vo.amount.CryptoAmount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Unit tests for DemoPaymentHandler
 * @author philip 2019-02-12
 *
 */
@Stepwise
@SpringBootTest()
@TestPropertySource("/test_application.properties")
class DemoPaymentHandlerSpec extends Specification {

    @Autowired
    ArticleDataRepository articleDataRepository

    @Autowired
    DemoFullPaymentDataRepository demoPaymentDataRepository

    @Autowired
    DemoPaymentHandler demoPaymentHandler

    @Autowired
    TokenGenerator tokenGenerator

    @Shared byte[] preImageHash

    OrderRequest validOrderRequest
    OrderRequest invalidOrderRequest

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup() {
        if (articleDataRepository.findByArticleId("abc123") == null) {
          ArticleData articleData = new ArticleData()
          articleData.articleId = "abc123"
          articleData.price = 10
          articleDataRepository.save(articleData)
        }

        if(preImageHash == null) {
            preImageHash = tokenGenerator.genPreImageData().preImageHash
        }

        validOrderRequest = new OrderRequest()
        validOrderRequest.articleId = "abc123"
        validOrderRequest.units = 2

        invalidOrderRequest = new OrderRequest()
        invalidOrderRequest.articleId = "notexists"

    }

    def "Verify that payment data handler method handles valid payment data request properly."(){
        when: // Verify that newPaymentData generates a new saved payment data.
        DemoFullPaymentData r1 = demoPaymentHandler.newPaymentData(preImageHash, validOrderRequest)
        then:
        r1.preImageHash == preImageHash
        !r1.settled
        r1.id != null
        ((CryptoAmount) r1.orderAmount).currencyCode == BTC.CURRENCY_CODE_BTC
        ((CryptoAmount) r1.orderAmount).value == 20

        when:
        DemoFullPaymentData r2  = demoPaymentHandler.findPaymentData(preImageHash)

        then:
        r2.id == r1.id

        when:
        r2.settled = true
        demoPaymentHandler.updatePaymentData(null,r2,null)
        DemoFullPaymentData r3 = demoPaymentDataRepository.findById(r2.id).get()
        then:
        r3.id == r2.id
        r3.settled
    }

    def "Verify that InternalErrorException is thrown if no article was found."(){
        when:
        demoPaymentHandler.newPaymentData(preImageHash, invalidOrderRequest)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error creating payment data, article id notexists doesn't exist in database."
    }


}
