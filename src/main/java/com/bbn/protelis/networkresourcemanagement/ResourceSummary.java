package com.bbn.protelis.networkresourcemanagement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.utils.ImmutableUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Summary information about the resources in a region. See
 * {@link ResourceReport} for information about capacity vs. load vs. demand.
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSummary.class);

    /**
     * 
     * @param region
     *            see {@link ResourceSummary#getRegion()}
     * @param serverLoad
     *            see {@link #getServerLoad()}
     * @param serverCapacity
     *            see {@link #getServerCapacity()}
     * @param networkCapacity
     *            see {@link #getNetworkCapacity()}
     * @param networkLoad
     *            see {@link #getNetworkLoad()}
     * @param networkDemand
     *            see {@link #getNetworkDemand()}
     * @param serverDemand
     *            see {@link #getServerDemand()}
     * @param minTimestamp
     *            see {@link #getMinTimestamp()}
     * @param maxTimestamp
     *            see {@link #getMaxTimestamp()}
     * @param demandEstimationWindow
     *            see {@link #getDemandEstimationWindow()}
     * @param serverAverageProcessingTimeCount
     *            Used to compute {@link #getServerAverageProcessingTime()}
     * @param serverAverageProcessingTimeSum
     *            Used to compute {@link #getServerAverageProcessingTime()}
     * 
     */
    public ResourceSummary(@JsonProperty("region") @Nonnull final RegionIdentifier region,
            @JsonProperty("minTimestamp") final long minTimestamp,
            @JsonProperty("maxTimestamp") final long maxTimestamp,
            @JsonProperty("demandEstimationWindow") @Nonnull final ResourceReport.EstimationWindow demandEstimationWindow,

            @JsonProperty("serverCapacity") @Nonnull final ImmutableMap<NodeAttribute<?>, Double> serverCapacity,
            @JsonProperty("serverLoad") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad,
            @JsonProperty("serverDemand") @Nonnull final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand,

            @JsonProperty("serverAverageProcessingTimeCount") @Nonnull final ImmutableMap<ServiceIdentifier<?>, Integer> serverAverageProcessingTimeCount,
            @JsonProperty("serverAverageProcessingTimeSum") @Nonnull final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTimeSum,

            @JsonProperty("networkCapacity") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity,
            @JsonProperty("networkLoad") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad,
            @JsonProperty("networkDemand") @Nonnull final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand) {
        this.region = region;
        this.minTimestamp = minTimestamp;
        this.maxTimestamp = maxTimestamp;
        this.demandEstimationWindow = demandEstimationWindow;
        this.serverLoad = serverLoad;
        this.serverDemand = serverDemand;
        this.serverCapacity = serverCapacity;
        this.serverAverageProcessingTimeCount = serverAverageProcessingTimeCount;
        this.serverAverageProcessingTimeSum = serverAverageProcessingTimeSum;

        final ImmutableMap.Builder<ServiceIdentifier<?>, Double> avgProcTime = ImmutableMap.builder();
        serverAverageProcessingTimeCount.forEach((service, count) -> {
            final double sum = serverAverageProcessingTimeSum.getOrDefault(service, 0D);
            final double average = sum / count;
            avgProcTime.put(service, average);
        });
        this.serverAverageProcessingTime = avgProcTime.build();

        this.networkCapacity = networkCapacity;
        this.networkLoad = networkLoad;
        this.networkDemand = networkDemand;
    }

    private final RegionIdentifier region;

    /**
     * @return the region that this summary information is for
     */
    @Nonnull
    public final RegionIdentifier getRegion() {
        return region;
    }

    private final long minTimestamp;

    /**
     * @return the minimum timestamp of the reports combined to create this
     *         summary
     * @see ResourceReport#getTimestamp()
     */
    public long getMinTimestamp() {
        return minTimestamp;
    }

    private final long maxTimestamp;

    /**
     * @return the maximum timestamp of the reports combined to create this
     *         summary
     * @see ResourceReport#getTimestamp()
     */
    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    private final ResourceReport.EstimationWindow demandEstimationWindow;

    /**
     * @return the window over which the demand values are computed
     * @see #getServerDemand()
     * @see #getNetworkDemand()
     */
    @Nonnull
    public ResourceReport.EstimationWindow getDemandEstimationWindow() {
        return demandEstimationWindow;
    }

    private final ImmutableMap<ServiceIdentifier<?>, Integer> serverAverageProcessingTimeCount;

    /**
     * @return Used to compute {@link #getServerAverageProcessingTime()} when
     *         executing {@link #merge(ResourceSummary, ResourceSummary)}.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Integer> getServerAverageProcessingTimeCount() {
        return serverAverageProcessingTimeCount;
    }

    private final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTimeSum;

    /**
     * @return Used to compute {@link #getServerAverageProcessingTime()} when
     *         executing {@link #merge(ResourceSummary, ResourceSummary)}.
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Double> getServerAverageProcessingTimeSum() {
        return serverAverageProcessingTimeSum;
    }

    private final ImmutableMap<ServiceIdentifier<?>, Double> serverAverageProcessingTime;

    /**
     * 
     * @return the average processing time for services in this region
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, Double> getServerAverageProcessingTime() {
        return serverAverageProcessingTime;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad;

    /**
     * Get server load for this region. This is a measured value. service ->
     * source region of the load -> measured attribute -> value.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getComputeLoad()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getServerLoad() {
        return serverLoad;
    }

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand;

    /**
     * Get server estimated demand for this region. service -> source region for
     * the demand -> attribute -> value.
     * 
     * @return the demand information. Not null.
     * @see ResourceReport#getComputeDemand()
     */
    @Nonnull
    public ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            getServerDemand() {
        return serverDemand;
    }

    private final ImmutableMap<NodeAttribute<?>, Double> serverCapacity;

    /**
     * Server capacity for this region.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getComputeCapacity()
     */
    @Nonnull
    public ImmutableMap<NodeAttribute<?>, Double> getServerCapacity() {
        return serverCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity;

    /**
     * Network capacity for neighboring regions. neighbor region -> attribute ->
     * value. Only direct neighbors are reported.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getNetworkCapacity()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> getNetworkCapacity() {
        return networkCapacity;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad;

    /**
     * Network load and where it comes from. neighbor region -> source region ->
     * service -> attribute -> value.
     * 
     * Only direct neighbors are reported.
     * 
     * @return the summary information. Not null.
     * @see ResourceReport#getNetworkLoad()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>>
            getNetworkLoad() {
        return networkLoad;
    }

    private final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand;

    /**
     * Network demand for neighboring regions. Only direct neighbors are
     * reported. See {@link #getNetworkLoad()} for a description of the map.
     * 
     * @return the demand information. Not null.
     * @see ResourceReport#getNetworkDemand()
     */
    @Nonnull
    public ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>>
            getNetworkDemand() {
        return networkDemand;
    }

    /**
     * Create a null summary object. This is used by Protelis as the base object
     * when creating a regional summary. Uses
     * {@link ResourceReport#NULL_TIMESTAMP} as the minimum and maximum
     * timestamps.
     * 
     * @param region
     *            the region
     * @param estimationWindow
     *            the window over which demand is estimated
     * @return empty summary for a region
     */
    public static ResourceSummary getNullSummary(@Nonnull final RegionIdentifier region,
            @Nonnull final ResourceReport.EstimationWindow estimationWindow) {
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = ImmutableMap
                .of();
        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = ImmutableMap.of();
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = ImmutableMap
                .of();
        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = ImmutableMap
                .of();

        return new ResourceSummary(region, ResourceReport.NULL_TIMESTAMP, ResourceReport.NULL_TIMESTAMP,
                estimationWindow, serverCapacity, serverLoad, serverLoad, ImmutableMap.of(), ImmutableMap.of(),
                networkCapacity, networkLoad, networkLoad);
    }

    /**
     * Merge two summaries. This is used by Protelis when building up the
     * summary for a region.
     * 
     * @param one
     *            the first summary to merge. Not null.
     * @param two
     *            the second summary to merge. Not null.
     * @return a newly created summary. Not null, but may be the result of
     *         {@link #getNullSummary(RegionIdentifier)}
     * @throws IllegalArgumentException
     *             if the 2 summaries are not for the same region or the 2
     *             summarizes have different estimation windows
     */
    @Nonnull
    public static ResourceSummary merge(@Nonnull final ResourceSummary one, @Nonnull final ResourceSummary two) {
        if (!one.getRegion().equals(two.getRegion())) {
            throw new IllegalArgumentException("Cannot merge resource summaries from different regions: "
                    + one.getRegion() + " != " + two.getRegion());
        }
        if (!one.getDemandEstimationWindow().equals(two.getDemandEstimationWindow())) {
            throw new IllegalArgumentException("Cannot merge resource summaries with different estimation windows: "
                    + one.getDemandEstimationWindow() + " != " + two.getDemandEstimationWindow());
        }

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = mergeMaps3(
                one.getServerLoad(), two.getServerLoad());
        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = mergeDoubleMapViaSum(one.getServerCapacity(),
                two.getServerCapacity());

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand = mergeMaps3(
                one.getServerDemand(), two.getServerDemand());

        final Map<ServiceIdentifier<?>, Double> serverAvgProcTimeSum = new HashMap<>(
                one.getServerAverageProcessingTimeSum());
        two.getServerAverageProcessingTimeSum().forEach((service, sum) -> {
            serverAvgProcTimeSum.merge(service, sum, Double::sum);
        });

        final Map<ServiceIdentifier<?>, Integer> serverAvgProcTimeCount = new HashMap<>(
                one.getServerAverageProcessingTimeCount());
        two.getServerAverageProcessingTimeCount().forEach((service, count) -> {
            serverAvgProcTimeCount.merge(service, count, Integer::sum);
        });

        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = mergeMaps2(
                one.getNetworkCapacity(), two.getNetworkCapacity());
        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = mergeMaps4(
                one.getNetworkLoad(), two.getNetworkLoad());
        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand = mergeMaps4(
                one.getNetworkDemand(), two.getNetworkDemand());

        final long minTimestamp = Math.min(one.getMinTimestamp(), two.getMinTimestamp());
        final long maxTimestamp = Math.max(one.getMaxTimestamp(), two.getMaxTimestamp());
        return new ResourceSummary(one.getRegion(), minTimestamp, maxTimestamp, one.getDemandEstimationWindow(),
                serverCapacity, serverLoad, serverDemand, ImmutableMap.copyOf(serverAvgProcTimeCount),
                ImmutableMap.copyOf(serverAvgProcTimeSum), networkCapacity, networkLoad, networkDemand);
    }

    /**
     * Convert a {@link ResourceReport} into a {@link ResourceSummary} to be
     * merged later. This is used by Protelis when building up the summary for a
     * region.
     * 
     * @param report
     *            the report to convert
     * @param nodeToRegion
     *            convert node identifiers to region identifiers
     * @return a new {@link ResourceSummary} object
     * @throws ClassCastException
     *             if nodeRegions does not contain {@link RegionIdentifier}
     *             objects.
     */
    @Nonnull
    public static ResourceSummary convertToSummary(@Nonnull final ResourceReport report,
            @Nonnull final RegionLookupService nodeToRegion) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Converting report with server load {}", report.getComputeLoad());
        }

        final RegionIdentifier reportRegion = nodeToRegion.getRegionForNode(report.getNodeName());

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> reportServerLoad = report
                .getComputeLoad();
        final ImmutableMap.Builder<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoadBuilder = ImmutableMap
                .builder();
        reportServerLoad.forEach((service, map) -> {
            final ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>> regionMap = convertNodeToRegion(
                    nodeToRegion, map);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Converted {} -> {}", map, regionMap);
            }
            serverLoadBuilder.put(service, regionMap);
        });
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad = serverLoadBuilder
                .build();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("new summary server load {}", serverLoad);
        }

        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<NodeIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> reportServerDemand = report
                .getComputeDemand();
        final ImmutableMap.Builder<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemandBuilder = ImmutableMap
                .builder();
        reportServerDemand.forEach((service, map) -> {
            final ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>> regionMap = convertNodeToRegion(
                    nodeToRegion, map);
            serverDemandBuilder.put(service, regionMap);
        });
        final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverDemand = serverDemandBuilder
                .build();

        final ImmutableMap<NodeAttribute<?>, Double> serverCapacity = report.getNodeComputeCapacity();

        // sum of single node is the value
        final ImmutableMap<ServiceIdentifier<?>, Double> serverAvgProcTimeSum = report.getAverageProcessingTime();
        final ImmutableMap.Builder<ServiceIdentifier<?>, Integer> serverAvgProcTimeCount = ImmutableMap.builder();
        serverAvgProcTimeSum.forEach((service, sum) -> {
            serverAvgProcTimeCount.put(service, 1);
        });

        // use node network capacity as the summaries don't care about the
        // containers
        final ImmutableMap<RegionIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkCapacity = convertNodeToRegionExcludingThis1(
                nodeToRegion, reportRegion, report.getNetworkCapacity());

        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkLoad = convertNodeToRegionExcludingThis3(
                nodeToRegion, reportRegion, report.getNetworkLoad());

        final ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<LinkAttribute<?>, Double>>>> networkDemand = convertNodeToRegionExcludingThis3(
                nodeToRegion, reportRegion, report.getNetworkDemand());

        final ResourceSummary summary = new ResourceSummary(reportRegion, report.getTimestamp(), report.getTimestamp(),
                report.getDemandEstimationWindow(), serverCapacity, serverLoad, serverDemand,
                serverAvgProcTimeCount.build(), serverAvgProcTimeSum, networkCapacity, networkLoad, networkDemand);
        return summary;
    }

    /**
     * Convert the map of NodeIdentifiers to RegionIdentifiers. Exclude neighbor
     * nodes that are in thisRegion.
     */
    private static <T>
            ImmutableMap<RegionIdentifier, ImmutableMap<RegionIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<T, Double>>>>
            convertNodeToRegionExcludingThis3(@Nonnull final RegionLookupService nodeToRegion,
                    @Nonnull final RegionIdentifier thisRegion,
                    final ImmutableMap<NodeIdentifier, ImmutableMap<NodeIdentifier, ImmutableMap<ServiceIdentifier<?>, ImmutableMap<T, Double>>>> source) {
        final Map<RegionIdentifier, Map<RegionIdentifier, Map<ServiceIdentifier<?>, Map<T, Double>>>> dest = new HashMap<>();

        source.forEach((neighborNode, neighborData) -> {
            final RegionIdentifier neighborRegion = nodeToRegion.getRegionForNode(neighborNode);
            if (null != neighborRegion) {
                if (!thisRegion.equals(neighborRegion)) {

                    final Map<RegionIdentifier, Map<ServiceIdentifier<?>, Map<T, Double>>> destNeighborData = dest
                            .computeIfAbsent(neighborRegion, k -> new HashMap<>());

                    neighborData.forEach((sourceNode, sourceData) -> {
                        final RegionIdentifier sourceRegion = nodeToRegion.getRegionForNode(sourceNode);
                        if (null != sourceRegion) {
                            final Map<ServiceIdentifier<?>, Map<T, Double>> destSourceData = destNeighborData
                                    .computeIfAbsent(sourceRegion, k -> new HashMap<>());

                            sourceData.forEach((service, serviceData) -> {
                                final Map<T, Double> destServiceData = destSourceData.computeIfAbsent(service,
                                        k -> new HashMap<>());

                                serviceData.forEach((attr, value) -> {
                                    destServiceData.merge(attr, value, Double::sum);
                                }); // foreach attribute

                            }); // foreach service
                        } else {
                            LOGGER.warn("Unable to find region for node {}", sourceNode);
                        }
                    }); // foreach source

                } // check for other region
            } else {
                LOGGER.warn("Unable to find region for node {}", neighborNode);
            }
        }); // foreach neighbor

        return ImmutableUtils.makeImmutableMap4(dest);
    }

    /**
     * Convert the map of NodeIdentifiers to RegionIdentifiers. Exclude this
     * region.
     */
    private static <T> ImmutableMap<RegionIdentifier, ImmutableMap<T, Double>> convertNodeToRegionExcludingThis1(
            @Nonnull final RegionLookupService nodeToRegion,
            @Nonnull final RegionIdentifier thisRegion,
            final ImmutableMap<NodeIdentifier, ImmutableMap<T, Double>> source) {

        final Map<RegionIdentifier, ImmutableMap<T, Double>> dest = new HashMap<>();
        source.forEach((node, v) -> {
            final RegionIdentifier region = nodeToRegion.getRegionForNode(node);
            if (null != region) {
                if (!thisRegion.equals(region)) {
                    dest.merge(region, v, ResourceSummary::mergeDoubleMapViaSum);
                }
            } else {
                LOGGER.warn("Unable to find region for node {}", node);
            }

        });

        return ImmutableMap.copyOf(dest);
    }

    /**
     * Convert the map of NodeIdentifiers to RegionIdentifiers.
     */
    private static <T> ImmutableMap<RegionIdentifier, ImmutableMap<T, Double>> convertNodeToRegion(
            @Nonnull final RegionLookupService nodeToRegion,
            final ImmutableMap<NodeIdentifier, ImmutableMap<T, Double>> source) {

        final Map<RegionIdentifier, ImmutableMap<T, Double>> dest = new HashMap<>();
        source.forEach((node, v) -> {
            final RegionIdentifier region = nodeToRegion.getRegionForNode(node);
            if (null != region) {
                dest.merge(region, v, ResourceSummary::mergeDoubleMapViaSum);
            } else {
                LOGGER.warn("Unable to find region for node {}", node);
            }

        });

        return ImmutableMap.copyOf(dest);
    }

    @Nonnull
    private static <K1, K2, K3, K4>
            ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>>
            mergeMaps4(
                    @Nonnull final ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>> one,
                    @Nonnull final ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>> two) {

        // add all keys that are in one and merge in the keys from two
        final Map<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>> newMap = new HashMap<>();
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>> oneEntry : one
                .entrySet()) {
            final ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>> twoValue = two.get(oneEntry.getKey());
            if (null != twoValue) {
                final ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>> oneValue = oneEntry.getValue();

                final ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>> newValue = mergeMaps3(oneValue,
                        twoValue);

                newMap.put(oneEntry.getKey(), newValue);
            } else {
                // no conflict, just add
                newMap.put(oneEntry.getKey(), oneEntry.getValue());
            }
        }

        // add all keys that are in two that aren't in one
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, ImmutableMap<K3, ImmutableMap<K4, Double>>>> twoEntry : two
                .entrySet()) {
            if (!one.containsKey(twoEntry.getKey())) {
                newMap.put(twoEntry.getKey(), twoEntry.getValue());
            }
        }

        return ImmutableMap.copyOf(newMap);
    }

    @Nonnull
    private static <K1, K2, K3> ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> mergeMaps3(
            @Nonnull final ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> one,
            @Nonnull final ImmutableMap<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> two) {

        // add all keys that are in one and merge in the keys from two
        final Map<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> newMap = new HashMap<>();
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> oneEntry : one.entrySet()) {
            final ImmutableMap<K2, ImmutableMap<K3, Double>> twoValue = two.get(oneEntry.getKey());
            if (null != twoValue) {
                final ImmutableMap<K2, ImmutableMap<K3, Double>> oneValue = oneEntry.getValue();

                final ImmutableMap<K2, ImmutableMap<K3, Double>> newValue = mergeMaps2(oneValue, twoValue);

                newMap.put(oneEntry.getKey(), newValue);
            } else {
                // no conflict, just add
                newMap.put(oneEntry.getKey(), oneEntry.getValue());
            }
        }

        // add all keys that are in two that aren't in one
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, ImmutableMap<K3, Double>>> twoEntry : two.entrySet()) {
            if (!one.containsKey(twoEntry.getKey())) {
                newMap.put(twoEntry.getKey(), twoEntry.getValue());
            }
        }

        return ImmutableMap.copyOf(newMap);
    }

    /**
     * Create a new map that sums the values of the inner-most map for matching
     * keys.
     * 
     * @param one
     *            the first map to combine
     * @param two
     *            the second map to combine
     * @return a new map
     * @param <K1>
     *            first level lookup
     * @param <K2>
     *            second level lookup
     */
    @Nonnull
    public static <K1, K2> ImmutableMap<K1, ImmutableMap<K2, Double>> mergeMaps2(
            @Nonnull final ImmutableMap<K1, ImmutableMap<K2, Double>> one,
            @Nonnull final ImmutableMap<K1, ImmutableMap<K2, Double>> two) {
        final Map<K1, ImmutableMap<K2, Double>> newMap = new HashMap<>();

        // add all keys that are in one and merge in the keys from two
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, Double>> oneEntry : one.entrySet()) {
            final ImmutableMap<K2, Double> twoValue = two.get(oneEntry.getKey());
            if (null != twoValue) {
                final ImmutableMap<K2, Double> oneValue = oneEntry.getValue();

                final ImmutableMap<K2, Double> newValue = mergeDoubleMapViaSum(oneValue, twoValue);

                newMap.put(oneEntry.getKey(), newValue);
            } else {
                // no conflict, just add
                newMap.put(oneEntry.getKey(), oneEntry.getValue());
            }
        }

        // add all keys that are in two that aren't in one
        for (final ImmutableMap.Entry<K1, ImmutableMap<K2, Double>> twoEntry : two.entrySet()) {
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
     * @param <T>
     *            the type of the map key
     */
    @Nonnull
    public static <T> ImmutableMap<T, Double> mergeDoubleMapViaSum(@Nonnull final ImmutableMap<T, Double> one,
            @Nonnull final ImmutableMap<T, Double> two) {

        final Map<T, Double> newMap = new HashMap<>(one);
        two.forEach((k, v) -> newMap.merge(k, v, Double::sum));

        final ImmutableMap<T, Double> newImmutableMap = ImmutableMap.copyOf(newMap);

        return newImmutableMap;
    }

    @Override
    public String toString() {
        return "{" + " region: " + getRegion() + " serverLoad: " + getServerLoad() + "}";
    }
}
