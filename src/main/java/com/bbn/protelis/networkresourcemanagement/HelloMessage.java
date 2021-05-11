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

/**
 * Sent to initiate a connection.
 * 
 * @author jschewe
 *
 */
public class HelloMessage extends ApMessage {

    private final NodeIdentifier id;

    /**
     * @return the id of the sender
     */
    public NodeIdentifier getId() {
        return id;
    }

    private final int nonce;

    /**
     * @return the nonce used to break loops
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * 
     * @param id
     *            see {@link #getId()}
     * @param nonce
     *            see {@link #getNonce()}
     */
    public HelloMessage(final NodeIdentifier id, final int nonce) {
        this.id = id;
        this.nonce = nonce;
    }

    @Override
    public void writeMessage(final DataOutputStream stream) throws IOException {
        final byte[] bytes = encodeData(id);

        stream.writeInt(bytes.length);
        stream.write(bytes);
        stream.writeInt(nonce);
    }

    private static final int MINIMUM_ID_SIZE = 1;

    /**
     * 
     * @param stream
     *            where to read the message from
     * @return the message that was read from the stream
     * @throws IOException
     *             if there is an error reading from the stream
     * @throws StreamSyncLostException
     *             if the message is too small, signaling that the stream should
     *             be restarted
     */
    public static HelloMessage readMessage(final DataInputStream stream) throws IOException, StreamSyncLostException {
        final int size = stream.readInt();
        if (size < MINIMUM_ID_SIZE) {
            throw new StreamSyncLostException("Message size is too small: " + size);
        }

        final byte[] bytes = new byte[size];
        stream.readFully(bytes);
        final NodeIdentifier id = decodeData(NodeIdentifier.class, bytes);

        final int nonce = stream.readInt();

        return new HelloMessage(id, nonce);
    }

}
