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
package org.lightningj.paywall.btcpayserver

import org.lightningj.paywall.keymgmt.KeySerializationHelper
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.HexUtils
import spock.lang.Shared
import spock.lang.Specification

import java.security.KeyFactory
import java.security.KeyPair

/**
 * Unit tests for BTCPayServerHelper
 *
 * Created by Philip Vendil on 2018-10-10.
 */
class BTCPayServerHelperSpec extends Specification {


    @Shared KeyPair keyPair

    BTCPayServerHelper btcPayServerHelper

    def setupSpec(){
        BCUtils.installBCProvider()
        keyPair = KeySerializationHelper.deserializeBTCPayServerKeyPair(privateKeyData,"foobar123".toCharArray(), KeyFactory.getInstance("EC","BC"),"secp256k1")
    }

    def setup(){
        btcPayServerHelper = new BTCPayServerHelper()
    }
    def "Verify that correct SIN number is generated for a given ECPublicKey"(){
        expect:
        btcPayServerHelper.toSIN("028EC9EE220F28DE5A0CAB46347D5D68503E3032CD5077A673DC9EF628545EE038") == "Tf8djDv7pBH19uYaRYjVuKwR6sXcibfCbCP"
        btcPayServerHelper.toSIN("02F840A04114081690223B7069071A70D6DABB891763B638CC20C7EC3BD58E6C86") == "TfG4ScDgysrSpodWD4Re5UtXmcLbY5CiUHA"
    }

    def "Verify that pubKeyInHex() converts the key ec Q point to compressed hexadecimal format"(){
        expect:
        btcPayServerHelper.pubKeyInHex(keyPair.public) == "026B871DF26A69210A2E8AD0EADA4F732931D3B16672DE7D97A521C798BA2860D0"
        btcPayServerHelper.pubKeyInHex(keyPair.public) == HexUtils.encodeHexString(keyPair.public.getQ().getEncoded(true))
    }

    def "Verify that doesn't throw exception when generating signature header"(){
        setup:
        String url = "https://btcpay302112.lndyn.com/invoices"
        String data = """{"price":0.1,"currency":"USD","token":"8zxKrx39TBcQPXgR4R2ih1b1V7YMVxHMT6Njcd2LQpzL"}"""
        when:
        btcPayServerHelper.genSignature(keyPair.private,url,data)
        then:
        true
    }

    // TODO Remove

    def "Postman Test and remove"(){
        setup:
        String sin = btcPayServerHelper.toSIN(btcPayServerHelper.pubKeyInHex(keyPair.public))
        String url = "https://btcpay302112.lndyn.com/invoices"
        String data = """{"price":0.1,"currency":"USD","token":"8zxKrx39TBcQPXgR4R2ih1b1V7YMVxHMT6Njcd2LQpzL"}"""
        when:
        println "sin: " + sin
        println "key hex: " + btcPayServerHelper.pubKeyInHex(keyPair.public)
        println "signature: " + btcPayServerHelper.genSignature(keyPair.private,url,data)
        then:
        true

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
