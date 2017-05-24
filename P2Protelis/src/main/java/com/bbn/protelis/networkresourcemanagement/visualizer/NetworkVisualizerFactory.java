package com.bbn.protelis.networkresourcemanagement.visualizer;

import javax.annotation.Nonnull;

import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;

/**
 * Factory interface for creating visualization objects for
 * {@link ScenarioVisualizer}.
 *
 * @param <DN>
 *            the {@link DisplayNode} type
 * @param <DL>
 *            the {@link DisplayEdge} type
 * @param <N>
 *            the {@link Node} type
 * @param <L>
 *            the {@link Link} type
 */
public interface NetworkVisualizerFactory<DN extends DisplayNode, DL extends DisplayEdge, N extends Node, L extends Link> {

    /**
     * Create a visualization for a node.
     * 
     * @param node
     *            the node to visualize
     * @return the display object. Not null.
     */
    @Nonnull
    DN createDisplayNode(N node);

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
    DL createDisplayLink(L link, DN head, DN tail);
}
