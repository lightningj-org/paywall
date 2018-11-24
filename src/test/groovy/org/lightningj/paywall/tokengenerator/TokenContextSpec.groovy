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

import org.lightningj.paywall.keymgmt.Context
import org.lightningj.paywall.tokengenerator.TokenContext
import spock.lang.Specification

/**
 * Unit test for TokenContext.
 *
 * Created by philip on 2018-09-14.
 */
class TokenContextSpec extends Specification {

    def "Verify constructor and getter"(){
        when:
        TokenContext context = new TokenContext("SomeContextType",Context.KeyUsage.ENC)
        then:
        context.getType() == "SomeContextType"
        context.getKeyUsage() == Context.KeyUsage.ENC
    }

    def "Verify toString()"(){
        expect:
        new TokenContext("SomeContextType",Context.KeyUsage.ENC).toString() == "TokenContext{type='SomeContextType', keyUsage=ENC}"
    }

    def "Verify equals() and hashCode()"(){
        setup:
        TokenContext context1 = new TokenContext("SomeContextType",Context.KeyUsage.ENC)
        TokenContext context1_2 = new TokenContext("SomeContextType",Context.KeyUsage.ENC)
        TokenContext context2 = new TokenContext("OtherContextType",Context.KeyUsage.ENC)
        TokenContext context3 = new TokenContext("SomeContextType",Context.KeyUsage.SIGN)
        expect:
        context1 == context1_2
        context1 != context2
        context1 != context3
        context1.hashCode() == context1_2.hashCode()
        context1.hashCode() != context2.hashCode()
        context1.hashCode() != context3.hashCode()
    }
}
