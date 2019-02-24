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

import org.lightningj.paywall.paymentflow.SettlementResult;
import org.lightningj.paywall.util.Base64Utils;
import org.lightningj.paywall.vo.Settlement;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * Value object used to return the current status of a settlement from
 * the CheckSettlementController
 *
 * @author philip 2019-02-13
 */
@XmlRootElement(name = "SettlementResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SettlementResponseType", propOrder = {
        "preImageHash",
        "token",
        "validUntil",
        "validFrom",
        "payPerRequest",
        "settled"
})
public class SettlementResponse {

    @XmlElement()
    private String preImageHash;
    @XmlElement()
    private String token;
    @XmlElement()
    private Date validUntil;
    @XmlElement()
    private Date validFrom;
    @XmlElement()
    private Boolean payPerRequest;
    @XmlElement(required = true)
    private boolean settled = false;

    /**
     * Empty Constructor
     */
    public SettlementResponse(){
    }

    /**
     * Constructor for settled invoices, of settlement is null is
     * only settled field set to false.
     * @param settlementResult A settlement result object, null if not settled.
     */
    public SettlementResponse(SettlementResult settlementResult){
        if(settlementResult != null && settlementResult.getSettlement() != null){
            Settlement settlement = settlementResult.getSettlement();
            preImageHash = Base64Utils.encodeBase64String(settlement.getPreImageHash());
            token = settlementResult.getToken();
            if(settlement.getValidUntil() != null) {
                validUntil = new Date(settlement.getValidUntil().toEpochMilli());
            }
            if(settlement.getValidFrom() != null) {
                validFrom = new Date(settlement.getValidFrom().toEpochMilli());
            }
            payPerRequest = settlement.isPayPerRequest();
            settled = true;
        }
    }

    /**
     *
     * @return related pre image hash hex encoded
     */
    public String getPreImageHash() {
        return preImageHash;
    }

    /**
     *
     * @param preImageHash related pre image hash hex encoded
     */
    public void setPreImageHash(String preImageHash) {
        this.preImageHash = preImageHash;
    }

    /**
     *
     * @return generated settlement JWT Token
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token generated settlement JWT Token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return how long the JWT Token is valid
     */
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     *
     * @param validUntil how long the JWT Token is valid
     */
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    /**
     *
     * @return the valid from date of the JWT Token, null if no valid from date exists.
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     *
     * @param validFrom the valid from date of the JWT Token, null if no valid from date exists.
     */
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    /**
     *
     * @return true if related payment is for one request only.
     */
    public Boolean getPayPerRequest() {
        return payPerRequest;
    }

    /**
     *
     * @param payPerRequest if related payment is for one request only.
     */
    public void setPayPerRequest(Boolean payPerRequest) {
        this.payPerRequest = payPerRequest;
    }

    /**
     *
     * @return true if payment is settled and settlement is generated.
     */
    public boolean isSettled() {
        return settled;
    }

    /**
     *
     * @param settled true if payment is settled and settlement is generated.
     */
    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    @Override
    public String toString() {
        return "SettlementResponse{" +
                "preImageHash='" + preImageHash + '\'' +
                ", token='" + token + '\'' +
                ", validUntil=" + validUntil +
                ", validFrom=" + validFrom +
                ", payPerRequest=" + payPerRequest +
                ", settled=" + settled +
                '}';
    }
}
