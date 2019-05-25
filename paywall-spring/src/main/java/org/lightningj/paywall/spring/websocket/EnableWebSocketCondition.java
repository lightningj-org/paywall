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

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import java.util.logging.Logger;

/**
 * Condition to check if paywall web socket functionality should be enabled.
 *
 * @author philip 2019-05-15
 */
public class EnableWebSocketCondition implements Condition {

    static Logger log = Logger.getLogger(EnableWebSocketCondition.class.getName());

    /**
     * Determine if the condition matches.
     *
     * @param context  the condition context
     * @param metadata metadata of the {@link AnnotationMetadata class}
     *                 or {@link MethodMetadata method} being checked
     * @return {@code true} if the condition matches and the component can be registered,
     * or {@code false} to veto the annotated component's registration
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String enableWebSocket =  context.getEnvironment().getProperty("paywall.websocket.enable");
        log.fine("Checking if paywall web socket should be registered: " + enableWebSocket);
        if(enableWebSocket != null){
            enableWebSocket = enableWebSocket.trim().toLowerCase();
            if(!enableWebSocket.equals("true") && !enableWebSocket.equals("false")){
                log.severe("Invalid paywall configuration, setting: 'paywall.websocket.enable' should be either 'true' or 'false', not '" + enableWebSocket + "'. Assuming false.");
            }
        }
        return enableWebSocket == null || enableWebSocket.equals("true");
    }
}
