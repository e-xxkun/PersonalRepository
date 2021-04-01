package com.xxkun.relayserver.component;

import com.xxkun.relayserver.component.exception.RequestResolutionException;
import com.xxkun.relayserver.dao.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class RequestListener extends BaseThread {
    @Autowired
    private DatagramSocket receiver;
    @Autowired
    private OnRequest onRequest;
    @Resource(name = "requestThreadPool")
    private ThreadPoolExecutor requestThreadPool;

    @Override
    public void run() {
        while (!stop) {
            byte[] inBuff = new byte[Request.UDP_MSG_MAX_LEN];
            DatagramPacket packet = new DatagramPacket(inBuff, inBuff.length);
            try {
                receiver.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            requestThreadPool.execute(() -> {
                try {
                    Request request = Request.decodeFromByteArray(packet.getData(), (InetSocketAddress)packet.getSocketAddress());
                    request.setReceiveDate(new Date());
                    onRequest.onRequest(packet.getSocketAddress(), request);
                } catch (RequestResolutionException e) {
                    System.out.println("Invalid message from " + packet.getSocketAddress() + ":" + new String(packet.getData()));
                }
            });
        }
    }

    public interface OnRequest {
        void onRequest(SocketAddress from, Request request);
    }
}
