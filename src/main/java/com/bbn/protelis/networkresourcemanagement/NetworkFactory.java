package com.bbn.protelis.networkresourcemanagement;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Factory to create network resources. The classes here should not inherit from
 * each other. It is assumed that {@link NetworkClient}s and
 * {@link NetworkServer}s are unrelated types, except by the interface
 * {@link NetworkNode}. Breaking this assumption will likely have undesired
 * results.
 * 
 * @param <N>
 *            the type of {@link NetworkServer}s to create
 * @param <L>
 *            the type of {@link NetworkLink}s to create
 * @param <C>
 *            the type of {@link NetworkClient}s to create
 */
public interface NetworkFactory<N extends NetworkServer, L extends NetworkLink, C extends NetworkClient> {

    /**
     * Create a node.
     * 
     * @param name
     *            the name of the node.
     * @param extraData
     *            any extra information that was read in about the the node.
     *            This can be used for setting additional properties.
     * @return the node. Not null.
     * @see NetworkServer#processExtraData(Map)
     */
    @Nonnull
    N createServer(@Nonnull String name, @Nonnull Map<String, Object> extraData);

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
     *            the bandwidth for the link in mega bits per second
     * @return the link. Not null.
     */
    @Nonnull
    L createLink(@Nonnull String name, @Nonnull NetworkNode left, @Nonnull NetworkNode right, double bandwidth);

    /**
     * Create a client.
     * 
     * @param name
     *            the name of the client.
     * @param extraData
     *            any extra information that was read in about the the client.
     *            This can be used for setting additional properties.
     * @return the node. Not null.
     * @see NetworkClient#processExtraData(Map)
     */
    @Nonnull
    C createClient(@Nonnull String name, @Nonnull Map<String, Object> extraData);
}
