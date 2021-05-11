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
 * Attributes for metrics on a node.
 * 
 */
public class LinkAttribute implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Transmit bandwidth in megabits per second.
     */
    public static final LinkAttribute DATARATE_TX = new LinkAttribute("DATARATE_TX");
    /**
     * Receive bandwidth in megabits per second.
     */
    public static final LinkAttribute DATARATE_RX = new LinkAttribute("DATARATE_RX");

    /**
     * Link delay in milliseconds. There is no capacity value for this
     * attribute. When this information is summarized across a region only link
     * delays between regions are considered.
     */
    public static final LinkAttribute DELAY = new LinkAttribute("DELAY");

    /**
     * Calls {@link #LinkAttribute(String, boolean)} with application specific
     * set to false.
     * 
     * @param name
     *            see {@link #getName()}
     */
    public LinkAttribute(@Nonnull final String name) {
        this(name, false);
    }

    /**
     * 
     * @param name
     *            see {@link #getName()}
     * @param applicationSpecific
     *            see {@link #isApplicationSpecific()}
     */
    public LinkAttribute(@JsonProperty("name") @Nonnull final String name,
            @JsonProperty("applicationSpecific") final boolean applicationSpecific) {
        this.name = name;
        this.applicationSpecific = applicationSpecific;
        this.hashCode = Objects.hash(applicationSpecific, name);
    }

    private final boolean applicationSpecific;

    /**
     * 
     * @return true if this metric is specific to an application, otherwise the
     *         metric applies to all applications
     */
    public boolean isApplicationSpecific() {
        return applicationSpecific;
    }

    private final String name;

    /**
     * 
     * @return the name of the metric
     */
    public String getName() {
        return name;
    }

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Only the name is compared.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof LinkAttribute) {
            final LinkAttribute other = (LinkAttribute) o;
            if (this.hashCode != other.hashCode) {
                return false;
            } else {
                return Objects.equals(getName(), other.getName());
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {" + getName() + ", " + isApplicationSpecific() + "}";
    }
}
