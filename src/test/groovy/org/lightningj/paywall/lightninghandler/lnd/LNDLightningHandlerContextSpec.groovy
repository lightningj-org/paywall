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
package org.lightningj.paywall.lightninghandler.lnd

import org.lightningj.paywall.lightninghandler.lnd.LNDLightningHandlerContext
import spock.lang.Specification

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

/**
 * Unit tests for LNDLightningHandlerContext
 * Created by Philip Vendil on 2018-11-28.
 */
class LNDLightningHandlerContextSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def lc1 = new LNDLightningHandlerContext()
        then:
        lc1.getAddIndex() == null
        lc1.getSettleIndex() == null
        when:
        lc1.setAddIndex(123)
        lc1.setSettleIndex(234)
        then:
        lc1.getAddIndex() == 123
        lc1.getSettleIndex() ==234

        when:
        def lc2 = new LNDLightningHandlerContext(123,234)
        then:
        lc2.getAddIndex() == 123
        lc2.getSettleIndex() ==234
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new LNDLightningHandlerContext().toJsonAsString(false) == """{"type":"lnd"}"""
        new LNDLightningHandlerContext(123,234).toJsonAsString(false) == """{"type":"lnd","addIndex":123,"settleIndex":234}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        LNDLightningHandlerContext d = new LNDLightningHandlerContext(toJsonObject("""{}"""))
        then:
        d.addIndex == null
        d.settleIndex == null
        when:
        d = new LNDLightningHandlerContext(toJsonObject("""{"addIndex":123,"settleIndex":234}"""))
        then:
        d.getAddIndex() == 123
        d.getSettleIndex() ==234
    }

}
