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

import org.lightningj.paywall.tokengenerator.TokenException;
import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * Value object class used to generate API error message when returning either
 * JSON or XML.
 */
@XmlRootElement
public class APIError {

    private HttpStatus status;
    private String message;
    private List<String> errors;
    private TokenException.Reason reason;

    /**
     * Empty Constructor
     */
    public APIError(){
    }

    /**
     * Constructor when having multiple errors
     * @param status the HTTP status code
     * @param message the error message associated with exception
     * @param errors List of constructed error messages
     */
    public APIError(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    /**
     * Constructor when having one errors
     * @param status the HTTP status code
     * @param message the error message associated with exception
     * @param error Constructed error messages
     */
    public APIError(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }

    /**
     *
     * @return the HTTP status code
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     *
     * @return the error message associated with exception
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return Constructed error messages
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     *
     * @param status the HTTP status code
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     *
     * @param message the error message associated with exception
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     *
     * @param errors Constructed error messages
     */
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /**
     *
     * @return if error is related to JWT token the reason is specified
     * otherwise null.
     */
    public TokenException.Reason getReason() {
        return reason;
    }

    /**
     *
     * @param reason if error is related to JWT token the reason is specified
     * otherwise null.
     */
    public void setReason(TokenException.Reason reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "APIError{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                ", reason=" + reason +
                '}';
    }
}
