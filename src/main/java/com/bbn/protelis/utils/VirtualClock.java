/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
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
