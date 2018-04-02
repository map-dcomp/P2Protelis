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

        final NetworkServer node = new NetworkServer(nodeLookupService, regionLookupService, instance, name,
                managerFactory, extraData);

        return node;
    }

    @Override
    @Nonnull
    public NetworkLink createLink(@Nonnull final String name,
            @Nonnull final NetworkNode left,
            @Nonnull final NetworkNode right,
            final double bandwidth) {
        return new NetworkLink(name, left, right, bandwidth);
    }

    @Override
    @Nonnull
    public NetworkClient createClient(@Nonnull final NodeIdentifier name,
            @Nonnull final Map<String, Object> extraData) {
        final NetworkClient client = new NetworkClient(name, extraData);

        return client;
    }

}
