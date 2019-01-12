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
package org.lightningj.paywall.keymgmt

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.util.BCUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.security.PublicKey
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

import static org.lightningj.paywall.keymgmt.TestDefaultRecipientKeyManager.log

/**
 * Unit tests for DefaultRecipientKeyManager
 *
 * Created by philip on 2018-11-21.
 */
class DefaultRecipientKeyManagerSpec extends Specification {

    def km = new TestDefaultFileKeyManager("target/tmp", "src/test/resources/testtruststoredir/","foobar321")
    def rkm = new TestDefaultRecipientKeyManager("src/test/resources/testrecipientsdir/",km)

    @Shared def originalLog
    def setupSpec(){
        BCUtils.installBCProvider()
        originalLog = DefaultRecipientKeyManager.log
    }

    def cleanupSpec(){
        DefaultRecipientKeyManager.log = originalLog
    }

    def setup(){
        DefaultRecipientKeyManager.log = Mock(Logger)
    }

    def "Verify that recipients cache is setup properly"(){
        setup:
        PublicKey untrustedKey = KeySerializationHelper.deserializePublicKey(DefaultFileKeyManagerSpec.untrustedKey, rkm.RSAKeyFactory)
        PublicKey trustedKey1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testrecipientsdir/pubkey1.pem").toPath()), rkm.RSAKeyFactory)
        PublicKey untrustedKey2 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testtruststoredir/pubkey2.pEm").toPath()), rkm.RSAKeyFactory)

        when:
        rkm.getReceipients(null).values().contains(trustedKey1)
        !rkm.getReceipients(null).values().contains(untrustedKey2)
        !rkm.getReceipients(null).values().contains(untrustedKey)
        rkm.trustedRecipientsKeysCache.size() == 1
        rkm.reciepientsStoreCacheExpireDate > 0
        then:
        1 * log.log(Level.SEVERE,"Error parsing recipient public key file: src/test/resources/testrecipientsdir/invalid1.pEm, error: Error parsing public key: encoded key spec not recognized: unknown object in getInstance: org.bouncycastle.asn1.DERApplicationSpecific",_ as InternalErrorException)
        1 * log.log(Level.SEVERE,"Error parsing recipient public key file: src/test/resources/testrecipientsdir/invalid2.pEm, error: Error parsing public key: encoded key spec not recognized: null",_ as InternalErrorException)
        1 * log.fine("Parsing recipient public key file: src/test/resources/testrecipientsdir/pubkey1.pem")
        1 * log.fine("Parsing recipient public key file: src/test/resources/testrecipientsdir/invalid1.pEm")
        1 * log.fine("Parsing recipient public key file: src/test/resources/testrecipientsdir/invalid2.pEm")

        when: // Verify that cache is recalculated after cache expires
        rkm.trustedRecipientsKeysCache.clear()
        rkm.forwardClock(Duration.parse("PT1M"))
        then:
        !rkm.getReceipients(null).values().contains(trustedKey1)
        when:
        rkm.forwardClock(Duration.parse("PT5M"))
        then:
        rkm.getReceipients(null).values().contains(trustedKey1)
    }

    def "Verify that if no recipient store directory have been configured is current public key trusted with warning"(){
        setup:
        rkm = new TestDefaultRecipientKeyManager(null,km)
        PublicKey key1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testrecipientsdir/pubkey1.pem").toPath()), km.RSAKeyFactory)
        when:
        PublicKey ownPk = km.getPublicKey(null)
        boolean isTrusted = rkm.getReceipients(null).values().contains(ownPk)
        then:
        isTrusted
        !rkm.getReceipients(null).values().contains(key1)
        rkm.trustedRecipientsKeysCache.size() == 1
        1 * log.warning("Warning: no recipients store directory configured, using own public key as recipient. Should not be used in production.")
    }

    def "Verify that invalid configured recipient store directory thrown InternalErrorException"(){
        setup:
        rkm = new TestDefaultRecipientKeyManager( "invaliddir",km)
        PublicKey key1 = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(new File("src/test/resources/testrecipientsdir/pubkey1.pem").toPath()), km.RSAKeyFactory)
        when:
        rkm.getReceipients(null).values().contains(key1)
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error parsing public keys in recipients store directory: invaliddir check that it exists and is readable"
    }
}
