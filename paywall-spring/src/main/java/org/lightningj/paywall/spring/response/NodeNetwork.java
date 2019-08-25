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
package org.lightningj.paywall.spring.response;

import org.lightningj.paywall.vo.NodeInfo;

import javax.xml.bind.annotation.XmlEnum;

/**
 * XML, JSON version of internal NodeNetwork object.
 */
@XmlEnum()
public enum NodeNetwork {

    MAIN_NET(NodeInfo.NodeNetwork.MAIN_NET),
    TEST_NET(NodeInfo.NodeNetwork.TEST_NET),
    UNKNOWN(NodeInfo.NodeNetwork.UNKNOWN);

    private NodeInfo.NodeNetwork intNodeNetwork;

    NodeNetwork(NodeInfo.NodeNetwork intNodeNetwork){
        this.intNodeNetwork = intNodeNetwork;
    }

    /**
     *
     * @return the internal NodeNetwork enum representation.
     */
    public NodeInfo.NodeNetwork  asInternalNodeNetwork(){
        return intNodeNetwork;
    }

    /**
     * Method to convert an internal nodeNetwork to JSON/XML representation.
     * @param intNodeNetwork internal nodeNetwork.
     * @return JSON/XML converted version.
     */
    public static NodeNetwork fromInternalNodeNetwork(NodeInfo.NodeNetwork  intNodeNetwork){
        return NodeNetwork.valueOf(intNodeNetwork.name());
    }

}
