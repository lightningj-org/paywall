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
package org.lightningj.paywall.spring.local;

import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.paymentflow.SettlementResult;
import org.lightningj.paywall.paymenthandler.PaymentEvent;
import org.lightningj.paywall.paymenthandler.PaymentEventType;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.paymenthandler.PaymentListener;
import org.lightningj.paywall.spring.APIError;
import org.lightningj.paywall.spring.PaywallExceptionHandler;
import org.lightningj.paywall.spring.response.SettlementResponse;
import org.lightningj.paywall.spring.util.RequestHelper;
import org.lightningj.paywall.spring.websocket.EnableWebSocketCondition;
import org.lightningj.paywall.spring.websocket.WebSocketSettledPaymentHandler;
import org.lightningj.paywall.tokengenerator.TokenContext;
import org.lightningj.paywall.tokengenerator.TokenException;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.vo.MinimalInvoice;
import org.lightningj.paywall.vo.RequestData;
import org.lightningj.paywall.vo.Settlement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.io.IOException;

/**
 * Local WebSocket Controller controlling the checkSettlement WebSocket Endpoint.
 * <p>
 *     It listens on the queue "/queue/paywall/checksettlement/{preImageHash}" and sends
 *     a settlements response or error response on the soecket.
 * </p>
 * @author Philip Vendil 2019-05-21
 */
@Controller
@Profile({"paywall_local","integration_paywall_local"})
@Conditional(EnableWebSocketCondition.class)
public class LocalWebSocketController {

    @Autowired
    WebSocketSettledPaymentHandler webSocketSettledPaymentHandler;

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    PaymentHandler paymentHandler;

    @Autowired
    PaywallExceptionHandler paywallExceptionHandler;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Subscribe method in charge of handling web socket connection and setup.
     * A PaymentListener is registered and the WebSocket will be notified as soon as
     * the payment is registered as settled.
     *
     * @param preImageHash the preImageHash in base64 encoded String as part of the
     *                     destination variable.
     * @param token the token header variable set in the WebSocket call.
     * @return either a settlement response directy or null if payment wasn't settled.
     *
     */
    @SubscribeMapping("/queue/paywall/checksettlement/{preImageHash}")
    public String subscribe(
            @DestinationVariable("preImageHash") String preImageHash,
            @Header("token") String token) {

        try {
            JwtClaims tokenClaims = tokenGenerator.parseToken(TokenContext.CONTEXT_INVOICE_TOKEN_TYPE, token);
            long expireDate =  tokenClaims.getExpirationTime().getValueInMillis();
            MinimalInvoice invoice = new MinimalInvoice(tokenClaims);
            RequestData requestData = new RequestData(tokenClaims);
            if (!preImageHash.equals(Base58.encodeToString(invoice.getPreImageHash()))) {
                throw new IllegalArgumentException("Token preImageHash doesn't match WebSocket name.");
            }
            Settlement settlement = paymentHandler.checkSettlement(invoice.getPreImageHash(), true);
            if (settlement == null) {
                webSocketSettledPaymentHandler.registerPaymentListener(preImageHash,expireDate ,new LocalWebSocketPaymentListener(invoice.getPreImageHash(), requestData));
            } else {
                return generateSettlementResponse(requestData, settlement);
            }
        }catch(Exception e){
            return generateErrorResponse(e);
        }
        return null;
    }

    /**
     * Help method to generate a settlement JSOM response to a WebSocket.
     * @param requestData the request data parsed from the JWT sent in the WebSocket subscription header.
     * @param settlement the settlement value object.
     * @return the JSON representation of the settlement response.
     * @throws InternalErrorException if internal error happened generating the settlement token.
     * @throws IOException if communication problems occurred communication with underlying components.
     * @throws TokenException if JWT generation problems occurred.
     */
    private String generateSettlementResponse(RequestData requestData, Settlement settlement) throws InternalErrorException, IOException, TokenException {
        String settlementToken = tokenGenerator.generateSettlementToken(null,settlement,requestData,settlement.getValidUntil(),settlement.getValidFrom(), null); // Source Node is null for local implementation.
        return new SettlementResponse(new SettlementResult(settlement,settlementToken)).toJsonAsString(false);
    }

    /**
     * Help method to convert an exception to an error sent through WebSocket.
     * @param exception the exception to convert to JSON error.
     * @return a JSON representation of the error.
     */
    private String generateErrorResponse(Exception exception){
        ResponseEntity<Object> errorResponse =  paywallExceptionHandler.handleException(RequestHelper.RequestType.JSON,exception);
        assert errorResponse.getBody() instanceof APIError : "Expected APIError in generated error response";
        APIError apiError = (APIError) errorResponse.getBody();
        return apiError.toJsonAsString(false);
    }

    class LocalWebSocketPaymentListener implements PaymentListener{

        byte[] preImageHash;
        RequestData requestData;

        LocalWebSocketPaymentListener(byte[] preImageHash, RequestData requestData){
            this.preImageHash = preImageHash;
            this.requestData = requestData;
        }
        /**
         * @return the pre image hash of the payment that the listener is interested in, null if all events
         * should be signaled.
         */
        @Override
        public byte[] getPreImageHash() {
            return preImageHash;
        }

        /**
         * @return the type of event the listener is interested in, use PaymentEventType.ANY_TYPE to receive
         * notification for any type.
         */
        @Override
        public PaymentEventType getType() {
            return PaymentEventType.INVOICE_SETTLED;
        }

        /**
         * @return flag indicating that the this listener should unregister itself after first matching
         * event have been triggered. This to avoid manual unregistration.
         */
        @Override
        public boolean unregisterAfterEvent() {
            return false;
        }

        /**
         * This method every time an and event related to a payment have been triggered.
         *
         * @param event the related payment event.
         * @see PaymentEvent
         */
        @Override
        public void onPaymentEvent(PaymentEvent event) {
            assert event.getType() == PaymentEventType.INVOICE_SETTLED : "LocalWebSocketController expected INVOICE_SETTLED event only, not " + event.getType();
            try {
                Settlement settlement = (Settlement) event.getPayment();
                String settlementResponse = generateSettlementResponse(requestData,settlement);
                messagingTemplate.convertAndSend("/queue/paywall/checksettlement/" + Base58.encodeToString(preImageHash), settlementResponse);
            }catch (Exception e){
                String errorResponse = generateErrorResponse(e);
                messagingTemplate.convertAndSend("/queue/paywall/checksettlement/" + Base58.encodeToString(preImageHash), errorResponse);
            }
        }
    }
}
