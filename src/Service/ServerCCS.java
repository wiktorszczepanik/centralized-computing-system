package Service;

import Constants.Arithmetic;
import Constants.Commands;
import Constants.Statistics;
import Exceptions.ArgumentException;
import Exceptions.OperationException;

import javax.swing.*;
import java.io.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerCCS {

    private final int bufferSize = 1500;
    private final int port;
    private final Map<Statistics, Integer> statistics
        = new ConcurrentHashMap<>();

    public ServerCCS(int port) {
        this.port = port;
        for (Statistics stat : Statistics.values())
            statistics.put(stat, 0);
    }

    public void run() {
        ScheduledExecutorService periodicStatisticsReport = Executors.newSingleThreadScheduledExecutor();
        ExecutorService clientCalculationPool = Executors.newCachedThreadPool();

        try (DatagramSocket udpSocket = new DatagramSocket(port);
             ServerSocket tcpSocket = new ServerSocket(port)) {
            periodicStatisticsReport.scheduleAtFixedRate(
                () -> reportStatistics(), 10, 10, TimeUnit.SECONDS);
            new Thread(() -> clientInit(udpSocket)).start();
            while (true) {
                Socket client = tcpSocket.accept();
                clientCalculationPool.submit(() -> {
                    try { clientDeal(client);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
                incrementStats(Statistics.CONNECTED_CLIENTS);
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            clientCalculationPool.shutdown();
            periodicStatisticsReport.shutdown();
        }
    }

    private void clientInit(DatagramSocket udpSocket) {
        byte[] buffer = new byte[bufferSize];
        DatagramPacket requestPacket, responsePacket;
        while (true) {
            try { requestPacket = getPacket(udpSocket);
                String message = transformToTextMessage(requestPacket.getData());
                if (message.startsWith(Commands.DISCOVER.getDescription())) {
                    String response = Commands.FOUND.getDescription();
                    responsePacket = new DatagramPacket(response.getBytes(), response.length(),
                        requestPacket.getAddress(), requestPacket.getPort());
                    udpSocket.send(responsePacket);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private DatagramPacket getPacket(DatagramSocket udpSocket) throws IOException {
        byte[] buffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
        udpSocket.receive(packet);
        return packet;
    }

    private String transformToTextMessage(byte[] messageArray) {
        StringBuilder text = new StringBuilder();
        for (byte b : messageArray)
            text.append((char) b);
        return text.toString();
    }

    private void clientDeal(Socket clientSocket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = reader.readLine()) != null) {
                incrementStats(Statistics.ATTEMPTS_COUNTER);
                String[] tokens = request.split(" ");
                int[] num = getNumberArguments(tokens);
                int result;
                try {
                    validateTokens(tokens);
                    Arithmetic operation = Arithmetic.getToken(tokens[0]);
                    switch (operation) {
                        case ADD: result = num[0] + num[1]; break;
                        case SUB: result = num[0] - num[1]; break;
                        case MUL: result = num[0] * num[1]; break;
                        case DIV: if (num[1] == 0) throw new ArithmeticException("Division by zero.");
                            result = num[0] / num[1]; break;
                        default: throw new OperationException("Invalid operation argument.");
                    }
                } catch (ArgumentException | OperationException | ArithmeticException exception) {
                    System.err.println(exception.getMessage());
                    incrementStats(Statistics.ERRORS_COUNTER);
                    continue;
                } catch (NumberFormatException exception) {
                    System.err.println("Incorrect number format in arguments.");
                    incrementStats(Statistics.ERRORS_COUNTER);
                    continue;
                }
                writer.println(result);
                incrementStats(Statistics.SUCCESS_COUNTER);
                resultSumStats(result);
            }
        } catch (IOException exception) {
            throw exception;
        }
    }

    private void validateTokens(String[] tokens) throws ArgumentException {
        if (tokens.length != 3) {
            incrementStats(Statistics.ERRORS_COUNTER);
            throw new ArgumentException("Incorrect number of arguments.\n" +
                "Syntax is: <OPER> <ARG1> <ARG2>");
        }
    }

    private int[] getNumberArguments(String[] tokens) throws NumberFormatException {
        int[] number;
        try {
            number = new int[]{
            Integer.parseInt(tokens[1]),
            Integer.parseInt(tokens[2])};
        } catch (NumberFormatException exception) {
            throw exception;
        }
        return number;
    }

    private void incrementStats(Statistics stat) {
        statistics.merge(stat, 1, (oldValue, newValue) -> oldValue + newValue);
    }

    private void resultSumStats(int value) {
        statistics.merge(Statistics.RESULT_SUM, value,
            (oldValue, newValue) -> oldValue + newValue);
    }

    private void reportStatistics() {
        for (Map.Entry<Statistics, Integer> pair : statistics.entrySet())
            System.out.println(pair);
    }

}
