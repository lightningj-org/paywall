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
package org.lightningj.paywall.spring.response

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for json/xml representation of NodeNetwork.
 */
class NodeNetworkSpec extends Specification {

    @Unroll
    def "Expect that internal node network #intNodeNetwork is same as #nodeNetwork"() {
        expect:
        nodeNetwork.asInternalNodeNetwork() == intNodeNetwork

        where:
        nodeNetwork           | intNodeNetwork
        NodeNetwork.MAIN_NET  | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.MAIN_NET
        NodeNetwork.TEST_NET  | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.TEST_NET
        NodeNetwork.UNKNOWN   | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.UNKNOWN
    }

    @Unroll
    def "Expect fromInternalNodeNetwork converts correctly from #intNodeNetwork to #nodeNetwork"() {
        expect:
        NodeNetwork.fromInternalNodeNetwork(intNodeNetwork) == nodeNetwork

        where:
        nodeNetwork           | intNodeNetwork
        NodeNetwork.MAIN_NET  | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.MAIN_NET
        NodeNetwork.TEST_NET  | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.TEST_NET
        NodeNetwork.UNKNOWN   | org.lightningj.paywall.vo.NodeInfo.NodeNetwork.UNKNOWN
    }
}
