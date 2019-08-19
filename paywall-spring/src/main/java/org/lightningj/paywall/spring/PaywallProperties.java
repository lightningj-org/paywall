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

    public static final boolean DEFAULT_LIGHTNINGHANDLER_AUTOCONNECT = true;
    public static final String LIGHTNINGHANDLER_AUTOCONNECT = "paywall.lightninghandler.autoconnect";
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
    public static final boolean DEFAULT_INVOICE_INCLUDE_NODEINFO = true;
    public static final String INVOICE_INCLUDE_NODEINFO = "paywall.invoice.includenodeinfo";

    // Settlement Settings
    public static final long DEFAULT_SETTLEMENT_DEFAULT_VALIDITY = 24 * 60 * 60; // 24 Hours
    public static final String SETTLEMENT_DEFAULT_VALIDITY = "paywall.settlement.defaultvalidity";

    public static final String DEFAULT_CHECK_SETTLEMENT_URL = "/paywall/api/checkSettlement";
    public static final String CHECK_SETTLEMENT_URL = "paywall.settlement.url";

    // QR Code Default settings
    public static final int DEFAULT_QR_CODE_DEFAULT_WIDTH = 300;
    public static final String QR_CODE_DEFAULT_WIDTH = "paywall.qrcode.width.default";
    public static final int DEFAULT_QR_CODE_DEFAULT_HEIGHT = 300;
    public static final String QR_CODE_DEFAULT_HEIGHT = "paywall.qrcode.height.default";
    public static final String DEFAULT_QR_CODE_URL = "/paywall/genqrcode";
    public static final String QR_CODE_DEFAULT_URL = "paywall.qrcode.url";

    public static final boolean DEFAULT_WEBSOCKET_ENABLE = true;
    public static final String WEBSOCKET_ENABLE = "paywall.websocket.enable";

    public static final String DEFAULT_WEBSOCKET_CHECK_SETTLEMENT_URL = "/paywall/api/websocket/checksettlement";
    public static final String WEBSOCKET_CHECK_SETTLEMENT_URL = "paywall.websocket.settlement.url";

    @Value("${" + LND_HOSTNAME +  ":}")
    private String lndHostname;

    @Value("${" + LND_PORT +  ":}")
    private String lndPort;

    @Value("${" + LND_TLS_CERT_PATH +  ":}")
    private String lndTLSCertPath;

    @Value("${" + LND_MACAROON_PATH +  ":}")
    private String lndMacaroonPath;

    @Value("${" + LIGHTNINGHANDLER_AUTOCONNECT +  ":" + DEFAULT_LIGHTNINGHANDLER_AUTOCONNECT + "}")
    private String lightningHandlerAutoconnect;

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

    @Value("${" + INVOICE_INCLUDE_NODEINFO +  ":" + DEFAULT_INVOICE_INCLUDE_NODEINFO + "}")
    private String invoiceIncludeNodeInfo;

    @Value("${" + SETTLEMENT_DEFAULT_VALIDITY +  ":" + DEFAULT_SETTLEMENT_DEFAULT_VALIDITY + "}")
    private String settlmentDefaultValidity;

    @Value("${" + CHECK_SETTLEMENT_URL +  ":" + DEFAULT_CHECK_SETTLEMENT_URL + "}")
    private String checkSettlementUrl;

    @Value("${" + QR_CODE_DEFAULT_WIDTH +  ":" + DEFAULT_QR_CODE_DEFAULT_WIDTH + "}")
    private String qrCodeDefaultWidth;

    @Value("${" + QR_CODE_DEFAULT_HEIGHT +  ":" + DEFAULT_QR_CODE_DEFAULT_HEIGHT + "}")
    private String qrCodeDefaultHeight;

    @Value("${" + QR_CODE_DEFAULT_URL +  ":" + DEFAULT_QR_CODE_URL + "}")
    private String qrCodeUrl;

    @Value("${" + WEBSOCKET_ENABLE +  ":" + DEFAULT_WEBSOCKET_ENABLE + "}")
    private String webSocketEnable;

    @Value("${" + WEBSOCKET_CHECK_SETTLEMENT_URL +  ":" + DEFAULT_WEBSOCKET_CHECK_SETTLEMENT_URL+ "}")
    private String webSocketCheckSettlementUrl;

    /**
     * Method that should return the hostname of IP address of the LND node to connect to.
     *
     * @return the hostname of IP address of the LND node to connect to.
     */
    public String getLndHostname() {
        return lndHostname;
    }

    /**
     * Method that should return the port number of the LND node to connect to.
     *
     * @return the port number of the LND node to connect to.
     */
    public String getLndPort() {
        return lndPort;
    }

    /**
     * The path to the LND tls certificate to trust, securing the communication to the LND node.
     * Should point to an file readable by the current user.
     *
     * @return the path to the tls cert path.
     */
    public String getLndTLSCertPath() {
        return lndTLSCertPath;
    }

    /**
     * The path to the macaroon file that is used to authenticate to the LND node. The macaroon
     * should have invoice creation rights.
     *
     * @return the path to the macaroon to use.
     */
    public String getLndMacaroonPath() {
        return lndMacaroonPath;
    }

    /**
     * True if BasePaymentHandler should connect automatically to Lightning Node upon initialization of bean.
     * if false should the implementing application connect the lightning handler manually during startup.
     * @return "true" or "false" (Default is "true").
     */
    public String getLightningHandlerAutoconnect(){
        return lightningHandlerAutoconnect;
    }

    /**
     * Returns the path of directory where trusted public key files are stored.
     *
     * @return the path to the directory where trusted public key store files are stored. Or null
     * if not configured.
     */
    public String getKeymgrAsymTruststorePath(){
        return keymgrAsymTruststorePath;
    }

    /**
     * Returns the path of directory where key files are stored.
     *
     * @return the path to the directory where key store files are stored. Or null
     * if not configured.
     */
    public String getKeymgrKeystorePath(){
        return keymgrKeystorePath;
    }

    /**
     * Method to retrieve the configured pass phrase used to protect generated keys.
     *
     * @return the configured protect pass phrase or null if no passphrase is configured.
     */
    public String getKeymgrPassword(){
        return keymgrPassword;
    }

    /**
     * @return the time in seconds for the not before field in generated
     * JWT tokens. This can be positive if it should be valid in the future, or negative
     * to support skewed clocked between systems. If unset is no not before date
     * set in the generated JWT tokens.
     */
    public String getJwtTokenNotBefore() {
        // Return null for empty strings
        if(jwtTokenNotBefore != null && jwtTokenNotBefore.trim().length() == 0){
            return null;
        }
        return jwtTokenNotBefore;
    }

    /**
     * @return true If settled invoice are received before any order have been created it should
     * registered as new payments automatically before marking them as settled.
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
     * @return true if node connection information should be included in generated invoices.
     */
    public String getInvoiceIncludeNodeInfo() {
        return invoiceIncludeNodeInfo;
    }

    /**
     *
     * @return the default validity for generated settlements if no valid until date have
     * been set explicit in PaymentData.
     */
    public String getSettlementDefaultValidity() {
        return settlmentDefaultValidity;
    }

    /**
     *
     * @return the url to the check settlement controller.
     */
    public String getCheckSettlementURL() {
        return checkSettlementUrl;
    }

    /**
     *
     * @return default QR Code width if no width parameter is specified in QR Code generation request.
     */
    public String getQrCodeDefaultWidth() {
        return qrCodeDefaultWidth;
    }

    /**
     *
     * @return default QR Code width if no height parameter is specified in QR Code generation request.
     */
    public String getQrCodeDefaultHeight() {
        return qrCodeDefaultHeight;
    }

    /**
     *
     * @return The URL to controller that generates QR code images.
     */
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    /**
     *
     * @return returns string "true" if WebSocket functionality should be enabled.
     */
    public String getWebSocketEnable() {
        return webSocketEnable;
    }

    /**
     *
     * @return the URL end point where check settlement Web Socket is listening.
     */
    public String getWebSocketCheckSettlementUrl() {
        return webSocketCheckSettlementUrl;
    }
}
