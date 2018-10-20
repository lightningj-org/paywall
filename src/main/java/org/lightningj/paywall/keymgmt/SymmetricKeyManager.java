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

package org.lightningj.paywall.keymgmt;

import org.lightningj.paywall.InternalErrorException;

import java.security.Key;

/**
 * Base interface for a KeyManager in charge of maintaining symmetric keys.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public interface SymmetricKeyManager extends KeyManager{

    /**
     * Returns the key that should be used for symmetric operations for the given context.
     *
     * @param context related context.
     * @return related key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred retrieving the key.
     */
    Key getSymmetricKey(Context context) throws UnsupportedOperationException, InternalErrorException;

}
