package com.bbn.protelis.networkresourcemanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Basic {@link ResourceManager} that expects to get report values from the
 * extra data that was parsed when the node was created.
 */
public class BasicResourceManager implements ResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicResourceManager.class);

    private final String nodeName;
    private final Map<String, Object> extraData;

    private static final String EXTRA_DATA_RESOURCE_REPORT_KEY = "resource-report";
    private static final String CLIENT_DEMAND_KEY = "clientDemand";
    private static final String SERVER_CAPACITY_KEY = "serverCapacity";
    private static final String NEIGHBOR_LINK_DEMAND_KEY = "neighborLinkDemand";

    private final ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> clientDemand;
    private final ImmutableMap<NodeAttribute, Double> serverCapacity;
    private final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> neighborLinkDemand;

    /**
     * Construct a resource manager for the specified node.
     * 
     * @param nodeName
     *            the node that this resource manager is for
     * @param extraData
     *            the extra data for the node. This contains the information to
     *            return from the methods.
     * @see NetworkServer#processExtraData(Map)
     */
    public BasicResourceManager(@Nonnull final String nodeName, @Nonnull final Map<String, Object> extraData) {
        this.nodeName = nodeName;
        this.extraData = new HashMap<String, Object>(extraData);

        final Object resourceReportValuesRaw = this.extraData.get(EXTRA_DATA_RESOURCE_REPORT_KEY);
        if (null != resourceReportValuesRaw && resourceReportValuesRaw instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> resourceReportValues = (Map<String, Object>) resourceReportValuesRaw;

            this.clientDemand = parseClientDemand(resourceReportValues);
            this.serverCapacity = parseServerCapacity(resourceReportValues);
            this.neighborLinkDemand = parseNeighborLinkDemand(resourceReportValues);
        } else {
            this.clientDemand = ImmutableMap.of();
            this.serverCapacity = ImmutableMap.of();
            this.neighborLinkDemand = ImmutableMap.of();
        }
    }

    private NetworkServer node = null;

    /**
     * Set the node object that is being used. This is used to get the link
     * capacity information.
     * 
     * @param node
     *            the node
     */
    public void setNode(@Nonnull final NetworkServer node) {
        this.node = node;
    }

    @Nonnull
    private ImmutableMap<NodeAttribute, Double> parseServerCapacity(
            @Nonnull final Map<String, Object> resourceReportValues) {
        final Object raw = resourceReportValues.get(SERVER_CAPACITY_KEY);
        if (null != raw && raw instanceof Map) {
            // found something specified in the extra data

            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) raw;

            final ImmutableMap<NodeAttribute, Double> specifiedServerCapacity = parseEnumDoubleMap(NodeAttribute.class,
                    map);

            return specifiedServerCapacity;
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> parseNeighborLinkDemand(
            @Nonnull final Map<String, Object> resourceReportValues) {
        final Object specifiedDemandRaw = resourceReportValues.get(NEIGHBOR_LINK_DEMAND_KEY);
        if (null != specifiedDemandRaw && specifiedDemandRaw instanceof Map) {
            // found something specified in the extra data

            // this will contain the new demand
            ImmutableMap.Builder<String, ImmutableMap<LinkAttribute, Double>> builder = ImmutableMap.builder();

            @SuppressWarnings("unchecked")
            final Map<String, Object> specifiedDemand = (Map<String, Object>) specifiedDemandRaw;
            specifiedDemand.forEach((nodeName, v) -> {

                if (null != v && v instanceof Map) {
                    // found demand for the specified service

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> individualDemand = (Map<String, Object>) v;
                    final ImmutableMap<LinkAttribute, Double> serviceDemand = parseEnumDoubleMap(LinkAttribute.class,
                            individualDemand);
                    builder.put(nodeName, serviceDemand);
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
    private ImmutableMap<String, ImmutableMap<NodeAttribute, Double>> parseClientDemand(
            @Nonnull final Map<String, Object> resourceReportValues) {
        final Object specifiedClientDemandRaw = resourceReportValues.get(CLIENT_DEMAND_KEY);
        if (null != specifiedClientDemandRaw && specifiedClientDemandRaw instanceof Map) {
            // found something specified in the extra data

            // this will contain the new clientDemand
            ImmutableMap.Builder<String, ImmutableMap<NodeAttribute, Double>> builder = ImmutableMap.builder();

            @SuppressWarnings("unchecked")
            final Map<String, Object> specifiedClientDemand = (Map<String, Object>) specifiedClientDemandRaw;
            specifiedClientDemand.forEach((serviceName, v) -> {

                if (null != v && v instanceof Map) {
                    // found demand for the specified service

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> individualClientDemand = (Map<String, Object>) v;
                    final ImmutableMap<NodeAttribute, Double> serviceDemand = parseEnumDoubleMap(NodeAttribute.class,
                            individualClientDemand);
                    builder.put(serviceName, serviceDemand);
                } else {
                    LOGGER.warn("While parsing resource report for node " + nodeName + " the service " + serviceName
                            + " doesn't have valid client demand data");
                }
            });
            return builder.build();
        } else {
            return ImmutableMap.of();
        }
    }

    @Nonnull
    private <T extends Enum<T>> ImmutableMap<T, Double> parseEnumDoubleMap(@Nonnull final Class<T> enumType,
            @Nonnull final Map<String, Object> sourceMap) {
        ImmutableMap.Builder<T, Double> builder = ImmutableMap.builder();

        sourceMap.forEach((attrStr, valueObj) -> {
            try {
                final T attr = Enum.valueOf(enumType, attrStr);
                if (valueObj instanceof Number) {
                    final double value = ((Number) valueObj).doubleValue();
                    builder.put(attr, value);
                }
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("While parsing resource report for node " + nodeName + " '" + attrStr
                        + "' does not parse as a NodeAttribute, ignoring");
            }
        });

        return builder.build();
    }

    @Override
    public ResourceReport getCurrentResourceReport() {
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> linkCapacity;
        if (null == node) {
            linkCapacity = ImmutableMap.of();
        } else {
            linkCapacity = node.getNeighborLinkCapacity();
        }
        final ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> linkDemand = computeNeighborLinkDemand();
        final ResourceReport report = new ResourceReport(nodeName, this.clientDemand, this.serverCapacity, linkCapacity,
                linkDemand);
        return report;
    }

    @Nonnull
    private ImmutableMap<String, ImmutableMap<LinkAttribute, Double>> computeNeighborLinkDemand() {
        if (null == node) {
            return ImmutableMap.of();
        } else {
            final ImmutableMap.Builder<String, ImmutableMap<LinkAttribute, Double>> builder = ImmutableMap.builder();
            this.node.getNeighbors().forEach(uid -> {
                final String neighborName = uid.getUID();
                if (this.neighborLinkDemand.containsKey(neighborName)) {
                    builder.put(neighborName, this.neighborLinkDemand.get(neighborName));
                } else if (neighborLinkDemand.containsKey("*")) {
                    builder.put(neighborName, this.neighborLinkDemand.get("*"));
                }
            });
            return builder.build();
        }
    }

    @Override
    public boolean reserveContainer(final String name, final Map<String, String> arguments) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean releaseContainer(final String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startTask(final String containerName,
            final String taskName,
            final List<String> arguments,
            final Map<String, String> environment) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopTask(final String containerName, final String taskName) {
        // TODO Auto-generated method stub
        return false;
    }

}
