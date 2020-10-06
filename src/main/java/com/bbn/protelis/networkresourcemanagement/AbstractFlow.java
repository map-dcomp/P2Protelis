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

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * A flow between two nodes or regions. The {@link #getServer()} property exists
 * to handle the situation where the definition of a server is different than
 * the definition of which node/region started the flow or there is no server to
 * the flow.
 * 
 * @param <T>
 *            the type of the objects in the flow
 */
public abstract class AbstractFlow<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T source;

    /**
     * 
     * @return the source of the flow
     */
    @Nonnull
    public T getSource() {
        return this.source;
    }

    private final T destination;

    /**
     * 
     * @return the destination of the flow
     */
    @Nonnull
    public T getDestination() {
        return this.destination;
    }

    private final T server;

    /**
     * The value will be {@link NodeIdentifier#UNKNOWN} or
     * {@link RegionIdentifier#UNKNOWN} if the server of the network flow cannot
     * be determined.
     * 
     * @return the server identifier
     */
    @Nonnull
    public T getServer() {
        return server;
    }

    /**
     * Create a network flow.
     * 
     * @param source
     *            see {@link #getSource()}
     * @param destination
     *            see {@link #getDestination()}
     * @param server
     *            see {@link #getServer()}
     */
    public AbstractFlow(@Nonnull final T source, @Nonnull final T destination, @Nonnull final T server) {
        this.source = source;
        this.destination = destination;
        this.server = server;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

    /**
     * Equality is defined as connecting the same pair of nodes with the same
     * server.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (null == o) {
            return false;
        } else if (getClass() == o.getClass()) {
            @SuppressWarnings("unchecked") // can't get the generic type
            final AbstractFlow<T> other = (AbstractFlow<T>) o;
            return Objects.equals(getSource(), other.getSource()) //
                    && Objects.equals(getDestination(), other.getDestination()) //
                    && Objects.equals(getServer(), other.getServer());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": [" + getSource() + " <-> " + getDestination() + " server: " + getServer() + "]";
    }

}
