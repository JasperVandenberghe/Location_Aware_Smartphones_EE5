package be.groept.emedialab.client;

public class LatencyTest {
    /**
     * Start time in nanoseconds.
     */
    public static long startTime;

    /**
     *
     * @return Latency in milliseconds.
     */
    public static long getLatency(){
        return (System.nanoTime() - LatencyTest.startTime) / 1000000; //1 millisecond = 1000 000 nanoseconds.
    }
}
