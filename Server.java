// Server Class to create Server (localhost) for Client to connect to and chat with

import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.Cipher;

public class Server {
    public static void main(String[] args) {
        try {
            printStatements();

            // Generate RSA Key Pair
            KeyPair keyPair = generateKeyPair();

            // Create Server Socket
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server is Running! Seraching for Clients...");

            while (true) {
                // Wait for and Connect to Client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Connected!");

                // Exchange Public Keys
                PublicKey clientPublicKey = receivePublicKey(clientSocket);
                sendPublicKey(clientSocket, keyPair.getPublic());
                System.out.println("Public Keys Successfully Exchanged!");
                System.out.println("------------------DEBUG-----------------");
                System.out.println("Server Public Key: " + keyPair.getPublic());
                System.out.println("Client Public Key: " + clientPublicKey);

                // Handle Chat Communication with Client
                System.out.println("----------------------------------------");
                handleChatCommunication(clientSocket, keyPair.getPrivate(), clientPublicKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printStatements() {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        System.out.println("===================================");
        System.out.println("     RSA-Encrypted Chat: Server    ");
        System.out.println("===================================");
    }

    // Generates RSA Key Pair
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // Receives Public Key from Client
    private static PublicKey receivePublicKey(Socket socket) throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        byte[] keyBytes = new byte[dis.readInt()];
        dis.readFully(keyBytes);
        return KeyUtils.convertBytesToPublicKey(keyBytes);
    }

    // Sends Public Key to Client
    private static void sendPublicKey(Socket socket, PublicKey publicKey) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        byte[] keyBytes = KeyUtils.convertPublicKeyToBytes(publicKey);
        dos.writeInt(keyBytes.length);
        dos.write(keyBytes);
    }

    // Decrypts Message sent by Client
    private static String decryptMessage(byte[] encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    // Encrypts Message to be sent to Client
    private static byte[] encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    // Handles Console Chat Communication with Client
    private static void handleChatCommunication(Socket socket, PrivateKey privateKey, PublicKey clientPublicKey) throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        System.out.println("Waiting for Client Message...");
        try {
            while (true) {
                // Receive Encrypted Message
                int length = dis.readInt();
                byte[] encryptedMessage = new byte[length];
                dis.readFully(encryptedMessage);

                // Decrypt Message
                String decryptedMessage = decryptMessage(encryptedMessage, privateKey);
                System.out.println("\n>>> Client: " + decryptedMessage);
                System.out.println("Encrypted Msg Receieved: " + new String(encryptedMessage)); // COMMENT OUT TO REMOVE ENCRYPTED MSGS

                // Check if the client wants to leave the chat
                if (decryptedMessage.equalsIgnoreCase("exit")) {
                    System.out.println("Client has left the chat.");
                    break; // Exit the loop and close the connection
                }

                // Send Response
                System.out.print("\n>>> Server: ");
                String response = System.console().readLine();
                byte[] encryptedResponse = encryptMessage(response, clientPublicKey);
                dos.writeInt(encryptedResponse.length);
                dos.write(encryptedResponse);
                System.out.println("Encrypted Msg Sent: " +  new String(encryptedResponse)); // COMMENT OUT TO REMOVE ENCRYPTED MSGS
            }
        } finally {
            // Close the socket after leaving the chat
            socket.close();
        }
    }
}
