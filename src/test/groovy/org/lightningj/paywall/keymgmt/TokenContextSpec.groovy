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

import spock.lang.Specification

/**
 * Unit test forTokenContext.
 *
 * Created by philip on 2018-09-14.
 */
class TokenContextSpec extends Specification {

    def "Verify constructor and getter"(){
        when:
        TokenContext context = new TokenContext("SomeContextType")
        then:
        context.getType() == "SomeContextType"
    }

    def "Verify toString()"(){
        expect:
        new TokenContext("SomeContextType").toString() == "TokenContext{type='SomeContextType'}"
    }

    def "Verify equals() and hashCode()"(){
        setup:
        TokenContext context1 = new TokenContext("SomeContextType")
        TokenContext context1_2 = new TokenContext("SomeContextType")
        TokenContext context2 = new TokenContext("OtherContextType")
        expect:
        context1 == context1_2
        context1 != context2
        context1.hashCode() == context1_2.hashCode()
        context1.hashCode() != context2.hashCode()
    }
}
