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

import org.lightningj.paywall.keymgmt.Context;

/**
 * Cryptographic context specific for btcpay api key generation.
 *
 * Created by philip on 2018-10-07.
 */
public class BTCPayServerKeyContext extends Context {

    public static BTCPayServerKeyContext INSTANCE = new BTCPayServerKeyContext();

    /**
     * Default constructor.
     */
    BTCPayServerKeyContext(){
    }

    @Override
    public String toString() {
        return "BTCPayServerKeyContext{}";
    }
}
