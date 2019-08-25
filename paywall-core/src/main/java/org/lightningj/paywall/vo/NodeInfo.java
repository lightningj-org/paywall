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
package org.lightningj.paywall.vo;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.JSONParsable;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object containing connection information to a Lightning Handlers
 * Lightning node.
 *
 * Created by Philip Vendil on 2018-11-11.
 */
public class NodeInfo extends JSONParsable{

    protected String publicKeyInfo;
    protected String nodeAddress;
    protected Integer nodePort;
    protected NodeNetwork nodeNetwork;

    /**
     * Empty Constructor
     */
    public NodeInfo(){
    }

    /**
     * Default Constructor
     * @param publicKeyInfo the public key info of the lightning handlers node. (Optional)
     * @param nodeAddress address information to the lightning handlers node. (Optional)
     * @param nodePort port the node is listening on for channel connections. (Optional)
     */
    public NodeInfo(String publicKeyInfo, String nodeAddress, Integer nodePort) {
        this.publicKeyInfo = publicKeyInfo;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
        this.nodeNetwork = NodeNetwork.UNKNOWN;
    }

    /**
     * Default Constructor
     * @param publicKeyInfo the public key info of the lightning handlers node. (Optional)
     * @param nodeAddress address information to the lightning handlers node. (Optional)
     * @param nodePort port the node is listening on for channel connections. (Optional)
     * @param nodeNetwork indicator which the network the LND node is connected to (UNKNOWN is default). (Optional)
     */
    public NodeInfo(String publicKeyInfo, String nodeAddress, Integer nodePort, NodeNetwork nodeNetwork) {
        this.publicKeyInfo = publicKeyInfo;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
        this.nodeNetwork = nodeNetwork;
    }

    /**
     * Constructor populating data from connect string
     * @param connectString the full connect string with publicKeyInfo@nodeaddress:port, where port is optional.
     * @throws InternalErrorException if given connect string was invalid.
     */
    public NodeInfo(String connectString) throws InternalErrorException{
        setConnectString(connectString);
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    public NodeInfo(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the public key info of the lightning handlers node. (Optional)
     */
    public String getPublicKeyInfo() {
        return publicKeyInfo;
    }

    /**
     *
     * @param publicKeyInfo the public key info of the lightning handlers node. (Optional)
     */
    public void setPublicKeyInfo(String publicKeyInfo) {
        this.publicKeyInfo = publicKeyInfo;
    }

    /**
     *
     * @return address information to the lightning handlers node. (Optional)
     */
    public String getNodeAddress() {
        return nodeAddress;
    }

    /**
     *
     * @param nodeAddress address information to the lightning handlers node. (Optional)
     */
    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     *
     * @return port the node is listening on for channel connections. (Optional)
     */
    public Integer getNodePort() {
        return nodePort;
    }

    /**
     *
     * @param nodePort port the node is listening on for channel connections. (Optional)
     */
    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    /**
     *
     * @return indicator which the network the LND node is connected to (UNKNOWN is default).
     */
    public NodeNetwork getNodeNetwork() {
        return nodeNetwork;
    }

    /**
     *
     * @param nodeNetwork indicator which the network the LND node is connected to (UNKNOWN is default).
     */
    public void setNodeNetwork(NodeNetwork nodeNetwork) {
        this.nodeNetwork = nodeNetwork;
    }

    /**
     *
     * @return the full connect string of configured publicKeyInfo@nodeaddress:port, where port is omitted if not set.
     */
    public String getConnectString() {
        if(nodePort == null){
            return publicKeyInfo + "@" + nodeAddress;
        }
        return publicKeyInfo + "@" + nodeAddress + ":" + nodePort;
    }

    /**
     * Setter using connect string. This method will parse the string and populate
     * publikKeyInfo, nodeAddress and nodePort.
     * @param connectString the full connect string with publicKeyInfo@nodeaddress:port, where port is optional.
     * @throws InternalErrorException if given connect string was invalid.
     */
    public void setConnectString(String connectString) throws InternalErrorException{
        String[] firstSplit = connectString.split("@");
        if(firstSplit.length != 2){
            throw new InternalErrorException("Invalid Lightning node info connect string: " + connectString + ". It should have format publicKeyInfo@nodeaddress:port, where port is optional.");
        }
        publicKeyInfo = firstSplit[0];
        if(firstSplit[1].contains(":")){
            String[] secondSplit = firstSplit[1].split(":");
            if(secondSplit.length != 2){
                throw new InternalErrorException("Invalid Lightning node info connect string: " + connectString + ". It should have format publicKeyInfo@nodeaddress:port, where port is optional.");
            }
            nodeAddress = secondSplit[0];
            try {
                nodePort = Integer.parseInt(secondSplit[1]);
            }catch(NumberFormatException e){
                throw new InternalErrorException("Invalid Lightning node port number in connect string: " + connectString + ". It should have format publicKeyInfo@nodeaddress:port, where port is optional.");
            }
        }else{
            nodeAddress = firstSplit[1];
            nodePort = null;
        }
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"publicKeyInfo",publicKeyInfo);
        addNotRequired(jsonObjectBuilder,"nodeAddress",nodeAddress);
        addNotRequired(jsonObjectBuilder,"nodePort",nodePort);
        if(nodeNetwork != null) {
            addNotRequired(jsonObjectBuilder, "nodeNetwork", nodeNetwork.toString());
        }
        if(publicKeyInfo != null && nodeAddress != null){
            add(jsonObjectBuilder,"connectString", getConnectString());
        }
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        publicKeyInfo = getStringIfSet(jsonObject,"publicKeyInfo");
        nodeAddress = getStringIfSet(jsonObject,"nodeAddress");
        nodePort = getIntIfSet(jsonObject,"nodePort");
        String nodeNetworkString = getStringIfSet(jsonObject,"nodeNetwork");
        if(nodeNetworkString != null){
            try{
                nodeNetwork = NodeNetwork.valueOf(nodeNetworkString);
            }catch (Exception e){
                throw new JsonException("Invalid value in JSON, field 'nodeNetwork' has a value " + nodeNetworkString + " that is unsupported.");
            }

        }else{
            nodeNetwork = NodeNetwork.UNKNOWN;
        }

    }

    /**
     * Enumeration of available Node networks the LND node can be connected to.
     */
    public enum NodeNetwork{
        /**
         * Production network.
         */
        MAIN_NET,
        /**
         * Test network.
         */
        TEST_NET,
        /**
         * Network couldn't be determined from LND node or configuration.
         */
        UNKNOWN
    }

}
