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

import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;

/**
 * Create {@link NetworkServer} and {@link NetworkLink} objects.
 * 
 */
public class BasicNetworkFactory implements NetworkFactory<NetworkServer, NetworkLink, NetworkClient> {

    private final NodeLookupService nodeLookupService;
    private final RegionLookupService regionLookupService;
    private final String program;
    private final boolean anonymousProgram;
    private final ResourceManagerFactory<NetworkServer> managerFactory;

    /**
     * Create a basic factory.
     * 
     * @param lookupService
     *            how to find other nodes
     * @param program
     *            the program to put in all nodes
     * @param anonymous
     *            if true, parse as main expression; if false, treat as a module
     *            reference
     * @param managerFactory
     *            used to create the {@link ResourceManager}s.
     * @param regionLookupService
     *            see
     *            {@link NetworkServer#NetworkServer(NodeLookupService, RegionLookupService, ProtelisProgram, String)}
     */
    public BasicNetworkFactory(@Nonnull final NodeLookupService lookupService,
            @Nonnull final RegionLookupService regionLookupService,
            @Nonnull final ResourceManagerFactory<NetworkServer> managerFactory,
            final String program,
            final boolean anonymous) {
        this.nodeLookupService = lookupService;
        this.regionLookupService = regionLookupService;
        this.program = program;
        this.anonymousProgram = anonymous;
        this.managerFactory = managerFactory;
    }

    @Override
    @Nonnull
    public NetworkServer createServer(@Nonnull final NodeIdentifier name,
            @Nonnull final Map<String, Object> extraData) {
        final ProtelisProgram instance;
        if (anonymousProgram) {
            instance = ProtelisLoader.parseAnonymousModule(program);
        } else {
            instance = ProtelisLoader.parse(program);
        }

        final ResourceManager<NetworkServer> manager = managerFactory.createResourceManager();
        final NetworkServer node = new NetworkServer(nodeLookupService, regionLookupService, instance, name, manager,
                extraData);
        manager.init(node, extraData);

        return node;
    }

    @Override
    @Nonnull
    public NetworkLink createLink(@Nonnull final String name,
            @Nonnull final NetworkNode left,
            @Nonnull final NetworkNode right,
            final double bandwidth,
            final double delay) {
        return new NetworkLink(name, left, right, bandwidth, delay);
    }

    @Override
    @Nonnull
    public NetworkClient createClient(@Nonnull final NodeIdentifier name,
            @Nonnull final Map<String, Object> extraData) {
        final NetworkClient client = new NetworkClient(name, extraData);

        return client;
    }

}
