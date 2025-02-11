package Service;

import Calculations.Arithmetic;
import Calculations.Statistics;
import Constants.Commands;
import Exceptions.ArgumentException;
import Exceptions.OperationException;
import Logs.Logger;
import Logs.Logs;

import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerCCS {

    private final int bufferSize = 1500;
    private final int port;
    private final Map<Statistics, Integer> statistics = new ConcurrentHashMap<>();

    public ServerCCS(int port) {
        this.port = port;
        for (Statistics stat : Statistics.values())
            statistics.put(stat, 0);
    }

    public void run() {
        Logger.log("Start server...");
        Logger.sendStatus(Logs.DONE);
        ScheduledExecutorService periodicStatisticsReport = Executors.newSingleThreadScheduledExecutor();
        ExecutorService clientCalculationPool = Executors.newCachedThreadPool();
        try (DatagramSocket udpSocket = new DatagramSocket(port);
             ServerSocket tcpSocket = new ServerSocket(port)) {
            Logger.log("Set statistics schedule...");
            periodicStatisticsReport.scheduleAtFixedRate(
                () -> reportStatistics(), 10, 10, TimeUnit.SECONDS);
            Logger.sendStatus(Logs.DONE);
            Logger.log("Set clients discovery...");
            new Thread(() -> clientInit(udpSocket)).start();
            Logger.sendStatus(Logs.DONE);
            while (true) {
                Socket client = tcpSocket.accept();
                clientCalculationPool.submit(() -> {
                    try { clientDeal(client); }
                    catch (IOException e) { throw new RuntimeException(e); }
                });
                incrementStatistics(Statistics.CONNECTED_CLIENTS);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            clientCalculationPool.shutdown();
            periodicStatisticsReport.shutdown();
        }
    }

    private void clientInit(DatagramSocket udpSocket) {
        DatagramPacket requestPacket, responsePacket;
        while (true) {
            try {
                requestPacket = getPacket(udpSocket);
                String message = transformToTextMessage(requestPacket.getData());
                if (message.startsWith(Commands.DISCOVER.getDescription())) {
                    Logger.log("Client found...");
                    String response = Commands.FOUND.getDescription();
                    responsePacket = new DatagramPacket(response.getBytes(), response.length(),
                        requestPacket.getAddress(), requestPacket.getPort());
                    udpSocket.send(responsePacket);
                    Logger.sendStatus(Logs.CORRECT);
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
        byte[] cleanMessageArray = dropNulls(messageArray);
        StringBuilder text = new StringBuilder();
        for (byte b : cleanMessageArray)
            text.append((char) b);
        return text.toString();
    }

    private static byte[] dropNulls(byte[] arrayMessage) {
        int length = arrayMessage.length;
        int lastIndex = 1;
        for (int i = length - 1; i > 0; i--)
            if (arrayMessage[i] != 0x00) {
                lastIndex = i + 1;
                break;
            }
        return Arrays.copyOfRange(arrayMessage, 0, lastIndex);
    }

    private void clientDeal(Socket clientSocket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = reader.readLine()) != null) {
                incrementStatistics(Statistics.ATTEMPTS_COUNTER);
                String[] tokens = request.split(" ");
                int[] num = getNumberArguments(tokens);
                int result;
                try {
                    validateTokens(tokens);
                    Logger.log("Calculate (" + tokens[0] + " " + tokens[1] + " " + tokens[2] + ")");
                    Arithmetic operation = Arithmetic.getToken(tokens[0]);
                    result = arithmeticOperationResult(operation, num[0], num[1]);
                    Logger.sendStatus(Logs.DONE);
                } catch (ArgumentException | OperationException | ArithmeticException exception) {
                    Logger.sendStatus(Logs.ERROR);
                    incrementStatistics(Statistics.ERRORS_COUNTER);
                    Logger.log("Send error message...");
                    writer.println(Commands.ERROR.getDescription() + exception.getMessage());
                    Logger.sendStatus(Logs.DONE);
                    continue;
                } catch (NumberFormatException exception) {
                    Logger.sendStatus(Logs.ERROR);
                    String exceptionInfo = "Incorrect number format in arguments.";
                    incrementStatistics(Statistics.ERRORS_COUNTER);
                    writer.println(Commands.ERROR.getDescription() + exceptionInfo);
                    continue;
                }
                Logger.log("Send calculations (" + result + ")");
                writer.println(result);
                Logger.sendStatus(Logs.DONE);
                incrementStatistics(Statistics.SUCCESS_COUNTER);
                resultSumStatistics(result);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            Logger.log("Closing client...");
            if (!clientSocket.isClosed()) clientSocket.close();
            // decrementStatistics(Statistics.CONNECTED_CLIENTS);
            Logger.sendStatus(Logs.DONE);
        }
    }

    private void validateTokens(String[] tokens) throws ArgumentException {
        if (tokens.length != 3) {
            incrementStatistics(Statistics.ERRORS_COUNTER);
            throw new ArgumentException("Incorrect number of arguments.\n" +
                "Syntax is: <OPER> <ARG1> <ARG2>");
        }
    }

    private int[] getNumberArguments(String[] tokens) throws NumberFormatException {
        int[] number;
        number = new int[]{
        Integer.parseInt(tokens[1]),
        Integer.parseInt(tokens[2])};
        return number;
    }

    private int arithmeticOperationResult(Arithmetic operation, int num1, int num2)
        throws ArithmeticException, OperationException {
        int result;
        switch (operation) {
            case ADD: result = num1 + num2; break;
            case SUB: result = num1 - num2; break;
            case MUL: result = num1 * num2; break;
            case DIV: if (num2 == 0) throw new ArithmeticException("Division by zero.");
                result = num1 / num2; break;
            default: throw new OperationException("Invalid operation argument.");
        }
        return result;
    }

    private void incrementStatistics(Statistics stat) {
        statistics.merge(stat, 1,
            (oldValue, newValue) -> oldValue + newValue);
    }

    private void decrementStatistics(Statistics stat) {
        statistics.merge(stat, 1,
                (oldValue, newValue) -> oldValue - newValue);
    }

    private void resultSumStatistics(int value) {
        statistics.merge(Statistics.RESULT_SUM, value,
            (oldValue, newValue) -> oldValue + newValue);
    }

    private void reportStatistics() {
        Statistics.printSeparator(50);
        for (Map.Entry<Statistics, Integer> pair : statistics.entrySet())
            System.out.println("[STATISTICS] " + pair.getKey() + " " + pair.getValue());
        Statistics.printSeparator(50);
    }

}
