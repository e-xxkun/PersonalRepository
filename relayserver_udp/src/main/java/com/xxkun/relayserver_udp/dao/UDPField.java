package com.xxkun.relayserver_udp.dao;

import org.springframework.lang.NonNull;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class UDPField implements Delayed {

    private static final int MAX_RESEND_TIME = 4;

    public static final int HEAD = 0xFEFDDFEB;

    public static final int HEAD_LEN = 28;

    private Long seq;

    private Date sendDate;

    private Date receiveDate;

    private final InetSocketAddress socketAddress;

    private final Integer rtt;

    private final UDPFieldType type;

    private final Integer clientVersion;

    private BodyBuffer bodyBuffer;

    private final String id;

    private int resendTime = 0;

    private UDPField(ByteBuffer buffer, Long seq, Integer clientVersion, Integer cmdId, Integer rtt, Integer bodyLength, @NonNull InetSocketAddress socketAddress) {
        this.bodyBuffer = new BodyBuffer(buffer, bodyLength);
        this.seq = seq;
        this.socketAddress = socketAddress;
        this.rtt = rtt;
        this.type = UDPFieldType.valueOf(cmdId);
        this.clientVersion = clientVersion;
        this.id = this.socketAddress.toString() + this.seq;
    }

    public Integer getRtt() {
        return rtt;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public UDPFieldType getType() {
        return type;
    }

    public Integer getClientVersion() {
        return clientVersion;
    }

    public long getSendDate() {
        return sendDate.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UDPField udpField = (UDPField) o;
        return id.equals(udpField.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return getSendDate();
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void incResendTime() {
        ++ resendTime;
    }

    public boolean isMaxResendTime() {
        return resendTime >= MAX_RESEND_TIME;
    }

    public boolean isResend() {
        return resendTime > 0;
    }

    public boolean isACK() {
        return type.isACK();
    }

    public static UDPField decodeFromByteArray(byte[] bytes, InetSocketAddress socketAddress) {
        if (bytes.length < HEAD_LEN)
            return null;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        if (buffer.getInt() != HEAD)
            return null;
        long seq = buffer.getLong();

        int clientVersion = buffer.getInt();

        int cmdId = buffer.getInt();
        if (cmdId >= UDPFieldType.values().length)
            return null;

        int rtt = buffer.getInt();

        int bodyLength = buffer.getInt();

        return new UDPField(buffer, seq, clientVersion, cmdId, rtt, bodyLength, socketAddress);
    }

    public DatagramPacket encodeToDatagramPacket() {
        byte[] body = bodyBuffer.getByteArray();
        return new DatagramPacket(body, body.length, socketAddress);
    }

    public BodyBuffer getByteBuffer() {
        return bodyBuffer;
    }

    public class BodyBuffer {

        private int bodyLength;

        private ByteBuffer byteBuffer;

        public BodyBuffer(ByteBuffer byteBuffer, Integer bodyLength) {
            this.byteBuffer = byteBuffer;
            this.bodyLength = bodyLength;
        }

        public int getInt() {
            return bodyBuffer.getInt();
        }

        public boolean skip(int length) {
            if (length > getRemainLength()) {
                return false;
            }
            byteBuffer.position(byteBuffer.position() + length);
            return true;
        }

        public int getRemainLength() {
            return HEAD_LEN + bodyLength - byteBuffer.position();
        }

        public void reset() {
            byteBuffer.position(HEAD_LEN);
        }

        public String getString(int length) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0;i < length;i ++) {
                builder.append(byteBuffer.getChar());
            }
            return builder.toString();
        }

        public byte[] getByteArray() {
            return byteBuffer.array();
        }
    }

    public enum UDPFieldType {
        ACK(0) {
            @Override
            public boolean isACK() {
                return true;
            }
        },
        PUT(1),
        GET(2),
        REPLY(3),
        UNKNOWN(4);

        private final int cmdId;

        UDPFieldType(int cmdId) {
            this.cmdId = cmdId;
        }

        public static UDPFieldType valueOf(int cmdId) {
            if (cmdId > -1 && cmdId < UDPFieldType.values().length) {
                return UDPFieldType.values()[cmdId];
            }
            return UNKNOWN;
        }

        public int getCmdId() {
            return cmdId;
        }

        public boolean isACK() {
            return false;
        }
    }
}
