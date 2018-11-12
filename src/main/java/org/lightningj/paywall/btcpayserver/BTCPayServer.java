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
package org.lightningj.paywall.btcpayserver;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.btcpayserver.vo.Invoice;
import org.lightningj.paywall.btcpayserver.vo.Token;
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lightningj.paywall.btcpayserver.BTCPayServerFacade.*;
import static org.lightningj.paywall.btcpayserver.BTCPayServerHTTPSender.METHOD.*;
/**
 * Main class in charge of communicating with BTC Pay Server API using REST.
 *
 * In charge of creating/parsing json data, and sending it to server and maintaining access tokens.
 *
 * Created by philip on 2018-10-17.
 */
public abstract class BTCPayServer {

    protected static final String ENDPOINT_INVOICE = "/invoices";
    protected static final String ENDPOINT_TOKENS = "/tokens";

    BTCPayServerTokenManager tokenManager = new BTCPayServerTokenManager();
    BTCPayServerHelper helper = new BTCPayServerHelper();
    BTCPayServerResponseParser parser = new BTCPayServerResponseParser();

    private BTCPayServerHTTPSender sender;

    protected static Logger log = Logger.getLogger(BTCPayServer.class.getName());

    public BTCPayServer(){
        this.sender = new BTCPayServerHTTPSender(getBaseURL(),getKeyManager());
    }


    public Invoice registerInvoice(Invoice invoice) throws IOException, InternalErrorException{
//        Token token = getToken(MERCHANT);
//        invoice.setToken(token.getToken());
        byte[] response = sender.send(POST,ENDPOINT_INVOICE, invoice,true);
        return parser.parseInvoice(response);
    }

    public Invoice fetchInvoice(String invoiceId) throws IOException, InternalErrorException{
        byte[] response = sender.send(GET,ENDPOINT_INVOICE + "/" + invoiceId,false);
        return parser.parseInvoice(response);
    }

    // get invoiced

    protected Token getToken(BTCPayServerFacade facade) throws IOException, InternalErrorException{
        Token token = tokenManager.get(facade);
        if(token == null){
            token = fetchToken(facade);
            tokenManager.put(token);
        }
        return token;
    }

    protected Token fetchToken(BTCPayServerFacade facade) throws IOException, InternalErrorException{
        PublicKey publicKey = getKeyManager().getPublicKey(BTCPayServerKeyContext.INSTANCE);
        String pubKeyHex = helper.pubKeyInHex((ECPublicKey) publicKey);
        String sIN = helper.toSIN(pubKeyHex);

       // Token token = new Token(sIN,getBTCPayServerClientName(),facade);
        Token token = new Token(sIN,null,null);
        //token.setPairingCode(getPairingCode());


        Token serverToken = parser.parseToken(sender.send(POST,ENDPOINT_TOKENS,token,false));

        if(log.isLoggable(Level.FINE)){
            log.fine("Fetched new token data for facade " + facade + ", token data: " + serverToken);
        }
        return serverToken;
    }



    protected abstract String getBTCPayServerClientName();
    protected abstract String getPairingCode();

    protected abstract String getBaseURL();

    protected abstract AsymmetricKeyManager getKeyManager();
}
