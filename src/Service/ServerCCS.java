package Service;


import Constants.ServerStatistics;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerCCS {

    private final int bufferSize = 1500;
    private final int port;
    private final Map<ServerStatistics, Integer> statistics
        = new ConcurrentHashMap<>();

    public ServerCCS(int port) {
        this.port = port;
        for (ServerStatistics stat : ServerStatistics.values())
            statistics.put(stat, 0);
    }

    public void run() {
        ScheduledExecutorService periodicStatisticsReport = Executors.newSingleThreadScheduledExecutor();
        ExecutorService clientCalculationPool = Executors.newCachedThreadPool();

        try (DatagramSocket udpSocket = new DatagramSocket(port);
             ServerSocket tcpSocket = new ServerSocket(port)) {
            // next step
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            clientCalculationPool.shutdown();
            periodicStatisticsReport.shutdown();
        }
    }

    private void clientInit(DatagramSocket udpSocket) {

    }

}
