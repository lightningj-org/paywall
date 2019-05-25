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

import org.lightningj.paywall.spring.util.RequestHelper;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Interface used by paywall related controllers to handle exceptions
 * in a uniform way. Injected as a bean in related controllers.
 *
 * @author philip 2019-02-15
 */
public interface PaywallExceptionHandler {

    /**
     * Method to handle paywall related exceptions in a uniform way.
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    ResponseEntity<Object> handleException(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Exception exception);

    /**
     * Method to handle paywall related exceptions in a uniform way.
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @param errorMessage extra constructed error message returned.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    ResponseEntity<Object> handleException(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Exception exception,
                                           String errorMessage);

    /**
     * Method to handle paywall related exceptions in a uniform way.
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @param errorMessages extra constructed error messages returned.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    ResponseEntity<Object> handleException(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Exception exception,
                                           List<String> errorMessages);

    /**
     * Method to handle paywall related exceptions in a uniform way but from
     * a WebSocket context.
     * @param requestType the expected type in response.
     * @param exception the exception thrown in controller.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    ResponseEntity<Object> handleException(RequestHelper.RequestType requestType,
                                           Exception exception);

    /**
     * Method to handle paywall related exceptions in a uniform way but from
     * a WebSocket context.
     * @param requestType the expected type in response.
     * @param exception the exception thrown in controller.
     * @param errorMessages extra constructed error messages returned.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    ResponseEntity<Object> handleException(RequestHelper.RequestType requestType,
                                           Exception exception,
                                           List<String> errorMessages);
}
