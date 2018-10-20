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
package org.lightningj.paywall.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * Bouncy castle related utility methods.
 *
 * Created by Philip Vendil on 2018-09-19.
 */
public class BCUtils {

    /**
     * Help method to install BouncyCastle Cryptographic library
     * into JVM.
     */
   public static void installBCProvider(){
       if (Security.getProvider("BC") == null){
           Security.addProvider(new BouncyCastleProvider());
       }
   }

}
