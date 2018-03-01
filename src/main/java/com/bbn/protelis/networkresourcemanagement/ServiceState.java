package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The state of a service in a container.
 * 
 * @author jschewe
 *
 */
public class ServiceState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The various statuses that a service can be in.
     * 
     * @author jschewe
     *
     */
    public enum Status {
        /**
         * The service is starting up and not yet able to respond to requests.
         */
        STARTING,
        /**
         * The service is running and able to respond to requests.
         */
        RUNNING,
        /**
         * The service is in the process of shutting down.
         */
        STOPPING,
        /**
         * The service is stopped.
         */
        STOPPED,
        /**
         * The service status is unknown.
         */
        UNKNOWN
    }

    /**
     * 
     * @param service
     *            see {@link #getService()}
     * @param status
     *            see {@link #getStatus()}
     */
    public ServiceState(@Nonnull final ServiceIdentifier<?> service, @Nonnull final Status status) {
        this.service = service;
        this.status = status;
    }

    private final ServiceIdentifier<?> service;

    /**
     * 
     * @return the service that this information is for
     */
    @Nonnull
    public ServiceIdentifier<?> getService() {
        return service;
    }

    private final Status status;

    /**
     * 
     * @return the current status of the service
     */
    @Nonnull
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (o.getClass().equals(this.getClass())) {
            final ServiceState other = (ServiceState) o;
            return getService().equals(other.getService()) && getStatus().equals(other.getStatus());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getService(), getStatus());
    }

    @Override
    public String toString() {
        return "{ service: " + getService() + " status: " + getStatus() + "}";
    }

}
