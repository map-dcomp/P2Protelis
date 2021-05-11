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
package com.bbn.protelis.networkresourcemanagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import org.nustaq.serialization.FSTConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Properties used by the system at the global level.
 * 
 * @author jschewe
 *
 */
public final class GlobalNetworkConfiguration {

    private GlobalNetworkConfiguration() {
    }

    private static final Object INSTANCE_LOCK = new Object();

    private static GlobalNetworkConfiguration instance = null;

    /**
     *
     * @return the singleton instance
     */
    public static GlobalNetworkConfiguration getInstance() {
        if (null == instance) {
            synchronized (INSTANCE_LOCK) {
                if (null == instance) {
                    instance = new GlobalNetworkConfiguration();
                }
                return instance;
            }
        }
        return instance;
    }

    /**
     * Read the configuration from the specified JSON file. Any properties not
     * specified in the file will have default values. It is important that this
     * is not called while any network serialization objects exist.
     * 
     * @param path
     *            the path to the file to read
     * @throws IOException
     *             if there is an error reading the file
     */
    public static void readFromFile(@Nonnull final Path path) throws IOException {
        synchronized (INSTANCE_LOCK) {
            final ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule())//
                    .registerModule(new ParameterNamesModule()) //
                    .registerModule(new Jdk8Module())//
                    .registerModule(new JavaTimeModule());
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                final GlobalNetworkConfiguration value = mapper.readValue(reader, GlobalNetworkConfiguration.class);
                instance = value;
            }
        }
    }

    /**
     * Reset all values back to their defaults. This is done by replacing the
     * value returned from {@link #getInstance()}. So any cases where the
     * instance is held will not see the new values.
     */
    public static void resetToDefaults() {
        synchronized (INSTANCE_LOCK) {
            // set to null, the next access will create a new instance with the
            // defaults
            instance = null;
        }
    }

    private static final double DEFAULT_MESSAGE_DROP_PERCENTAGE = 0;

    private double messageDropPercentage = DEFAULT_MESSAGE_DROP_PERCENTAGE;

    /**
     * If this number is greater than 0, then it is the percentage of messages
     * to drop to simulate network failures. The default value is 0.
     * 
     * @return the percentage of messages to drop
     */
    public double getMessageDropPercentage() {
        return messageDropPercentage;
    }

    /**
     * 
     * @param v
     *            see {@link #getMessageDropPercentage()}
     * @throws IllegalArgumentException
     *             if the value is not between 0 and 1.
     */
    public void setMessageDropPercentage(final double v) throws IllegalArgumentException {
        if (v < 0 || v > 1) {
            throw new IllegalArgumentException("Message drop percentage must be between 0 and 1");
        }
        messageDropPercentage = v;
    }

    /**
     * @return the FST serialization configuration for this thread
     */
    public FSTConfiguration getFstConfiguration() {
        return FSTConfiguration.createDefaultConfiguration();
    }

    private static final boolean USE_JAVA_SERIALIZATION_DEFAULT = false;

    private boolean useJavaSerialization = USE_JAVA_SERIALIZATION_DEFAULT;

    /**
     * 
     * @return if true, then use Java serialization for communication, otherwise
     *         use FST.
     */
    public boolean getUseJavaSerialization() {
        return useJavaSerialization;
    }

    /**
     * 
     * @param v
     *            {@link #getUseJavaSerialization()}
     */
    public void setUseJavaSerialization(final boolean v) {
        useJavaSerialization = v;
    }

    private static final boolean USE_COMPRESSION_DEFAULT = true;

    private boolean useCompression = USE_COMPRESSION_DEFAULT;

    /**
     * 
     * @return if true, then use compression for AP messages.
     */
    public boolean getUseCompression() {
        return useCompression;
    }

    /**
     * 
     * @param v
     *            {@link #getUseCompression()}
     */
    public void setUseCompression(final boolean v) {
        useCompression = v;
    }

    private static final boolean USE_DELTA_COMPRESSION_DEFAULT = true;

    private boolean useDeltaCompression = USE_DELTA_COMPRESSION_DEFAULT;

    /**
     * 
     * @return if true, then use delta compression for AP messages.
     */
    public boolean getUseDeltaCompression() {
        return useDeltaCompression;
    }

    /**
     * 
     * @param v
     *            {@link #getUseDeltaCompression()}
     */
    public void setUseDeltaCompression(final boolean v) {
        useDeltaCompression = v;
    }

}
