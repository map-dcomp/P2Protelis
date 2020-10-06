/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020>, <Raytheon BBN Technologies>
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

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Factory to create network resources. The classes here should not inherit from
 * each other. It is assumed that {@link NetworkClient}s and
 * {@link NetworkServer}s are unrelated types, except by the interface
 * {@link NetworkNode}. Breaking this assumption will likely have undesired
 * results.
 * 
 * @param <N>
 *            the type of {@link NetworkServer}s to create
 * @param <L>
 *            the type of {@link NetworkLink}s to create
 * @param <C>
 *            the type of {@link NetworkClient}s to create
 */
public interface NetworkFactory<N extends NetworkServer, L extends NetworkLink, C extends NetworkClient> {

    /**
     * Create a node.
     * 
     * @param name
     *            the name of the node.
     * @param extraData
     *            any extra information that was read in about the the node.
     *            This can be used for setting additional properties.
     * @return the node. Not null.
     * @see NetworkServer#processExtraData(Map)
     */
    @Nonnull
    N createServer(@Nonnull NodeIdentifier name, @Nonnull Map<String, Object> extraData);

    /**
     * Create a link.
     * 
     * @param name
     *            name of the link
     * @param left
     *            the left node
     * @param right
     *            the right node
     * @param bandwidth
     *            the bandwidth for the link in mega bits per second
     * @param delay
     *            the link delay in milliseconds
     * @return the link. Not null.
     */
    @Nonnull
    L createLink(@Nonnull String name,
            @Nonnull NetworkNode left,
            @Nonnull NetworkNode right,
            double bandwidth,
            double delay);

    /**
     * Create a client.
     * 
     * @param name
     *            the name of the client.
     * @param extraData
     *            any extra information that was read in about the the client.
     *            This can be used for setting additional properties.
     * @return the node. Not null.
     * @see NetworkClient#processExtraData(Map)
     */
    @Nonnull
    C createClient(@Nonnull NodeIdentifier name, @Nonnull Map<String, Object> extraData);
}
