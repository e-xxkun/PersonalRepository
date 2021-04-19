package com.xxkun.relayserver.send;

import com.xxkun.relayserver.component.exception.ResponseConvertException;
import com.xxkun.relayserver.pojo.response.Response;
import com.xxkun.udptransfer.TransferPacket;
import com.xxkun.udptransfer.TransferServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

@Component
public class ResponseSender implements ResponsePool.OnResponseTimeout {

    @Autowired
    private TransferServer sender;
    @Autowired
    private ResponsePool responsePool;

    public void send(Response response, boolean needAck) {
        try {
            if (needAck) {
                response.setSendDate(new Date());
                responsePool.add(response);
            }
            TransferPacket packet = new TransferPacket(response.getBodyBuffer(), response.getSocketAddress());
            sender.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(Response response) {
        send(response, true);
        if (response.hashNext()) {
            response = response.next();
            send(response, true);
        }
    }

    @Override
    public void onResponseTimeout(Response response) {
        System.out.println("RESEND: " + response);
        resend(response);
    }

    private void resend(Response response) {
        if (response.isMaxResendTime()) {
            return;
        }
        response.incResendTime();
        send(response);
    }
}
