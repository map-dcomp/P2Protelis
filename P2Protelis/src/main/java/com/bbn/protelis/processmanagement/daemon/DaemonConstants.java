/**
 * 
 */
package com.bbn.protelis.processmanagement.daemon;

/**
 * @author Danilo Pianini
 *
 */
public final class DaemonConstants {
    
    public static int DEFAULT_PORT = 21999;
    public static long SLEEP_TIME = 500; // 500;
    public static long NBR_TIMEOUT = 10000; // drop neighbors not interacting within 10 seconds
    public static int DAEMON_PORT_OFFSET = 10000;   // For multiple processes per host, base the port on the Monitorable.
}
