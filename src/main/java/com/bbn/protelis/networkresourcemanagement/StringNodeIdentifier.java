package com.bbn.protelis.networkresourcemanagement;

import javax.annotation.Nonnull;

import com.bbn.protelis.utils.StringUID;

/**
 * Identifier for a node that just uses a string name.
 */
public class StringNodeIdentifier extends StringUID implements ContainerIdentifier {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
     *            the name for the node
     */
    public StringNodeIdentifier(@Nonnull final String name) {
        super(name);
    }

    @Override
    public String getName() {
        return this.toString();
    }
}
