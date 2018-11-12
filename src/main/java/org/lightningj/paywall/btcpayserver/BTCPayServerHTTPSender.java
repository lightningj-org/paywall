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
package org.lightningj.paywall.btcpayserver;

import com.google.common.io.ByteStreams;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.JSONParsable;
import org.lightningj.paywall.keymgmt.AsymmetricKeyManager;
import org.lightningj.paywall.keymgmt.Context;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.interfaces.ECPublicKey;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class in charge of the http transport parts of sending JSON data to BTC Pay Server.
 *
 * Created by philip on 2018-10-15.
 */
public class BTCPayServerHTTPSender {

    /**
     * Enumeration of available HTTP Metod values.
     */
    public enum METHOD{
        GET,
        POST,
        PUT,
        DELETE;
    }

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String JSON_CONTENT_TYPE = "application/json";

    public static final String HEADER_ACCEPT = "Accept";

    public static final String HEADER_ACCEPT_VERSION= "x-accept-version";
    public static final String DEFAULT_ACCEPT_VERSION = "2.0.0";

    public static final String HEADER_IDENTITY = "x-identity";
    public static final String HEADER_SIGNATURE = "x-signature";

    protected String baseURL;
    protected AsymmetricKeyManager keyManager;
    protected BTCPayServerHelper helper = new BTCPayServerHelper();
    protected SSLContext sslContext = null;
    protected static final Context keyCtx = BTCPayServerKeyContext.INSTANCE;

    protected static Logger log = Logger.getLogger(BTCPayServerHTTPSender.class.getName());
    private static Level debugLevel = Level.INFO;

    /**
     * Constructor of BTCPayServerHTTPSender when using default SSL Context.
     *
     * @param baseURL the baseURL for the REST endpoint of BTC Pay Server.
     * @param keyManager the current key manager holding the signing keys.
     */
    public BTCPayServerHTTPSender(String baseURL, AsymmetricKeyManager keyManager){
        this(baseURL,keyManager,null);
    }

    /**
     * Constructor of BTCPayServerHTTPSender when using a custom SSL Context.
     *
     * @param baseURL the baseURL for the REST endpoint of BTC Pay Server.
     * @param keyManager the current key manager holding the signing keys.
     * @param sslContext the custom SSL Context in order to connect to BTC Pay Server.
     */
    public BTCPayServerHTTPSender(String baseURL, AsymmetricKeyManager keyManager, SSLContext sslContext){
        this.baseURL = baseURL;
        if(baseURL.endsWith("/")){
            this.baseURL = baseURL.substring(0,baseURL.length()-1);
        }
        this.keyManager = keyManager;
        this.sslContext = sslContext;
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param sign if the request should contain a x-signature header.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, boolean sign) throws InternalErrorException, IOException{
        return send(method,endpoint,(byte[]) null, sign,null);
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param requestJson The JSON data to send, null to not perform any output body. (GET requests)
     * @param sign if the request should contain a x-signature header.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, byte[] requestJson, boolean sign) throws InternalErrorException, IOException{
        return send(method,endpoint,requestJson, sign,null);
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param requestJson The JSON data to send, null to not perform any output body. (GET requests)
     * @param sign if the request should contain a x-signature header.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, JSONParsable requestJson, boolean sign) throws InternalErrorException, IOException{
        return send(method,endpoint,requestJson, sign,null);
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param sign if the request should contain a x-signature header.
     * @param queryParams a map of query parameters appended to the URI string.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, boolean sign, Map<String,String> queryParams) throws InternalErrorException, IOException{
        return send(method,endpoint,(byte[]) null,sign, queryParams);
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param requestJson The JSON data to send, null to not perform any output body. (GET requests)
     * @param sign if the request should contain a x-signature header.
     * @param queryParams a map of query parameters appended to the URI string.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, JSONParsable requestJson, boolean sign, Map<String,String> queryParams) throws InternalErrorException, IOException{
        return send(method,endpoint,requestJson.toJsonAsString(false).getBytes("UTF-8"),sign,queryParams);
    }

    /**
     * Method to send data to given REST endpoint of BTC Pay Server.
     *
     * @param method the HTTP method to use.
     * @param endpoint the REST endpoint, i.e '/invoices', starting with "/"
     * @param requestJson The JSON data to send, null to not perform any output body. (GET requests)
     * @param sign if the request should contain a x-signature header.
     * @param queryParams a map of query parameters appended to the URI string.
     * @return the resulting json data in the response.
     * @throws InternalErrorException if internal error occurred setting up communication or BTC Pay Server signaled error.
     * @throws IOException if communication problems occurred.
     */
    public byte[] send(METHOD method, String endpoint, byte[] requestJson, boolean sign, Map<String,String> queryParams) throws InternalErrorException, IOException{
        try {
            String urlString = baseURL + endpoint + constructQueryString(queryParams);
            if(log.isLoggable(debugLevel)){
                log.log(debugLevel, "Sending to BTC Server " + method + " " + urlString + ", data: " + (requestJson != null ? new String(requestJson, "UTF-8") : ""));
            }
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(connection instanceof HttpsURLConnection && sslContext != null){
                HttpsURLConnection sslConnection = (HttpsURLConnection) connection;
                sslConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            connection.setRequestMethod(method.name());
            connection.addRequestProperty(HEADER_ACCEPT_VERSION,DEFAULT_ACCEPT_VERSION);
            connection.setDoInput(true);
            connection.setRequestProperty(HEADER_CONTENT_TYPE, JSON_CONTENT_TYPE);
            connection.setRequestProperty(HEADER_ACCEPT, JSON_CONTENT_TYPE);
            if(requestJson != null) {
                connection.setDoOutput(true);
            }
            if(sign){
                connection.setRequestProperty(HEADER_IDENTITY,helper.pubKeyInHex((ECPublicKey) keyManager.getPublicKey(keyCtx)));
                String dataString = requestJson == null ? "" : new String(requestJson,"UTF-8");
                connection.setRequestProperty(HEADER_SIGNATURE,helper.genSignature(keyManager.getPrivateKey(keyCtx), urlString, dataString));
            }
            if(requestJson != null) {
                connection.setRequestProperty(HEADER_CONTENT_TYPE, JSON_CONTENT_TYPE);
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestJson);
            }
            int result = connection.getResponseCode();
            if(result == HttpURLConnection.HTTP_OK){
                byte[] response = ByteStreams.toByteArray(connection.getInputStream());
                if(log.isLoggable(debugLevel)){
                    log.log(debugLevel, "Received from BTC Server, " + result + ", data: " + new String(response,"UTF-8"));
                }
                return response;
            }else{
                if(log.isLoggable(debugLevel)){
                    log.log(debugLevel, "Received from BTC Server, " + result + ", data: " + connection.getResponseMessage());
                }
                throw new InternalErrorException("Error communicating with BTC Pay Server: " + connection.getResponseMessage());
            }

        }catch(MalformedURLException e){
            throw new InternalErrorException("Malformed URL when connecting to BTC Pay Server: " + e.getMessage(),e);
        }
    }

    /**
     * Help method used to build a query string for the given set of query parameters.
     * @param queryParams a map of key, value URL parameters.
     * @return a constructed query string starting with ?
     * @throws UnsupportedEncodingException if UTF-8 encoding wasn't supported.
     */
    private String constructQueryString(Map<String,String> queryParams) throws UnsupportedEncodingException{
        if(queryParams == null || queryParams.size() == 0){
            return "";
        }
        String retval = "?";
        boolean first = true;
        for(String key : queryParams.keySet()){
            if(first){
                first=false;
            }else{
                retval += "&";
            }
            retval += URLEncoder.encode(key,"UTF-8") + "=" + URLEncoder.encode(queryParams.get(key),"UTF-8");
        }
        return retval;
    }
}
