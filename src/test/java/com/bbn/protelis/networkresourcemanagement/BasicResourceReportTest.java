package com.bbn.protelis.networkresourcemanagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.junit.Test;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;

import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;

/**
 * Tests for {@link BasicResourceManager}.
 */
public class BasicResourceReportTest {

    /**
     * Test that a {@link ResourceReport} created by this manager can be
     * serialized.
     * 
     * @throws IOException
     *             if there is an error reading the test data.
     */
    @Test
    public void testSerialization() throws IOException {
        final String nodeName = "nodeA3";
        final String basePath = "ns2/multinode";
        final Map<String, Object> extraData = NS2Parser.getNodeDataFromResource(basePath, nodeName);

        final RegionLookupService regionLookup = new DelegateRegionLookup();
        final String programStr = "true";
        final ProtelisProgram program = ProtelisLoader.parseAnonymousModule(programStr);
        final int dummyBasePort = 5000;
        final VirtualClock clock = new SimpleClock();
        final BasicResourceManagerFactory resMgrFactory = new BasicResourceManagerFactory(clock);
        final ResourceManager<NetworkServer> resMgr = resMgrFactory.createResourceManager();
        final NetworkServer node = new NetworkServer(new LocalNodeLookupService(dummyBasePort), regionLookup, program,
                new DnsNameIdentifier(nodeName), resMgr, extraData);
        final ResourceReport report = node.getResourceManager()
                .getCurrentResourceReport(ResourceReport.EstimationWindow.SHORT);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream serializaer = new ObjectOutputStream(output)) {
                serializaer.writeObject(report);
            }
        }
    }

}
