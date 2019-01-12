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
package org.lightningj.paywall.tokengenerator;

import org.lightningj.paywall.keymgmt.Context;

/**
 * Cryptographic context specific for token generation specifying which
 * type of token about to be generated.
 *
 * Created by philip on 2018-10-07.
 */
public class TokenContext extends Context {

    public static String CONTEXT_PAYMENT_TOKEN_TYPE = "PAYMENT_TOKEN";
    public static String CONTEXT_INVOICE_TOKEN_TYPE = "INVOICE_TOKEN";
    public static String CONTEXT_SETTLEMENT_TOKEN_TYPE = "SETTLEMENT_TOKEN";

    private String type;
    private KeyUsage keyUsage;

    /**
     * Default constructor.
     *
     * @param type type of token context
     */
    TokenContext(String type, KeyUsage keyUsage){
        this.type = type;
        this.keyUsage = keyUsage;
    }

    /**
     *
     * @return type of token context
     */
    public String getType(){
        return type;
    }

    /**
     *
     * @return returns the requested key usage.
     */
    public KeyUsage getKeyUsage(){ return keyUsage; }

    @Override
    public String toString() {
        return "TokenContext{" +
                "type='" + type + '\'' +
                ", keyUsage=" + keyUsage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenContext that = (TokenContext) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return keyUsage == that.keyUsage;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (keyUsage != null ? keyUsage.hashCode() : 0);
        return result;
    }
}
