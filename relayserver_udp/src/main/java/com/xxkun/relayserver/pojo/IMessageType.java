package com.xxkun.relayserver.pojo;

import com.xxkun.relayserver.component.exception.MessageResolutionException;
import com.xxkun.relayserver.pojo.request.Message;
import com.xxkun.relayserver.pojo.request.Request;

public interface IMessageType extends IInnerMessageType {

    int getCode();

    Message createMessage(Request request) throws MessageResolutionException;
}