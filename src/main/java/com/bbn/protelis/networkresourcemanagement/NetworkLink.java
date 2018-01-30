package com.bbn.protelis.networkresourcemanagement;

import java.util.Objects;

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
     * @return bandwidth in megabits per second
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
        // make sure that edge(one, two) is equal to edge(two, one)
        // Objects.hash returns different values depending on the order
        if (left.hashCode() < right.hashCode()) {
            this.hashCode = Objects.hash(left, right);
        } else {
            this.hashCode = Objects.hash(right, left);
        }
    }

    private final int hashCode;

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Equality is defined as connecting the same pair of nodes.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (null == o) {
            return false;
        } else if (getClass() == o.getClass()) {
            // edges are bidirectional, so left and right don't matter for
            // equality
            final NetworkLink other = (NetworkLink) o;
            return (left.equals(other.getLeft()) && right.equals(other.getRight()))
                    || (left.equals(other.getRight()) && right.equals(other.getLeft()));
        } else {
            return false;
        }
    }
}
