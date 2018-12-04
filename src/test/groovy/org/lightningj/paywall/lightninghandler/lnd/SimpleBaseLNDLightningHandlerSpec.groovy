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
package org.lightningj.paywall.lightninghandler.lnd

import org.lightningj.lnd.wrapper.AsynchronousLndAPI
import org.lightningj.lnd.wrapper.SynchronousLndAPI
import org.lightningj.paywall.InternalErrorException
import spock.lang.Specification

/**
 * Unit tests for SimpleBaseLNDLightningHandler.
 * Created by Philip Vendil on 2018-12-02.
 */
class SimpleBaseLNDLightningHandlerSpec extends Specification {

    // Most of the testing is done by the integration test.
    // This class only tests cases that couldn't be tested
    // during integration test.
    SimpleBaseLNDLightningHandler handler

    def setup(){
        handler = new TestDefaultLNDLightningHandler("localhost",9001,"src/test/resources/testtlscert.pem","src/test/resources/testmacaroon")
    }

    def "Verify that connect throws InternalErrorException if tls cert had invalid path"(){
        setup:
        handler = new TestDefaultLNDLightningHandler("localhost",9001,"src/test/resources/invalidpath.pem","src/test/resources/testmacaroon")
        when:
        handler.connect(null)
        then:
        def e = thrown InternalErrorException
        e.message == "No LND TLS certificate file found at path: src/test/resources/invalidpath.pem"
    }

    def "Verify that connect throws InternalErrorException if macaroon had invalid path"(){
        setup:
        handler = new TestDefaultLNDLightningHandler("localhost",9001,"src/test/resources/testtlscert.pem","src/test/resources/invalidmacaroon")
        when:
        handler.connect(null)
        then:
        def e = thrown InternalErrorException
        e.message == "No LND Macaroon file found at path: src/test/resources/invalidmacaroon"
    }

    def "Verify that close closes the connection on both apis"(){
        setup:
        def asynchronousLndAPI = Mock(AsynchronousLndAPI)
        handler.asynchronousLndAPI = asynchronousLndAPI
        def synchronousLndAPI = Mock(SynchronousLndAPI)
        handler.synchronousLndAPI = synchronousLndAPI
        handler.connected = true
        when:
        handler.close()
        then:
        1 * asynchronousLndAPI.close()
        handler.asynchronousLndAPI == null
        1 * synchronousLndAPI.close()
        handler.synchronousLndAPI == null
    }

    def "Verify that close closes doesn the connection on apis if not connected"(){
        setup:
        def asynchronousLndAPI = Mock(AsynchronousLndAPI)
        handler.asynchronousLndAPI = asynchronousLndAPI
        def synchronousLndAPI = Mock(SynchronousLndAPI)
        handler.synchronousLndAPI = synchronousLndAPI
        handler.connected = false
        when:
        handler.close()
        then:
        0 * asynchronousLndAPI.close()
        0 * synchronousLndAPI.close()
    }


    def "Verify that close closes both connection even though first generates exception."(){
        setup:
        def asynchronousLndAPI = Mock(AsynchronousLndAPI)
        handler.asynchronousLndAPI = asynchronousLndAPI
        def synchronousLndAPI = Mock(SynchronousLndAPI)
        handler.synchronousLndAPI = synchronousLndAPI
        handler.connected = true
        when:
        handler.close()
        then:
        def e = thrown InternalErrorException
        e.message == "Internal error closing LND connection: test"
        1 * asynchronousLndAPI.close() >> {throw new IOException("test")}
        1 * synchronousLndAPI.close()

    }

    static class TestDefaultLNDLightningHandler extends SimpleBaseLNDLightningHandler{

        private String host
        private int port
        private String tlsCertPath
        private String macaroonPath

        TestDefaultLNDLightningHandler(String host, int port, String tlsCertPath, String macaroonPath){
            this.host = host
            this.port = port
            this.tlsCertPath = tlsCertPath
            this.macaroonPath = macaroonPath
        }

        @Override
        protected String getHost() throws InternalErrorException {
            return host
        }

        @Override
        protected int getPort() throws InternalErrorException {
            return port
        }

        @Override
        protected String getTLSCertPath() throws InternalErrorException {
            return tlsCertPath
        }

        @Override
        protected String getMacaroonPath() throws InternalErrorException {
            return macaroonPath
        }
    }
}
