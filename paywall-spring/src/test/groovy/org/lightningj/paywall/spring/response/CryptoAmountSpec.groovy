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
package org.lightningj.paywall.spring.response

import org.lightningj.paywall.vo.amount.CryptoAmount as IntCryptoAmount
import spock.lang.Specification

/**
 * Unit test for XML/JSON version of CryptoAmount
 * @author philip 2019-04-15
 */
class CryptoAmountSpec extends Specification {

    IntCryptoAmount intCryptoAmount1 = new  IntCryptoAmount(123, IntCryptoAmount.CURRENCY_CODE_BTC)
    IntCryptoAmount intCryptoAmount2 = new  IntCryptoAmount(2233, IntCryptoAmount.CURRENCY_CODE_LTC, org.lightningj.paywall.vo.amount.Magnetude.MILLI)


    def "Verify constructor and getter and setters"(){
        when:
        CryptoAmount ca1 = new CryptoAmount()
        then:
        ca1.getValue() == 0
        ca1.getCurrencyCode() == IntCryptoAmount.CURRENCY_CODE_BTC
        ca1.getMagnetude() == Magnetude.NONE
        when:
        ca1.setValue(88)
        ca1.setCurrencyCode("LTC")
        ca1.setMagnetude(Magnetude.MILLI)
        then:
        ca1.getValue() == 88
        ca1.getCurrencyCode() == "LTC"
        ca1.getMagnetude() == Magnetude.MILLI
        when:
        CryptoAmount ca2 = new CryptoAmount(intCryptoAmount1)
        then:
        ca2.value == 123
        ca2.currencyCode == IntCryptoAmount.CURRENCY_CODE_BTC
        ca2.magnetude == Magnetude.NONE
        when:
        CryptoAmount ca3 = new CryptoAmount(intCryptoAmount2)
        then:
        ca3.value == 2233
        ca3.currencyCode == IntCryptoAmount.CURRENCY_CODE_LTC
        ca3.magnetude == Magnetude.MILLI
    }

    def "Verify toString()"(){
        expect:
        new CryptoAmount(intCryptoAmount1).toString() == """CryptoAmount{value='123', currencyCode='BTC', magnetude=NONE}"""
    }

}
