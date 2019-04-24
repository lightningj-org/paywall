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
package org.lightningj.paywall.spring.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * XML and JSON representation of CryptoAmount.
 *
 * @author philip 2019-04-15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CryptoAmountType", propOrder = {
        "value",
        "currencyCode",
        "magnetude"
})
public class CryptoAmount {

    @XmlElement(required = true)
    private long value;
    @XmlElement(defaultValue = org.lightningj.paywall.vo.amount.CryptoAmount.CURRENCY_CODE_BTC)
    private String currencyCode;
    @XmlElement(defaultValue = "NONE")
    private Magnetude magnetude;

    /**
     * Empty constructor.
     */
    public CryptoAmount(){}

    /**
     * Constructor converting an internal cryptoAmount to XML/JSON version.
     * @param cryptoAmount the cryptoAmount to convert.
     */
    public CryptoAmount(org.lightningj.paywall.vo.amount.CryptoAmount cryptoAmount){
        this.value = cryptoAmount.getValue();
        this.currencyCode = cryptoAmount.getCurrencyCode();
        this.magnetude = Magnetude.fromInternalMagnetude(cryptoAmount.getMagnetude());
    }

    /**
     *
     * @return the crypto amount value.
     */
    public long getValue() {
        return value;
    }

    /**
     *
     * @param value the crypto amount value.
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     *
     * @return the crypto currency code
     */
    public String getCurrencyCode() {
        if(currencyCode == null){
            return org.lightningj.paywall.vo.amount.CryptoAmount.CURRENCY_CODE_BTC;
        }
        return currencyCode;
    }

    /**
     *
     * @param currencyCode the crypto currency code
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     *
     * @return the magnetude of the amount.
     */
    public Magnetude getMagnetude() {
        if(magnetude == null){
            return Magnetude.NONE;
        }
        return magnetude;
    }

    /**
     *
     * @return the magnetude of the amount.
     */
    public void setMagnetude(Magnetude magnetude) {
        this.magnetude = magnetude;
    }

    @Override
    public String toString() {
        return "CryptoAmount{" +
                "value='" + value + '\'' +
                ", currencyCode='" + getCurrencyCode() + '\'' +
                ", magnetude=" + getMagnetude() +
                '}';
    }
}
