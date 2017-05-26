package com.bbn.protelis.networkresourcemanagement.visualizer;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * Create visualization objects using the basic types.
 * 
 * @param <N> the node class used
 * @param <L> the link class used
 */
public class BasicNetworkVisualizerFactory<N extends Node, L extends Link>
        implements NetworkVisualizerFactory<DisplayNode, DisplayEdge, N, L> {

    @Override
    @Nonnull
    public DisplayNode createDisplayNode(final N node) {
        return new DisplayNode(node);
    }

    @Override
    @Nonnull
    public DisplayEdge createDisplayLink(final L link, final DisplayNode head, final DisplayNode tail) {
        return new DisplayEdge(link, head, tail);
    }

}
