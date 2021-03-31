package com.xxkun.relayserver_udp.component;

import com.xxkun.relayserver_udp.component.handler.HeartbeatMsgHandler;
import com.xxkun.relayserver_udp.component.handler.IMessageHandler;
import com.xxkun.relayserver_udp.component.handler.PunchMsgHandler;
import com.xxkun.relayserver_udp.component.queue.IMsgQueue;
import com.xxkun.relayserver_udp.dao.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSendHandler implements IMsgQueue.OnMessage{

    @Override
    public void onMessage(Message msg) {
        IMessageHandler messageHandler = msg.getType().getMessageHandler();
        if (messageHandler != null) {
            messageHandler.consume(msg);
        }
    }
}
