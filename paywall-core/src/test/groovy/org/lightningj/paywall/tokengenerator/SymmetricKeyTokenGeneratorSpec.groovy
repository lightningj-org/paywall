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
import org.lightningj.paywall.keymgmt.DummyKeyManagerInstance
import org.lightningj.paywall.keymgmt.SymmetricKeyManager
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.vo.Order
import org.lightningj.paywall.vo.amount.BTC
import spock.lang.Specification

import javax.crypto.KeyGenerator
import java.security.Key
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for SymmetricKeyTokenGenerator
 * Created by philip on 2018-11-17.
 */
class SymmetricKeyTokenGeneratorSpec extends Specification {

    SymmetricKeyManager keyManager =  DummyKeyManagerInstance.commonInstance
    Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
    Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
    def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
    def ctx = TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE

    SymmetricKeyTokenGenerator tokenGenerator
    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup(){
        tokenGenerator = new SymmetricKeyTokenGenerator(keyManager)
    }

    def "Verify that HS256 algorithm is used in generated JWS tokens"(){
        when:
        String token = tokenGenerator.generateToken(ctx,expireDate,notBefore,false,null,paymentData)
        then:
        JsonWebSignature jws = new JsonWebSignature()
        jws.setCompactSerialization(token)
        jws.getHeader("alg") == "HS256"
    }

    def "Verify that modified signature in token generates TokenVerifyException"(){
        setup:
        String token = tokenGenerator.generateToken(ctx,expireDate,notBefore,false,null,paymentData)
        char[] tokenChars = token.toCharArray()
        tokenChars[55] = 'a'
        String modifiedToken = new String(tokenChars)
        when:
        tokenGenerator.parseToken(ctx,modifiedToken)
        then:
        def e = thrown TokenException
        e.message == "Invalid signature for token."
    }

    def "Verify that invalid key when verifying signature in token generates TokenVerifyException"(){
        setup:
        String token = tokenGenerator.generateToken(ctx,expireDate,notBefore,false,null,paymentData)

        tokenGenerator.keyManager.symmetricKey = newSymmetricKey()
        when:
        tokenGenerator.parseToken(ctx,token)
        then:
        def e = thrown TokenException
        e.message == "Invalid signature for token."
    }

    def "Verify that it is not possible to decrypt a token with another symmetric key"(){
        setup:
        String token = tokenGenerator.generateToken(ctx,expireDate,notBefore,true,null,paymentData)

        tokenGenerator.keyManager.symmetricKey = newSymmetricKey()
        when:
        tokenGenerator.parseToken(ctx,token)
        then:
        def e = thrown TokenException
        e.message =~ "Unable to decrypt token: Authentication tag check failed."
    }

    def "Verify that it is not possible to decrypt altered data"(){
        setup:
        String token = tokenGenerator.generateToken(ctx,expireDate,notBefore,true,null,paymentData)
        char[] tokenChars = token.toCharArray()
        tokenChars[100] = 'a'
        String modifiedToken = new String(tokenChars)
        when:
        tokenGenerator.parseToken(ctx,modifiedToken)
        then:
        def e = thrown TokenException
        e.message =~ "Unable to decrypt token: Authentication tag check failed."
    }

    private Key newSymmetricKey(){
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BC");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
