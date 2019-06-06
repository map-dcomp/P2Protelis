/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
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

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Parameters to start a container. See
 * {@link ResourceManager#startService(ServiceIdentifier, ContainerParameters)}
 * for usage.
 * 
 * @author jschewe
 *
 */
public final class ContainerParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param computeCapacity
     *            see {@link #getComputeCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     */
    public ContainerParameters(@JsonProperty("computeCapacity") final @Nonnull ImmutableMap<NodeAttribute<?>, Double> computeCapacity,
            @JsonProperty("networkCapacity") final @Nonnull ImmutableMap<LinkAttribute<?>, Double> networkCapacity) {
        this.computeCapacity = computeCapacity;
        this.networkCapacity = networkCapacity;
    }

    private final ImmutableMap<NodeAttribute<?>, Double> computeCapacity;

    /**
     * 
     * @return the compute capacity for the container
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getComputeCapacity() {
        return computeCapacity;
    }

    private final ImmutableMap<LinkAttribute<?>, Double> networkCapacity;

    /**
     * 
     * @return the network capacity for the container. The values are applied to
     *         all directly connected nodes.
     */
    @Nonnull
    public ImmutableMap<LinkAttribute<?>, Double> getNetworkCapacity() {
        return networkCapacity;
    }
}
