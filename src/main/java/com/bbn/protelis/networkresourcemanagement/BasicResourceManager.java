package com.bbn.protelis.networkresourcemanagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Basic {@link ResourceManager} that expects to get report values from the
 * extra data that was parsed when the node was created. The demand is static
 * and equal to the load. All load is from inside the same region. One could
 * expand the extra data to include this information as well.
 */
public class BasicResourceManager implements ResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicResourceManager.class);

    private final Map<String, Object> extraData;

    /**
     * Used to find resource report information in extraData.
     */
    public static final String EXTRA_DATA_RESOURCE_REPORT_KEY = "resource-report";
    /**
     * Used to find server load in
     * {@link BasicResourceManager#EXTRA_DATA_RESOURCE_REPORT_KEY}.
     */
    public static final String SERVER_LOAD_KEY = "serverLoad";
    /**
     * Used to find server capacity in
     * {@link BasicResourceManager#EXTRA_DATA_RESOURCE_REPORT_KEY}.
     */
    public static final String SERVER_CAPACITY_KEY = "serverCapacity";
    /**
     * Used to find the server average processing time per service in
     * {@link #EXTRA_DATA_RESOURCE_REPORT_KEY}.
     */
    public static final String SERVER_AVG_PROC_TIME_KEY = "serverAverageProcessingTime";
    /**
     * Used to find network load in
     * {@link BasicResourceManager#EXTRA_DATA_RESOURCE_REPORT_KEY}.
     */
    public static final String NETWORK_LOAD_KEY = "networkLoad";

    private final ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> serverLoad;
    private final ImmutableMap<NodeAttribute<?>, Double> serverCapacity;
    private final ImmutableMap<ServiceIdentifier<?>, Double> serverAvgProcTime;
    private final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> networkLoad;

    /**
     * Construct a resource manager for the specified node.
     * 
     * @param node
     *            the node that this resource manager is for
     * @param extraData
     *            the extra data for the node. This contains the information to
     *            return from the methods.
     * @see NetworkServer#processExtraData(Map)
     */
    public BasicResourceManager(@Nonnull final NetworkServer node, @Nonnull final Map<String, Object> extraData) {
        this.node = node;
        this.extraData = new HashMap<String, Object>(extraData);

        final Object resourceReportValuesRaw = this.extraData.get(EXTRA_DATA_RESOURCE_REPORT_KEY);
        if (null != resourceReportValuesRaw && resourceReportValuesRaw instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> resourceReportValues = (Map<String, Object>) resourceReportValuesRaw;

            this.serverLoad = parseClientDemand(resourceReportValues);
            this.serverCapacity = parseServerCapacity(resourceReportValues);
            this.serverAvgProcTime = parseServerAverageProcessingTime(resourceReportValues);
            this.networkLoad = parseNeighborLinkDemand(resourceReportValues);
        } else {
            this.serverLoad = ImmutableMap.of();
            this.serverCapacity = ImmutableMap.of();
            this.serverAvgProcTime = ImmutableMap.of();
            this.networkLoad = ImmutableMap.of();
        }

    }

    private final NetworkServer node;

    @Nonnull
    private ImmutableMap<NodeAttribute<?>, Double>
            parseServerCapacity(@Nonnull final Map<String, Object> resourceReportValues) {
        final Object raw = resourceReportValues.get(SERVER_CAPACITY_KEY);
        if (null != raw && raw instanceof Map) {
            // found something specified in the extra data

            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) raw;

            final ImmutableMap<NodeAttribute<?>, Double> specifiedServerCapacity = parseNodeAttributeDoubleMap(map);

            return specifiedServerCapacity;
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>>
            parseNeighborLinkDemand(@Nonnull final Map<String, Object> resourceReportValues) {
        final Object specifiedDemandRaw = resourceReportValues.get(NETWORK_LOAD_KEY);
        if (null != specifiedDemandRaw && specifiedDemandRaw instanceof Map) {
            // found something specified in the extra data

            // this will contain the new demand
            ImmutableMap.Builder<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> builder = ImmutableMap
                    .builder();

            @SuppressWarnings("unchecked")
            final Map<String, Object> specifiedDemand = (Map<String, Object>) specifiedDemandRaw;
            specifiedDemand.forEach((nodeName, v) -> {

                if (null != v && v instanceof Map) {
                    // found demand for the specified service

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> individualDemand = (Map<String, Object>) v;
                    final ImmutableMap<LinkAttribute<?>, Double> serviceDemand = parseLinkAttributeDoubleMap(
                            individualDemand);
                    builder.put(new StringNodeIdentifier(nodeName), serviceDemand);
                } else {
                    LOGGER.warn("While parsing resource report for node " + nodeName + " the service " + nodeName
                            + " doesn't have valid client demand data");
                }
            });
            return builder.build();
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private ImmutableMap<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>>
            parseClientDemand(@Nonnull final Map<String, Object> resourceReportValues) {
        final Object specifiedClientDemandRaw = resourceReportValues.get(SERVER_LOAD_KEY);
        if (null != specifiedClientDemandRaw && specifiedClientDemandRaw instanceof Map) {
            // found something specified in the extra data

            // this will contain the new clientDemand
            ImmutableMap.Builder<ServiceIdentifier<?>, ImmutableMap<RegionIdentifier, ImmutableMap<NodeAttribute<?>, Double>>> builder = ImmutableMap
                    .builder();

            @SuppressWarnings("unchecked")
            final Map<String, Object> specifiedClientDemand = (Map<String, Object>) specifiedClientDemandRaw;
            specifiedClientDemand.forEach((serviceName, v) -> {

                if (null != v && v instanceof Map) {
                    // found demand for the specified service

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> individualClientDemand = (Map<String, Object>) v;
                    final ImmutableMap<NodeAttribute<?>, Double> serviceDemand = parseNodeAttributeDoubleMap(
                            individualClientDemand);

                    // builder.put(new ApplicationIdentifier(new
                    // GAV("groupPlaceholder", serviceName,
                    // "versionPlaceholder")), serviceDemand);
                    builder.put(new StringServiceIdentifier(serviceName),
                            ImmutableMap.of(node.getRegionIdentifier(), serviceDemand));

                } else {
                    LOGGER.warn("While parsing resource report for node " + node.getName() + " the service "
                            + serviceName + " doesn't have valid client demand data");
                }
            });
            return builder.build();
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private ImmutableMap<ServiceIdentifier<?>, Double>
            parseServerAverageProcessingTime(@Nonnull final Map<String, Object> resourceReportValues) {
        final Object avgProcTimeRaw = resourceReportValues.get(SERVER_LOAD_KEY);
        if (null != avgProcTimeRaw && avgProcTimeRaw instanceof Map) {
            // found something specified in the extra data

            final ImmutableMap.Builder<ServiceIdentifier<?>, Double> builder = ImmutableMap.builder();

            @SuppressWarnings("unchecked")
            final Map<String, Object> avgProcTime = (Map<String, Object>) avgProcTimeRaw;
            avgProcTime.forEach((serviceName, avgObj) -> {

                if (avgObj instanceof Number) {
                    final double value = ((Number) avgObj).doubleValue();
                    builder.put(new StringServiceIdentifier(serviceName), value);
                }
            });
            return builder.build();
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private ImmutableMap<NodeAttribute<?>, Double>
            parseNodeAttributeDoubleMap(@Nonnull final Map<String, Object> sourceMap) {
        ImmutableMap.Builder<NodeAttribute<?>, Double> builder = ImmutableMap.builder();

        sourceMap.forEach((attrStr, valueObj) -> {
            try {
                final NodeAttributeEnum attr = Enum.valueOf(NodeAttributeEnum.class, attrStr);
                if (valueObj instanceof Number) {
                    final double value = ((Number) valueObj).doubleValue();
                    builder.put(attr, value);
                }
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("While parsing resource report for node " + node.getName() + " '" + attrStr
                        + "' does not parse as a NodeAttribute, ignoring");
            }
        });

        return builder.build();
    }

    @Nonnull
    private ImmutableMap<LinkAttribute<?>, Double>
            parseLinkAttributeDoubleMap(@Nonnull final Map<String, Object> sourceMap) {
        ImmutableMap.Builder<LinkAttribute<?>, Double> builder = ImmutableMap.builder();

        sourceMap.forEach((attrStr, valueObj) -> {
            try {
                final LinkAttributeEnum attr = Enum.valueOf(LinkAttributeEnum.class, attrStr);
                if (valueObj instanceof Number) {
                    final double value = ((Number) valueObj).doubleValue();
                    builder.put(attr, value);
                }
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("While parsing resource report for node " + node.getName() + " '" + attrStr
                        + "' does not parse as a NodeAttribute, ignoring");
            }
        });

        return builder.build();
    }

    @Override
    public ResourceReport getCurrentResourceReport(@Nonnull final ResourceReport.EstimationWindow demandWindow) {
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> linkCapacity = node
                .getNeighborLinkCapacity(LinkAttributeEnum.DATARATE);
        final ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> linkDemand = computeNeighborLinkDemand();
        final ResourceReport report = new ResourceReport(new StringNodeIdentifier(node.getName()),
                System.currentTimeMillis(), demandWindow, this.serverCapacity, this.serverLoad, this.serverLoad,
                this.serverAvgProcTime, linkCapacity, linkDemand, linkDemand);
        return report;
    }

    @Nonnull
    private ImmutableMap<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> computeNeighborLinkDemand() {
        final ImmutableMap.Builder<NodeIdentifier, ImmutableMap<LinkAttribute<?>, Double>> builder = ImmutableMap
                .builder();
        this.node.getNeighbors().forEach(neighborId -> {
            if (this.networkLoad.containsKey(neighborId)) {
                builder.put(neighborId, this.networkLoad.get(neighborId));
            } else if (networkLoad.containsKey("*")) {
                builder.put(neighborId, this.networkLoad.get("*"));
            }
        });
        return builder.build();
    }

    @Override
    public boolean reserveContainer(@Nonnull final NodeIdentifier name,
            @Nonnull final ImmutableMap<String, String> arguments) {
        LOGGER.info("Reserved container {} with arguments {}", name, arguments);
        return true;
    }

    @Override
    public boolean releaseContainer(@Nonnull final NodeIdentifier name) {
        LOGGER.info("Released container {}", name);
        return true;
    }

    private final Map<NodeIdentifier, Set<ServiceIdentifier<?>>> runningServices = new HashMap<>();

    @Override
    public boolean startService(@Nonnull final NodeIdentifier containerName,
            @Nonnull final ServiceIdentifier<?> service) {
        LOGGER.info("Started service {} in container {}", service, containerName);
        final Set<ServiceIdentifier<?>> containerServices = runningServices.computeIfAbsent(containerName,
                c -> new HashSet<>());
        return containerServices.add(service);
    }

    @Override
    public boolean stopService(@Nonnull final NodeIdentifier containerName,
            @Nonnull final ServiceIdentifier<?> service) {
        LOGGER.info("Stopped service {} in container {}", service, containerName);
        final Set<ServiceIdentifier<?>> containerServices = runningServices.getOrDefault(containerName,
                new HashSet<>());
        return containerServices.remove(service);
    }

    @Override
    public ImmutableMap<NodeIdentifier, ImmutableSet<ServiceIdentifier<?>>> getRunningServices() {
        final ImmutableMap.Builder<NodeIdentifier, ImmutableSet<ServiceIdentifier<?>>> builder = ImmutableMap.builder();
        runningServices.forEach((name, services) -> {
            builder.put(name, ImmutableSet.copyOf(services));
        });
        return builder.build();
    }

    @Override
    public ImmutableMap<NodeAttribute<?>, Double> getServerCapacity() {
        return serverCapacity;
    }

}
