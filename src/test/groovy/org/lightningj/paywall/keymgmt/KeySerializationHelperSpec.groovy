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

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.btcpayserver.BTCPayServerHelper
import spock.lang.Shared
import spock.lang.Specification

import javax.crypto.KeyGenerator
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator

/**
 * Unit tests for KeySerializationHelper
 *
 * Created by Philip Vendil on 2018-09-16.
 */
class KeySerializationHelperSpec extends Specification {

    @Shared
    Key symmetricKey
    @Shared
    KeyPair asymmetricKey
    @Shared
    KeyPair btcPayServerKey

    def setupSpec() {
        BCUtils.installBCProvider()

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        symmetricKey = keyGenerator.generateKey()

        KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA")
        rsaKeyPairGenerator.initialize(1024)
        asymmetricKey = rsaKeyPairGenerator.generateKeyPair()

        KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC", "BC")
        ECNamedCurveParameterSpec curveSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
        ecKeyGen.initialize(curveSpec)
        btcPayServerKey = ecKeyGen.generateKeyPair()
    }

    def "Verify that serializeSecretKey() generates valid data and that deserializeSecretKey() can parse it"() {
        when:
        byte[] data = KeySerializationHelper.serializeSecretKey(symmetricKey, "foobar123".toCharArray())

        String dataString = new String(data, "UTF-8")
        then:
        dataString.contains(KeySerializationHelper.ID_TAG)
        dataString.contains(KeySerializationHelper.GENERATED_TAG)
        dataString.contains(KeySerializationHelper.HOSTNAME_TAG)
        dataString.contains(KeySerializationHelper.DATA_TAG)

        when:
        Key result = KeySerializationHelper.deserializeSecretKey(data, "foobar123".toCharArray())
        then:
        result.encoded == symmetricKey.encoded
    }

    def "Verify that serializeSecretKey() throws InternalErrorException for fault data"() {
        when:
        KeySerializationHelper.serializeSecretKey(null, "foobar123".toCharArray())
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error encoding secret key data: null"
    }

    def "Verify that serializeSecretKey() throws InternalErrorException for invalid pass phrase"() {
        when:
        KeySerializationHelper.serializeSecretKey(symmetricKey, null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error encoding secret key data: Error encrypting symmetric key, no protect pass phrase defined."
        when:
        KeySerializationHelper.serializeSecretKey(symmetricKey, new char[0])
        then:
        e = thrown(InternalErrorException)
        e.message == "Internal error encoding secret key data: Error encrypting symmetric key, no protect pass phrase defined."
    }

    def "Verify that deserializeSecretKey() throws InternalErrorException for fault data"() {
        when:
        KeySerializationHelper.deserializeSecretKey(null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error decoding secret key data: null"

        when:
        KeySerializationHelper.deserializeSecretKey("Invalid data\nOther row".getBytes())
        then:
        e = thrown(InternalErrorException)
        e.message == "Internal error decoding secret key data: no Data: tag found in secret key file"
    }

    def "Verify that deserializeSecretKey() throws InternalErrorException with password hint if pass phrase is wrong."() {
        setup:
        byte[] data = KeySerializationHelper.serializeSecretKey(symmetricKey, "foobar123".toCharArray())

        when:
        KeySerializationHelper.deserializeSecretKey(data, null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error decoding secret key data: Internal error encrypting secret key, (check passphrase) : The wrapped key is not padded correctly"

        when:
        KeySerializationHelper.deserializeSecretKey(data,"bad".toCharArray())
        then:
        e = thrown(InternalErrorException)
        e.message == "Internal error decoding secret key data: Internal error encrypting secret key, (check passphrase) : The wrapped key is not padded correctly"
    }

    def "Verify that serializeAsymKeyPair() generates valid key data with unencrypted private key and that deserializeKeyPair converts it back again."() {
        when:
        def data = KeySerializationHelper.serializeAsymKeyPair(asymmetricKey, "foobar123".toCharArray())

        def publicDataString = new String(data[0], "UTF-8")
        def privateDataString = new String(data[01], "UTF-8")

        then:
        publicDataString.contains(KeySerializationHelper.ID_TAG)
        publicDataString.contains(KeySerializationHelper.GENERATED_TAG)
        publicDataString.contains(KeySerializationHelper.HOSTNAME_TAG)
        publicDataString.contains(KeySerializationHelper.BEGIN_PUBLIC_KEY_TAG)
        publicDataString.contains(KeySerializationHelper.END_PUBLIC_KEY_TAG)

        privateDataString.contains(KeySerializationHelper.ID_TAG)
        privateDataString.contains(KeySerializationHelper.GENERATED_TAG)
        privateDataString.contains(KeySerializationHelper.HOSTNAME_TAG)
        privateDataString.contains("-----BEGIN RSA PRIVATE KEY-----")
        privateDataString.contains("-----END RSA PRIVATE KEY-----")
        privateDataString.contains("Proc-Type: 4,ENCRYPTED")
        privateDataString.contains("DEK-Info: AES-256-CB")

        when:
        def keyPair = KeySerializationHelper.deserializeAsymKeyPair(data[0], data[1], "foobar123".toCharArray(), KeyFactory.getInstance("RSA"))

        then:
        keyPair.public.encoded == asymmetricKey.public.encoded
        keyPair.private.encoded == asymmetricKey.private.encoded
    }



    def "Verify that serializeAsymKeyPair() with invalid data throws InternalErrorException"() {
        when:
        KeySerializationHelper.serializeAsymKeyPair(null,"foobar123".toCharArray())
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error encoding asymmetric key data: null"
    }

    def "Verify that serializeAsymKeyPair() throws InternalErrorException with pass phrase hint if passphrase is null or empty"() {
        when:
        KeySerializationHelper.serializeAsymKeyPair(asymmetricKey,null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Error encrypting asymmetric key, no protect pass phrase defined."
        when:
        KeySerializationHelper.serializeAsymKeyPair(asymmetricKey,new char[0])
        then:
        e = thrown(InternalErrorException)
        e.message == "Error encrypting asymmetric key, no protect pass phrase defined."
    }

    def "Verify that serializeAsymKeyPair() with null data throws InternalErrorException"() {
        when:
        KeySerializationHelper.deserializeAsymKeyPair(null, null, "foobar123".toCharArray(), KeyFactory.getInstance("RSA"))
        then:
        def e = thrown(InternalErrorException)
        e.message == "Error parsing public key: null"

        when:
        def data = KeySerializationHelper.serializeAsymKeyPair(asymmetricKey,  "foobar123".toCharArray())
        KeySerializationHelper.deserializeAsymKeyPair(data[0], data[1], "foobar123".toCharArray(), KeyFactory.getInstance("EC"))
        then:
        e = thrown(InternalErrorException)
        e.message.startsWith("Error parsing public key:")
    }

    def "Verify that serializeAsymKeyPair() throws InternalErrorException with pass phrase hint if passphrase is null or wrong"() {
        setup:
        def data = KeySerializationHelper.serializeAsymKeyPair(asymmetricKey,  "foobar123".toCharArray())
        def kf = KeyFactory.getInstance("RSA")
        when:
        KeySerializationHelper.deserializeAsymKeyPair(data[0],data[1],null,kf)
        then:
        def e = thrown(InternalErrorException)
        e.message.startsWith("Internal error decoding asymmetric key data (Check protect passphrase):")
        when:
        KeySerializationHelper.deserializeAsymKeyPair(data[0],data[1],"bad".toCharArray(),kf)
        then:
        e = thrown(InternalErrorException)
        e.message.startsWith("Internal error decoding asymmetric key data (Check protect passphrase):")
    }

    def "Verify that serializeBTCPayServerKeyPair() generates valid key data with unencrypted private key and that deserializeKeyPair converts it back again."() {
        when:
        def data = KeySerializationHelper.serializeBTCPayServerKeyPair(btcPayServerKey, "foobar123".toCharArray())

        def publicDataString = new String(data[0], "UTF-8")
        def privateDataString = new String(data[1], "UTF-8")

        then:
        publicDataString == new BTCPayServerHelper().pubKeyInHex(btcPayServerKey.public)

        privateDataString.contains(KeySerializationHelper.ID_TAG)
        privateDataString.contains(KeySerializationHelper.GENERATED_TAG)
        privateDataString.contains(KeySerializationHelper.HOSTNAME_TAG)
        privateDataString.contains("-----BEGIN EC PRIVATE KEY-----")
        privateDataString.contains("-----END EC PRIVATE KEY-----")
        privateDataString.contains("Proc-Type: 4,ENCRYPTED")
        privateDataString.contains("DEK-Info: AES-256-CB")

        when:
        def keyPair = KeySerializationHelper.deserializeBTCPayServerKeyPair(data[1], "foobar123".toCharArray(), KeyFactory.getInstance("EC"), "secp256k1")

        then:
        keyPair.public.encoded == btcPayServerKey.public.encoded
        keyPair.private.encoded == btcPayServerKey.private.encoded
    }

    def "Verify that serializeBTCPayServerKeyPair() with invalid data throws InternalErrorException"() {
        when:
        KeySerializationHelper.serializeBTCPayServerKeyPair(null,"foobar123".toCharArray())
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error encoding asymmetric key data: null"
    }

    def "Verify that serializeBTCPayServerKeyPair() throws InternalErrorException with pass phrase hint if passphrase is null or empty"() {
        when:
        KeySerializationHelper.serializeBTCPayServerKeyPair(btcPayServerKey,null)
        then:
        def e = thrown(InternalErrorException)
        e.message == "Error encrypting BTC Pay Server Token Access key, no protect pass phrase defined."
        when:
        KeySerializationHelper.serializeBTCPayServerKeyPair(btcPayServerKey,new char[0])
        then:
        e = thrown(InternalErrorException)
        e.message == "Error encrypting BTC Pay Server Token Access key, no protect pass phrase defined."
    }

    def "Verify that deserializeBTCPayServerKeyPair() with null data throws InternalErrorException"() {
        when:
        KeySerializationHelper.deserializeBTCPayServerKeyPair( null, "foobar123".toCharArray(), KeyFactory.getInstance("EC"),"secp256k1")
        then:
        def e = thrown(InternalErrorException)
        e.message == "Internal error decoding btc pay server token access key data (Check protect passphrase): null"

        when:
        def data = KeySerializationHelper.serializeBTCPayServerKeyPair(btcPayServerKey,  "foobar123".toCharArray())
        KeySerializationHelper.deserializeBTCPayServerKeyPair(data[1], "foobar123".toCharArray(), KeyFactory.getInstance("RSA"),"secp256k1")
        then:
        e = thrown(InternalErrorException)
        e.message.startsWith("Internal error decoding btc pay server token access key data (Check protect passphrase):")
    }

    def "Verify that deserializeBTCPayServerKeyPair() throws InternalErrorException with pass phrase hint if passphrase is null or wrong"() {
        setup:
        def data = KeySerializationHelper.serializeBTCPayServerKeyPair(btcPayServerKey,  "foobar123".toCharArray())
        def kf = KeyFactory.getInstance("EC")
        when:
        KeySerializationHelper.deserializeBTCPayServerKeyPair(data[1],null,kf,"secp256k1")
        then:
        def e = thrown(InternalErrorException)
        e.message.startsWith("Internal error decoding btc pay server token access key data (Check protect passphrase):")
        when:
        KeySerializationHelper.deserializeBTCPayServerKeyPair(data[1],"bad".toCharArray(),kf,"secp256k1")
        then:
        e = thrown(InternalErrorException)
        e.message.startsWith("Internal error decoding btc pay server token access key data (Check protect passphrase):")
    }
}
