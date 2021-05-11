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

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Information about the services running on a node.
 * 
 * @author jschewe
 *
 */
public class ServiceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param nodeName
     *            see {@link #getNodeName()}
     * @param timestamp
     *            see {@link #getTimestamp()}
     * @param serviceState
     *            see {@link #getServiceState()}
     */
    public ServiceReport(@JsonProperty("nodeName") @Nonnull final NodeIdentifier nodeName,
            @JsonProperty("timestamp") final long timestamp,
            @JsonProperty("serviceState") @Nonnull final ImmutableMap<NodeIdentifier, ServiceState> serviceState) {
        this.nodeName = nodeName;
        this.timestamp = timestamp;
        this.serviceState = serviceState;
        this.hashCode = Objects.hash(this.nodeName, this.serviceState);
    }

    private final NodeIdentifier nodeName;

    /**
     * @return the identifier of the node that the report came from
     */
    @Nonnull
    public final NodeIdentifier getNodeName() {
        return nodeName;
    }

    private final long timestamp;

    /**
     * The units of the timestamp are determined by the clock used for the
     * network. Possible examples may be milliseconds since the epoch or
     * milliseconds since the start of the application. It is not expected that
     * this time be converted to a date time for display to the user. This value
     * is used to differentiate 2 reports for the same node taken at different
     * times.
     * 
     * @return when the report was generated
     */
    public long getTimestamp() {
        return timestamp;
    }

    private final ImmutableMap<NodeIdentifier, ServiceState> serviceState;

    /**
     * State of all containers on a node.
     * 
     * @return container id -> state for it's services
     */
    @Nonnull
    public ImmutableMap<NodeIdentifier, ServiceState> getServiceState() {
        return serviceState;
    }

    /**
     * Ignores timestamp differences.
     */
    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass().equals(getClass())) {
            final ServiceReport other = (ServiceReport) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return getNodeName().equals(other.getNodeName()) && getServiceState().equals(other.getServiceState());
            }
        } else {
            return false;
        }
    }

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "{" + " node: " + getNodeName() + " serviceState: " + getServiceState() + "}";
    }

}
