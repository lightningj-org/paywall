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
package org.lightningj.paywall.spring.response;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * XML and JSON representation of NodeInfo.
 *
 * @author philip 2019-04-15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeInfoType", propOrder = {
        "publicKeyInfo",
        "nodeAddress",
        "nodePort",
        "network",
        "connectString"
})
public class NodeInfo {

    @XmlElement(required = true)
    private String publicKeyInfo;
    @XmlElement(required = true)
    private String nodeAddress;
    @XmlElement(required = true)
    private Integer nodePort;
    @XmlElement(defaultValue = "UNKNOWN")
    private NodeNetwork network;
    @XmlElement(required = true)
    private String connectString;

    /**
     * Empty constructor.
     */
    public NodeInfo(){}

    /**
     * Constructor converting an internal noneInfo to XML/JSON version.
     * @param nodeInfo the nodeInfo to convert.
     */
    public NodeInfo(org.lightningj.paywall.vo.NodeInfo nodeInfo){
        publicKeyInfo = nodeInfo.getPublicKeyInfo();
        nodeAddress = nodeInfo.getNodeAddress();
        nodePort = nodeInfo.getNodePort();
        network = nodeInfo.getNodeNetwork() != null ? NodeNetwork.fromInternalNodeNetwork(nodeInfo.getNodeNetwork()) : null;
        connectString = nodeInfo.getConnectString();
    }

    /**
     *
     * @return the node's publicKeyInfo
     */
    public String getPublicKeyInfo() {
        return publicKeyInfo;
    }

    /**
     *
     * @param publicKeyInfo the node's publicKeyInfo
     */
    public void setPublicKeyInfo(String publicKeyInfo) {
        this.publicKeyInfo = publicKeyInfo;
    }

    /**
     *
     * @return the node's address
     */
    public String getNodeAddress() {
        return nodeAddress;
    }

    /**
     *
     * @param nodeAddress the node's address
     */
    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     *
     * @return the node's port
     */
    public Integer getNodePort() {
        return nodePort;
    }

    /**
     *
     * @param nodePort the node's port
     */
    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    /**
     *
     * @return the network node is following.
     */
    public NodeNetwork getNetwork() {
        if(network == null){
            network = NodeNetwork.UNKNOWN;
        }
        return network;
    }

    /**
     *
     * @param network the network node is following.
     */
    public void setNetwork(NodeNetwork network) {
        this.network = network;
    }

    /**
     *
     * @return the complete connect string to the lightning node.
     */
    public String getConnectString() {
        return connectString;
    }

    /**
     *
     * @param connectString the complete connect string to the lightning node.
     */
    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "publicKeyInfo='" + publicKeyInfo + '\'' +
                ", nodeAddress='" + nodeAddress + '\'' +
                ", nodePort=" + nodePort +
                ", network=" + getNetwork() +
                ", connectString='" + connectString + '\'' +
                '}';
    }
}
