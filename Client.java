// Client class to connect to Server and chat with

import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.Cipher;

public class Client {
    public static void main(String[] args) {
        try {
            printStatements();

            // Generate RSA Key Pair
            KeyPair keyPair = generateKeyPair(); 

            // Connect to Server Socket
            System.out.println("Searching for Server...");
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Connected to Server!");

            // Exchange Public Keys
            sendPublicKey(socket, keyPair.getPublic());
            PublicKey serverPublicKey = receivePublicKey(socket);
            System.out.println("Public Keys Successfully Exchanged!");
            System.out.println("------------------DEBUG-----------------");
            System.out.println("Server Public Key: " + serverPublicKey);
            System.out.println("Client Public Key: " + keyPair.getPublic());

            // Handle Chat Communication with Server
            System.out.println("----------------------------------------");
            handleChatCommunication(socket, keyPair.getPrivate(), serverPublicKey);
        } catch (ConnectException e) {
            System.out.println("Failed to connect to the server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printStatements() {
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        System.out.println("===================================");
        System.out.println("     RSA-Encrypted Chat: Client    ");
        System.out.println("===================================");
    }

    // Generates RSA Key Pair
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); 
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // Sends Public Key to Server
    private static void sendPublicKey(Socket socket, PublicKey publicKey) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        byte[] keyBytes = KeyUtils.convertPublicKeyToBytes(publicKey);
        dos.writeInt(keyBytes.length);
        dos.write(keyBytes);
    }

    // Receives Public Key from Server
    private static PublicKey receivePublicKey(Socket socket) throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        byte[] keyBytes = new byte[dis.readInt()];
        dis.readFully(keyBytes);
        return KeyUtils.convertBytesToPublicKey(keyBytes);
    }

    // Decrypts Message sent by Server
    private static String decryptMessage(byte[] encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    // Encrypts Message to be sent to Server
    private static byte[] encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    // Handles Console Chat Communication with Server
    private static void handleChatCommunication(Socket socket, PrivateKey privateKey, PublicKey serverPublicKey) throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        try {
            while (true) {
                // Send Message
                System.out.print("\n>>> Client: ");
                String message = System.console().readLine();
                byte[] encryptedMessage = encryptMessage(message, serverPublicKey);
                dos.writeInt(encryptedMessage.length);
                dos.write(encryptedMessage);
                System.out.println("Encrypted Msg Sent: " + new String(encryptedMessage)); // COMMENT OUT TO REMOVE ENCRYPTED MSGS

                // Check if the user wants to leave the chat
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Leaving the chat.");
                    break;
                }

                // Receive Encrypted Message
                int length = dis.readInt();
                byte[] encryptedResponse = new byte[length];
                dis.readFully(encryptedResponse);

                // Decrypt Message
                String decryptedResponse = decryptMessage(encryptedResponse, privateKey);
                System.out.println("\n>>> Server: " + decryptedResponse);
                System.out.println("Encrypted Msg Receieved: " + new String(encryptedResponse)); // COMMENT OUT TO REMOVE ENCRYPTED MSGS
            }
        } finally {
            // Close the socket after leaving the chat
            socket.close();
        }
    }
}
