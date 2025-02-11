package Client;

import Calculations.Arithmetic;
import Constants.Commands;
import Exceptions.FlagException;
import Logs.Dependencies.Site;
import Logs.Logger;
import Logs.Logs;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientCCS {

    private static final int bufferSize = 1500;
    private static int port;

    public static void main(String[] args) {
        Logger.setPrefix(Site.CLIENT);
        Logger.log("Validate args...");
        if (args.length != 1) {
            try { throw new FlagException("Incorrect number of flags.\n" +
                "Syntax is: java -jar ClientCCS.jar <port>");
            } catch (FlagException fe) {
                Logger.sendStatus(Logs.ERROR);
                System.err.println("Input values exception: " + fe.getMessage());
                return;
            }
        }
        Logger.sendStatus(Logs.CORRECT);
        port = Integer.parseInt(args[0]);
        InetAddress serverAddress;
        try {
            Logger.log("Discover CCS server...");
            serverAddress = serverInit(port);
            Logger.sendStatus(Logs.DONE);
        } catch (IOException ioe) {
            Logger.sendStatus(Logs.ERROR);
            System.err.println("\nFailed to discover server: " + ioe.getMessage());
            return;
        }
        Logger.log("Connect to server...");
        try (Socket tcpSocket = new Socket(serverAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(tcpSocket.getOutputStream(), true)) {
            Logger.sendStatus(Logs.DONE);
            Random random = new Random();
            while (true) {
                Arithmetic operation = Arithmetic.getRandom(random);
                int arg1 = random.nextInt(10);
                int arg2 = random.nextInt(10);
                String request = operation + " " + arg1 + " " + arg2;
                Logger.log("Sending request: (" + request + ")");
                writer.println(request);
                Logger.sendStatus(Logs.CORRECT);
                String response = reader.readLine();
                Logger.log("Response: " + response + " ");
                if (request.startsWith("ERROR")) Logger.sendStatus(Logs.ERROR);
                else Logger.sendStatus(Logs.RECEIVED);
                TimeUnit.SECONDS.sleep(random.nextInt(4) + 1);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during client-server communication: " + e.getMessage());
        }
    }

    private static InetAddress serverInit(int port) throws IOException {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            udpSocket.setBroadcast(true);
            byte[] sendData = Commands.DISCOVER.getDescription().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                InetAddress.getByName("localhost"), port);
            udpSocket.send(sendPacket);
            byte[] receiveBuffer = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            udpSocket.setSoTimeout(5000);
            udpSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (Commands.FOUND.getDescription().equals(response.trim()))
                return receivePacket.getAddress();
            else throw new IOException("Invalid response from server.");
        }
    }
}
