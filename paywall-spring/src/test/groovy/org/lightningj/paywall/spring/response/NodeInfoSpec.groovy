/*
 * ***********************************************************************
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
package org.lightningj.paywall.spring.response

import org.lightningj.paywall.vo.NodeInfo as IntNodeInfo

import spock.lang.Specification

/**
 * Unit test for XML/JSON version of NodeInfo
 * @author philip 2019-04-15
 */
class NodeInfoSpec extends Specification {

    IntNodeInfo intNodeInfo = new IntNodeInfo("8371babdk9382719281722@10.10.10.1:123")

    def "Verify constructor and getter and setters"() {
        when:
        NodeInfo nodeInfo1 = new NodeInfo()
        then:
        nodeInfo1.getPublicKeyInfo() == null
        nodeInfo1.getNodeAddress() == null
        nodeInfo1.getNodePort() == null
        nodeInfo1.getMainNet()
        nodeInfo1.getConnectString() == null
        when:
        nodeInfo1.setPublicKeyInfo("PublicKeyInfo")
        nodeInfo1.setNodeAddress("NodeAddress")
        nodeInfo1.setNodePort(124)
        nodeInfo1.setMainNet(false)
        nodeInfo1.setConnectString("someconnectstring")
        then:
        nodeInfo1.getPublicKeyInfo() == "PublicKeyInfo"
        nodeInfo1.getNodeAddress() == "NodeAddress"
        nodeInfo1.getNodePort() == 124
        !nodeInfo1.getMainNet()
        nodeInfo1.getConnectString() == "someconnectstring"

        when:
        NodeInfo nodeInfo2 = new NodeInfo(intNodeInfo)
        then:
        nodeInfo2.getPublicKeyInfo() == "8371babdk9382719281722"
        nodeInfo2.getNodeAddress() == "10.10.10.1"
        nodeInfo2.getNodePort() == 123
        nodeInfo2.getMainNet()
        nodeInfo2.getConnectString() == "8371babdk9382719281722@10.10.10.1:123"
    }

    def "Verify toString()"() {
        expect:
        new NodeInfo(intNodeInfo).toString() == """NodeInfo{publicKeyInfo='8371babdk9382719281722', nodeAddress='10.10.10.1', nodePort=123, mainNet=true, connectString='8371babdk9382719281722@10.10.10.1:123'}"""
    }
}
