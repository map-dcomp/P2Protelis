package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Summary information about the resources in a region.
 * 
 */
public class ResourceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param name
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
    public ResourceSummary(@Nonnull final String name,
            @Nonnull final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand,
            @Nonnull final ImmutableMap<NodeAttribute, Double> serverCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity,
            @Nonnull final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand) {
        this.name = name;
        this.clientDemand = clientDemand;
        this.serverCapacity = serverCapacity;
        this.neighborLinkCapacity = neighborLinkCapacity;
        this.neighborLinkDemand = neighborLinkDemand;
    }

    private final String name;

    /**
     * @return the name of the region
     */
    public final String getRegionName() {
        return name;
    }

    private final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand;

    /**
     * Get client demand for this region. Key is the service name, value is the
     * demand by {@link NodeAttribute}.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> getClientDemand() {
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

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity;

    /**
     * Link capacity for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkCapacity() {
        return neighborLinkCapacity;
    }

    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Link demand for neighboring regions. Key is region name.
     * 
     * @return the summary information. Not null.
     */
    @Nonnull
    public ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> getNeighborLinkDemand() {
        return neighborLinkDemand;
    }

    /**
     * 
     * @param regionName
     *            the name of the region
     * @return empty summary for a region
     */
    public static ResourceSummary getNullSummary(@Nonnull final String regionName) {
        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand = ImmutableMap.of();
        final ImmutableMap<NodeAttribute, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = ImmutableMap.of();
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = ImmutableMap.of();

        return new ResourceSummary(regionName, clientDemand, serverCapacity, neighborLinkCapacity, neighborLinkDemand);
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
        if (!one.getRegionName().equals(two.getRegionName())) {
            throw new IllegalArgumentException("Cannot merge resource summaries from different regions");
        }

        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand = mergeStringAnyDoubleMapViaSum(
                one.getClientDemand(), two.getClientDemand());
        final ImmutableMap<NodeAttribute, Double> serverCapacity = mergeAnyDoubleMapViaSum(one.getServerCapacity(),
                two.getServerCapacity());

        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = mergeStringAnyDoubleMapViaSum(
                one.getNeighborLinkCapacity(), two.getNeighborLinkCapacity());
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = mergeStringAnyDoubleMapViaSum(
                one.getNeighborLinkDemand(), two.getNeighborLinkDemand());

        return new ResourceSummary(one.getRegionName(), clientDemand, serverCapacity, neighborLinkCapacity,
                neighborLinkDemand);
    }

    /**
     * Merge a report and a summary.
     * 
     * @param report
     *            the report to merge. Not null.
     * @param summary
     *            the summary to merge. Not null.
     * @return a newly created summary. Not null, but may be the result of
     *         {@link #getNullSummary(String)}.
     */
    @Nonnull
    public static ResourceSummary merge(@Nonnull final ResourceReport report, @Nonnull final ResourceSummary summary) {

        // TODO: do we need to check the region of the report?

        final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand = mergeStringAnyDoubleMapViaSum(
                report.getClientDemand(), summary.getClientDemand());

        final ImmutableMap<NodeAttribute, Double> serverCapacity = mergeAnyDoubleMapViaSum(report.getServerCapacity(),
                summary.getServerCapacity());

        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkCapacity = mergeStringAnyDoubleMapViaSum(
                report.getNeighborLinkCapacity(), summary.getNeighborLinkCapacity());

        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand = mergeStringAnyDoubleMapViaSum(
                report.getNeighborLinkDemand(), summary.getNeighborLinkDemand());

        return new ResourceSummary(summary.getRegionName(), clientDemand, serverCapacity, neighborLinkCapacity,
                neighborLinkDemand);
    }

    @Nonnull
    private static <T> ImmutableMap<String, ImmutableMap<T, Double>> mergeStringAnyDoubleMapViaSum(
            @Nonnull final ImmutableMap<String, ImmutableMap<T, Double>> one,
            @Nonnull final ImmutableMap<String, ImmutableMap<T, Double>> two) {
        final Map<String, ImmutableMap<T, Double>> newMap = new HashMap<>();
        for (final ImmutableMap.Entry<String, ImmutableMap<T, Double>> oneEntry : one.entrySet()) {
            final ImmutableMap<T, Double> twoValue = two.get(oneEntry.getKey());
            if (null != twoValue) {
                final ImmutableMap<T, Double> oneValue = oneEntry.getValue();

                final ImmutableMap<T, Double> newValue = mergeAnyDoubleMapViaSum(oneValue, twoValue);

                newMap.put(oneEntry.getKey(), newValue);
            } else {
                // no conflict, just add
                newMap.put(oneEntry.getKey(), oneEntry.getValue());
            }
        }
        for (final ImmutableMap.Entry<String, ImmutableMap<T, Double>> twoEntry : two.entrySet()) {
            if (!one.containsKey(twoEntry.getKey())) {
                newMap.put(twoEntry.getKey(), twoEntry.getValue());
            }
        }

        return ImmutableMap.copyOf(newMap);
    }

    /**
     * Create a new ImmutableMap that sums the values for matching keys.
     * 
     * @param one
     *            the first map to combine
     * @param two
     *            the second map to combine
     * @return a new map
     * @param <T> the type of the map key
     */
    @Nonnull
    public static <T> ImmutableMap<T, Double> mergeAnyDoubleMapViaSum(@Nonnull final ImmutableMap<T, Double> one,
            @Nonnull final ImmutableMap<T, Double> two) {

        final Map<T, Double> newMap = new HashMap<>(one);
        two.forEach((k, v) -> newMap.merge(k, v, Double::sum));

        final ImmutableMap<T, Double> newImmutableMap = ImmutableMap.copyOf(newMap);

        return newImmutableMap;
    }

}
