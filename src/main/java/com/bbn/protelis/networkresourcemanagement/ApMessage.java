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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 * Base message for AP sharing.
 * 
 * @author jschewe
 *
 */
public abstract class ApMessage {

    /**
     * Write the message to {@code stream}.
     * 
     * @param stream
     *            where to write the message
     * @throws IOException
     *             if there is an error writing to the stream
     */
    public abstract void writeMessage(DataOutputStream stream) throws IOException;

    /**
     * Encode an object for transfer across a network. Use
     * {@link #decodeData(Class, byte[])} to decode the data.
     * 
     * @param data
     *            the object to encode, needs to be serializable
     * @return the data as bytes
     * @throws IOException
     *             if there is an error writing the data
     * @see #decodeData(Class, byte[])
     */
    public static byte[] encodeData(final Object data) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            try (OutputStream gz = getCompressionOutputStream(bytes)) {
                try (ObjectOutput oos = getSerializationOutputStream(gz)) {
                    oos.writeObject(data);
                } // serialization
            } // gzip
            return bytes.toByteArray();
        } // byte array
    }

    /**
     * Inverse of {@link #encodeData(Object)}.
     * 
     * @param bytes
     *            the data to decode
     * @return the decoded object
     * @throws IOException
     *             if there is an error reading the data
     * @throws StreamSyncLostException
     *             if something goes wrong decoding the data
     * @param clazz
     *            class definition for type of data to decode
     * @param <T>
     *            type of data being decoded
     */
    public static <T> T decodeData(final Class<T> clazz, final byte[] bytes)
            throws IOException, StreamSyncLostException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            try (InputStream gz = getCompressionInputStream(input)) {
                try (ObjectInput iis = getSerializationInputStream(gz)) {
                    try {
                        final Object incoming = iis.readObject();

                        if (clazz.isInstance(incoming)) {
                            final T data = clazz.cast(incoming);
                            return data;
                        } else {
                            throw new StreamSyncLostException("Got unexpected type of object: "
                                    + (null == incoming ? "NULL" : incoming.getClass().getName()));
                        }
                    } catch (final ClassNotFoundException e) {
                        throw new StreamSyncLostException("Unknown class received", e);
                    }
                } // serialization
            } // gzip
        } // byte array
    }

    private static ObjectOutput getSerializationOutputStream(final OutputStream output) throws IOException {
        if (GlobalNetworkConfiguration.getInstance().getUseJavaSerialization()) {
            return new ObjectOutputStream(output);
        } else {
            return new FSTObjectOutput(output, GlobalNetworkConfiguration.getInstance().getFstConfiguration());
        }
    }

    private static ObjectInput getSerializationInputStream(final InputStream input) throws IOException {
        if (GlobalNetworkConfiguration.getInstance().getUseJavaSerialization()) {
            return new ObjectInputStream(input);
        } else {
            return new FSTObjectInput(input, GlobalNetworkConfiguration.getInstance().getFstConfiguration());
        }
    }

    private static OutputStream getCompressionOutputStream(final OutputStream output) throws IOException {
        if (GlobalNetworkConfiguration.getInstance().getUseCompression()) {
            return new GZIPOutputStream(output);
        } else {
            return output;
        }
    }

    private static InputStream getCompressionInputStream(final InputStream input) throws IOException {
        if (GlobalNetworkConfiguration.getInstance().getUseCompression()) {
            return new GZIPInputStream(input);
        } else {
            return input;
        }
    }

}
