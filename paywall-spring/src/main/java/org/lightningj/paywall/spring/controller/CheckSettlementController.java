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
package org.lightningj.paywall.spring.controller;

import org.lightningj.paywall.paymentflow.ExpectedTokenType;
import org.lightningj.paywall.paymentflow.PaymentFlow;
import org.lightningj.paywall.paymentflow.PaymentFlowManager;
import org.lightningj.paywall.paymentflow.SettlementResult;
import org.lightningj.paywall.spring.PaywallExceptionHandler;
import org.lightningj.paywall.spring.response.SettlementResponse;
import org.lightningj.paywall.spring.util.PaywallRuntimeException;
import org.lightningj.paywall.spring.util.RequestHelper;
import org.lightningj.paywall.spring.util.SpringCachableHttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for checking settlement using REST, either json or xml is
 * supported.
 */
@RestController
public class CheckSettlementController {

    RequestHelper requestHelper = new RequestHelper();

    @Autowired
    PaymentFlowManager paymentFlowManager;

    @Autowired
    PaywallExceptionHandler paywallExceptionHandler;

    // TODO Next is WebSocket

    /**
     * Main controller action checking settlement for a generated invoice token set in header wih name
     * 'Payment'.
     * @param request the related http request.
     * @param response the related http response.
     * @return a settlement response containing either settled=false of a settlement token with meta data.
     */
    @RequestMapping("/paywall/api/checkSettlement")
    public SettlementResponse checkSettlement(HttpServletRequest request, HttpServletResponse response) {

        RequestHelper.RequestType requestType = requestHelper.getRequestType(request, RequestHelper.RequestType.JSON);
        SpringCachableHttpServletRequest cachableHttpServletRequest = new SpringCachableHttpServletRequest(request);

        try {
            SettlementResponse settlementResponse;
            PaymentFlow paymentFlow = paymentFlowManager.getPaymentFlowFromToken(cachableHttpServletRequest, ExpectedTokenType.INVOICE_TOKEN);

            if(paymentFlow.isSettled()){
                SettlementResult settlementResult = paymentFlow.getSettlement();
                settlementResponse = new SettlementResponse(settlementResult);
            }else{
                settlementResponse = new SettlementResponse();
            }
            response.setContentType(requestType.getContentType());
            return settlementResponse;
        }catch (Exception e){
            throw new PaywallRuntimeException(e);
        }
    }

    /**
     * Exception handler for this controller.
     * @param request the related http request.
     * @param response the related http response.
     * @param e the related exception
     * @return generated response object.
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        return paywallExceptionHandler.handleException(request,response,e);
    }
}
