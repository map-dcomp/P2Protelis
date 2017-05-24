package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import org.protelis.vm.ProtelisProgram;

/**
 * Create {@link Node} and {@link Link} objects.
 * 
 */
public class BasicNetworkFactory implements NetworkFactory<Node, Link> {

    private final NodeLookupService lookupService;
    private final ProtelisProgram program;

    /**
     * Create a basic factory.
     * 
     * @param lookupService
     *            how to find other nodes
     * @param program
     *            the program to put in all nodes
     */
    public BasicNetworkFactory(final NodeLookupService lookupService, final ProtelisProgram program) {
        this.lookupService = lookupService;
        this.program = program;
    }

    @Override
    @Nonnull
    public Node createNode(final String name) {
        return new Node(lookupService, program, name);
    }

    @Override
    @Nonnull
    public Link createLink(final String name, final Node left, final Node right, final double bandwidth) {
        return new Link(name, left, right, bandwidth);
    }

}
