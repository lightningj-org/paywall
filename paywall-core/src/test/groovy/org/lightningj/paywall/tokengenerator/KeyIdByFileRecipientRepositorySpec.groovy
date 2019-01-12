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
package org.lightningj.paywall.tokengenerator

import org.jose4j.jwk.EllipticCurveJsonWebKey
import org.jose4j.jwk.RsaJsonWebKey
import org.lightningj.paywall.keymgmt.DummyKeyManagerInstance
import org.lightningj.paywall.keymgmt.RecipientKeyManager
import org.lightningj.paywall.keymgmt.TestDefaultRecipientKeyManager
import org.lightningj.paywall.util.BCUtils
import spock.lang.Shared
import spock.lang.Specification

import java.security.interfaces.DSAPublicKey
import java.util.logging.Logger

import static org.lightningj.paywall.tokengenerator.TokenContext.*
import static org.lightningj.paywall.keymgmt.Context.KeyUsage.*

/**
 * Unit tests for KeyIdByFileRecipientRepository.
 *
 * Created by Philip Vendil on 2018-11-21.
 */
class KeyIdByFileRecipientRepositorySpec extends Specification {

    KeyIdByFileRecipientRepository rr

    def ctx = new TokenContext(CONTEXT_PAYMENT_TOKEN_TYPE,SIGN)

    @Shared def orgLog
    def setupSpec(){
        // Remove expected error logging from stdout
        orgLog = TestDefaultRecipientKeyManager.log
        TestDefaultRecipientKeyManager.log = Mock(Logger)
    }

    def cleanupSpec(){
        TestDefaultRecipientKeyManager.log = orgLog
    }

    def setup(){
        BCUtils.installBCProvider()
        TestDefaultRecipientKeyManager rkm = new TestDefaultRecipientKeyManager("src/test/resources/testrecipientsdir",DummyKeyManagerInstance.commonInstance)
        rr = new KeyIdByFileRecipientRepository(rkm)
    }

    def "Verify that findRecipientKey returns valid RSA public key for subject that matches existing key id"(){
        when:
        def res =rr.findRecipientKey(null,"9CCEC6A03406C02F")
        then:
        res instanceof RsaJsonWebKey
        res.key != null
        res.keyId == "9CCEC6A03406C02F"
    }

    def "Verify that findRecipientKey throws TokenException for unknown subject"(){
        when:
        rr.findRecipientKey(null,"someunknown")
        then:
        def e = thrown(TokenException)
        e.message == "Error couldn't find any recipient key to encrypt token to having key id (subject): someunknown"
        e.reason == TokenException.Reason.INVALID
    }

    def "Verify that findRecipientKey returns EllipticCurveJsonWebKey for an EC public key"(){
        setup:
        RecipientKeyManager rkm = Mock(RecipientKeyManager)
        KeyIdByFileRecipientRepository rr = new KeyIdByFileRecipientRepository(rkm)
        when:
        def res = rr.findRecipientKey(ctx, "abc123")
        then:
        res instanceof EllipticCurveJsonWebKey
        res.key != null
        res.keyId == "abc123"
        then:
        1 * rkm.getReceipients(ctx) >> {
            ["abc123": DummyKeyManagerInstance.commonInstance.btcPayServerKey.public]
        }
    }

    def "Verify that findRecipientKey throws TokenException if underlying public key is of unsupported type"(){
        setup:
        RecipientKeyManager rkm = Mock(RecipientKeyManager)
        KeyIdByFileRecipientRepository rr = new KeyIdByFileRecipientRepository(rkm)
        when:
        rr.findRecipientKey(ctx,"abc123")
        then:
        def e = thrown(TokenException)
        e.message =~ "Error finding public key to encrypt token with key id abc123, unsupport key type:"
        1 * rkm.getReceipients(ctx) >> {
            ["abc123": Mock(DSAPublicKey)]
        }
    }

    def "Verify that null subject throws TokenException"(){
        when:
        rr.findRecipientKey(ctx,null)
        then:
        def e = thrown(TokenException)
        e.message == "Error finding recipient key for subject null."
        e.reason == TokenException.Reason.INVALID
    }
}
