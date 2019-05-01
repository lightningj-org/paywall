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
import org.lightningj.paywall.util.BCUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

/**
 * Unit tests for SpringDefaultFileKeyManager
 */
@ContextConfiguration(classes = [TestPaymentHandler, PaywallProperties, LocalProfileBeanConfiguration])
@TestPropertySource("/test_application.properties")
class SpringDefaultFileKeyManagerSpec extends Specification {

    @Autowired
    SpringDefaultFileKeyManager keyManager

    def setupSpec() {
        BCUtils.installBCProvider()
    }

    def "Verify that getAsymTrustStorePath() return the setting for keymgrAsymTruststorePath"() {
        expect:
        keyManager.getAsymTrustStorePath() == "/tmp/truststorepath"
    }

    def "Verify that getKeyStorePath() return the setting for keymgrKeystorePath"() {
        expect:
        keyManager.getKeyStorePath() == "/tmp/keystorepath"
    }

    def "Verify that getProtectPassphrase() return the setting for keymgrPassword"() {
        expect:
        keyManager.getProtectPassphrase() == "foo123"
    }

    def "Verify that getAsymTrustStorePath() throws InternalErrorException if not set"() {
        setup:
        keyManager.paywallProperties = new PaywallProperties()
        when:
        keyManager.getAsymTrustStorePath()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.keys.truststorepath is set in configuration."
    }

    def "Verify that getKeyStorePath() throws InternalErrorException if not set"() {
        setup:
        keyManager.paywallProperties = new PaywallProperties()
        when:
        keyManager.getKeyStorePath()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.keys.keystorepath is set in configuration."
    }

    def "Verify that getProtectPassphrase() throws InternalErrorException if not set"() {
        setup:
        keyManager.paywallProperties = new PaywallProperties()
        when:
        keyManager.getProtectPassphrase()
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting paywall.keys.password is set in configuration."
    }
}
