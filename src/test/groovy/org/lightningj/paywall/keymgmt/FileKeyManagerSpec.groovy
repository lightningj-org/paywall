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

import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Logger

/**
 * Unit tests for FileKeyManager
 *
 * Created by Philip Vendil on 2018-09-19.
 */
class FileKeyManagerSpec extends Specification {

    def km = new TestSymmetricFileKeyManager("target/tmp", "foobar321")

    @Shared def originalLog
    def setupSpec(){
        originalLog = FileKeyManager.log
    }

    def cleanupSpec(){
        FileKeyManager.log = originalLog
    }

    def setup(){
        FileKeyManager.log = Mock(Logger)
    }

    def "Verify that getProvider() returns BC"(){
        expect:
        km.getProvider(null) == "BC"
    }

    def "Verify that getProtectPassphraseWithDefault() returns configured pass phrase if configured"(){
        when:
        def result = km.getProtectPassphraseWithDefault()
        then:
        result == "foobar321".toCharArray()
        0 * FileKeyManager.log.warning(_)
    }

    def """Verify that getProtectPassphraseWithDefault() returns default
passphrase and logs a warning if no pass phrase have been configured"""(){
        when:
        km = new TestSymmetricFileKeyManager("target/tmp", null)
        def result = km.getProtectPassphraseWithDefault()
        then:
        result == FileKeyManager.DEFAULT_PROTECT_PASSPHRASE.toCharArray()
        1 * FileKeyManager.log.warning("WARNING: no protection pass phrase for JSON Web Token keys set, using built-in default pass phrase, should not be used in production environments.")
        when:
        km = new TestSymmetricFileKeyManager("target/tmp", " ")
        result = km.getProtectPassphraseWithDefault()
        then:
        result == FileKeyManager.DEFAULT_PROTECT_PASSPHRASE.toCharArray()
        1 * FileKeyManager.log.warning("WARNING: no protection pass phrase for JSON Web Token keys set, using built-in default pass phrase, should not be used in production environments.")
    }

    def "Verify that if keystore location is configured is directory created if not exists"(){
        setup:
        File d = new File("target/tmp")
        if(d.exists()){
            d.delete()
        }
        when:
        def path = km.getDirectory("Some Type","testdir")

        File pathDir = new File(path)
        then:
        path == "target/tmp"
        pathDir.exists()
        pathDir.isDirectory()
        0 * FileKeyManager.log.warning(_)
    }

    def "Verify that if keystore location is not configured is temporary directory created and used and warning log created."(){
        setup:
        km = km = new TestSymmetricFileKeyManager(null, null)
        when:
        def path = km.getDirectory("Some Type","testdir")

        File pathDir = new File(path)
        then:
        path == System.getProperty("java.io.tmpdir") + "testdir"
        pathDir.exists()
        pathDir.isDirectory()
        1 * FileKeyManager.log.warning("No Some Type configured, using temporary directory /var/folders/91/lcc2y45902b9z_9qqmkrmplm0000gn/T/. THIS SHOULD NOT BE USED IN TEST ENVIRONMENTS.")

        when:
        km = km = new TestSymmetricFileKeyManager(" ", null)
        path = km.getDirectory("Some Type","testdir")

        pathDir = new File(path)
        then:
        path == System.getProperty("java.io.tmpdir") + "testdir"
        pathDir.exists()
        pathDir.isDirectory()
        1 * FileKeyManager.log.warning("No Some Type configured, using temporary directory /var/folders/91/lcc2y45902b9z_9qqmkrmplm0000gn/T/. THIS SHOULD NOT BE USED IN TEST ENVIRONMENTS.")
    }
}
