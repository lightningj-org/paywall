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
package org.lightningj.paywall.util

import spock.lang.Specification

import java.security.MessageDigest

/**
 * Unit Tests for DigestUtils
 * Created by Philip Vendil on 2018-09-16.
 */
class DigestUtilsSpec extends Specification {

    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def "Verify that sha256 generates a valid sha256 digest"(){
        setup:
        MessageDigest md = MessageDigest.getInstance("SHA-256")
        md.update("somedata".getBytes("UTF-8"))
        byte[] digest = md.digest()
        expect:
        DigestUtils.sha256("somedata".getBytes("UTF-8")) == digest
    }

    def "Verify that sha256 generates a valid ripemd160 digest"(){
        setup:
        MessageDigest md = MessageDigest.getInstance("RipeMD160","BC")
        md.update("somedata".getBytes("UTF-8"))
        byte[] digest = md.digest()
        expect:
        DigestUtils.ripeMD160("somedata".getBytes("UTF-8")) == digest
    }
}
