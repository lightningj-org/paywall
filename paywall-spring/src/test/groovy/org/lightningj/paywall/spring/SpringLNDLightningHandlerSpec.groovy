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
package org.lightningj.paywall.spring

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.spring.local.LocalProfileBeanConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static org.lightningj.paywall.vo.NodeInfo.NodeNetwork.*

/**
 * Unit tests for SpringLNDLightningHandler
 */
@ContextConfiguration(classes = [TestPaymentHandler, PaywallProperties, LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class SpringLNDLightningHandlerSpec extends Specification {

    @Autowired
    SpringLNDLightningHandler lightningHandler


    def "Verify that getHost() return the setting for lndHostname"() {
        expect:
        lightningHandler.getHost() == "somehost"
    }

    def "Verify that getPort() return the setting for lndPort"() {
        expect:
        lightningHandler.getPort() == 10000
    }

    def "Verify that getTLSCertPath() return the setting for lndTLSCertPath"() {
        expect:
        lightningHandler.getTLSCertPath() == "/tmp/tlscertpath"
    }

    def "Verify that getMacaroonPath() return the setting for lndMacaroonPath"() {
        expect:
        lightningHandler.getMacaroonPath() == "/tmp/macaroonpath"
    }

    def "Verify that getHost() throws InternalErrorException if not set"() {
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        when:
        lightningHandler.getHost()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.lnd.hostname is set in configuration."
    }

    def "Verify that getPort() throws InternalErrorException if not set"() {
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        when:
        lightningHandler.getPort()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.lnd.port is set in configuration."
    }

    def "Verify that getPort() throws InternalErrorException if setting is non integer"() {
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndPort = "abc"
        when:
        lightningHandler.getPort()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.lnd.port has a number value, not abc"
    }

    def "Verify that getTLSCertPath() throws InternalErrorException if not set"() {
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        when:
        lightningHandler.getTLSCertPath()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.lnd.tlscertpath is set in configuration."
    }

    def "Verify that getMacaroonPath() throws InternalErrorException if not set"() {
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        when:
        lightningHandler.getMacaroonPath()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.lnd.macaroonpath is set in configuration."
    }

    def "Verify that getNodeInfoFromConfiguration returns an empty NodeInfo if paywall.invoice.includenodeinfo is set to false"(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.invoiceIncludeNodeInfo = "false"
        expect:
        lightningHandler.getNodeInfoFromConfiguration().nodeAddress == null
    }

    def "Verify that getNodeInfoFromConfiguration returns null if paywall.invoice.includenodeinfo is true but no connect string was set"(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        expect:
        lightningHandler.getNodeInfoFromConfiguration() == null

        when:
        lightningHandler.paywallProperties.lndConnectString = "   "
        then:
        lightningHandler.getNodeInfoFromConfiguration() == null
    }

    def "Verify that getNodeInfoFromConfiguration returns NodeInfo if paywall.invoice.connectstring is set and if paywall.invoice.includenodeinfo is true but no network have been set"(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndConnectString = "asf@localhost:8000"
        when:
        def ni = lightningHandler.getNodeInfoFromConfiguration()
        then:
        ni.connectString == "asf@localhost:8000"
        ni.nodeNetwork == UNKNOWN
    }

    def "Verify that getNodeInfoFromConfiguration returns NodeInfo if paywall.lnd.connectstring is set and if paywall.invoice.includenodeinfo is true and network have been set"(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndConnectString = "asf@localhost:8000"
        lightningHandler.paywallProperties.lndNetwork = "MAIN_NET"
        when:
        def ni = lightningHandler.getNodeInfoFromConfiguration()
        then:
        ni.connectString == "asf@localhost:8000"
        ni.nodeNetwork == MAIN_NET
    }

    def "Verify that getNodeInfoFromConfiguration throws InternalErrorException if invalid configured paywall.lnd.connectstring."(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndConnectString = "invalid"
        when:
        lightningHandler.getNodeInfoFromConfiguration()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid Lightning node info connect string: invalid. It should have format publicKeyInfo@nodeaddress:port, where port is optional."
    }

    def "Verify that getNodeInfoFromConfiguration throws InternalErrorException if invalid configured paywall.lnd.network."(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndConnectString = "asf@localhost:8000"
        lightningHandler.paywallProperties.lndNetwork = "INVALID"
        when:
        lightningHandler.getNodeInfoFromConfiguration()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid configuration, unsupported network value: INVALID in setting paywall.lnd.network."
    }

    def "Verify that getSupportedCurrencyCode returns configuration from paywall properties."(){
        setup:
        lightningHandler.paywallProperties = new PaywallProperties()
        lightningHandler.paywallProperties.lndCurrencyCode = "LTC"
        expect:
        lightningHandler.getSupportedCurrencyCode() == "LTC"
    }
}
