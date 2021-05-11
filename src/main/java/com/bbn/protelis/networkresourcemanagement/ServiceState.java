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

/**
 * The state of a service in a container.
 * 
 * @author jschewe
 *
 */
public class ServiceState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param service
     *            see {@link #getService()}
     * @param status
     *            see {@link #getStatus()}
     */
    public ServiceState(@JsonProperty("service") @Nonnull final ServiceIdentifier<?> service,
            @JsonProperty("status") @Nonnull final ServiceStatus status) {
        this.service = service;
        this.status = status;
        this.hashCode = Objects.hash(this.service, this.status);
    }

    private final ServiceIdentifier<?> service;

    /**
     * 
     * @return the service that this information is for
     */
    @Nonnull
    public ServiceIdentifier<?> getService() {
        return service;
    }

    private final ServiceStatus status;

    /**
     * 
     * @return the current status of the service
     */
    @Nonnull
    public ServiceStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o.getClass().equals(this.getClass())) {
            final ServiceState other = (ServiceState) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return getService().equals(other.getService()) && getStatus().equals(other.getStatus());
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
        return "{ service: " + getService() + " status: " + getStatus() + "}";
    }

}
