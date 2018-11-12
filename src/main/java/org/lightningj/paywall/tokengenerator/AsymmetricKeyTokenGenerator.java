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
package org.lightningj.paywall.tokengenerator;

import org.lightningj.paywall.keymgmt.AsymmetricKeyManager;


/**
 * Token Manager to generate token using asymmetric key manager.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public class AsymmetricKeyTokenGenerator extends BaseTokenGenerator{

    AsymmetricKeyManager keyManager;

    public AsymmetricKeyTokenGenerator(AsymmetricKeyManager keyManager){
        this.keyManager = keyManager;
    }

    // TODO

}
