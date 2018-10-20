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
package org.lightningj.paywall.btcpayserver;

import org.lightningj.paywall.btcpayserver.vo.Token;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Class acting as a cache and manages generated BTC Pay Server API tokens.
 *
 * Created by Philip Vendil on 2018-10-14.
 */
public class BTCPayServerTokenManager {

    private Map<BTCPayServerFacade, Token> tokenCache = new HashMap<>();

    protected Clock clock = Clock.systemDefaultZone();

    /**
     * Default constructor
     */
    public BTCPayServerTokenManager(){
    }

    /**
     * Fetches the related token for the given facade or null if doesn't exist of have expired.
     * @param facade the facade to fetch token for.
     * @return the related token value or null if doesn't exist or expired.
     */
    public Token get(BTCPayServerFacade facade){
        Token retval = tokenCache.get(facade);
        if(retval != null){
            if(retval.getExpireDate() != null && retval.getExpireDate().isAfter(clock.instant().minus(15, ChronoUnit.MINUTES))){ // allow 15 minutes clock desyncronisation.
                tokenCache.remove(facade);
                retval = null;
            }
        }
        return retval;
    }

    /**
     * Adds the given token in related to the facade to the cache.
     */
    public void put(Token token){
        tokenCache.put(token.getFacade(), token);
    }

    /**
     * Method to clear the current token cache.
     */
    public void clear(){
        tokenCache.clear();
    }


}
