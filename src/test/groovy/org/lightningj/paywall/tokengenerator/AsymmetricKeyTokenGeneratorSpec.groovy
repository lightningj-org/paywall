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

import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.lightningj.paywall.keymgmt.*
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.PaymentData
import org.lightningj.paywall.vo.amount.BTC
import spock.lang.Shared
import spock.lang.Specification

import java.security.PublicKey
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.logging.Logger

import static org.lightningj.paywall.keymgmt.Context.KeyUsage.SIGN
import static org.lightningj.paywall.tokengenerator.TokenContext.*

/**
 * Unit tests for AsymmetricKeyTokenGenerator
 * Created by philip on 2018-11-17.
 */
class AsymmetricKeyTokenGeneratorSpec extends Specification {

    AsymmetricKeyManager keyManager =  DummyKeyManagerInstance.commonInstance
    Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
    Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
    def paymentData = new PaymentData("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
    def ctx_type = CONTEXT_PAYMENT_TOKEN_TYPE

    AsymmetricKeyTokenGenerator tokenGenerator

    @Shared def orgLog
    def setupSpec(){
        BCUtils.installBCProvider()
        // Remove expected error logging from stdout
        orgLog = TestDefaultRecipientKeyManager.log
        TestDefaultRecipientKeyManager.log = Mock(Logger)
    }

    def cleanupSpec(){
        TestDefaultRecipientKeyManager.log = orgLog
    }

    def setup(){

        RecipientKeyManager rkm = Mock(RecipientKeyManager)
        rkm.getReceipients(_) >> { Context ctx ->
            PublicKey publicKey = keyManager.getPublicKey(ctx)
            String keyId = KeySerializationHelper.genKeyId(publicKey.encoded)
            return [ (keyId) : publicKey]
        }
        def recipientRepository = new KeyIdByFileRecipientRepository(rkm)
        tokenGenerator = new TestAsymmetricKeyTokenGenerator(keyManager,recipientRepository)
    }

    def "Verify that asymmetric JWS is correctly signed and algorithm RS256 is used, and can be parsed and verified"(){
        when:
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData)
       println token

        JsonWebSignature jws = new JsonWebSignature()
        jws.setCompactSerialization(token)
        then:
        jws.getHeader("alg") == "RS256"
        jws.getHeader("kid") == KeySerializationHelper.genKeyId(keyManager.getPublicKey(new TokenContext(ctx_type,SIGN)).encoded)

        when:
        tokenGenerator.parseToken(ctx_type,token)
        then:
        true
    }

    def "Verify that unknown key id throws TokenVerifyException"(){
        setup:
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData)
        AsymmetricKeyManager keyManager2 = new DummyKeyManager()
        tokenGenerator.keyManager = keyManager2 // new key set
        when:
        tokenGenerator.parseToken(ctx_type,token)
        then:
        def e = thrown TokenException
        e.message == "Error verifying token signature, signature key is not trusted."
    }

    def "Verify that altered token data throws TokenVerifyException"(){
        setup:
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData)
        char[] tokenChars = token.toCharArray()
        tokenChars[55] = 'a'
        String modifiedToken = new String(tokenChars)
        when:
        tokenGenerator.parseToken(ctx_type,modifiedToken)
        then:
        def e = thrown TokenException
        e.message == "Invalid signature for token."
    }

    def "Verify that getTrustedKeysAsJWTKeys builds cache correctly"(){
        setup:
        tokenGenerator.keyManager = Mock(AsymmetricKeyManager)
        when:

        def set1 = tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_INVOICE_TOKEN_TYPE,SIGN)).jsonWebKeys
        def set2 = tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_PAYMENT_TOKEN_TYPE,SIGN)).jsonWebKeys
        def set3 = tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_SETTLEMENT_TOKEN_TYPE,SIGN)).jsonWebKeys
        tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_SETTLEMENT_TOKEN_TYPE, SIGN)).jsonWebKeys
        then:
        set1.size() == 1
        set2.size() == 1
        set3.size() == 1
        tokenGenerator.cacheExpireDate > 0
        3 * tokenGenerator.keyManager.getTrustedKeys(!null) >> {
            keyManager.getTrustedKeys(null)
        }
        when: // Verify that cache is recalculated after cache expires
        tokenGenerator.trustedSigningPublicKeys.clear()
        tokenGenerator.forwardClock(Duration.parse("PT1M"))
        def result = tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_INVOICE_TOKEN_TYPE,SIGN))
        then:
        result == null
        0 * tokenGenerator.keyManager.getTrustedKeys(!null)
        when:
        tokenGenerator.forwardClock(Duration.parse("PT5M"))
        def set4 = tokenGenerator.getTrustedKeysAsJWTKeys(new TokenContext(CONTEXT_INVOICE_TOKEN_TYPE,SIGN)).jsonWebKeys
        then:
        set4.size() == 1
        3 * tokenGenerator.keyManager.getTrustedKeys(!null) >> keyManager.getTrustedKeys(null)
    }

    def "Verify that it is possible to encrypt and decrypt using asymmetric encryption."(){
        setup:
        String recipientKeyId = KeySerializationHelper.genKeyId(keyManager.getPublicKey(null).encoded)
        when:
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,true,recipientKeyId,paymentData)
        //println token
        then:
        token != null
        when:
        JwtClaims claims = tokenGenerator.parseToken(ctx_type,token)
        PaymentData pd = new PaymentData(claims)
        then:
        claims.subject == recipientKeyId
        pd != null
    }

    def "Verify that it is not possible to decrypt a token with another asymmetric key"(){
        setup:
        RecipientKeyManager rkm = new TestDefaultRecipientKeyManager("src/test/resources/testtruststoredir/",keyManager)
        def recipientRepository = new KeyIdByFileRecipientRepository(rkm)
        tokenGenerator = new TestAsymmetricKeyTokenGenerator(keyManager,recipientRepository)
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,true,"9CCEC6A03406C02F",paymentData)

        when:
        tokenGenerator.parseToken(ctx_type,token)
        then:
        def e = thrown TokenException
        e.message =~ "Unable to decrypt token: Authentication tag check failed."
    }

    def "Verify that it is not possible to decrypt altered data"(){
        setup:
        String recipientKeyId = KeySerializationHelper.genKeyId(keyManager.getPublicKey(null).encoded)
        String token = tokenGenerator.generateToken(ctx_type,expireDate,notBefore,true,recipientKeyId,paymentData)
        char[] tokenChars = token.toCharArray()
        tokenChars[100] = 'a'
        String modifiedToken = new String(tokenChars)
        when:
        tokenGenerator.parseToken(ctx_type,modifiedToken)
        then:
        def e = thrown TokenException
        e.message =~ "Unable to decrypt token: Authentication tag check failed."
    }

    def "Verify that getIssuerName returns the public key id"(){
        expect:
        tokenGenerator.getIssuerName(null) == KeySerializationHelper.genKeyId(keyManager.getPublicKey(null).encoded)
    }

    static class TestAsymmetricKeyTokenGenerator extends AsymmetricKeyTokenGenerator{

        TestAsymmetricKeyTokenGenerator(AsymmetricKeyManager keyManager,RecipientRepository recipientRepository) {
            super(keyManager, recipientRepository)
        }

        void forwardClock(Duration duration){
            this.clock = Clock.offset(clock,duration);
        }
    }
}
