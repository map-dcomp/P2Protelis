package com.bbn.protelis.networkresourcemanagement;

import org.junit.Assert;
import org.junit.Test;
import org.protelis.lang.datatype.Field;
import org.protelis.lang.datatype.impl.FieldMapImpl;

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
        final NodeIdentifier nodeName = new StringNodeIdentifier("testNode");
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
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverLoadValue)));
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand = ImmutableMap
                .of(service, ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverDemandValue)));

        final LinkAttribute<?> linkAttribute = LinkAttributeEnum.DATARATE;
        final double networkCapacityValue = 20;
        final double networkLoadValue = 15;
        final double networkDemandValue = 13;
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of(nodeName, ImmutableMap.of(linkAttribute, networkCapacityValue));
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad = ImmutableMap
                .of(nodeName, ImmutableMap.of(linkAttribute, networkLoadValue));
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand = ImmutableMap
                .of(nodeName, ImmutableMap.of(linkAttribute, networkDemandValue));

        final ResourceReport report = new ResourceReport(nodeName, timestamp, estimationWindow, serverCapacity,
                serverLoad, serverDemand, networkCapacity, networkLoad, networkDemand);

        final Field nodeToRegion = new FieldMapImpl(1, 1);
        nodeToRegion.addSample(nodeName, region);

        final ResourceSummary summary = ResourceSummary.convertToSummary(report, nodeToRegion);

        Assert.assertEquals(region, summary.getRegion());
        Assert.assertEquals(timestamp, summary.getMinTimestamp());
        Assert.assertEquals(timestamp, summary.getMaxTimestamp());
        Assert.assertEquals(estimationWindow, summary.getDemandEstimationWindow());

        Assert.assertEquals(serverCapacity, summary.getServerCapacity());
        Assert.assertEquals(serverLoad, summary.getServerLoad());
        Assert.assertEquals(serverDemand, summary.getServerDemand());

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkCapacity = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkCapacityValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkLoad = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkLoadValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkDemand = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkDemandValue));

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

        final LinkAttribute<?> linkAttribute = LinkAttributeEnum.DATARATE;
        final double networkCapacityValue = 20;
        final double networkLoadValue = 15;
        final double networkDemandValue = 13;
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkCapacityValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkLoadValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkDemandValue));

        final ResourceSummary sourceSummary = new ResourceSummary(region, minTimestamp, maxTimestamp, estimationWindow,
                serverCapacity, serverLoad, serverDemand, networkCapacity, networkLoad, networkDemand);
        final ResourceSummary nullSummary = ResourceSummary.getNullSummary(region, estimationWindow);

        final ResourceSummary resultSummary = ResourceSummary.merge(sourceSummary, nullSummary);

        Assert.assertEquals(sourceSummary.getRegion(), resultSummary.getRegion());
        Assert.assertEquals(ResourceReport.NULL_TIMESTAMP, resultSummary.getMinTimestamp());
        Assert.assertEquals(sourceSummary.getMaxTimestamp(), resultSummary.getMaxTimestamp());
        Assert.assertEquals(sourceSummary.getDemandEstimationWindow(), resultSummary.getDemandEstimationWindow());

        Assert.assertEquals(sourceSummary.getServerCapacity(), resultSummary.getServerCapacity());
        Assert.assertEquals(sourceSummary.getServerLoad(), resultSummary.getServerLoad());
        Assert.assertEquals(sourceSummary.getServerDemand(), resultSummary.getServerDemand());

        Assert.assertEquals(sourceSummary.getNetworkCapacity(), resultSummary.getNetworkCapacity());
        Assert.assertEquals(sourceSummary.getNetworkLoad(), resultSummary.getNetworkLoad());
        Assert.assertEquals(sourceSummary.getNetworkDemand(), resultSummary.getNetworkDemand());
    }

    /**
     * Test that merging a {@link ResourceSummary} with itself produces twice
     * the values.
     * 
     */
    @Test
    public void testMergeWithSelf() {
        final NodeIdentifier nodeName = new StringNodeIdentifier("testNode");
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

        final LinkAttribute<?> linkAttribute = LinkAttributeEnum.DATARATE;
        final double networkCapacityValue = 20;
        final double networkLoadValue = 15;
        final double networkDemandValue = 13;
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkCapacityValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkLoadValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkDemand = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkDemandValue));

        final ResourceSummary sourceSummary = new ResourceSummary(region, minTimestamp, maxTimestamp, estimationWindow,
                serverCapacity, serverLoad, serverDemand, networkCapacity, networkLoad, networkDemand);

        final Field nodeToRegion = new FieldMapImpl(1, 1);
        nodeToRegion.addSample(nodeName, region);

        final ResourceSummary resultSummary = ResourceSummary.merge(sourceSummary, sourceSummary);

        Assert.assertEquals(region, resultSummary.getRegion());
        Assert.assertEquals(minTimestamp, resultSummary.getMinTimestamp());
        Assert.assertEquals(maxTimestamp, resultSummary.getMaxTimestamp());
        Assert.assertEquals(estimationWindow, resultSummary.getDemandEstimationWindow());

        final ImmutableMap<NodeAttribute<?>, Double> expectedServerCapacity = ImmutableMap.of(nodeAttribute,
                serverCapacityValue + serverCapacityValue);
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> expectedServerLoad = ImmutableMap
                .of(service,
                        ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverLoadValue + serverLoadValue)));
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> expectedServerDemand = ImmutableMap
                .of(service,
                        ImmutableMap.of(region, ImmutableMap.of(nodeAttribute, serverDemandValue + serverDemandValue)));

        Assert.assertEquals(expectedServerCapacity, resultSummary.getServerCapacity());
        Assert.assertEquals(expectedServerLoad, resultSummary.getServerLoad());
        Assert.assertEquals(expectedServerDemand, resultSummary.getServerDemand());

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkCapacity = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkCapacityValue + networkCapacityValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkLoad = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkLoadValue + networkLoadValue));
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> expectedNetworkDemand = ImmutableMap
                .of(region, ImmutableMap.of(linkAttribute, networkDemandValue + networkDemandValue));

        Assert.assertEquals(expectedNetworkCapacity, resultSummary.getNetworkCapacity());
        Assert.assertEquals(expectedNetworkLoad, resultSummary.getNetworkLoad());
        Assert.assertEquals(expectedNetworkDemand, resultSummary.getNetworkDemand());
    }

}
