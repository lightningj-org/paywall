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
package org.lightningj.paywall.spring;

import org.lightningj.paywall.AlreadyExecutedException;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory;
import org.lightningj.paywall.paymentflow.InvoiceResult;
import org.lightningj.paywall.paymentflow.PaymentFlow;
import org.lightningj.paywall.paymentflow.PaymentFlowManager;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.spring.response.InvoiceResponse;
import org.lightningj.paywall.spring.util.RequestHelper;
import org.lightningj.paywall.spring.util.SpringCachableHttpServletRequest;
import org.lightningj.paywall.spring.websocket.PaywallWebSocketConfig;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.util.SettingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main interceptor catching calls finding end points with a @PaymentRequired and
 * starting the related payment flow.
 *
 * Created by Philip Vendil on 2019-04-23.
 */
public class PaywallInterceptor implements HandlerInterceptor {

    /**
     * Request attribute set to true if related payment is per per request.
     */
    public static final String REQUEST_ATTRIBUTE_PAY_PER_REQUEST = "PAYWALL_PAY_PER_REQUEST";
    /**
     * Request attribute containing the related cached payment flow object using.
     */
    public static final String REQUEST_ATTRIBUTE_PAYMENT_FLOW = "PAYWALL_PAYMENT_FLOW";
    /**
     * Request attribute containing the related payment pre image hash.
     */
    public static final String REQUEST_ATTRIBUTE_PREIMAGE_HASH = "PAYWALL_PRE_IMAGEHASH";

    RequestHelper requestHelper = new RequestHelper();

    @Autowired
    PaywallProperties paywallProperties;

    @Autowired
    LightningHandler lightningHandler;

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    CurrencyConverter currencyConverter;

    @Autowired
    OrderRequestGeneratorFactory orderRequestGeneratorFactory;

    @Autowired
    PaymentHandler paymentHandler;

    @Autowired
    PaymentFlowManager paymentFlowManager;

    @Autowired
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Autowired
    PaywallExceptionHandler paywallExceptionHandler;

    Logger log = Logger.getLogger(PaywallInterceptor.class.getName());

    /**
     * PreHandle method checking if related end-point has @PaymentRequired set and then
     * checks if payment is required or is settled before continuing to the end-point.
     * @param request the related http servlet request.
     * @param response the related http servlet request.
     * @param handler the end-point handler.
     * @return true if endpoint doesn't have @PaymentRequired annotation or related payment is settled.
     * @throws Exception if uncatchable exception occurred.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        PaymentRequired paymentRequired = findPaymentRequired(handler);
        if (paymentRequired != null) {
            RequestHelper.RequestType requestType = requestHelper.getRequestType(request, RequestHelper.RequestType.JSON);
            if(log.isLoggable(Level.FINE)){
                log.fine("Paywall Interceptor: Checking payment flow for PaymentRequired annotated service: " + request.getRequestURI());
            }
            try{
                if (isRestCall(handler)) {
                    SpringCachableHttpServletRequest cachableHttpServletRequest = new SpringCachableHttpServletRequest(request);

                    PaymentFlow paymentFlow = paymentFlowManager.getPaymentFlowByAnnotation(paymentRequired, cachableHttpServletRequest);

                    boolean isPaymentRequired = false;
                    try{
                        isPaymentRequired = paymentFlow.isPaymentRequired();
                    }catch (AlreadyExecutedException e){
                        log.fine("Paywall Interceptor: Pay Per Request Payment (preImageHash=" + displayablePreImageHash(e.getPreImageHash()) + ") already executed. Message: " + e.getMessage());
                        isPaymentRequired = true;
                    }

                    if (isPaymentRequired) {
                      InvoiceResult requestPaymentResult = paymentFlow.requestPayment();
                      InvoiceResponse invoiceResponse = genInvoiceResponse(requestPaymentResult,paymentRequired);
                      generatePaymentRequiredResponse(requestType,invoiceResponse,response);
                      if(log.isLoggable(Level.FINE)){
                          log.fine("Paywall Interceptor: New payment required (preImageHash=" + displayablePreImageHash(invoiceResponse.getPreImageHash()) + ") for  service: " + request.getRequestURI() + ".");
                      }
                      return false;
                    }else{
                        if(log.isLoggable(Level.FINE)) {
                            log.fine("Paywall Interceptor: Serving settled request with preImageHash=" + displayablePreImageHash(paymentFlow.getPreImageHash()) + " for  service: " + request.getRequestURI() + ".");
                        }
                        if(paymentRequired.payPerRequest()){
                            // Set pay_per_request_flag
                            request.setAttribute(REQUEST_ATTRIBUTE_PAY_PER_REQUEST, true);
                            request.setAttribute(REQUEST_ATTRIBUTE_PAYMENT_FLOW, paymentFlow);
                            request.setAttribute(REQUEST_ATTRIBUTE_PREIMAGE_HASH,paymentFlow.getPreImageHash());
                        }
                    }
                } else {
                    throw new InternalErrorException("Unsupported Endpoint with PaymentRequired annotation. Currently is only @RestController annotated services supported.");
                }
            }catch(Exception e){
                logError(e);
                ResponseEntity<Object> responseEntity = paywallExceptionHandler.handleException(request,response,e);
                generateExceptionResponse(requestType,responseEntity,response);
                return false;
            }
        }

        return true;
    }

    /**
     * Interceptor method called after view have been completed that checks if related
     * request has payPerRequest set and if true marks the related payment as executed and spent.
     *
     * @param request the related http servlet request.
     * @param response the related http servlet request.
     * @param handler the end-point handler.
     * @param ex the exception if thrown.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        try {
            byte[] preImageHash = (byte[]) request.getAttribute(REQUEST_ATTRIBUTE_PREIMAGE_HASH);
            if(ex == null && response.getStatus() == HttpServletResponse.SC_OK && request.getAttribute(REQUEST_ATTRIBUTE_PAY_PER_REQUEST) != null){
                PaymentFlow paymentFlow = (PaymentFlow) request.getAttribute(REQUEST_ATTRIBUTE_PAYMENT_FLOW);
                paymentFlow.markAsExecuted();
                if(log.isLoggable(Level.FINE)) {
                    log.fine("Paywall interceptor: Request related to preImageHash=" + displayablePreImageHash(preImageHash) + " was marked as executed (spent).");
                }
        }else{
            if(request.getAttribute(REQUEST_ATTRIBUTE_PAY_PER_REQUEST) != null){
                log.severe("Paywall interceptor: Request related to preImageHash=" + displayablePreImageHash(preImageHash) + " couldn't be marked as executed (spent) due to problems servicing the request.");
            }
        }
        }catch (Exception e){
            logError(e);
        }
    }

    /**
     * Method to check if related end point have a @PaymentRequired annotation.
     * @param handler the target handler end point to check.
     * @return the PaymentRequired if exists on end point otherwise null.
     */
    private PaymentRequired findPaymentRequired(Object handler){
        PaymentRequired paymentRequired = null;
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            paymentRequired = handlerMethod.getMethodAnnotation(PaymentRequired.class);
            if (paymentRequired == null) {
                paymentRequired = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PaymentRequired.class);
            }
        }
        return paymentRequired;
    }


    /**
     * Method checking if related controller has @RestController annotation.
     * @param handler the related object handler.
     * @return true if related call is a rest call.
     */
    private boolean isRestCall(Object handler){
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RestController.class) != null;
        }
        return false;
    }

    /**
     * Generates a JSON/XML version of a invoice.
     * @param invoiceResult the invoice result from used PaymentFlow.
     * @param paymentRequired the payment required annotation.
     * @return JSON/XML variant of generated invoice result.
     * @throws InternalErrorException if internal configuration was faulty.
     */
    private InvoiceResponse genInvoiceResponse(InvoiceResult invoiceResult, PaymentRequired paymentRequired) throws InternalErrorException {

        String webSocketCheckSettlementUrl = null;
        String webSocketCheckSettlementQueue = null;
        if(SettingUtils.checkBooleanWithDefault(paywallProperties.getWebSocketEnable(), PaywallProperties.WEBSOCKET_ENABLE, PaywallProperties.DEFAULT_WEBSOCKET_ENABLE)){
            webSocketCheckSettlementUrl = paywallProperties.getWebSocketCheckSettlementUrl();
            webSocketCheckSettlementQueue = PaywallWebSocketConfig.CHECK_SETTLEMENT_QUEUE_PREFIX;
        }

        return new InvoiceResponse(invoiceResult,
                paymentRequired.payPerRequest(),
                paymentRequired.requestPolicy(),
                SettingUtils.checkRequiredBoolean(paywallProperties.getInvoiceIncludeNodeInfo(),PaywallProperties.INVOICE_INCLUDE_NODEINFO),
                paywallProperties.getCheckSettlementURL(),
                paywallProperties.getQrCodeUrl(),
                webSocketCheckSettlementUrl,
                webSocketCheckSettlementQueue);
    }



    /**
     * Method that sets payment required resposne data in JSON or XML depending on
     * request type.
     * <p>
     *     Also sets the content type and HTTP status to SC_PAYMENT_REQUIRED (402)
     * </p>
     *
     * @param requestType the request type of expected response.
     * @param invoiceResponse the invoice response to return.
     * @param response the servlet http response object to write the JSON or XML data to.
     * @throws InternalErrorException if internal exception occurred converting the data.
     * @throws IOException if communication exception occurred converting the data.
     */
    private void generatePaymentRequiredResponse(RequestHelper.RequestType requestType, InvoiceResponse invoiceResponse, HttpServletResponse response) throws InternalErrorException, IOException {
        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        response.setContentType(requestType.getContentType());
        HttpMessageConverter converter = getHttpMessageConverter(requestType);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(response);
        converter.write(invoiceResponse, requestType.getMediaType(), servletServerHttpResponse);
    }

    /**
     * Method that generates response data in either Json or XML depending on request type.
     * <p>
     *     Content type and status is set depending on the type of exception determined by the used
     *     exception handler.
     * </p>
     * @param requestType the request type of expected response.
     * @param responseEntity the response entity generated by the exception handler.
     * @param response the servlet http response object to write the JSON or XML data to.
     * @throws InternalErrorException if internal exception occurred converting the data.
     * @throws IOException if communication exception occurred converting the data.
     */
    private void generateExceptionResponse(RequestHelper.RequestType requestType, ResponseEntity<Object> responseEntity, HttpServletResponse response) throws InternalErrorException, IOException {
        response.setStatus(responseEntity.getStatusCodeValue());
        response.setContentType(requestType.getContentType());
        HttpMessageConverter converter = getHttpMessageConverter(requestType);
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(response);
        converter.write(responseEntity.getBody(), requestType.getMediaType(), servletServerHttpResponse);
    }

    /**
     * Method fetching RequestMappingHandlerAdapters registered HttpMessageConverter and
     * builds a cache of requestType -> HttpMessageConverter used to convert message to
     * http response data.
     */
    private HashMap<MediaType, HttpMessageConverter> converterCache = new HashMap<>();
    private HttpMessageConverter getHttpMessageConverter(RequestHelper.RequestType requestType) throws InternalErrorException{
        HttpMessageConverter retval = converterCache.get(requestType.getMediaType());
        if(retval == null){
            List<HttpMessageConverter<?>> converters = requestMappingHandlerAdapter.getMessageConverters();
            for(HttpMessageConverter converter : converters){
                if(converter.canWrite(InvoiceResponse.class, requestType.getMediaType())){
                    retval = converter;
                    break;
                }
            }
            if(retval == null){
                throw new InternalErrorException("Paywall Internal error converting InvoiceResponse, no HttpMessageConverter found for requeset type: " + requestType);
            }
            converterCache.put(requestType.getMediaType(), retval);
        }
        return retval;
    }

    /**
     * Help method logging an exception with level fine for IllegalArgumentException, info for TokenException and
     * severe for other errors.
     * @param e the expection to log.
     */
    private void logError(Exception e){
        if(e instanceof IllegalArgumentException){
            log.log(Level.FINE,"Paywall Interceptor: invalid argument when parsing payment data: " + e.getMessage(),e);
        }else {
            if(e instanceof TokenException){
                log.log(Level.INFO,"Paywall Interceptor: JWT Token exception: " + e.getMessage(),e);
            }else {
                log.log(Level.SEVERE,"Paywall Interceptor: Error occurred processing payment data (" + e.getClass().getSimpleName() + "): " + e.getMessage(),e);
            }
        }
    }

    /**
     *
     * @param preImageHash the byte array representation of the preImageHash
     * @return a displayable version of the preImageHash
     */
    private String displayablePreImageHash(byte[] preImageHash){
        return preImageHash != null ? Base58.encodeToString(preImageHash) : "null";
    }

    /**
     *
     * @param preImageHash the String representation of the preImageHash
     * @return a displayable version of the preImageHash
     */
    private String displayablePreImageHash(String preImageHash){
        return preImageHash != null ? preImageHash : "null";
    }

}
