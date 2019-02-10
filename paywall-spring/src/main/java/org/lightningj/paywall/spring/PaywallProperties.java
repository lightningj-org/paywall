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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Spring bean containing all paywall related configuration settings.
 */
@Configuration
public class PaywallProperties {

    // LND Settings
    public static final String LND_HOSTNAME = "paywall.lnd.hostname";
    public static final String LND_PORT = "paywall.lnd.port";
    public static final String LND_TLS_CERT_PATH = "paywall.lnd.tlscertpath";
    public static final String LND_MACAROON_PATH = "paywall.lnd.macaroonpath";
    // Key Store Manager Settings
    public static final String KEYMGR_ASYMTRUSTSTOREPATH = "paywall.keys.truststorepath";
    public static final String KEYMGR_KEYSTOREPATH = "paywall.keys.keystorepath";
    public static final String KEYMGR_PASSWORD = "paywall.keys.password";
    // JWT Token Settings
    public static final String JWT_TOKEN_NOTBEFORE = "paywall.jwt.notbefore";
    // Invoice Settings
    public static final boolean DEFAULT_INVOICE_REGISTER_NEW = false;
    public static final String INVOICE_REGISTER_NEW = "paywall.invoice.registernew";
    public static final long DEFAULT_INVOICE_DEFAULT_VALIDITY = 60 * 60; // 1 Hour
    public static final String INVOICE_DEFAULT_VALIDITY = "paywall.invoice.defaultvalidity";

    // Settlement Settings
    public static final long DEFAULT_SETTLEMENT_DEFAULT_VALIDITY = 24 * 60 * 60; // 24 Hours
    public static final String SETTLEMENT_DEFAULT_VALIDITY = "paywall.settlement.defaultvalidity";

    @Value("${" + LND_HOSTNAME +  ":}")
    private String lndHostname;

    @Value("${" + LND_PORT +  ":}")
    private String lndPort;

    @Value("${" + LND_TLS_CERT_PATH +  ":}")
    private String lndTLSCertPath;

    @Value("${" + LND_MACAROON_PATH +  ":}")
    private String lndMacaroonPath;

    @Value("${" + KEYMGR_ASYMTRUSTSTOREPATH +  ":}")
    private String keymgrAsymTruststorePath;

    @Value("${" + KEYMGR_KEYSTOREPATH +  ":}")
    private String keymgrKeystorePath;

    @Value("${" + KEYMGR_PASSWORD +  ":}")
    private String keymgrPassword;

    @Value("${" + JWT_TOKEN_NOTBEFORE +  ":}")
    private String jwtTokenNotBefore;

    @Value("${" + INVOICE_REGISTER_NEW +  ":" + DEFAULT_INVOICE_REGISTER_NEW + "}")
    private String invoiceRegisterNew;

    @Value("${" + INVOICE_DEFAULT_VALIDITY +  ":" + DEFAULT_INVOICE_DEFAULT_VALIDITY + "}")
    private String invoiceDefaultValidity;

    @Value("${" + SETTLEMENT_DEFAULT_VALIDITY +  ":" + DEFAULT_SETTLEMENT_DEFAULT_VALIDITY + "}")
    private String settlmentDefaultValidity;

    /**
     * Method that should return the hostname of IP address of the LND node to connect to.
     *
     * @return the hostname of IP address of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    public String getLndHostname() {
        return lndHostname;
    }

    /**
     * Method that should return the port number of the LND node to connect to.
     *
     * @return the port number of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    public String getLndPort() {
        return lndPort;
    }

    /**
     * The path to the LND tls certificate to trust, securing the communication to the LND node.
     * Should point to an file readable by the current user..
     *
     * @return the path to the tls cert path.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    public String getLndTLSCertPath() {
        return lndTLSCertPath;
    }

    /**
     * The path to the macaroon file that is used to authenticate to the LND node. The macaroon
     * should have invoice creation rights.
     *
     * @return the path to the macaroon to use.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    public String getLndMacaroonPath() {
        return lndMacaroonPath;
    }

    /**
     * Returns the path of directory where trusted public key files are stored.
     *
     * @return the path to the directory where trusted public key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the trust store path.
     */
    public String getKeymgrAsymTruststorePath(){
        return keymgrAsymTruststorePath;
    }

    /**
     * Returns the path of directory where key files are stored.
     *
     * @return the path to the directory where key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the key store path.
     */
    public String getKeymgrKeystorePath(){
        return keymgrKeystorePath;
    }

    /**
     * Method to retrieve the configured pass phrase used to protect generated keys.
     *
     * @return the configured protect pass phrase or null if no passphrase is configured.
     * @throws InternalErrorException if internal error occurred retrieving configuration.
     */
    public String getKeymgrPassword(){
        return keymgrPassword;
    }

    /**
     * @return the time i seconds for the not before field in generated
     * JWT tokens. This can be positive if it should be valid in the future, or negative
     * to support skewed clocked between systems. If unset no not before date is
     * set in generated JWT tokens.
     */
    public String getJwtTokenNotBefore() {
        // Return null for empty strings
        if(jwtTokenNotBefore != null && jwtTokenNotBefore.trim().length() == 0){
            return null;
        }
        return jwtTokenNotBefore;
    }

    /**
     * @return true if settled invoices are presented before any order is created should
     * be registered as new payments automatically when register them as settled.
     */
    public String getInvoiceRegisterNew() {
        return invoiceRegisterNew;
    }

    /**
     *
     * @return the default validity for generated invoices if no expire date have
     * been set explicit in PaymentData.
     */
    public String getInvoiceDefaultValidity() {
        return invoiceDefaultValidity;
    }

    /**
     *
     * @return the default validity for generated settlements if no valid until date have
     * been set explicit in PaymentData.
     */
    public String getSettlmentDefaultValidity() {
        return settlmentDefaultValidity;
    }
}
