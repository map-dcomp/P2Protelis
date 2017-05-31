package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

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
     * @param extraData
     *            any extra information that was read in about the the node.
     *            This can be used for setting additional properties.
     * @return the node. Not null.
     */
    @Nonnull
    N createNode(String name, Map<String, String> extraData);

    /**
     * Create a link.
     * 
     * @param name
     *            name of the link
     * @param left
     *            the left node
     * @param right
     *            the right node
     * @param bandwidth
     *            the bandwidth for the link
     * @return the link. Not null.
     */
    @Nonnull
    L createLink(String name, N left, N right, double bandwidth);
}
