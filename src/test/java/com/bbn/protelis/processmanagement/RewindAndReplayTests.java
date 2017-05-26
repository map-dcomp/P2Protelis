package com.bbn.protelis.processmanagement;

import org.junit.Test;

public class RewindAndReplayTests extends JSONFrameworkTest {

    /**
     * Test rewind function against a fixed message set and dummy processes.
     */
    @Test
    public void rewindDummyTest() {
        // By-hand version for debugging:
        //String[] extraArgs = {"-v","true","-n","Distributed Attack Rewind","-e","Inject Attack","-i","Inject Attack","badMessages"};
        //runTest("/com/bbn/processmanagement/rewind/rewindDummy.pt",true,"networks/tangleDummy.json",100000,"tests/rewind/rewindDummyTest.json",extraArgs);
        String[] extraArgs = {"-v","true"};
        runTest("/com/bbn/processmanagement/rewind/rewindDummy.pt",true,"networks/tangleDummy.json",25,"tests/rewind/rewindDummyTest.json",extraArgs);
    }

    /**
     * Test rewind function with a set of live processes.
     */
    @Test
    public void rewindTest() {
        // By-hand version for debugging:
        //String[] extraArgs = {"-v","true","-n","Distributed Attack Rewind","-e","Inject Attack","-i","Inject Attack","badMessages"};
        //runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/tangle.json",100000,"tests/rewind/rewindTest.json",extraArgs);
        String[] extraArgs = {"-v","true"};
        runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/tangle.json",250,"tests/rewind/rewindTest.json",extraArgs);
    }

    /**
     * Test rewind function with a chain of live processes.
     */
    @Test
    public void chainTest() {
        // By-hand version for debugging:
        //String[] extraArgs = {"-v","true","-n","Distributed Attack Rewind","-e","Inject Attack","-i","Inject Attack","badMessages"};
        //runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/tangle.json",100000,"tests/rewind/rewindTest.json",extraArgs);
        String[] extraArgs = {"-v","true"};
        runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/chain.json",250,"tests/rewind/rewindChainTest.json",extraArgs);
    }

    /**
     * Test rewind function with a chain of live processes.
     */
    @Test
    public void tbmcsTest() {
        // By-hand version for debugging:
        //String[] extraArgs = {"-v","true","-n","Distributed Attack Rewind","-e","Inject Attack","-i","Inject Attack","badMessages"};
        //runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/tangle.json",100000,"tests/rewind/rewindTest.json",extraArgs);
        String[] extraArgs = {"-n","TBMCS rewind","-v","true","-i","deletedMessages","attackMessages"}; //,"contaminated"
        runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/tbmcs.json",250,"tests/rewind/tbmcsTest.json",extraArgs);
    }

    /**
     * Test replay function with a chain of live processes.
     */
    @Test
    public void replayTest() {
        String[] extraArgs = {"-v","true","-d","trace","-i","Rollback","Replay","deletedMessages","attackMessages"};
        //String[] extraArgs = {"-v","true","-d","trace","-e","Rollback","Replay","-i","Rollback","Replay"};
        runTest("/com/bbn/processmanagement/rewind/rewind.pt",true,"networks/replayChain.json",2500,"tests/rewind/replayChainTest.json",extraArgs);
    }

}