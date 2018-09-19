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
 * Unit test for KeyManager.Context.
 *
 * Created by philip on 2018-09-14.
 */
class ContextSpec extends Specification {

    def "Verify constructor and getter"(){
        when:
        KeyManager.Context context = new  KeyManager.Context("SomeContextType")
        then:
        context.getContextType() == "SomeContextType"
    }

    def "Verify toString()"(){
        expect:
        new  KeyManager.Context("SomeContextType").toString() == "Context{contextType='SomeContextType'}"
    }

    def "Verify equals() and hashCode()"(){
        setup:
        KeyManager.Context context1 = new  KeyManager.Context("SomeContextType")
        KeyManager.Context context1_2 = new  KeyManager.Context("SomeContextType")
        KeyManager.Context context2 = new  KeyManager.Context("OtherContextType")
        expect:
        context1 == context1_2
        context1 != context2
        context1.hashCode() == context1_2.hashCode()
        context1.hashCode() != context2.hashCode()


    }
}
