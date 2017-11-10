package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;

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
        STOPPED
    }

    /**
     * 
     * @param service
     *            see {@link #getService()}
     * @param status
     *            see {@link #getStatus()}
     */
    public ServiceState(final ServiceIdentifier<?> service, final Status status) {
        this.service = service;
        this.status = status;
    }

    private final ServiceIdentifier<?> service;

    /**
     * 
     * @return the service that this information is for
     */
    public ServiceIdentifier<?> getService() {
        return service;
    }

    private final Status status;

    /**
     * 
     * @return the current status of the service
     */
    public Status getStatus() {
        return status;
    }

}
