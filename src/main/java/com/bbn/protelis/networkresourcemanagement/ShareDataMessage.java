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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.protelis.vm.CodePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Share AP data.
 * 
 * @author jschewe
 *
 */
public class ShareDataMessage extends ApMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareDataMessage.class);

    private final Map<CodePath, Object> data;

    private final byte[] encoded;

    /**
     * 
     * @return the data that is shared
     */
    public Map<CodePath, Object> getData() {
        return data;
    }

    /**
     * 
     * @param data
     *            the data to share in this message
     * @throws IOException
     *             if there is an error encoding the dataF
     */
    public ShareDataMessage(final Map<CodePath, Object> data) throws IOException {
        this.data = data;
        // encode in constructor so that this is only done once
        this.encoded = encodeData(this.data);
    }

    /**
     * Write this message to {@code stream}.
     * 
     * @param stream
     *            where to write the message
     * @throws IOException
     *             if there is an error writing to the stream
     */
    public void writeMessage(final DataOutputStream stream) throws IOException {
        LOGGER.trace("Sending message of size {}", encoded.length);

        stream.writeInt(encoded.length);
        stream.write(encoded);
    }

    private static final int MINIMUM_DATA_SIZE = 1;

    /**
     * 
     * @param stream
     *            where to read from
     * @return the message that was read
     * @throws IOException
     *             if there is an error reading from the stream
     * @throws StreamSyncLostException
     *             if the message is too small, signaling that the stream should
     *             be restarted
     */
    public static ShareDataMessage readMessage(final DataInputStream stream)
            throws IOException, StreamSyncLostException {
        final int size = stream.readInt();
        if (size < MINIMUM_DATA_SIZE) {
            throw new StreamSyncLostException("Message size is too small: " + size);
        }

        final byte[] bytes = new byte[size];
        stream.readFully(bytes);

        @SuppressWarnings("unchecked")
        final Map<CodePath, Object> data = decodeData(Map.class, bytes);
        return new ShareDataMessage(data);
    }

}
