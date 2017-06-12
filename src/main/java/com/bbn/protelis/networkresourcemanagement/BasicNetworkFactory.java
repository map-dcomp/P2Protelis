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

    private final NodeLookupService lookupService;
    private final String program;
    private final boolean anonymousProgram;

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
     */
    public BasicNetworkFactory(final NodeLookupService lookupService, final String program, final boolean anonymous) {
        this.lookupService = lookupService;
        this.program = program;
        this.anonymousProgram = anonymous;
    }

    @Override
    @Nonnull
    public NetworkServer createServer(@Nonnull final String name, @Nonnull final Map<String, Object> extraData) {
        final ProtelisProgram instance;
        if (anonymousProgram) {
            instance = ProtelisLoader.parseAnonymousModule(program);
        } else {
            instance = ProtelisLoader.parse(program);
        }

        final BasicResourceManager manager = new BasicResourceManager(name, extraData);
        final NetworkServer node = new NetworkServer(lookupService, instance, name, manager);
        manager.setNode(node);

        node.processExtraData(extraData);

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
    public NetworkClient createClient(@Nonnull final String name, @Nonnull final Map<String, Object> extraData) {
        final NetworkClient client = new NetworkClient(name);

        client.processExtraData(extraData);

        return client;
    }

}
