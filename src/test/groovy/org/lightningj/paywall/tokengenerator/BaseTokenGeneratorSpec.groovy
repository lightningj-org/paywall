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

import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.NumericDate
import org.lightningj.paywall.keymgmt.DummyKeyManagerInstance
import org.lightningj.paywall.keymgmt.KeySerializationHelper
import org.lightningj.paywall.keymgmt.SymmetricKeyManager
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.DigestUtils
import org.lightningj.paywall.vo.*
import org.lightningj.paywall.vo.amount.BTC
import spock.lang.Shared
import spock.lang.Specification

import javax.json.JsonException
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for BaseTokenGenerator
 *
 * Created by Philip Vendil on 2018-10-29.
 */
class BaseTokenGeneratorSpec extends Specification {

    @Shared SymmetricKeyManager keyManager
    def ctx_type= TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE

    BaseTokenGenerator baseTokenGenerator
    def setupSpec(){
        BCUtils.installBCProvider()
        keyManager =  DummyKeyManagerInstance.commonInstance
    }

    def setup(){
        baseTokenGenerator = new SymmetricKeyTokenGenerator(keyManager)
    }

    def "Verify that correct PreImageData is generated"(){
        when:
        PreImageData d1 = baseTokenGenerator.genPreImageData()
        PreImageData d2 = baseTokenGenerator.genPreImageData()
        then:
        verifyPreImageData(d1)
        verifyPreImageData(d2)
        d1.preImage != d2.preImage

    }

    def "Test generate generateToken and parseToken generates valid JWS data without encryption."(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
        def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
        when:
        String token = baseTokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData)
        then:
        token != null
        when:
        JwtClaims jwtClaims = baseTokenGenerator.parseToken(ctx_type,token)
        Order d = new Order(jwtClaims)
        then:
        jwtClaims.getIssuer() == KeySerializationHelper.genKeyId(keyManager.getSymmetricKey(null).encoded)
        jwtClaims.getSubject() == null
        jwtClaims.getNotBefore().value == notBefore.getEpochSecond()
        jwtClaims.getExpirationTime().value == expireDate.getEpochSecond()
        d.preImageHash == "abasrekwsdf".getBytes()
        when: // Verify that if recipientSubject sets subject in claims
        token = baseTokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,"abc123",paymentData)
        jwtClaims = baseTokenGenerator.parseToken(ctx_type,token)
        then:
        jwtClaims.getSubject() == "abc123"
    }

    def "Verify that checkExpireDate with no expire date set in jwtClaims throws TokenVerifyException"(){
        setup:
        JwtClaims jwtClaims = new JwtClaims()
        when:
        baseTokenGenerator.checkExpireDate(jwtClaims)
        then:
        def e = thrown TokenException
        e.message == "Couldn't verify token, couldn't retrieve expire date from JWT claims."
    }

    def "Verify that checkExpireDate with expired jwtClaims throws TokenVerifyException"(){
        setup:
        JwtClaims jwtClaims = new JwtClaims()
        jwtClaims.expirationTime = NumericDate.fromMilliseconds(baseTokenGenerator.clock.instant().toEpochMilli() - BaseTokenGenerator.ALLOWED_CLOCK_SKEW - 1000)
        when:
        baseTokenGenerator.checkExpireDate(jwtClaims)
        then:
        def e = thrown TokenException
        e.message == "JWT Token have expired."
    }

    def "Verify that checkNotBefore with not yet claim set doesn't throw TokenVerifyException"(){
        setup:
        JwtClaims jwtClaims = new JwtClaims()
        when:
        baseTokenGenerator.checkNotBefore(jwtClaims)
        then:
        true
    }

    def "Verify that checkNotBefore with not yet jwtClaims throws TokenVerifyException"(){
        setup:
        JwtClaims jwtClaims = new JwtClaims()
        jwtClaims.notBefore = NumericDate.fromMilliseconds(baseTokenGenerator.clock.instant().toEpochMilli() + BaseTokenGenerator.ALLOWED_CLOCK_SKEW + 1000)
        when:
        baseTokenGenerator.checkNotBefore(jwtClaims)
        then:
        def e = thrown TokenException
        e.message == "JWT Token not yet valid."
    }


    def "Verify that parsing of VO objects works"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
        def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
        def invoiceData = new Invoice("abasrekwsdf".getBytes(), "abasdreser",null,new BTC(10000),new NodeInfo("1231232@10.10.10.1"),expireDate,Instant.now())
        def settlementData = new Settlement("abasrekwsdf".getBytes(),null,expireDate,null)
        when:
        String token = baseTokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData,invoiceData,settlementData)
        //println token
        JwtClaims claims = baseTokenGenerator.parseToken(ctx_type,token)

        Order pd1 = new Order(claims)
        Invoice id1 = new Invoice(claims)
        Settlement sd1 = new Settlement(claims)
        then:

        pd1.preImageHash == paymentData.preImageHash
        id1.preImageHash == invoiceData.preImageHash
        sd1.preImageHash == settlementData.preImageHash

    }

    def "Verify that parsing of VO objects throws JsonException if no related claim found in token"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
        def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
        when:
        String token = baseTokenGenerator.generateToken(ctx_type,expireDate,notBefore,false,null,paymentData)
        JwtClaims claims = baseTokenGenerator.parseToken(ctx_type,token)
        new Invoice(claims)
        then:
        def e = thrown JsonException
        e.message =~ "Exception parsing JSON data for claim invoice in JWT token:"
    }

    def "Verify that generation of encrypted JWT is decryptable with same key"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant notBefore = Instant.now().minus(10, ChronoUnit.MINUTES)
        def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
        when:
        String token = baseTokenGenerator.generateToken(ctx_type,expireDate,notBefore,true,null,paymentData)

        then:
        JsonWebEncryption jwe = new JsonWebEncryption()
        jwe.setCompactSerialization(token)
        jwe.getHeader("alg") == "dir"
        jwe.getHeader("enc") == "A128CBC-HS256"

        when:
        JwtClaims claims = baseTokenGenerator.parseToken(ctx_type,token)
        def paymentData1 = new Order(claims)
        then:
        paymentData1.preImageHash == "abasrekwsdf".getBytes()

    }

    def "Verify that parseToken() with null token throws TokenException"(){
        when:
        baseTokenGenerator.parseToken(ctx_type,null)
        then:
        def e = thrown TokenException
        e.message == "Couldn't verify null JWT token."
    }

    def "Verify that generatePaymentToken generates a valid payment token"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant requestDate = Instant.now().minus(10, ChronoUnit.MINUTES)
        def paymentData = new Order("abasrekwsdf".getBytes(), "Some Description", new BTC(10000), expireDate)
        def requestData = new RequestData("avksjedf".getBytes(),requestDate)
        when:
        String token = baseTokenGenerator.generatePaymentToken(paymentData,requestData,expireDate,null,null)
        JwtClaims claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE,token)
        then:
        def pd2 = new Order(claims)
        def rd2 = new RequestData(claims)
        pd2.expireDate == expireDate
        rd2.requestDate == requestDate

        when:
        token = baseTokenGenerator.generatePaymentToken(paymentData,null,expireDate,null,null)
        claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_PAYMENT_TOKEN_TYPE,token)
        then:
        def pd3 = new Order(claims)
        pd3.expireDate == expireDate
    }

    def "Verify that generateInvoiceToken generates a valid invoice token"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant requestDate = Instant.now().minus(10, ChronoUnit.MINUTES)
        def invoiceData = new Invoice("abasrekwsdf".getBytes(), "abasdreser",null,new BTC(10000),new NodeInfo("1231232@10.10.10.1"),expireDate,Instant.now())
        def requestData = new RequestData("avksjedf".getBytes(),requestDate)
        when:
        String token = baseTokenGenerator.generateInvoiceToken(invoiceData,requestData,expireDate,null,null)
        JwtClaims claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,token)
        then:
        def id2 = new Invoice(claims)
        def rd2 = new RequestData(claims)
        id2.expireDate == expireDate
        rd2.requestDate == requestDate

        when:
        token = baseTokenGenerator.generateInvoiceToken(invoiceData,null,expireDate,null,null)
        claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,token)
        then:
        def id3 = new Invoice(claims)
        id3.expireDate == expireDate
    }

    def "Verify that generateSettlementToken generates a valid invoice token"(){
        setup:
        Instant expireDate = Instant.now().plus(10, ChronoUnit.MINUTES)
        Instant requestDate = Instant.now().minus(10, ChronoUnit.MINUTES)
        def settlementData = new Settlement("abasrekwsdf".getBytes(),null,expireDate,null)
        def requestData = new RequestData("avksjedf".getBytes(),requestDate)
        when:
        String token = baseTokenGenerator.generateSettlementToken(settlementData,requestData,expireDate,null,null)
        JwtClaims claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,token)
        then:
        def sd2 = new Settlement(claims)
        def rd2 = new RequestData(claims)
        sd2.validUntil == expireDate
        rd2.requestDate == requestDate

        when:
        token = baseTokenGenerator.generateSettlementToken(settlementData,null,expireDate,null,null)
        claims = baseTokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE,token)
        then:
        def sd3 = new Settlement(claims)
        sd3.validUntil == expireDate
    }


    // Then check, website access 10 minutes how to do this


    private void verifyPreImageData(PreImageData data){
        assert data.preImage.length == BaseTokenGenerator.PREIMAGE_LENGTH
        assert DigestUtils.sha256(data.preImage) == data.preImageHash
    }


}
