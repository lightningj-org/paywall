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

/**
 * Base interface for a KeyManager in charge of maintaining symmetric and asymmetric keys.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public interface KeyManager {

    /**
     * Method to return the Security Provider to use in given context.
     * @param context the related context
     * @return the provider to use.
     */
    String getProvider(Context context);

}
