package org.lightningj.paywall.springboot2.test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriggerController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping("/triggerws1")
    public String trigger1(){

        // 1. Test to trigger before, connected
        // TTL

        // TODO figure out send to sender, sessionId = preImageHash, get communciation going
        // TODO Implement Interceptor
        // TODO Error Handling
        // TODO SockJS
        messagingTemplate.convertAndSend("/queue/reply/abc123","testmessage1");


        return "triggered1";
    }

    @RequestMapping("/triggerws2")
    public String trigger2(){

        messagingTemplate.convertAndSend("/queue/reply/abc234","testmessage2");

        return "triggered2";
    }
}
