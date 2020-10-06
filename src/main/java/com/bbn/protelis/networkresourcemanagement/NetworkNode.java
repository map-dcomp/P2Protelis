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

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Common functionality between {@link NetworkServer} and {@link NetworkClient}.
 * 
 */
public interface NetworkNode {

    /**
     * 
     * @return the ID of the object
     */
    @Nonnull
    NodeIdentifier getNodeIdentifier();

    /**
     * @return the name of the node
     */
    @Nonnull
    String getName();

    /**
     * @return the name of the region that this node currently belongs to, may
     *         be null
     */
    RegionIdentifier getRegionIdentifier();

    /**
     * Add a neighbor. If the neighbor node already exists, the bandwidth
     * capacity for the neighbor is replaced with the new value.
     * 
     * @param v
     *            the neighbor to add
     * @param bandwidth
     *            to the neighbor in bytes per second
     * @see #addNeighbor(NodeIdentifier, double)
     */
    void addNeighbor(@Nonnull NetworkNode v, double bandwidth);

    /**
     * The neighbors of this {@link NetworkServer}. Note that these IDs may
     * refer to either {@link NetworkServer} or {@link NetworkClient}.
     * 
     * @return unmodifiable set
     */
    @Nonnull
    Set<NodeIdentifier> getNeighbors();

    /**
     * 
     * @return the hardware platform for this node, may be null
     */
    String getHardware();

    /**
     * 
     * @param hardware
     *            the hardware platform for this node
     * @see #getHardware()
     */
    void setHardware(String hardware);

}
