package com.bbn.protelis.networkresourcemanagement.visualizer;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.NetworkLink;
import com.bbn.protelis.networkresourcemanagement.NetworkNode;

/**
 * Factory interface for creating visualization objects for
 * {@link ScenarioVisualizer}.
 *
 * @param <DN>
 *            the {@link DisplayNode} type
 * @param <DL>
 *            the {@link DisplayEdge} type
 */
public interface NetworkVisualizerFactory<DN extends DisplayNode, DL extends DisplayEdge> {

    /**
     * Create a visualization for a node.
     * 
     * @param node
     *            the node to visualize
     * @return the display object. Not null.
     */
    @Nonnull
    DN createDisplayNode(NetworkNode node);

    /**
     * Create the visualization for a link.
     * 
     * @param link
     *            the link to visualize
     * @param head
     *            the head of the link
     * @param tail
     *            the tail of the link
     * @return the display object. Not null.
     */
    @Nonnull
    DL createDisplayLink(NetworkLink link, DN head, DN tail);
}
