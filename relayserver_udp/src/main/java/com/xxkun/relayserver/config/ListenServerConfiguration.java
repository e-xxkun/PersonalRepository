package com.xxkun.relayserver.config;

import com.xxkun.udptransfer.TransferServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ListenServerConfiguration {

    @Value("${udpserver.common.port}")
    private int port;

    @Bean
    public TransferServer serverSocket() {
        TransferServer serverSocket = null;
        try {
             serverSocket = new TransferServer(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }

    @Bean(value = "requestThreadPool")
    public ThreadPoolExecutor requestThreadPool() {
        return new ThreadPoolExecutor(6, 15, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Bean(value = "responseThreadPool")
    public ThreadPoolExecutor responseThreadPool() {
        return new ThreadPoolExecutor(6, 15, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }
}
