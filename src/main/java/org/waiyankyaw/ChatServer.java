package org.waiyankyaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class ChatServer {

    public final int PORT = 5000;
    private CountDownLatch serverReadyLatch;
    private List<PrintWriter> clientWriterList = new LinkedList<>();

    public ChatServer(CountDownLatch serverReadyLatch){
        this.serverReadyLatch = serverReadyLatch;
    }

    public void run(){
        var threadExecutor = Executors.newCachedThreadPool();
        try(var serverSocket = ServerSocketChannel.open()){
            try {
                serverSocket.bind(new InetSocketAddress("127.0.0.1", PORT));
                serverReadyLatch.countDown();
                while (serverSocket.isOpen()){
                    var clientSocket = serverSocket.accept();
                    var writer = new PrintWriter(Channels.newWriter(clientSocket, StandardCharsets.UTF_8), true);
                    clientWriterList.add(writer);
                    threadExecutor.submit(new ClientHandler(clientSocket));
                }
            } catch (AlreadyBoundException e){
                System.out.printf("ChatServer -> Port %d is already in use.\n", PORT);
            }
        } catch (IOException e){
            System.out.printf("ChatServer -> Cannot run the server, reason :  ", e.getMessage());
        }
    }

    private class ClientHandler implements Runnable{

        private String clientName;
        private SocketChannel clientSocket;
        private BufferedReader reader;

        public ClientHandler(SocketChannel clientSocket){
            this.clientSocket = clientSocket;
            reader = new BufferedReader(Channels.newReader(clientSocket, StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            try {
                String message = reader.readLine();
                if (message != null && !message.trim().isEmpty()) {
                    clientName = message.trim();
                    broadcastMessage(clientName, "Connected to the Server!");
                    System.out.printf("Client '%s' connected to the server. \n", clientName);
                } else {
                    clientSocket.close();
                    return;
                }
                while ((message = reader.readLine()) != null) {
                    broadcastMessage(clientName, message);
                }
            } catch (IOException e){
                System.out.println("ChatServer -> Connection error with client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcastMessage(String name, String message){
        for (var writer : clientWriterList){
            writer.printf("%s : %s\n", name, message);
        }
    }
}
