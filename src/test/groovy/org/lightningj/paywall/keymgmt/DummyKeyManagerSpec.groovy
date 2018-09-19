/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.keymgmt

import spock.lang.Shared
import spock.lang.Specification

import javax.crypto.SecretKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

/**
 * Unit Tests for DummyKeyManager
 *
 * Created by Philip Vendil on 2018-09-14.
 */
class DummyKeyManagerSpec extends Specification {

    @Shared
    DummyKeyManager km = new DummyKeyManager()

    def "Verify that correct keys are returned by dummy key manager"(){
        expect:
        km.getSymmetricKey(null) instanceof SecretKey
        km.getPublicKey(null) instanceof RSAPublicKey
        km.getPrivateKey(null) instanceof RSAPrivateKey

        when:
        SecretKey s = km.getSymmetricKey(null)

        then:
        true
    }

    def "Verify that all keys are trusted"(){
        expect:
        km.isTrusted(null,null)
    }
}
