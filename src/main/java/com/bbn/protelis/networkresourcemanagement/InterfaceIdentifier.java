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

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representation of a network interface.
 * 
 * @author jschewe
 *
 */
public class InterfaceIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            see {@link #getName()}
     * @param neighbors
     *            see {@link #getNeighbors()}
     */
    public InterfaceIdentifier(@Nonnull final String name, @Nonnull final ImmutableSet<NodeIdentifier> neighbors) {
        this.name = Objects.requireNonNull(name);
        this.neighbors = Objects.requireNonNull(neighbors);
    }

    private final ImmutableSet<NodeIdentifier> neighbors;

    /**
     * 
     * @return the neighbors connected to this interface
     */
    public ImmutableSet<NodeIdentifier> getNeighbors() {
        return neighbors;
    }

    private final String name;

    /**
     * 
     * @return name of the interface
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Equal if the {@link #getName()}'s match.
     */
    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        } else if (this == o) {
            return true;
        } else if (this.getClass().equals(o.getClass())) {
            final InterfaceIdentifier other = (InterfaceIdentifier) o;
            return getName().equals(other.getName());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" ");
        sb.append(getName());
        sb.append(" -> ");
        sb.append(getNeighbors().stream().map(NodeIdentifier::getName).collect(Collectors.joining(",")));
        return sb.toString();
    }
}
