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
package org.lightningj.paywall.spring.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class containing help method for determine state in a
 * HTTP Requests.
 *
 * @author philip 2019-02-13
 */
public class RequestHelper {

    public static final String HEADER_ACCEPT = "Accept";

    /**
     * Enumeration of different supported request handling types.
     */
    public enum RequestType{
        JSON("application/json"),
        XML("application/xml");

        private String contentType;
        RequestType(String contentType){
            this.contentType=contentType;
        }

        public String getContentType(){
            return this.contentType;
        }
    }

    /**
     * Help method to determine the type or request by looking at URI ending,
     * and the contentType and accept header values.
     * @param request the related http request
     * @param defaultType default type if no matching could be found.
     * @return the determined RequestType related to the request.
     */
    public RequestType getRequestType(HttpServletRequest request, RequestType defaultType){
        String contentType = request.getContentType();
        if(contentType == null){
            contentType = "";
        }
        String accept = request.getHeader(HEADER_ACCEPT);
        if(accept == null){
            accept = "";
        }

        if (request.getRequestURI().endsWith(".xml")){
            return RequestType.XML;
        }
        if (request.getRequestURI().endsWith(".json")){
            return RequestType.JSON;
        }

        if (accept.trim().equals(RequestType.XML.getContentType())){
            return RequestType.XML;
        }

        if (accept.trim().equals(RequestType.JSON.getContentType())){
            return RequestType.JSON;
        }

        if (contentType.trim().equals(RequestType.XML.getContentType())){
            return RequestType.XML;
        }

        if (contentType.trim().equals(RequestType.JSON.getContentType())){
            return RequestType.JSON;
        }

        return defaultType;
    }

}
