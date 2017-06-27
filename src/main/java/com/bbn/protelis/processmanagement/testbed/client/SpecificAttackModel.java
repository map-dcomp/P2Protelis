package com.bbn.protelis.processmanagement.testbed.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.bbn.protelis.processmanagement.daemon.Monitorable;

/** 
 * Attack in precisely one instance, targeted at one client.
 */
public class SpecificAttackModel implements AttackModel {
    private String targetName = "";
    private int session;

    static class SpecificTargetAttack implements Attack, Serializable {
        private static final long serialVersionUID = -5567612012104276482L;
        private String targetName;
        SpecificTargetAttack(final String targetName) {
            this.targetName = targetName;
        }

        @Override
        public boolean apply(final Monitorable m) {
            QueryResponseNode qr = (QueryResponseNode)m;
            return targetName.equals(qr.identifier);
        }
        
    }
    
    @Override
    public Set<Attack> attackInstanceFor(final Monitorable client) {
        Set<Attack> s = new HashSet<>();
        QueryResponseNode qr = (QueryResponseNode)client;
        if (session == qr.sessionID) {
            s.add(new SpecificTargetAttack(targetName));
        }
        return s;
    }

}
