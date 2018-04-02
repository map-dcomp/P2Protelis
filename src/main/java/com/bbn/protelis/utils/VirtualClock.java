package com.bbn.protelis.utils;

/**
 * A clock abstraction to make working with time easier. Times are relative to
 * when {@link VirtualClock#startClock()} is executed.
 * 
 */
public interface VirtualClock {

    /**
     * Start the clock. The clock will start at zero.
     * 
     * @throws IllegalStateException
     *             if the clock is already started
     */
    void startClock() throws IllegalStateException;

    /**
     * @return the current time of the clock. Always greater than or equal to 0.
     */
    long getCurrentTime();

    /**
     * 
     * @return has the clock been started and is not currently stopped.
     */
    boolean isStarted();

    /**
     * Stop the clock and wake up all waits.
     * 
     * @see #waitForDuration(long)
     * @throws IllegalStateException
     *             if the clock has already been stopped
     */
    void stopClock() throws IllegalStateException;

    /**
     * Wait for the specified duration. May be woken up early if the clock is
     * stopped.
     * 
     * @param duration
     *            the amount of time to wait for
     * @throws IllegalStateException
     *             if the clock is currently stopped
     */
    void waitForDuration(long duration) throws IllegalStateException;

    /**
     * Shutdown the clock. Once shutdown it cannot be started.
     */
    void shutdown();

    /**
     * 
     * @return true if shutdown has been called.
     */
    boolean isShutdown();

    /**
     * Wait for the clock to start or shutdown to be called.
     * 
     * @see #startClock()
     * @see #shutdown()
     */
    void waitForClockStart();

    /**
     * Wait until the clock reaches the specified time or the clock is shutdown.
     * May be called before the clock is started
     * 
     * @param time
     *            the time to wait for.
     */
    void waitUntilTime(long time);

}
