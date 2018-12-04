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
package org.lightningj.paywall.lightninghandler.lnd;

import org.lightningj.lnd.wrapper.AsynchronousLndAPI;
import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.lightninghandler.LightningHandlerContext;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Extension of BaseLNDLightningHandler that also manages APIs and opening/closing connection.
 * Implementing classes only need to give host,port, path to TLS cert and macaroon.
 *
 * Created by Philip Vendil on 2018-11-28.
 */
public abstract class SimpleBaseLNDLightningHandler extends BaseLNDLightningHandler {

    protected AsynchronousLndAPI asynchronousLndAPI;
    protected SynchronousLndAPI synchronousLndAPI;

    protected boolean connected = false;

    /**
     * Method to open up a connection to the configured LND node. Calls to register and un-register listeners
     * should be done before opening a connection to make sure the listeners receives all invoice notifications.
     * @param context the last context by the payment handler, containing the indicies of the last invoices processed.
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred opening up a connection with LND node.
     */
    @Override
    public void connect(LightningHandlerContext context) throws IOException, InternalErrorException {
        clearCache();
        File tlsCertFile = new File(getTLSCertPath());
        if(!tlsCertFile.exists() || !tlsCertFile.canRead() || !tlsCertFile.isFile()){
            throw new InternalErrorException("No LND TLS certificate file found at path: " + tlsCertFile);
        }
        File macaroonFile = new File(getMacaroonPath());
        if(!macaroonFile.exists() || !macaroonFile.canRead() || !macaroonFile.isFile()){
            throw new InternalErrorException("No LND Macaroon file found at path: " + macaroonFile);
        }

        try {
            asynchronousLndAPI = new AsynchronousLndAPI(getHost(), getPort(), tlsCertFile, macaroonFile);
            synchronousLndAPI = new SynchronousLndAPI(getHost(), getPort(), tlsCertFile, macaroonFile);
            listenToInvoices(context);
            connected = true;
            log.log(Level.INFO,"Connected to LND Node Successfully.");
        }catch(ClientSideException e){
            throw new InternalErrorException("Error connecting to LND API: " + e.getMessage(),e);
        }
    }

    /**
     * Method to close the LND connections and release underlying resources.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred closing the connections with lightning node.
     */
    @Override
    public void close() throws IOException, InternalErrorException {
        if(connected) {
            clearCache();
            Exception exception = null;
            try {
                asynchronousLndAPI.close();
                asynchronousLndAPI = null;
            } catch (Exception e) {
                exception = e;
            }
            try {
                synchronousLndAPI.close();
                synchronousLndAPI = null;
            } catch (Exception e) {
                exception = e;
            }

            if (exception != null) {
                throw new InternalErrorException("Internal error closing LND connection: " + exception.getMessage(), exception);
            }
            log.log(Level.INFO,"Closed Connection to LND Node Successfully.");
            connected = false;
        }
    }

    /**
     * Method to check if handler is connected to node.
     *
     * @return true if connected,
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred checking connection status.
     */
    @Override
    public boolean isConnected() throws IOException, InternalErrorException {
        return connected;
    }

    /**
     * Method to get the asynchronous lnd api from lightningj.
     * @return the asynchronous lnd api from lightningj.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred with LND node.
     */
    @Override
    protected AsynchronousLndAPI getAsyncAPI() throws IOException, InternalErrorException {
        return asynchronousLndAPI;
    }

    /**
     * Method to get the synchronous lnd api from lightningj.
     * @return the synchronous lnd api from lightningj.
     *
     * @throws IOException if communication problems occurred with underlying node.
     * @throws InternalErrorException if internal problems occurred with LND node.
     */
    @Override
    protected SynchronousLndAPI getSyncAPI() throws IOException, InternalErrorException {
        return synchronousLndAPI;
    }

    /**
     * Method that should return the hostname of IP address of the LND node to connect to.
     * @return the hostname of IP address of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    protected abstract String getHost() throws InternalErrorException;

    /**
     * Method that should return the port number of the LND node to connect to.
     * @return the port number of the LND node to connect to.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    protected abstract int getPort() throws InternalErrorException;

    /**
     * The path to the LND tls certificate to trust, securing the communication to the LND node.
     * Should point to an file readable by the current user..
     * @return the path to the tls cert path.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    protected abstract String getTLSCertPath() throws InternalErrorException;

    /**
     * The path to the macaroon file that is used to authenticate to the LND node. The macaroon
     * should have invoice creation rights.
     *
     * @return the path to the macaroon to use.
     * @throws InternalErrorException if problems occurred getting the configuration information.
     */
    protected abstract String getMacaroonPath() throws InternalErrorException;
}
