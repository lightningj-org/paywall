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
package org.lightningj.paywall.vo

import org.lightningj.paywall.InternalErrorException
import spock.lang.Specification

import javax.json.JsonException

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject

import static org.lightningj.paywall.vo.NodeInfo.NodeNetwork.*

/**
 * Unit tests for NodeInfo
 * Created by Philip Vendil on 2018-11-11.
 */
class NodeInfoSpec extends Specification {

    def "Verify constructors and getter and setters"(){
        when:
        def ni1 = new NodeInfo()
        then:
        ni1.getNodeAddress() == null
        ni1.getPublicKeyInfo() == null
        ni1.getNodePort() == null
        ni1.getNodeNetwork() == null
        when:
        ni1.setNodeAddress("10.10.10.1")
        ni1.setPublicKeyInfo("8371babdk9382719281722")
        ni1.setNodePort(123)
        ni1.setNodeNetwork(MAIN_NET)
        then:
        ni1.getNodeAddress() == "10.10.10.1"
        ni1.getPublicKeyInfo() == "8371babdk9382719281722"
        ni1.getNodePort() == 123
        ni1.getNodeNetwork() == MAIN_NET
        when:
        def ni2 = new NodeInfo("8371babdk9382719281722","10.10.10.1",123)
        then:
        ni2.getNodeAddress() == "10.10.10.1"
        ni2.getPublicKeyInfo() == "8371babdk9382719281722"
        ni2.getNodePort() == 123
        ni2.getNodeNetwork() == UNKNOWN
    }

    def "Verify that getConnectString works as expected"(){
        expect:
        new NodeInfo("8371babdk9382719281722","10.10.10.1",123).getConnectString() == "8371babdk9382719281722@10.10.10.1:123"
        new NodeInfo("8371babdk9382719281722","10.10.10.1",null).getConnectString() == "8371babdk9382719281722@10.10.10.1"
    }

    def "Verify that setConnectString and connectString constructor works as expected"(){
        when:
        def ni1  = new NodeInfo("8371babdk9382719281722@10.10.10.1:123")
        then:
        ni1.getPublicKeyInfo() == "8371babdk9382719281722"
        ni1.getNodeAddress() == "10.10.10.1"
        ni1.getNodePort() == 123
        when:
        def ni2  = new NodeInfo("8371babdk9382719281722@10.10.10.1")
        then:
        ni2.getPublicKeyInfo() == "8371babdk9382719281722"
        ni2.getNodeAddress() == "10.10.10.1"
        ni2.getNodePort() == null

        when:
        new NodeInfo("8371babdk9382719281722@10.10@10.1")
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid Lightning node info connect string: 8371babdk9382719281722@10.10@10.1. It should have format publicKeyInfo@nodeaddress:port, where port is optional."

        when:
        new NodeInfo("8371babdk9382719281722@10.10.10.1:123:124")
        then:
        e = thrown InternalErrorException
        e.message == "Invalid Lightning node info connect string: 8371babdk9382719281722@10.10.10.1:123:124. It should have format publicKeyInfo@nodeaddress:port, where port is optional."

        when:
        new NodeInfo("8371babdk9382719281722@10.10.10.1:1v3")
        then:
        e = thrown InternalErrorException
        e.message == "Invalid Lightning node port number in connect string: 8371babdk9382719281722@10.10.10.1:1v3. It should have format publicKeyInfo@nodeaddress:port, where port is optional."
    }

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new NodeInfo().toJsonAsString(false) == '{}'
        new NodeInfo("8371babdk9382719281722","10.10.10.1",123, MAIN_NET).toJsonAsString(false) == """{"publicKeyInfo":"8371babdk9382719281722","nodeAddress":"10.10.10.1","nodePort":123,"nodeNetwork":"MAIN_NET","connectString":"8371babdk9382719281722@10.10.10.1:123"}"""
        new NodeInfo("8371babdk9382719281722","10.10.10.1",123).toJsonAsString(false) == """{"publicKeyInfo":"8371babdk9382719281722","nodeAddress":"10.10.10.1","nodePort":123,"nodeNetwork":"UNKNOWN","connectString":"8371babdk9382719281722@10.10.10.1:123"}"""
        new NodeInfo("8371babdk9382719281722","10.10.10.1",null).toJsonAsString(false) == """{"publicKeyInfo":"8371babdk9382719281722","nodeAddress":"10.10.10.1","nodeNetwork":"UNKNOWN","connectString":"8371babdk9382719281722@10.10.10.1"}"""
        new NodeInfo("8371babdk9382719281722",null,null).toJsonAsString(false) == """{"publicKeyInfo":"8371babdk9382719281722","nodeNetwork":"UNKNOWN"}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        NodeInfo ni1 = new NodeInfo(toJsonObject("""{"publicKeyInfo":"8371babdk9382719281722","nodeAddress":"10.10.10.1","nodePort":123,"nodeNetwork":"TEST_NET"}"""))
        then:
        ni1.getNodeAddress() == "10.10.10.1"
        ni1.getPublicKeyInfo() == "8371babdk9382719281722"
        ni1.getNodePort() == 123
        ni1.getNodeNetwork() == TEST_NET

        when:
        NodeInfo ni2 = new NodeInfo(toJsonObject("""{}"""))
        then:
        ni2.getNodeAddress() == null
        ni2.getPublicKeyInfo() == null
        ni2.getNodePort() == null
        ni2.getNodeNetwork() == UNKNOWN

        when:
        new NodeInfo(toJsonObject("""{"nodeNetwork":"INVALID"}"""))
        then:
        def e = thrown JsonException
        e.message == "Invalid value in JSON, field 'nodeNetwork' has a value INVALID that is unsupported."
    }
}
