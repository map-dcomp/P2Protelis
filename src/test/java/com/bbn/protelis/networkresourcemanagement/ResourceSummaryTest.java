/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019>, <Raytheon BBN Technologies>
To be applied to the DCOMP/MAP Public Source Code Release dated 2019-03-14, with
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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.bbn.protelis.networkresourcemanagement.ResourceReport.EstimationWindow;
import com.google.common.collect.ImmutableMap;

/**
 * Test cases for {@link ResourceSummary}.
 * 
 * @author jschewe
 *
 */
public class ResourceSummaryTest {

    /**
     * Test that converting a {@link ResourceReport} to an
     * {@link ResourceSummary} is sane.
     */
    @Test
    public void testConvert() {
        final NodeIdentifier nodeName = new DnsNameIdentifier("testNode");
        final NodeIdentifier neighborNodeName = new DnsNameIdentifier("testNeighborNode");
        final long timestamp = 0;
        final EstimationWindow estimationWindow = EstimationWindow.SHORT;

        final NodeAttribute<?> nodeAttribute = NodeAttributeEnum.TASK_CONTAINERS;
        final double serverCapacityValue = 10;
        final double serverLoadValue = 5;
        final double serverDemandValue = 3;
        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = ImmutableMap.of(nodeAttribute,
                serverCapacityValue);
        final ServiceIdentifier<?> service = new StringServiceIdentifier("testService");
        final RegionIdentifier region = new StringRegionIdentifier("A");
        final RegionIdentifier neighborRegion = new StringRegionIdentifier("B");

        final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>> serverLoad = ImmutableMap
                .of(nodeName, ImmutableMap.of(nodeAttribute, serverLoadValue));
        final ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>> serverDemand = ImmutableMap
                .of(nodeName, ImmutableMap.of(nodeAttribute, serverDemandValue));
        final double serverAverageProcessingTime = 30000D;

        final LinkAttribute<?> linkAttribute = LinkAttributeEnum.DATARATE;
        final double networkCapacityValue = 20;
        final double networkLoadValue = 15;
        final double networkDemandValue = 13;
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of(neighborNodeName, ImmutableMap.of(linkAttribute, networkCapacityValue));

        final ImmutableMap<NodeIdentifier, ImmutableMap<NodeIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = ImmutableMap
                .of(neighborNodeName, ImmutableMap.of(neighborNodeName,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkLoadValue))));

        final ImmutableMap<NodeIdentifier, ImmutableMap<NodeIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand = ImmutableMap
                .of(neighborNodeName, ImmutableMap.of(neighborNodeName,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkDemandValue))));

        // all traffic is going to the container through the node so both have
        // the same network load and demand values
        final NodeIdentifier containerId = new DnsNameIdentifier("container0");
        final ContainerResourceReport containerReport = new ContainerResourceReport(containerId, timestamp, service,
                estimationWindow, serverCapacity, serverLoad, serverDemand, serverAverageProcessingTime,
                networkCapacity, networkLoad, networkDemand);

        final ResourceReport report = new ResourceReport(nodeName, timestamp, estimationWindow, serverCapacity,
                networkCapacity, networkLoad, networkDemand, ImmutableMap.of(containerId, containerReport));

        final TestRegionLookup regionLookup = new TestRegionLookup();
        regionLookup.addMapping(nodeName, region);
        regionLookup.addMapping(neighborNodeName, neighborRegion);

        final ResourceSummary summary = ResourceSummary.convertToSummary(report, regionLookup);

        Assert.assertEquals(region, summary.getRegion());
        Assert.assertEquals(timestamp, summary.getMinTimestamp());
        Assert.assertEquals(timestamp, summary.getMaxTimestamp());
        Assert.assertEquals(estimationWindow, summary.getDemandEstimationWindow());

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> expectedServerLoad = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverLoadValue)));

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> expectedServerDemand = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverDemandValue)));

        Assert.assertEquals(serverCapacity, summary.getServerCapacity());
        Assert.assertEquals(expectedServerLoad, summary.getServerLoad());
        Assert.assertEquals(expectedServerDemand, summary.getServerDemand());
        Assert.assertEquals(ImmutableMap.of(service, serverAverageProcessingTime),
                summary.getServerAverageProcessingTime());

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkCapacity = ImmutableMap
                .of(neighborRegion, ImmutableMap.of(linkAttribute, networkCapacityValue));

        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> expectedNetworkLoad = ImmutableMap
                .of(neighborRegion, ImmutableMap.of(neighborRegion,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkLoadValue))));
        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> expectedNetworkDemand = ImmutableMap
                .of(neighborRegion, ImmutableMap.of(neighborRegion,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkDemandValue))));

        Assert.assertEquals(expectedNetworkCapacity, summary.getNetworkCapacity());
        Assert.assertEquals(expectedNetworkLoad, summary.getNetworkLoad());
        Assert.assertEquals(expectedNetworkDemand, summary.getNetworkDemand());

    }

    /**
     * Test that merging a {@link ResourceSummary} with
     * {@link ResourceSummary#getNullSummary(RegionIdentifier, EstimationWindow)}
     * yields the original summary.
     */
    @Test
    public void testMergeWithNull() {
        final long minTimestamp = 0;
        final long maxTimestamp = 10;
        final EstimationWindow estimationWindow = EstimationWindow.SHORT;

        final NodeAttribute<?> nodeAttribute = NodeAttributeEnum.TASK_CONTAINERS;
        final double serverCapacityValue = 10;
        final double serverLoadValue = 5;
        final double serverDemandValue = 3;
        final ServiceIdentifier<?> service = new StringServiceIdentifier("testService");
        final RegionIdentifier region = new StringRegionIdentifier("A");

        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = ImmutableMap.of(nodeAttribute,
                serverCapacityValue);
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverLoadValue)));

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverDemandValue)));

        final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTimeSum = ImmutableMap.of(service,
                30000D);
        final ImmutableMap<ServiceIdentifier<?>, Integer> serverAverageProcessingTimeCount = ImmutableMap.of(service,
                1);

        final LinkAttribute<?> linkAttribute = LinkAttributeEnum.DATARATE;
        final double networkCapacityValue = 20;
        final double networkLoadValue = 15;
        final double networkDemandValue = 13;
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkCapacityValue));

        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = ImmutableMap
                .of(region, ImmutableMap.of(region,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkLoadValue))));

        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand = ImmutableMap
                .of(region, ImmutableMap.of(region,
                        ImmutableMap.of(service, ImmutableMap.of(linkAttribute, networkDemandValue))));

        final ResourceSummary sourceSummary = new ResourceSummary(region, minTimestamp, maxTimestamp, estimationWindow,
                serverCapacity, serverLoad, serverDemand, serverAverageProcessingTimeCount,
                serverAverageProcessingTimeSum, networkCapacity, networkLoad, networkDemand);
        final ResourceSummary nullSummary = ResourceSummary.getNullSummary(region, estimationWindow);

        final ResourceSummary resultSummary = ResourceSummary.merge(sourceSummary, nullSummary);

        Assert.assertEquals(sourceSummary.getRegion(), resultSummary.getRegion());
        Assert.assertEquals(ResourceReport.NULL_TIMESTAMP, resultSummary.getMinTimestamp());
        Assert.assertEquals(sourceSummary.getMaxTimestamp(), resultSummary.getMaxTimestamp());
        Assert.assertEquals(sourceSummary.getDemandEstimationWindow(), resultSummary.getDemandEstimationWindow());

        Assert.assertEquals(sourceSummary.getServerCapacity(), resultSummary.getServerCapacity());
        Assert.assertEquals(sourceSummary.getServerLoad(), resultSummary.getServerLoad());
        Assert.assertEquals(sourceSummary.getServerDemand(), resultSummary.getServerDemand());
        Assert.assertEquals(sourceSummary.getServerAverageProcessingTime(),
                resultSummary.getServerAverageProcessingTime());

        Assert.assertEquals(sourceSummary.getNetworkCapacity(), resultSummary.getNetworkCapacity());
        Assert.assertEquals(sourceSummary.getNetworkLoad(), resultSummary.getNetworkLoad());
        Assert.assertEquals(sourceSummary.getNetworkDemand(), resultSummary.getNetworkDemand());
    }

    /**
     * Check that when converting a {@link ResourceReport} to a
     * {@link ResourceSummary} with network capacity and load to a neighbor
     * inside the region and outside the region that only the capacity and load
     * to outside the region is in the summary.
     * 
     */
    @Test
    public void testIgnoreConvertSameRegionNet() {
        final double loadToA1 = 10;
        final double capacityToA1 = 100;
        final double loadToB0 = 20;
        final double capacityToB0 = 200;

        final NodeIdentifier a0 = new DnsNameIdentifier("a0");
        final NodeIdentifier a1 = new DnsNameIdentifier("a1");
        final NodeIdentifier b0 = new DnsNameIdentifier("b0");
        final NodeIdentifier source = new DnsNameIdentifier("source");
        final ServiceIdentifier<?> service = new StringServiceIdentifier("service");
        final RegionIdentifier regionA = new StringRegionIdentifier("A");
        final RegionIdentifier regionB = new StringRegionIdentifier("B");
        final RegionIdentifier regionSource = new StringRegionIdentifier("Z");
        final double tolerance = 1E-6;

        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap.of(//
                a1, ImmutableMap.of(LinkAttributeEnum.DATARATE, capacityToA1), //
                b0, ImmutableMap.of(LinkAttributeEnum.DATARATE, capacityToB0));

        final ImmutableMap<NodeIdentifier, ImmutableMap<NodeIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = //
                ImmutableMap.of(//
                        a1, //
                        ImmutableMap.of(source, ImmutableMap.of(service, //
                                ImmutableMap.of(LinkAttributeEnum.DATARATE, loadToA1))), //
                        b0, //
                        ImmutableMap.of(source, //
                                ImmutableMap.of(service, ImmutableMap.of(LinkAttributeEnum.DATARATE, loadToB0))) //
                );

        final ResourceReport report = new ResourceReport(a0, 0, EstimationWindow.SHORT, ImmutableMap.of(),
                networkCapacity, networkLoad, ImmutableMap.of(), ImmutableMap.of());

        final TestRegionLookup nodeToRegion = new TestRegionLookup();
        nodeToRegion.addMapping(a0, regionA);
        nodeToRegion.addMapping(a1, regionA);
        nodeToRegion.addMapping(b0, regionB);
        nodeToRegion.addMapping(source, regionSource);

        final ResourceSummary summary = ResourceSummary.convertToSummary(report, nodeToRegion);

        // ---- check capacity
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> summaryNetCapacity = summary
                .getNetworkCapacity();
        assertThat(summaryNetCapacity, notNullValue());

        // make sure region A isn't in the capacity
        assertThat(summaryNetCapacity.get(regionA), nullValue());

        // check that region B has capacity and that it matches the expected
        // value
        final ImmutableMap<LinkAttribute<?>, Double> summaryNetCapacityB = summaryNetCapacity.get(regionB);
        assertThat(summaryNetCapacityB, notNullValue());

        final Double summaryNetCapacityBvalue = summaryNetCapacityB.get(LinkAttributeEnum.DATARATE);
        assertThat(summaryNetCapacityBvalue, notNullValue());
        assertThat(summaryNetCapacityBvalue, closeTo(capacityToB0, tolerance));

        // ---- check load
        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> summaryNetLoad = summary
                .getNetworkLoad();
        assertThat(summaryNetLoad, notNullValue());

        // make sure region A isn't in the capacity
        assertThat(summaryNetLoad.get(regionA), nullValue());

        // check that region B has load and that it matches the expected
        // value
        final ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>> summaryNetLoadB = summaryNetLoad
                .get(regionB);
        assertThat(summaryNetLoadB, notNullValue());

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>> summaryNetSourceLoad = summaryNetLoadB
                .get(regionSource);
        assertThat(summaryNetSourceLoad, notNullValue());

        final ImmutableMap<LinkAttribute<?>, Double> summaryNetServiceLoad = summaryNetSourceLoad.get(service);
        assertThat(summaryNetServiceLoad, notNullValue());

        final Double summaryNetLoadBvalue = summaryNetServiceLoad.get(LinkAttributeEnum.DATARATE);
        assertThat(summaryNetLoadBvalue, notNullValue());
        assertThat(summaryNetLoadBvalue, closeTo(loadToB0, tolerance));

    }

    private static final class TestRegionLookup implements RegionLookupService {

        private final Map<NodeIdentifier, RegionIdentifier> data = new HashMap<>();

        public void addMapping(@Nonnull final NodeIdentifier node, @Nonnull final RegionIdentifier region) {
            data.put(node, region);
        }

        @Override
        public RegionIdentifier getRegionForNode(final NodeIdentifier nodeId) {
            return data.get(nodeId);
        }

    }

}
