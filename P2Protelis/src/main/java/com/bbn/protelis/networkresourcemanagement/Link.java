package com.bbn.protelis.networkresourcemanagement;

/**
 * A network link.
 * 
 */
public class Link {

    private final String name;

    /**
     * 
     * @return link name
     */
    public String getName() {
        return this.name;
    }

    private final Node left;

    /**
     * 
     * @return left side of the link
     */
    public Node getLeft() {
        return this.left;
    }

    private final Node right;

    /**
     * 
     * @return right side of the link
     */
    public Node getRight() {
        return this.right;
    }

    private final double bandwidth;

    /**
     * 
     * @return bandwidth in bits per second
     */
    public double getBandwidth() {
        return this.bandwidth;
    }

    /**
     * Create a link.
     * 
     * @param name
     *            the name
     * @param left
     *            the left node
     * @param right
     *            the right node
     * @param bandwidth
     *            in bits per second
     */
    public Link(final String name, final Node left, final Node right, final double bandwidth) {
        this.name = name;
        this.left = left;
        this.right = right;
        this.bandwidth = bandwidth;
    }
}
