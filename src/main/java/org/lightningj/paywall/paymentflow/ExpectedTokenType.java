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
package org.lightningj.paywall.paymentflow;

import org.lightningj.paywall.tokengenerator.TokenContext;

/**
 * Enumeration determining the expected type of token by the caller to
 * payment flow or payment flow manager.
 *
 *  Created by Philip Vendil on 2019-01-01.
 */
public enum ExpectedTokenType {

    PAYMENT_TOKEN(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE),
    INVOICE_TOKEN(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE),
    SETTLEMENT_TOKEN(TokenContext.CONTEXT_SETTLEMENT_TOKEN_TYPE);

    private String tokenContext;

    ExpectedTokenType(String tokenContext){
        this.tokenContext=tokenContext;
    }

    /**
     *
     * @return the related expected token context for given token type.
     */
    public String getTokenContext(){
        return tokenContext;
    }

}
