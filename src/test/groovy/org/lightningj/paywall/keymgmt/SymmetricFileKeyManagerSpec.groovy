/*
 ************************************************************************
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

import org.lightningj.paywall.util.BCUtils
import spock.lang.Shared
import spock.lang.Specification

import java.security.Key
import java.util.logging.Logger

/**
 * Unit tests for SymmetricFileKeyManager
 *
 * Created by philip on 2018-09-19.
 */
class SymmetricFileKeyManagerSpec extends Specification {

    def km = new TestSymmetricFileKeyManager("target/tmp", "foobar321")

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

    def "Verify that a new key is generated if not exists"(){
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

    def "Verify that a if a key file exists it is loaded from disk."(){
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


    //getSymmetricKey, existing, non existing
}
