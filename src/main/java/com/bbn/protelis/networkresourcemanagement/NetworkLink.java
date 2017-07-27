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
     * @return bandwidth in bytes per second
     */
    public double getBandwidth() {
        return this.bandwidth;
    }

    /**
     * Create a link.
     * 
     * @param name
     *            see {@link #getName()}
     * @param left
     *            see {@link #getLeft()}
     * @param right
     *            see {@link #getRight()}
     * @param bandwidth
     *            see {@link #getBandwidth()}
     */
    public NetworkLink(final String name, final NetworkNode left, final NetworkNode right, final double bandwidth) {
        this.name = name;
        this.left = left;
        this.right = right;
        this.bandwidth = bandwidth;
    }

}
