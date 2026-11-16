package org.waiyankyaw;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        CountDownLatch serverReadyLatch = new CountDownLatch(1);

        new Thread(() -> {
            ChatServer server = new ChatServer(serverReadyLatch);
            server.run();
        }).start();

        try {
            serverReadyLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted while waiting for server.");
            return;
        }

        try (var threadExecutor = Executors.newCachedThreadPool()) {
            // Add Client Servers Here
            threadExecutor.submit(() -> new ClientServer().run());
            threadExecutor.submit(() -> new ClientServer().run());
        }
    }
}