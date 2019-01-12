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
package org.lightningj.paywall.keymgmt

/**
 * Help class containing a common instance of dummy key manager to avoid regenerating new keys for each
 * unit test and speed up run.
 *
 * Created by philip on 2018-11-22.
 */
class DummyKeyManagerInstance {

    private static DummyKeyManager commonInstance = null;

    /**
     *
     * @return returns a one common instance that can be reused in test scripts to avoid regenerating keys for each
     * unit test.
     */
    static DummyKeyManager getCommonInstance(){
        if(commonInstance == null){
            commonInstance = new DummyKeyManager()
        }
        return commonInstance
    }
}
