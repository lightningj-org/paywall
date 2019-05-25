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
package org.lightningj.paywall.spring.websocket;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.spring.PaywallProperties;
import org.lightningj.paywall.util.Base58;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class in charge of setting up Paywall WebSocket Endpoint with related
 * stomp queues.
 * <p>
 *     Set's up on dynamic queue /queue/paywall/checksettlement/{preImageHash} at
 *     end point '/paywall/api/checkSettlementWebSocket' with SockJS support
 * </p>
 * @author Philip Vendil 2019-05-22
 */
@Configuration
@EnableWebSocketMessageBroker
@Conditional(EnableWebSocketCondition.class)
public class PaywallWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    Logger log = Logger.getLogger(PaywallWebSocketConfig.class.getName());

    public static final String CHECK_SETTLEMENT_QUEUE_PREFIX = "/queue/paywall/checksettlement/";

    @Autowired
    WebSocketSettledPaymentHandler webSocketSettledPaymentHandler;

    @Autowired
    PaywallProperties paywallProperties;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue/paywall");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(paywallProperties.getWebSocketCheckSettlementUrl()).withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new PaywallChannelInterceptor());
    }


    class PaywallChannelInterceptor implements ChannelInterceptor{

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {

            StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (StompCommand.SEND.equals(accessor.getCommand())) {
                log.info("Paywall WebSocket API Received unsupported SEND command.");
                return null;

            }
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                // Here free resources
                List destinations = accessor.getNativeHeader("destination");
                String destination = "NOT_SET";
                if(destinations != null && destinations.size() > 0){
                    destination = (String) destinations.get(0);
                }
            }
            if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
                // Here free resources
                List destinations = accessor.getNativeHeader("destination");
                String destination = "NOT_SET";
                if(destinations != null && destinations.size() > 0){
                    destination = (String) destinations.get(0);
                }
                unregisterListener(destination);
            }

            return message;
        }

    }

    /**
     * Method that unregisters the related preImageHash if queue name starts with
     * /queue/paywall/checksettlement/.
     * @param destinationQueue the full queue name containing the preImageHash
     */
    private void unregisterListener(String destinationQueue){
        if(destinationQueue.startsWith(CHECK_SETTLEMENT_QUEUE_PREFIX)) {
            String preImageHash = destinationQueue.substring(CHECK_SETTLEMENT_QUEUE_PREFIX.length());
            try {
                webSocketSettledPaymentHandler.unregisterPaymentListener(preImageHash);
            } catch (Exception e) {
                log.log(Level.SEVERE,"Paywall Internal Error Exception occurred when unregistering WebSocket listener: " + e.getMessage(),e);
            }
        }
    }
}
