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

import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ResourceManager} that always returns an empty report and fails to do
 * anything.
 * 
 */
public class NullResourceManager implements ResourceManager<NetworkServer> {

    private final NodeIdentifier nodeId;
    private final VirtualClock clock = new SimpleClock();

    /**
     * 
     * @param nodeName
     *            the node name to use for {@link #getCurrentResourceReport()}
     */
    public NullResourceManager(final NodeIdentifier nodeName) {
        this.nodeId = nodeName;
    }

    @Override
    public ResourceReport getCurrentResourceReport(@Nonnull final ResourceReport.EstimationWindow demandWindow) {
        return ResourceReport.getNullReport(nodeId, demandWindow);
    }

    @Override
    public NodeIdentifier startService(@Nonnull final ServiceIdentifier<?> service,
            @Nonnull final ContainerParameters parmeters) {
        return null;
    }

    @Override
    public boolean stopService(@Nonnull final NodeIdentifier containerName) {
        return true;
    }

    @Override
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getComputeCapacity() {
        return ImmutableMap.of();
    }

    @Override
    @Nonnull
    public ServiceReport getServiceReport() {
        return new ServiceReport(nodeId, -1, ImmutableMap.of());
    }

    @Override
    @Nonnull
    public VirtualClock getClock() {
        return clock;
    }

    @Override
    public void init(@Nonnull final NetworkServer node, @Nonnull final Map<String, Object> extraData) {
        // nothing to do
    }

    @Override
    public void fetchImage(@Nonnull final ServiceIdentifier<?> service) {
        // nothing to do

    }

    @Override
    public boolean waitForImage(@Nonnull final ServiceIdentifier<?> service) {
        return true;
    }
}
