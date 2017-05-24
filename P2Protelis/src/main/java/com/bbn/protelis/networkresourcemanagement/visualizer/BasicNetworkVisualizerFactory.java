package com.bbn.protelis.networkresourcemanagement.visualizer;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * Create visualization objects using the basic types. 
 */
public class BasicNetworkVisualizerFactory implements NetworkVisualizerFactory<DisplayNode, DisplayEdge, Node, Link> {

    @Override
    @Nonnull
    public DisplayNode createDisplayNode(final Node node) {
        return new DisplayNode(node);
    }

    @Override
    @Nonnull
    public DisplayEdge createDisplayLink(final Link link, final DisplayNode head, final DisplayNode tail) {
        return new DisplayEdge(link, head, tail);
    }

}
