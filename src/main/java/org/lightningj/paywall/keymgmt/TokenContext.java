/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.keymgmt;

/**
 * Cryptographic context specific for token generation specifying which
 * type of token about to be generated.
 *
 * Created by philip on 2018-10-07.
 */
public class TokenContext extends Context {

    public static TokenContext CONTEXT_PAYMENT_TOKEN = new TokenContext("PAYMENT_TOKEN");
    public static TokenContext CONTEXT_INVOICE_TOKEN = new TokenContext("INVOICE_TOKEN");
    public static TokenContext CONTEXT_SETTLEMENT_TOKEN = new TokenContext("SETTLEMENT_TOKEN");

    private String type;

    /**
     * Default constructor.
     *
     * @param type type of token context
     */
    TokenContext(String type){
        this.type = type;
    }

    /**
     *
     * @return type of token context
     */
    public String getType(){
        return type;
    }

    @Override
    public String toString() {
        return "TokenContext{" +
                "type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenContext context = (TokenContext) o;

        return type != null ? type.equals(context.type) : context.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
