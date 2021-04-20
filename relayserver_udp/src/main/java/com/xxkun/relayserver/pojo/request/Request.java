package com.xxkun.relayserver.pojo.request;

import com.xxkun.relayserver.component.exception.RequestResolutionException;
import com.xxkun.relayserver.pojo.RequestType;
import com.xxkun.udptransfer.TransferPacket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;

import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Date;

public final class Request {

    private final static int HEAD_LEN = 2 * Integer.BYTES;
    private final InetSocketAddress socketAddress;
    private final int clientVersion;
    private final RequestType type;
    private final TransferPacket.BodyBuffer bodyBuffer;

    private Request(TransferPacket.BodyBuffer buffer, int clientVersion, RequestType type, InetSocketAddress socketAddress) {
        this.bodyBuffer = buffer;
        this.type = type;
        this.socketAddress = socketAddress;
        this.clientVersion = clientVersion;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public int getBodyLength() {
        return bodyBuffer.getBodyLength() - HEAD_LEN;
    }

    public RequestType getType() {
        return type;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public TransferPacket.BodyBuffer getBodyBuffer() {
        bodyBuffer.position(HEAD_LEN);
        return bodyBuffer;
    }

    public static int getHeadLength() {
        return HEAD_LEN;
    }

    public static Request decodeFromByteArray(TransferPacket.BodyBuffer buffer, InetSocketAddress socketAddress) throws RequestResolutionException {
        if (buffer.getBodyLength() < HEAD_LEN) {
            throw new RequestResolutionException();
        }
        // clientVersion|type  ->  int|int
        int clientVersion = buffer.getInt();
        if (clientVersion < 0) {
            throw new RequestResolutionException();
        }
        int code = buffer.getInt();
        if (code < 0 || code >= RequestType.values().length) {
            throw new RequestResolutionException();
        }

        return new Request(buffer, clientVersion, RequestType.fromCode(code), socketAddress);
    }
}
