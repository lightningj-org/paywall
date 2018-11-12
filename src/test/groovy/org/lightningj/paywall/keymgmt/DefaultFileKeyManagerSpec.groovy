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

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.btcpayserver.BTCPayServerKeyContext
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.btcpayserver.BTCPayServerHelper
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

import static DefaultFileKeyManager.*
/**
 * Unit tests for DefaultFileKeyManager
 *
 * Created by Philip Vendil on 2018-09-20.
 */
class DefaultFileKeyManagerSpec extends Specification {

    def km = new TestDefaultFileKeyManager("target/tmp", "src/test/resources/testtruststoredir/","foobar321")

    BTCPayServerHelper btcPayServerUtils = new BTCPayServerHelper()

    @Shared def originalLog
    def setupSpec(){
        BCUtils.installBCProvider()
        originalLog = FileKeyManager.log
    }

    def cleanupSpec(){
        FileKeyManager.log = originalLog
    }

    def setup(){
        FileKeyManager.log = Mock(Logger)
    }

    def "Verify that a new key pair is generated if not exists"(){
        setup:
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).delete()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).delete()
        expect: // Verify that key isn't cached
        km.keyPairField == null
        when:
        PrivateKey privateKey = km.getPrivateKey(null)
        PublicKey publicKey = km.getPublicKey(null)
        then:
        km.getPrivateKey(null) == privateKey // Verify it is cached
        km.getPublicKey(null) == publicKey

        km.keyPairField != null
        km.btcKeyPair == null
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).exists()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).exists()
        1 * FileKeyManager.log.info("New asymmetric key generated and stored in files target/tmp/asymkey_pub.pem and target/tmp/asymkey_prv.pem")
        cleanup:
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).delete()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).delete()
    }

    def "Verify that a existing key pair is loaded if exists"(){
        setup:
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).delete()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).delete()
        km.getPrivateKey(null) // make sure the keys are generated
        km.keyPairField = null // clean cache
        when:
        PrivateKey privateKey = km.getPrivateKey(null)
        PublicKey publicKey = km.getPublicKey(null)
        then:
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).exists()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).exists()
        km.getPrivateKey(null) == privateKey // Verify it is cached
        km.getPublicKey(null) == publicKey

        1 * FileKeyManager.log.info("Loading existing asymmetric key from files target/tmp/asymkey_pub.pem and target/tmp/asymkey_prv.pem")
        cleanup:
        new File("target/tmp" + ASYM_PRIVATE_KEYNAME).delete()
        new File("target/tmp" + ASYM_PUBLIC_KEYNAME).delete()
    }

    def "Verify that trust cache is setup properly"(){
        setup:
        PublicKey untrustedKey = KeySerializationHelper.deserializePublicKey(untrustedKey, km.RSAKeyFactory)
        PublicKey trustedKey1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testtruststoredir/pubkey1.pem").toPath()), km.RSAKeyFactory)
        PublicKey trustedKey2 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testtruststoredir/pubkey2.pEm").toPath()), km.RSAKeyFactory)

        when:
        km.isTrusted(null,trustedKey1)
        km.isTrusted(null,trustedKey2)
        !km.isTrusted(null,untrustedKey)
        km.trustedKeysCache.size() == 2
        km.cacheExpireDate > 0
        then:
        1 * log.log(Level.SEVERE,"Error parsing trusted public key file: src/test/resources/testtruststoredir/invalid1.pEm, error: Error parsing public key: encoded key spec not recognized: unknown object in getInstance: org.bouncycastle.asn1.DERApplicationSpecific",_ as InternalErrorException)
        1 * log.log(Level.SEVERE,"Error parsing trusted public key file: src/test/resources/testtruststoredir/invalid2.pEm, error: Error parsing public key: encoded key spec not recognized: null",_ as InternalErrorException)
        1 * log.fine("Parsing trusted public key file: src/test/resources/testtruststoredir/pubkey1.pem")
        1 * log.fine("Parsing trusted public key file: src/test/resources/testtruststoredir/pubkey2.pEm")
        1 * log.fine("Parsing trusted public key file: src/test/resources/testtruststoredir/invalid1.pEm")
        1 * log.fine("Parsing trusted public key file: src/test/resources/testtruststoredir/invalid2.pEm")

        when: // Verify that cache is recalculated after cache expires
        km.trustedKeysCache.clear()
        km.forwardClock(Duration.parse("PT1M"))
        then:
        !km.isTrusted(null,trustedKey1)
        when:
        km.forwardClock(Duration.parse("PT5M"))
        then:
        km.isTrusted(null,trustedKey1)
    }

    def "Verify that if no trust store directory have been configured is current public key trusted with warning"(){
        setup:
        km = new TestDefaultFileKeyManager("target/tmp", null,"foobar321")
        PublicKey key1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testtruststoredir/pubkey1.pem").toPath()), km.RSAKeyFactory)
        when:
        PublicKey ownPk = km.getPublicKey(null)
        boolean isTrusted = km.isTrusted(null,ownPk)
        then:
        !km.isTrusted(null,key1)
        km.trustedKeysCache.size() == 1
        1 * log.warning("Warning: no trust store directory configured, using own public key as trust. Should not be used in production.")
        1 * log.fine("Parsing trusted public key file: target/tmp/asymkey_pub.pem")
    }

    def "Verify that invalid configured trust store directory thrown InternalErrorException"(){
        setup:
        km = new TestDefaultFileKeyManager("target/tmp", "invaliddir","foobar321")
        PublicKey key1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testtruststoredir/pubkey1.pem").toPath()), km.RSAKeyFactory)
        when:
        km.isTrusted(null,key1)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error parsing public keys in trust store directory: invaliddir check that it exists and is readable"


    }

    def "Verify that a new symmetric key is generated if not exists"(){
        setup:
        File keyFile = new File("target/tmp/secret.key")
        if(keyFile.exists()){
            keyFile.delete()
        }
        expect: // Verify that key isn't cached
        km.secretKey == null
        when:
        Key secretKey = km.getSymmetricKey(null)
        then:
        km.secretKey == secretKey // Verify it is cached
        km.secretKey.getAlgorithm() == "AES"
        new File("target/tmp/secret.key").exists()
        1 * FileKeyManager.log.info("New symmetric key generated and stored in file: target/tmp/secret.key")
        cleanup:
        new File("target/tmp/secret.key").delete()
    }

    def "Verify that a if a symmetric key file exists it is loaded from disk."(){
        setup: // Create key file and clear cached key
        km.getSymmetricKey(null)
        km.secretKey = null
        expect:
        new File("target/tmp/secret.key").exists()
        when:
        Key secretKey = km.getSymmetricKey(null)
        then:
        km.secretKey == secretKey // Verify it is cached
        km.secretKey.getAlgorithm() == "AES"
        new File("target/tmp/secret.key").exists()
        1 * FileKeyManager.log.info("Loading existing symmetric key from file: target/tmp/secret.key")
        cleanup:
        new File("target/tmp/secret.key").delete()
    }

    def "Verify that a new BTC Pay Server private key is generated if not exists"(){
        setup:
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).delete()
        expect: // Verify that key isn't cached
        km.btcKeyPair == null
        km.keyPairField == null
        when:
        PrivateKey privateKey = km.getPrivateKey(BTCPayServerKeyContext.INSTANCE)
        PublicKey publicKey = km.getPublicKey(BTCPayServerKeyContext.INSTANCE)

        String publicKeyHex = btcPayServerUtils.pubKeyInHex(publicKey)
        String sIN = btcPayServerUtils.toSIN(publicKeyHex)
        then:
        km.getPrivateKey(BTCPayServerKeyContext.INSTANCE) == privateKey // Verify it is cached
        km.getPublicKey(BTCPayServerKeyContext.INSTANCE) == publicKey
        km.btcKeyPair != null
        km.keyPairField == null
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).exists()
        File pubFile = new File("target/tmp" + BTCPAY_SERVER_PUBLIC_KEYNAME.replaceAll("@SIN@",sIN))
        pubFile.exists()
        Files.readAllLines(pubFile.toPath())[0] == publicKeyHex
        1 * FileKeyManager.log.info(_) >> { it -> assert it =~ "New BTC Pay Server access key generated and stored in files target/tmp/btcpayserver_key_prv.pem and target/tmp/btcpayserver_key_pub_sin_"}
        cleanup:
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).delete()
    }

    def "Verify that a BTC Pay Server key pair is loaded if exists"(){
        setup:
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).delete()
        km.getPrivateKey(BTCPayServerKeyContext.INSTANCE) // make sure the keys are generated
        km.keyPairField = null // clean cache
        km.btcKeyPair = null
        when:
        PrivateKey privateKey = km.getPrivateKey(BTCPayServerKeyContext.INSTANCE)
        PublicKey publicKey = km.getPublicKey(BTCPayServerKeyContext.INSTANCE)
        then:
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).exists()
        km.getPrivateKey(BTCPayServerKeyContext.INSTANCE) == privateKey // Verify it is cached
        km.getPublicKey(BTCPayServerKeyContext.INSTANCE) == publicKey

        1 * FileKeyManager.log.info(_) >> { it -> assert it =~ "Loading existing BTC Pay Server access key from file target/tmp/btcpayserver_key_prv.pem, SIN: "}
        cleanup:
        new File("target/tmp" + BTCPAY_SERVER_PRIVATE_KEYNAME).delete()
    }

    def untrustedKey = """Id :617EDB5463CF4873
Generated :2018-09-20 10:38:37
Hostname :Philips-MBP
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtsWyuyeJSz6Cr29eFcZr
4v9N/whbJlhA93sAlmfzOPgX8ZLhHiUJi9jfv6BJSKyIL7q+JaF8w8QO7Mbncgv3
YkZJHrDXTSv7oqLt3DWb8ZWyAxLTECLfVsSij0xDwO/JQKkAighldvgLO+JnmVhb
klBvtmkaCXxTMIzNGWlNXx1kkHRzuIawzh6waaQWxcVCW+osI0Hyje5EoZ9iatxL
zBdfmKaSVUYVs/s0/QvE4yPAqLjHt9e6kIonfFpsAFCzo0Dnfs4oQ3tg4vkgEjEC
cPjPv7dPO9BLM5B3ZcXAw3KcuVC0GKp4FbiIY5Z/3Hq7U0DHG+LZPm3yqaIx7tJc
yQIDAQAB
-----END PUBLIC KEY-----""".getBytes("UTF-8")

    static def btcPayServerPrivateKeyData = """Id :614A2C8B6C81A4CC
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
