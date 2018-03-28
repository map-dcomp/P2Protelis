package com.bbn.protelis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple clock that just stores the time offset and then uses the system time.
 */
public class SimpleClock implements VirtualClock {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClock.class);

    private long offset = 0;
    /**
     * The time reported when stopped. This is initially 0, but will change to
     * the time that stop is called. This allows one to keep querying the clock
     * after it's stopped and time not go backwards. Note that this time already
     * has <code>offset</code> applied.
     */
    private long stoppedTime = 0;

    private boolean clockRunning = false;

    @Override
    public boolean isStarted() {
        synchronized (lock) {
            return clockRunning;
        }
    }

    private boolean shutdown = false;

    @Override
    public void shutdown() {
        synchronized (lock) {
            shutdown = true;
            clockRunning = false;
            stoppedTime = -1;
            lock.notifyAll();
        }
    }

    private final Object lock = new Object();

    @Override
    public void startClock() {
        synchronized (lock) {
            if (clockRunning) {
                throw new IllegalStateException("Clock is already running, cannot be started again until stopped");
            }
            if (shutdown) {
                throw new IllegalStateException("Clock has been shutdown, it can no longer be used.");
            }

            offset = System.currentTimeMillis();
            clockRunning = true;
            lock.notifyAll();
        }
    }

    @Override
    public void stopClock() {
        synchronized (lock) {
            if (shutdown) {
                throw new IllegalStateException("Clock has been shutdown, it can no longer be used.");
            }
            if (!clockRunning) {
                throw new IllegalStateException("Clock is not running, cannot be stopped");
            }

            stoppedTime = System.currentTimeMillis() - offset;
            clockRunning = false;

            // wake everyone up
            lock.notifyAll();
        }
    }

    @Override
    public long getCurrentTime() {
        synchronized (lock) {
            return internalGetCurrentTime();
        }
    }

    /**
     * Must hold the lock when calling this method.
     */
    private long internalGetCurrentTime() {
        if (clockRunning) {
            return System.currentTimeMillis() - offset;
        } else {
            return stoppedTime;
        }
    }

    @Override
    public void waitForClockStart() {
        synchronized (lock) {
            if (shutdown) {
                return;
            }

            while (!clockRunning && !shutdown) {
                try {
                    lock.wait();
                } catch (final InterruptedException e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Interrupted waiting for clock to start", e);
                    }
                }
            }
        }
    }

    @Override
    public void waitForDuration(final long duration) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Enter waitForDuration: " + duration);
        }
        synchronized (lock) {
            if (shutdown) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Returning because shutdown");
                }
                return;
            }
            if (!clockRunning) {
                throw new IllegalStateException("Cannot wait on stopped clock");
            }

            try {
                final long initialNow = internalGetCurrentTime();
                final long wakeTime = initialNow + duration;
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("wakeTime: " + wakeTime + " initialNow: " + initialNow);
                }

                while (clockRunning) {
                    final long now = internalGetCurrentTime();
                    final long remainingSleep = wakeTime - now;
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("remainingSleep: " + remainingSleep + " now: " + now);
                    }
                    if (remainingSleep <= 0) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Finished with sleep");
                        }

                        break;
                    } else {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Starting wait for: {} ms", remainingSleep);
                        }
                        try {
                            lock.wait(remainingSleep);
                        } catch (final InterruptedException e) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Interrupted during sleep", e);
                            }
                        }
                    }
                }
            } catch (final IllegalStateException e) {
                if (!shutdown) {
                    throw e;
                }
            }
        } // lock
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Exiting waitForDuration");
        }
    }

    @Override
    public void waitUntilTime(final long time) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("waitUntilTime: waiting for clock to start");
        }

        waitForClockStart();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("waitUntilTime: clock is started");
        }

        synchronized (lock) {
            if (shutdown) {
                return;
            }

            while (clockRunning) {
                final long remainingSleep;
                try {
                    final long now = internalGetCurrentTime();
                    remainingSleep = time - now;

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("remainingSleep: " + remainingSleep + " now: " + now);
                    }

                } catch (final IllegalStateException e) {
                    if (shutdown) {
                        return;
                    } else {
                        throw e;
                    }
                }
                if (remainingSleep > 0) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("waitUntilTime: waiting for " + remainingSleep);
                    }

                    try {
                        lock.wait(remainingSleep);
                    } catch (final InterruptedException e) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Interrupted during sleep", e);
                        }
                    }
                } else {
                    return;
                }
            }

        }
    }

    @Override
    public boolean isShutdown() {
        synchronized (lock) {
            return shutdown;
        }
    }
}
