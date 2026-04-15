package br.com.systemdesign.urlshortening.util;

import lombok.experimental.UtilityClass;
import lombok.CustomLog;

/**
 * Utility class for generating short URLs using Snowflake ID algorithm
 * Snowflake ID structure: 1 sign bit | 41 timestamp bits | 10 worker bits | 12 sequence bits
 */
@UtilityClass
@CustomLog
public class ShortUrlGenerator {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Snowflake ID bits
    private static final long EPOCH = 1_609_459_200_000L; // 2021-01-01 00:00:00 UTC
    private static final int WORKER_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    private static final int WORKER_ID = getWorkerId();
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    /**
     * Generates a distributed unique ID using Snowflake algorithm and encodes to base62
     */
    public static String generate() {
        long id = generateSnowflakeId();
        return encodeToBase62(id);
    }

    /**
     * Generates a Snowflake ID
     */
    private static synchronized long generateSnowflakeId() {
        long timestamp = System.currentTimeMillis() - EPOCH;

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Sequence overflow, wait for next millisecond
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp << TIMESTAMP_SHIFT) |
               ((long) WORKER_ID << WORKER_ID_SHIFT) |
               sequence;
    }

    /**
     * Waits until the next millisecond
     */
    private static long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }

    /**
     * Gets worker ID from machine hostname or environment variable
     */
    private static int getWorkerId() {
        try {
            String workerId = System.getenv("WORKER_ID");
            if (workerId != null) return Integer.parseInt(workerId) & (int) MAX_WORKER_ID;

            // Use hostname hash as fallback
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            return (Math.abs(hostname.hashCode()) % (int) (MAX_WORKER_ID + 1));
        } catch (Exception e) {
            log.warn("Could not determine worker ID, using random value", e);
            return (int) (Math.random() * (MAX_WORKER_ID + 1));
        }
    }

    /**
     * Encodes a given number to base62 string
     */
    public static String encodeToBase62(long num) {
        if (num == 0) return "0";
        var sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

}
