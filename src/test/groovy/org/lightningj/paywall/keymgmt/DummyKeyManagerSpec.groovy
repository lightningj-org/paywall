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

import org.lightningj.paywall.btcpayserver.BTCPayServerKeyContext
import org.lightningj.paywall.util.BCUtils
import spock.lang.Shared
import spock.lang.Specification

import javax.crypto.SecretKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

/**
 * Unit Tests for DummyKeyManager
 *
 * Created by Philip Vendil on 2018-09-14.
 */
class DummyKeyManagerSpec extends Specification {

    @Shared
    DummyKeyManager km

    def setupSpec(){
        BCUtils.installBCProvider()
        km = new DummyKeyManager()
    }

    def "Verify that correct keys are returned by dummy key manager"(){
        expect:
        km.getSymmetricKey(null) instanceof SecretKey
        km.getPublicKey(null) instanceof RSAPublicKey
        km.getPrivateKey(null) instanceof RSAPrivateKey
        km.getPublicKey(BTCPayServerKeyContext.INSTANCE) instanceof ECPublicKey
        km.getPrivateKey(BTCPayServerKeyContext.INSTANCE) instanceof ECPrivateKey
    }

    def "Verify that all keys are trusted"(){
        expect:
        km.isTrusted(null,null)
    }
}
