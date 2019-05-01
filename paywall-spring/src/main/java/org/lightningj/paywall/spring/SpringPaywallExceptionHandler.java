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
import org.lightningj.paywall.spring.util.PaywallRuntimeException;
import org.lightningj.paywall.spring.util.RequestHelper;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Default Spring implementation of PaywallExceptionHandler
 *
 * @see org.lightningj.paywall.spring.PaywallExceptionHandler
 */
public class SpringPaywallExceptionHandler implements PaywallExceptionHandler{

    static final String INTERNAL_SERVER_ERROR_MSG = "Internal Server Error";

    private static Map<Class, HttpStatus> statusCodeMap = new HashMap<>();
    private static Map<Class, String> errorMsgPrefix = new HashMap<>();
    static {
        // Default is 500
        statusCodeMap.put(IllegalArgumentException.class,HttpStatus.BAD_REQUEST);
        statusCodeMap.put(IOException.class,HttpStatus.SERVICE_UNAVAILABLE);
        statusCodeMap.put(TokenException.class,HttpStatus.UNAUTHORIZED);

        errorMsgPrefix.put(IllegalArgumentException.class, "Invalid Request");
        errorMsgPrefix.put(IOException.class, "Internal Communication Problems");
        errorMsgPrefix.put(TokenException.class, "JWT Token Problem");
    }

    RequestHelper requestHelper = new RequestHelper();

    /**
     * Method to handle paywall related exceptions in a uniform way.
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    @Override
    public ResponseEntity<Object> handleException(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  Exception exception){
        List<String> errorMessages = new ArrayList<>();
        String errorMessage;
        if (exception instanceof PaywallRuntimeException) {
            errorMessage = getErrorMessageFromException(exception.getCause());
        } else {
            errorMessage = getErrorMessageFromException(exception);
        }
        if (errorMessage != null) {
            errorMessages.add(errorMessage);
        }

        return handleException(request,response,exception, errorMessages);
    }


    /**
     * Method to handle paywall related exceptions in a uniform way.
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @param errorMessage extra constructed error message returned.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    @Override
    public ResponseEntity<Object> handleException(HttpServletRequest request,
                                           HttpServletResponse response,
                                           Exception exception,
                                           String errorMessage){
        return handleException(request,response,exception, Arrays.asList(errorMessage));
    }

    /**
     * Method to handle paywall related exceptions in a uniform way.
     *
     * @param request the related http request.
     * @param response the related http response.
     * @param exception the exception thrown in controller.
     * @param errorMessages a list of constructed error messages to return.
     * @return a response entity containing error information sent back to requester if exception
     * occurs during processing.
     */
    @Override
    public ResponseEntity<Object> handleException(HttpServletRequest request, HttpServletResponse response,
                                                  Exception exception, List<String> errorMessages){

        RequestHelper.RequestType requestType = requestHelper.getRequestType(request, RequestHelper.RequestType.JSON);
        if(exception instanceof PaywallRuntimeException){
            exception = (Exception) exception.getCause();
        }
        HttpStatus httpStatus = statusCodeMap.get(exception.getClass());
        if(httpStatus == null){
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String messagePrefix = errorMsgPrefix.get(exception.getClass());
        String message;
        if(messagePrefix == null){
            message = INTERNAL_SERVER_ERROR_MSG;
        }else {
            message = messagePrefix;
            if (exception.getLocalizedMessage() != null) {
                message += ": " + exception.getLocalizedMessage();
            }
        }
        APIError apiError = new APIError(httpStatus,message,errorMessages);
        if(exception instanceof TokenException){
            apiError.setReason(((TokenException) exception).getReason());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(requestType.getContentType()));
        return new ResponseEntity<>(
                apiError, httpHeaders, apiError.getStatus());
    }

    /**
     * Help method returning the localized message if not exception is not
     * related to an internal server error. Then the details is masked out.
     * @param exception the expection to get error message from.
     * @return the localized message from exception or "Internal Server Error" if
     * exception is related to internal error.
     */
    private String getErrorMessageFromException(Throwable exception){
        if(errorMsgPrefix.get(exception.getClass()) != null){
            return exception.getLocalizedMessage();
        }else{
            return INTERNAL_SERVER_ERROR_MSG;
        }
    }
}
