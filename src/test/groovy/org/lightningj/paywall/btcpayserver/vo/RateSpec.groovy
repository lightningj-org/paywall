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
package org.lightningj.paywall.btcpayserver.vo

import spock.lang.Specification

import javax.json.Json
import javax.json.JsonObject

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
/**
 * Unit tests for Rate
 *
 * Created by Philip Vendil on 2018-10-18.
 */
class RateSpec extends Specification {

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new Rate().toJsonAsString(false) == "{}"
        genFullRate().toJsonAsString(false) == """{"code":"BTC","name":"Bitcoin","rate":1.2}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        Rate r = new Rate(toJsonObject("{ }"))
        then:
        r.name == null
        when: // Simple Invoice
        r = new Rate(toJsonObject("""{"code":"BTC","name":"Bitcoin","rate":1.2}"""))
        then:
        r.code == "BTC"
        r.name == "Bitcoin"
        r.rate == 1.2

    }

    private Rate genFullRate(){
        def r = new Rate()
        r.code = "BTC"
        r.name = "Bitcoin"
        r.rate = 1.2
        return r
    }

}
