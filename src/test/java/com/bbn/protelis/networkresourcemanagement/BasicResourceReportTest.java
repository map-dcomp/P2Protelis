/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020,2021>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2018-04-19, with
the exception of the dcop implementation identified below (see notes).

Dispersed Computing (DCOMP)
Mission-oriented Adaptive Placement of Task and Data (MAP) 

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
BBN_LICENSE_END*/
package com.bbn.protelis.networkresourcemanagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;

import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Tests for {@link BasicResourceManager}.
 */
public class BasicResourceReportTest {

    /**
     * Rules for running tests.
     */
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Used by the JUnit framework")
    @Rule
    public RuleChain chain = NetworkResourceTestUtils.getStandardRuleChain();

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

        final String programStr = "true";
        final ProtelisProgram program = ProtelisLoader.parseAnonymousModule(programStr);
        final int dummyBasePort = 5000;
        final VirtualClock clock = new SimpleClock();
        final BasicResourceManagerFactory resMgrFactory = new BasicResourceManagerFactory(clock);
        final ResourceManager<NetworkServer> resMgr = resMgrFactory.createResourceManager();
        final NetworkServer node = new NetworkServer(new LocalNodeLookupService(dummyBasePort), program, new DnsNameIdentifier(nodeName),
                resMgr, extraData);
        resMgr.init(node, Collections.emptyMap());
        final ResourceReport report = node.getResourceManager()
                .getCurrentResourceReport(ResourceReport.EstimationWindow.SHORT);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream serializaer = new ObjectOutputStream(output)) {
                serializaer.writeObject(report);
            }
        }
    }

}
