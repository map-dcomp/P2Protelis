package com.bbn.protelis.networkresourcemanagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.junit.Test;

import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;

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

        final BasicResourceManager manager = new BasicResourceManager(nodeName, extraData);
        final ResourceReport report = manager.getCurrentResourceReport();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ObjectOutputStream serializaer = new ObjectOutputStream(output)) {
                serializaer.writeObject(report);
            }
        }
    }

}
