package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.Field;

import com.google.common.collect.ImmutableMap;

/**
 * Summary information about the resources in a region.
 * 
 */
public class ResourceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param region
     *            the name of this region
     * @param clientDemand
     *            the client demand for services from this region. Key is
     *            service name, value is map of attribute to value
     * @param serverCapacity
     *            the capacity of the servers in this region
     * @param neighborLinkCapacity
     *            the capacity of the links to the neighboring regions (key is
     *            region name)
     * @param neighborLinkDemand
     *            the demand on the links to the neighboring regions (key is
     *            region name)
     */
    public ResourceSummary(@Nonnull final RegionIdentifier region,
            @Nonnull final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> clientDemand,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity,
            @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.region = region;
        this.clientDemand = clientDemand;
        this.serverCapacity = serverCapacity;
        this.neighborLinkCapacity = neighborLinkCapacity;
        this.neighborLinkDemand = neighborLinkDemand;
    }

    private final RegionIdentifier region;

    /**
     * @return the name of the region
     */
    @Nonnull
    public final RegionIdentifier getRegion() {
        return region;
    }

    private final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> clientDemand;

    /**
     * Get client demand for this region. Key is the service name, value is the
     * demand by {@link NodeAttribute}.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> getClientDemand() {
        return clientDemand;
    }

    private final ImmutableMap<NodeAttribute, Double> serverCapacity;

    /**
     * Server capacity for this region.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<NodeAttribute, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity;

    /**
     * Link capacity for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        return neighborLinkCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Link demand for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

    /**
     * 
     * @param region
     *            the region
     * @return empty summary for a region
     */
    public static ResourceSummary getNullSummary(@Nonnull final RegionIdentifier region) {
        final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> clientDemand = ImmutableMap.of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = ImmutableMap
                .of();
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap
                .of();

        return new ResourceSummary(region, clientDemand, serverCapacity, neighborLinkCapacity, neighborLinkDemand);
    }

    /**
     * Merge two summaries.
     * 
     * @param one
     *            the first summary to merge. Not null.
     * @param two
     *            the second summary to merge. Not null.
     * @return a newly created summary. Not null, but may be the result of
     *         {@link #getNullSummary(String)}
     * @throws IllegalArgumentException
     *             if the 2 summaries are not for the same region
     */
    @Nonnull
    public static ResourceSummary merge(@Nonnull final ResourceSummary one, @Nonnull final ResourceSummary two) {
        if (!one.getRegion().equals(two.getRegion())) {
            throw new IllegalArgumentException("Cannot merge resource summaries from different regions");
        }

        final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> clientDemand = mergeStringAnyDoubleMapViaSum(
                one.getClientDemand(), two.getClientDemand());
        final ImmutableMap<NodeAttribute, Double> serverCapacity = mergeNodeDoubleMapViaSum(one.getServerCapacity(),
                two.getServerCapacity());

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = mergeStringAnyDoubleMapViaSum(
                one.getNeighborLinkCapacity(), two.getNeighborLinkCapacity());
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = mergeStringAnyDoubleMapViaSum(
                one.getNeighborLinkDemand(), two.getNeighborLinkDemand());

        return new ResourceSummary(one.getRegion(), clientDemand, serverCapacity, neighborLinkCapacity,
                neighborLinkDemand);
    }

    /**
     * Convert a {@link ResourceReport} into a {@link ResourceSummary} to be
     * merged later.
     * 
     * @param report
     *            the report to convert
     * @param nodeToRegion
     *            the mapping of {@link NodeIdentifier} to
     *            {@link RegionIdentifier}.
     * @return a new {@link ResourceSummary} object
     * @throws ClassCastException
     *             if nodeRegions does not contain {@link RegionIdentifier}
     *             objects.
     */
    @Nonnull
    public static ResourceSummary convertToSummary(@Nonnull final ResourceReport report,
            @Nonnull final Field nodeToRegion) {
        final ImmutableMap<ServiceIdentifier, ImmutableMap<NodeAttribute, Double>> clientDemand = report
                .getClientDemand();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = report.getServerCapacity();

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = convertNodeToRegion(
                nodeToRegion, report.getNeighborLinkCapacity());

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = convertNodeToRegion(
                nodeToRegion, report.getNeighborLinkDemand());

        final RegionIdentifier reportRegion = (RegionIdentifier) nodeToRegion.getSample(report.getNodeName());

        final ResourceSummary summary = new ResourceSummary(reportRegion, clientDemand, serverCapacity,
                neighborLinkCapacity, neighborLinkDemand);
        return summary;
    }

    private static <T> ImmutableMap<RegionIdentifier, ImmutableMap<T, Double>> convertNodeToRegion(
            @Nonnull final Field nodeToRegion, final ImmutableMap<NodeIdentifier, ImmutableMap<T, Double>> source) {

        final Map<RegionIdentifier, ImmutableMap<T, Double>> dest = new HashMap<>();
        source.forEach((k, v) -> dest.merge((RegionIdentifier) nodeToRegion.getSample(k), v,
                ResourceSummary::mergeNodeDoubleMapViaSum));

        return ImmutableMap.copyOf(dest);
    }

    @Nonnull
    private static <ID, T> ImmutableMap<ID, ImmutableMap<T, Double>> mergeStringAnyDoubleMapViaSum(
            @Nonnull final ImmutableMap<ID, ImmutableMap<T, Double>> one,
            @Nonnull final ImmutableMap<ID, ImmutableMap<T, Double>> two) {
        final Map<ID, ImmutableMap<T, Double>> newMap = new HashMap<>();
        for (final ImmutableMap.Entry<ID, ImmutableMap<T, Double>> oneEntry : one.entrySet()) {
            final ImmutableMap<T, Double> twoValue = two.get(oneEntry.getKey());
            if (null != twoValue) {
                final ImmutableMap<T, Double> oneValue = oneEntry.getValue();

                final ImmutableMap<T, Double> newValue = mergeNodeDoubleMapViaSum(oneValue, twoValue);

                newMap.put(oneEntry.getKey(), newValue);
            } else {
                // no conflict, just add
                newMap.put(oneEntry.getKey(), oneEntry.getValue());
            }
        }
        for (final ImmutableMap.Entry<ID, ImmutableMap<T, Double>> twoEntry : two.entrySet()) {
            if (!one.containsKey(twoEntry.getKey())) {
                newMap.put(twoEntry.getKey(), twoEntry.getValue());
            }
        }

        return ImmutableMap.copyOf(newMap);
    }

    @Nonnull
    private static <T> ImmutableMap<T, Double> mergeNodeDoubleMapViaSum(@Nonnull final ImmutableMap<T, Double> one,
            @Nonnull final ImmutableMap<T, Double> two) {

        final Map<T, Double> newMap = new HashMap<>(one);
        two.forEach((k, v) -> newMap.merge(k, v, Double::sum));

        final ImmutableMap<T, Double> newImmutableMap = ImmutableMap.copyOf(newMap);

        return newImmutableMap;
    }

}
