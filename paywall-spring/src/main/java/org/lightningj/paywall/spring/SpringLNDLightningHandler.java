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
package org.lightningj.paywall.spring;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.lnd.SimpleBaseLNDLightningHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import static org.lightningj.paywall.spring.PaywallProperties.*;
import static org.lightningj.paywall.util.SettingUtils.checkRequiredInteger;
import static org.lightningj.paywall.util.SettingUtils.checkRequiredString;

/**
 * Spring implementation of LND Lightning Handler.
 */
public class SpringLNDLightningHandler extends SimpleBaseLNDLightningHandler {

    @Autowired
    PaywallProperties paywallProperties;

    /**
     * Method that should return the hostname of IP address of the LND node to connect to.
     *
     * @return the hostname of IP address of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    @Override
    protected String getHost() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getLndHostname(),LND_HOSTNAME);
    }

    /**
     * Method that should return the port number of the LND node to connect to.
     *
     * @return the port number of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    @Override
    protected int getPort() throws InternalErrorException {
        return checkRequiredInteger(paywallProperties.getLndPort(), LND_PORT);
    }

    /**
     * The path to the LND tls certificate to trust, securing the communication to the LND node.
     * Should point to an file readable by the current user..
     *
     * @return the path to the tls cert path.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    @Override
    protected String getTLSCertPath() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getLndTLSCertPath(),LND_TLS_CERT_PATH);
    }

    /**
     * The path to the macaroon file that is used to authenticate to the LND node. The macaroon
     * should have invoice creation rights.
     *
     * @return the path to the macaroon to use.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    @Override
    protected String getMacaroonPath() throws InternalErrorException {
        return checkRequiredString(paywallProperties.getLndMacaroonPath(),LND_MACAROON_PATH);
    }
}
