package com.xxkun.relayserver.component.queue;

import com.xxkun.relayserver.common.BaseThread;
import com.xxkun.relayserver.pojo.request.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class GetMessageQueue implements IMessageQueue {
    @Qualifier("GETMessageHandler")
    @Autowired
    private OnMessage onMessage;
    @Resource(name = "responseThreadPool")
    private ThreadPoolExecutor msgQueueThreadPool;

    private final LinkedBlockingQueue<Message> messageQueue;

    private final MessageListenThread messageListenThread;

    public GetMessageQueue() {
        messageQueue = new LinkedBlockingQueue<>();
        messageListenThread = new MessageListenThread();
        messageListenThread.start();
    }

    @Override
    public boolean sendMessage(Message message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        messageListenThread.close();
        if (!msgQueueThreadPool.isShutdown()) {
            msgQueueThreadPool.shutdown();
        }
    }

    class MessageListenThread extends BaseThread {
        @Override
        public void run() {
            while (!stop) {
                Message message = null;
                try {
                    message = messageQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (message != null) {
                    Message finalMessage = message;
                    msgQueueThreadPool.execute(() -> onMessage.onMessage(finalMessage));
                }
            }
        }
    }
}
