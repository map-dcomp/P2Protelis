package com.bbn.protelis.processmanagement.testbed.client;

import java.util.HashSet;
import java.util.Set;

import com.bbn.protelis.processmanagement.daemon.Monitorable;

/**
 * Null attack.
 */
public class NullAttackModel implements AttackModel {

    @Override
    public Set<Attack> attackInstanceFor(final Monitorable client) {
        return new HashSet<>();
    }

}
