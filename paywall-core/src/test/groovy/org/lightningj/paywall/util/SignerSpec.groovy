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
package org.lightningj.paywall.util

import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.lightningj.paywall.keymgmt.KeySerializationHelper
import spock.lang.Shared
import spock.lang.Specification

import java.security.KeyFactory
import java.security.KeyPair

/**
 * Unit tests for Signer.
 *
 * Created by Philip Vendil on 2018-10-11.
 */
class SignerSpec extends Specification {


    @Shared KeyPair keyPair
    Signer signer = new Signer(Signer.ALG_SHA256_WITH_ECDSA)

    def setupSpec(){
        BCUtils.installBCProvider()
        keyPair = KeySerializationHelper.deserializeBTCPayServerKeyPair(privateKeyData,"foobar123".toCharArray(), KeyFactory.getInstance("EC","BC"),"secp256k1")
    }

    def "Verify that sign generates a valid Bitcoin encoded signature."(){
        setup:
        byte[] data = "abc123".getBytes("UTF-8")
        when:
        byte[] signature = signer.sign(keyPair.private,data)
        //println HexUtils.encodeHexString(signature)
        then:
        signer.verify(keyPair.public,data,signature)

    }


    static def privateKeyData = """Id :614A2C8B6C81A4CC
Generated :2018-10-11 11:18:40
Hostname :Philips-MBP
-----BEGIN EC PRIVATE KEY-----
Proc-Type: 4,ENCRYPTED
DEK-Info: AES-256-CBC,FD2F0E58A75E2ADB9E615D1B569BD360

ZquXZ4bORxSH3ODxOM2AF3objTJn+w+eGaVm264LFSQR3PUZZZJ7powuMixPJ8qR
3MKWKR+jcgPbo9XxBKCB+LJ8BVzdMjQQfOJ0xD+qy2x3bssWNqTydMaFn7v3VQ6a
sHD2QPBg67MXJjFTp7DiEiBArmA6UrQCjkVbcJjswEk=
-----END EC PRIVATE KEY-----""".getBytes()


}
