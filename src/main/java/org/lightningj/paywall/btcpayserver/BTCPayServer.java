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
package org.lightningj.paywall.btcpayserver;

import org.lightningj.paywall.keymgmt.AsymmetricKeyManager;

/**
 * Main class in charge of communicating with BTC Pay Server API using REST.
 *
 * In charge of creating/parsing json data, and sending it to server and maintaining access tokens.
 *
 * Created by philip on 2018-10-17.
 */
public abstract class BTCPayServer {

    BTCPayServerTokenManager tokenManager = new BTCPayServerTokenManager();

    private BTCPayServerHTTPSender sender;

    public BTCPayServer(){
        this.sender = new BTCPayServerHTTPSender(getBaseURL(),getKeyManager());
    }




    protected abstract String getBaseURL();

    protected abstract AsymmetricKeyManager getKeyManager();
}
