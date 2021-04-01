package com.xxkun.relayserver.component.queue;

import com.xxkun.relayserver.dao.Message;

public interface IMessageQueue {

    boolean sendMessage(Message message);

    interface OnMessage {
        void onMessage(Message message);
    }
}