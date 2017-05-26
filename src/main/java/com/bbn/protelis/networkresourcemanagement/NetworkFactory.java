package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

/**
 * Factory to create {@link Node}s and {@link Link}s.
 * 
 * @param <N>
 *            the type of {@link Node}s to create
 * @param <L>
 *            the type of {@link Link}s to create
 */
public interface NetworkFactory<N extends Node, L extends Link> {

    /**
     * Create a node.
     * 
     * @param name
     *            the name of the node.
     * @return the node. Not null.
     */
    @Nonnull
    N createNode(String name);

    /**
     * Create a link.
     * 
     * @param name name of the link
     * @param left the left node
     * @param right the right  node
     * @param bandwidth the bandwidth for the link
     * @return the link. Not null.
     */
    @Nonnull
    L createLink(String name, N left, N right, double bandwidth);
}
