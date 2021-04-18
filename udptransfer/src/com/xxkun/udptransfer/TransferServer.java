package com.xxkun.udptransfer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransferServer implements TransferPool.OnPacketConfirmTimeout {

    public final static int MAX_TRANSFER_LEN = 512;

    private DatagramSocket socket;
    private TransferPool pool;

    private OnPacketReachMaxResendTime onPacketReachMaxResendTime;

    public TransferServer(int port) throws SocketException {
        socket = new DatagramSocket(port);
        init();
    }

    public TransferServer() throws SocketException {
        socket = new DatagramSocket();
        init();
    }

    private void init() {
        pool = new TransferPool();
        pool.setOnPacketConfirmTimeout(this);
    }

    public void send(TransferPacket packet) throws IOException {
        send(packet, true);
    }

    public void send(TransferPacket packet, boolean needACK) throws IOException {
        if (needACK) {
            packet.setSendTime(System.currentTimeMillis());
            pool.addUnconfirmedPacket(packet);
        }
        socket.send(packet.getDatagramPacket());
    }

    public TransferPacket receive() throws IOException {
        DatagramPacket packet = pool.createPacket();
        while (true) {
            socket.receive(packet);
            TransferPacket transferPacket = TransferPacket.decodeFromDatagramPacket(packet);
            if (transferPacket == null) {
                continue;
            }
            transferPacket.setReceiveTime(System.currentTimeMillis());
            if (transferPacket.isACK()) {
                pool.confirm(transferPacket);
            } else {
                sendACK(transferPacket);
                return transferPacket;
            }
        }
    }

    private void sendACK(TransferPacket packet) throws IOException {
        TransferPacket ackPacket = pool.createACKPacket(packet);
        send(ackPacket, false);
    }

    @Override
    public void onPacketConfirmTimeout(TransferPacket packet) {
        if (packet.isMaxResendTime()) {
            if (onPacketReachMaxResendTime != null) {
                onPacketReachMaxResendTime.onPacketReachMaxResendTime(packet);
            }
            return;
        }
        packet.incResendTime();
        try {
            send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnPacketReachMaxResendTime(OnPacketReachMaxResendTime onPacketReachMaxResendTime) {
        this.onPacketReachMaxResendTime = onPacketReachMaxResendTime;
    }

    public interface OnPacketReachMaxResendTime {
        void onPacketReachMaxResendTime(TransferPacket packet);
    }
}
