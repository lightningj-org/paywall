package org.lightningj.paywall.springboot2;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class CheckSettlementWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

        System.out.println("message: " + message.getPayload());
        try {
            session.sendMessage(new TextMessage("hello"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
