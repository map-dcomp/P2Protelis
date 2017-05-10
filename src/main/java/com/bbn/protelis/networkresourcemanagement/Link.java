package com.bbn.protelis.networkresourcemanagement;

/**
 * A network link.
 * 
 * @author jschewe
 *
 */
public class Link {

	private final String name;

	public String getName() {
		return this.name;
	}

	private final Node left;

	public Node getLeft() {
		return this.left;
	}

	private final Node right;

	public Node getRight() {
		return this.right;
	}

	public Link(final String name, final Node left, final Node right) {
		this.name = name;
		this.left = left;
		this.right = right;
	}
}
