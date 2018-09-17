package com.bbn.protelis.processmanagement.testbed.client;

import java.io.Serializable;

import com.bbn.protelis.processmanagement.daemon.Monitorable;

/**
 * An attack.
 */
public interface Attack extends Serializable {
    /**
     * Apply an attack to a client, testing whether it succeeds or fails.
     * @param m the client
     * @return whether the attack succeeds
     */
    boolean apply(Monitorable m);
}
