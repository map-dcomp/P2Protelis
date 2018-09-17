package com.bbn.protelis.networkresourcemanagement.ns2;

import java.util.Objects;

/**
 * A link between two {@link NetworkDevice} objects.
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

    private final NetworkDevice left;

    /**
     * 
     * @return left side of the link
     */
    public NetworkDevice getLeft() {
        return this.left;
    }

    private final NetworkDevice right;

    /**
     * 
     * @return right side of the link
     */
    public NetworkDevice getRight() {
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
     * Create a link and adds it to the connected nodes.
     * 
     * @param name
     *            see {@link #getName()}
     * @param left
     *            see {@link #getLeft()}
     * @param right
     *            see {@link #getRight()}
     * @param bandwidth
     *            see {@link #getBandwidth()}
     * @see Node#addLink(Link)
     */
    public Link(final String name, final NetworkDevice left, final NetworkDevice right, final double bandwidth) {
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

        if (left instanceof Node) {
            ((Node) left).addLink(this);
        }
        if (right instanceof Node) {
            ((Node) right).addLink(this);
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
            final Link other = (Link) o;
            return (left.equals(other.getLeft()) && right.equals(other.getRight()))
                    || (left.equals(other.getRight()) && right.equals(other.getLeft()));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getLeft() + " - " + getRight() + " @ " + getBandwidth() + "Mbps";
    }
}
