package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Information about the services running on a node.
 * 
 * @author jschewe
 *
 */
public class ServiceReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param nodeName
     *            see {@link #getNodeName()}
     * @param serviceState
     *            see {@link #getServiceState()}
     */
    public ServiceReport(@Nonnull final NodeIdentifier nodeName,
            @Nonnull final ImmutableMap<ContainerIdentifier, ServiceState> serviceState) {
        this.nodeName = nodeName;
        this.serviceState = serviceState;
    }

    private final NodeIdentifier nodeName;

    /**
     * @return the identifier of the node that the report came from
     */
    @Nonnull
    public final NodeIdentifier getNodeName() {
        return nodeName;
    }

    private final ImmutableMap<ContainerIdentifier, ServiceState> serviceState;

    /**
     * State of all containers on a node.
     * 
     * @return container id -> state for it's services
     */
    @Nonnull
    public ImmutableMap<ContainerIdentifier, ServiceState> getServiceState() {
        return serviceState;
    }

}
