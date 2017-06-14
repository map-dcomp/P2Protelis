package com.bbn.protelis.networkresourcemanagement.visualizer;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.NetworkLink;
import com.bbn.protelis.networkresourcemanagement.NetworkNode;

/**
 * Create visualization objects using the basic types.
 * 
 */
public class BasicNetworkVisualizerFactory implements NetworkVisualizerFactory<DisplayNode, DisplayEdge> {

    @Override
    @Nonnull
    public DisplayNode createDisplayNode(final NetworkNode node) {
        return new DisplayNode(node);
    }

    @Override
    @Nonnull
    public DisplayEdge createDisplayLink(final NetworkLink link, final DisplayNode head, final DisplayNode tail) {
        return new DisplayEdge(link, head, tail);
    }

}
