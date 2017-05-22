package com.bbn.protelis.networkresourcemanagement;

/**
 * A network link.
 * 
 * @author jschewe
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

    /**
     * Create a link.
     * 
     * @param name
     *            the name
     * @param left
     *            the left node
     * @param right
     *            the right node
     */
    public Link(final String name, final Node left, final Node right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }
}
