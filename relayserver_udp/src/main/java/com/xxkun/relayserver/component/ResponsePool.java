package com.xxkun.relayserver.component;

import com.xxkun.relayserver.dao.Response;
import com.xxkun.relayserver.dao.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

@Component
public class ResponsePool {
    @Autowired
    private OnResponseTimeout onResponseTimeout;

    private final DelayQueue<Response> responseQueue;

    private final ConcurrentHashMap<InetSocketAddress, UserClient> clientMap;

    private final ResponseTimeoutListenThread timeoutListenThread;

    public ResponsePool() {
        responseQueue = new DelayQueue<>();
        clientMap = new ConcurrentHashMap<>();
        timeoutListenThread = new ResponseTimeoutListenThread();
        timeoutListenThread.start();
    }

    public void add(Response response) {
        responseQueue.add(response);
        UserClient client = clientMap.get(response.getSocketAddress());
        if (client == null) {
            client = new UserClient(response.getSocketAddress());
            clientMap.put(response.getSocketAddress(), client);
        }
        if (!response.isResend()) {
            response.setSequence(client.getCurSequence());
        }
        client.addResponse(response);
    }

    public void ack(Response response) {
        UserClient client = clientMap.get(response.getSocketAddress());
        if (client != null) {
//            responseQueue.remove(response);
            boolean res = client.removeResponse(response);
            if (client.responseCount() == 0) {
                clientMap.remove(client.getSocketAddress());
            }
        }
    }

    public boolean isTimeout(Response response) {
        UserClient client = clientMap.get(response.getSocketAddress());
        if (client != null) {
            return client.containsResponse(response);
        }
        return true;
    }

    public void close() {
        timeoutListenThread.close();
    }

    public class ResponseTimeoutListenThread extends BaseThread {
        @Override
        public void run() {
            while (stop) {
                try {
                    Response response = responseQueue.take();
                    if (isTimeout(response)) {
                        onResponseTimeout.onResponseTimeout(response);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public interface OnResponseTimeout {
        void onResponseTimeout(Response response);
    }
}
