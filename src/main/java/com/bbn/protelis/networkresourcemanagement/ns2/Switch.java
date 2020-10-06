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
package com.bbn.protelis.networkresourcemanagement.ns2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A network switch in the topology. May also be referred to as a LAN.
 * 
 * @author jschewe
 *
 */
public class Switch extends NetworkDevice {

    /**
     * Create a switch and all implicit links.
     * 
     * @param name
     *            see {@link NetworkDevice#getName()}
     * @param nodes
     *            see {@link #getNodes()}
     * @param bandwidth
     *            used as the bandwidth for the created links see
     *            {@link Link#getBandwidth()}
     * @param delay
     *            used as the delay for the created links see
     *            {@link Link#getDelay()}
     * @see Link#Link(String, NetworkDevice, NetworkDevice, double, double)
     */
    public Switch(@Nonnull final String name,
            @Nonnull final Set<Node> nodes,
            final double bandwidth,
            final double delay) {
        super(name);
        this.bandwidth = bandwidth;

        nodes.forEach(n -> {
            final Link link = new Link(name, this, n, bandwidth, delay);
            links.put(n, link);
        });

    }

    private final Map<Node, Link> links = new HashMap<>();

    /**
     * 
     * @return the links between this switch and other nodes. Unmodifiable
     *         object.
     */
    public Map<Node, Link> getLinks() {
        return Collections.unmodifiableMap(links);
    }

    /**
     * 
     * @return the nodes connected to the switch. Unmodifiable object.
     */
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(links.keySet());
    }

    private final double bandwidth;

    /**
     * 
     * @return the bandwidth in megabits per second
     */
    public double getBandwidth() {
        return bandwidth;
    }

}
