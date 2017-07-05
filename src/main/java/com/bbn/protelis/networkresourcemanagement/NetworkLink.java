package com.bbn.protelis.networkresourcemanagement;

/**
 * A link between two {@link NetworkNode}s.
 * 
 */
public class NetworkLink {

    private final String name;

    /**
     * 
     * @return link name
     */
    public String getName() {
        return this.name;
    }
    
    private final NetworkNode left;

    /**
     * 
     * @return left side of the link
     */
    public NetworkNode getLeft() {
        return this.left;
    }

    private final NetworkNode right;

    /**
     * 
     * @return right side of the link
     */
    public NetworkNode getRight() {
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
    public NetworkLink(final String name, final NetworkNode left, final NetworkNode right, final double bandwidth) {
        this.name = name;
        this.left = left;
        this.right = right;
        this.bandwidth = bandwidth;
    }

}
