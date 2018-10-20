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

import java.io.IOException;

/**
 * Enumeration indicating the different roles that might access the BTC Pay Server API.
 *
 * Created by philip on 2018-10-14.
 */
public enum BTCPayServerFacade {

    MERCHANT,
    POS,
    PUBLIC;

    public String toString(){
        return this.name().toLowerCase();
    }


}
