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
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The state of a service in a container.
 * 
 * @author jschewe
 *
 */
public class ServiceState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The various statuses that a service can be in.
     * 
     * @author jschewe
     *
     */
    public enum Status {
        /**
         * The service is starting up and not yet able to respond to requests.
         */
        STARTING,
        /**
         * The service is running and able to respond to requests.
         */
        RUNNING,
        /**
         * The service is in the process of shutting down.
         */
        STOPPING,
        /**
         * The service is stopped.
         */
        STOPPED,
        /**
         * The service status is unknown.
         */
        UNKNOWN
    }

    /**
     * 
     * @param service
     *            see {@link #getService()}
     * @param status
     *            see {@link #getStatus()}
     */
    public ServiceState(@Nonnull final ServiceIdentifier<?> service, @Nonnull final Status status) {
        this.service = service;
        this.status = status;
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

    private final Status status;

    /**
     * 
     * @return the current status of the service
     */
    @Nonnull
    public Status getStatus() {
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
            return getService().equals(other.getService()) && getStatus().equals(other.getStatus());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getService(), getStatus());
    }

    @Override
    public String toString() {
        return "{ service: " + getService() + " status: " + getStatus() + "}";
    }

}
