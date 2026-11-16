package org.waiyankyaw;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ClientServer {

    private boolean isNameSet = false;
    private JTextField message;
    private JTextArea chatArea;
    private PrintWriter writer;

    public void run() {
        var screen = new JFrame("Group Chat");
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        chatArea = new JTextArea(20, 40);
        chatArea.setFocusable(false);
        chatArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        chatArea.append("\tPlease submit your name to connect to the server\n");
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        var messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));

        message = new JTextField();
        message.setBorder(new LineBorder(Color.BLACK, 1, true));
        JButton button = new JButton("Submit");
        button.addActionListener(event -> sendData());
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        messagePanel.add(message);
        messagePanel.add(Box.createHorizontalStrut(5));
        messagePanel.add(button);

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        panel.add(messagePanel);

        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screen.setLocationRelativeTo(null);
        screen.getContentPane().add(panel);
        screen.pack();
        screen.setVisible(true);
    }

    private void connectServer(){
        var threadExecutor = Executors.newSingleThreadExecutor();
        try {
            var server = SocketChannel.open(new InetSocketAddress("127.0.0.1", 5000));
            var reader = new BufferedReader(Channels.newReader(server, StandardCharsets.UTF_8));
            writer = new PrintWriter(Channels.newWriter(server, StandardCharsets.UTF_8), true);
            writer.println(message.getText().trim());
            message.setText("");
            isNameSet = true;

            threadExecutor.submit(() -> {
                String message;
                while (true) {
                    try {
                        while ((message = reader.readLine()) != null) {
                            chatArea.append(message + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e){
            System.out.println("Client --> Cannot connect to server, " + e.getMessage());
        } finally {
            threadExecutor.shutdown();
        }
    }

    private void sendData(){
        if (!message.getText().isEmpty()) {
            if (!isNameSet) {
                connectServer();
            } else {
                writer.println(message.getText().trim());
                message.setText("");
                message.requestFocus();
            }
        }
    }
}
