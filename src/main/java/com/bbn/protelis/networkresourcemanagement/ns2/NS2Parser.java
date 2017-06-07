package com.bbn.protelis.networkresourcemanagement.ns2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.protelis.lang.datatype.DeviceUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.NeverTerminate;
import com.bbn.protelis.networkresourcemanagement.BasicNetworkFactory;
import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.NetworkFactory;
import com.bbn.protelis.networkresourcemanagement.Node;
import com.bbn.protelis.networkresourcemanagement.NodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;
import com.bbn.protelis.networkresourcemanagement.testbed.ScenarioRunner;
import com.bbn.protelis.networkresourcemanagement.visualizer.BasicNetworkVisualizerFactory;
import com.bbn.protelis.networkresourcemanagement.visualizer.DisplayEdge;
import com.bbn.protelis.networkresourcemanagement.visualizer.DisplayNode;
import com.bbn.protelis.networkresourcemanagement.visualizer.ScenarioVisualizer;
import com.bbn.protelis.utils.StringUID;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

/**
 * Read NS2 files in and create a network for protelis.
 */
public final class NS2Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NS2Parser.class);

    private static final long BYTES_IN_KILOBYTE = 1024;
    private static final long BYTES_IN_MEGABYTE = BYTES_IN_KILOBYTE * 1024;

    /**
     * Name of the file that defines the network topology for a configuration.
     * This file is in the NS2 file format.
     */
    public static final String TOPOLOGY_FILENAME = "topology.ns";

    private NS2Parser() {
    }

    /**
     * Parse an NS2 file into a map of Nodes. This reads from a classpath
     * resource.
     * 
     * @param scenarioName
     *            name of the scenario to create
     * @param baseDirectory
     *            the directory that contains the data.
     * @param factory
     *            how to create {@link Node}s and {@link Link}s
     * @param <N>
     *            the {@link Node} type created
     * @param <L>
     *            the {@link Link} type created
     * @return the network scenario
     * @throws IOException
     *             if there is an error reading from the reader
     */
    public static <N extends Node, L extends Link> Scenario<N, L> parseFromResource(final String scenarioName,
            final String baseDirectory,
            final NetworkFactory<N, L> factory) throws IOException {

        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(baseDirectory + "/" + TOPOLOGY_FILENAME)) {

            return parseFromStream(scenarioName, baseDirectory, factory, stream, false);

        } // topology stream
    }

    /**
     * Parse an NS2 file into a map of Nodes. This reads from a file.
     * 
     * @param scenarioName
     *            name of the scenario to create
     * @param baseDirectory
     *            the directory that contains the data.
     * @param factory
     *            how to create {@link Node}s and {@link Link}s
     * @param <N>
     *            the {@link Node} type created
     * @param <L>
     *            the {@link Link} type created
     * @return the network scenario
     * @throws IOException
     *             if there is an error reading from the reader
     */
    public static <N extends Node, L extends Link> Scenario<N, L> parseFromFile(final String scenarioName,
            final String baseDirectory,
            final NetworkFactory<N, L> factory) throws IOException {

        try (InputStream stream = new FileInputStream(baseDirectory + "/" + TOPOLOGY_FILENAME)) {

            return parseFromStream(scenarioName, baseDirectory, factory, stream, true);

        } // topology stream
    }

    private static <L extends Link, N extends Node> Scenario<N, L> parseFromStream(final String scenarioName,
            final String baseDirectory,
            final NetworkFactory<N, L> factory,
            final InputStream stream,
            final boolean readingFromFile) throws IOException {
        final Map<String, N> nodesByName = new HashMap<>();
        final Set<L> links = new HashSet<>();

        String simulator = null;

        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            try (BufferedReader bufReader = new BufferedReader(reader)) {

                // final Pattern setRegExp = Pattern.compile("set (\\s+)
                // \\[([^]]+)\\]");
                final Pattern setRegExp = Pattern.compile("^set\\s+(\\S+)\\s+\\[([^]]+)\\]$");
                final Pattern bandwidthExp = Pattern.compile("^(\\d+\\.?\\d*)(\\S+)$");

                String line;
                while (null != (line = bufReader.readLine())) {
                    line = line.trim();
                    if ("".equals(line) || line.startsWith("#")) {
                        // comment or blank
                        continue;
                    } else if (line.startsWith("source ")) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Ignoring source line: " + line);
                        }
                    } else if (line.startsWith("set")) {
                        final Matcher match = setRegExp.matcher(line);
                        if (!match.matches()) {
                            throw new NS2FormatException("line doesn't match expected format for set: '" + line + "'");
                        }

                        final String name = match.group(1);

                        final String arguments = match.group(2);
                        if ("new Simulator".equals(arguments)) {
                            if (null != simulator) {
                                throw new NS2FormatException("Cannot have 2 simulators: " + simulator + " and " + name);
                            }

                            simulator = name;
                        } else {
                            final String[] tokens = arguments.split("\\s");
                            if (tokens[0].startsWith("$")) {
                                final String self = tokens[0].substring(1);
                                if (null == simulator) {
                                    throw new NS2FormatException(
                                            "Cannot constructor nodes and links without a simulator");
                                }
                                if (!self.equals(simulator)) {
                                    throw new NS2FormatException(
                                            "Only creating simulated objects is supported line: " + line);
                                }

                                final String objectType = tokens[1];

                                final Map<String, Object> extraData;
                                if (readingFromFile) {
                                    extraData = getNodeDataFromFile(baseDirectory, name);
                                } else {
                                    extraData = getNodeDataFromResource(baseDirectory, name);
                                }

                                if ("node".equals(objectType)) {
                                    final N node = factory.createNode(name, extraData);
                                    nodesByName.put(name, node);
                                } else if ("duplex-link".equals(objectType)) {
                                    if (!tokens[2].startsWith("$") || !tokens[3].startsWith("$")) {
                                        throw new NS2FormatException(
                                                "Expecting nodes for link to start with $ on line: " + line);
                                    }

                                    final String leftNodeName = tokens[2].substring(1);
                                    final String rightNodeName = tokens[3].substring(1);

                                    if (!nodesByName.containsKey(leftNodeName)) {
                                        throw new NS2FormatException(
                                                "Unknown node " + leftNodeName + " on line: " + line);
                                    }

                                    if (!nodesByName.containsKey(rightNodeName)) {
                                        throw new NS2FormatException(
                                                "Unknown node " + rightNodeName + " on line: " + line);
                                    }

                                    final String bandwidthStr = tokens[4];
                                    final Matcher bandwidthMatch = bandwidthExp.matcher(bandwidthStr);
                                    if (!bandwidthMatch.matches()) {
                                        throw new NS2FormatException(
                                                "Bandwidth spec doesn't match expected format: '" + bandwidthStr + "'");
                                    }
                                    final double bandwidth = Double.parseDouble(bandwidthMatch.group(1));
                                    final String bandwidthUnits = bandwidthMatch.group(2);
                                    final double bandwidthMultiplier;
                                    if ("mb".equalsIgnoreCase(bandwidthUnits)) {
                                        bandwidthMultiplier = BYTES_IN_MEGABYTE;
                                    } else if ("kb".equalsIgnoreCase(bandwidthUnits)) {
                                        bandwidthMultiplier = BYTES_IN_KILOBYTE;
                                    } else {
                                        throw new NS2FormatException("Unknown bandwidth units: " + bandwidthUnits);
                                    }

                                    // final String delayStr = tokens[5];
                                    // final String queueBehavior =
                                    // tokens[6];

                                    final N leftNode = nodesByName.get(leftNodeName);
                                    final N rightNode = nodesByName.get(rightNodeName);

                                    final L link = factory.createLink(name, leftNode, rightNode,
                                            bandwidth * bandwidthMultiplier);
                                    
                                    leftNode.addNeighbor(rightNode, link.getBandwidth());
                                    rightNode.addNeighbor(leftNode, link.getBandwidth());

                                    links.add(link);
                                } else {
                                    throw new NS2FormatException(
                                            "Unsupported object type: " + objectType + " on line: " + line);
                                }
                            }
                        }
                    } else if (line.startsWith("tb-set-node-os")) {
                        final String[] tokens = line.split("\\s");
                        if (tokens.length != 3) {
                            throw new NS2FormatException("Expecting tb-set-node-os to have 3 tokens: " + line);
                        }

                        if (!tokens[1].startsWith("$")) {
                            throw new NS2FormatException("Expecting node name to start with $ on line: " + line);
                        }
                        final String nodeName = tokens[1].substring(1);

                        if (!nodesByName.containsKey(nodeName)) {
                            throw new NS2FormatException("Unknown node " + nodeName + " on line: " + line);
                        }

                        // final Node node = nodes.get(nodeName);
                        // node.operatingSystem = tokens[2];

                    } else {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Ignoring unknown line '" + line + "'");
                        }
                    }
                }

            } // bufReader
        } // input stream reader

        final Map<DeviceUID, N> nodes = nodesByName.entrySet().stream()
                .collect(Collectors.toMap(e -> new StringUID(e.getKey()), Map.Entry::getValue));
        final Scenario<N, L> scenario = new Scenario<>(scenarioName, nodes, links);
        return scenario;
    }

    /**
     * Read the data for a node that isn't in the NS2 file. This reads from a
     * class resource relative to baseDirectory.
     * 
     * @param baseDirectory
     *            the directory that the dataset is in
     * @param nodeName
     *            the name of the node (used for the filename)
     * @return the data that was read
     * @throws IOException
     *             if there is an error reading from the file
     */
    @Nonnull
    private static Map<String, Object> getNodeDataFromResource(final String baseDirectory, final String nodeName)
            throws IOException {

        final String path = baseDirectory + "/" + nodeName + ".json";

        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            return getNodeDataFromStream(stream);
        } // stream

    }

    /**
     * Read the data for a node that isn't in the NS2 file. This reads from a
     * file relative to baseDirectory.
     * 
     * @param baseDirectory
     *            the directory that the dataset is in
     * @param nodeName
     *            the name of the node (used for the filename)
     * @return the data that was read
     * @throws IOException
     *             if there is an error reading from the file
     */
    @Nonnull
    private static Map<String, Object> getNodeDataFromFile(final String baseDirectory, final String nodeName)
            throws IOException {

        final String path = baseDirectory + "/" + nodeName + ".json";
        final File file = new File(path);
        if (file.exists()) {
            try (InputStream stream = new FileInputStream(path)) {
                return getNodeDataFromStream(stream);
            } // stream
        } else {
            return Collections.emptyMap();
        }

    }

    private static Map<String, Object> getNodeDataFromStream(final InputStream stream) {
        if (null != stream) {
            @SuppressWarnings("unchecked")
            final JsonObject<String, Object> obj = (JsonObject<String, Object>) JsonReader.jsonToJava(stream,
                    Collections.singletonMap(JsonReader.USE_MAPS, true));
            return obj;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Thrown when there is an error in the NS2 file format.
     *
     */
    public static final class NS2FormatException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * 
         * @param message
         *            the reason the exception is thrown
         */
        public NS2FormatException(final String message) {
            super(message);
        }
    }

    /**
     * Open up the network specified by the first argument.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        try {
            if (args.length < 1) {
                LOGGER.error("You need to spcify the file to load");
                return;
            }

            final NodeLookupService lookupService = new LocalNodeLookupService(5000);

            final BasicNetworkFactory factory = new BasicNetworkFactory(lookupService,
                    "/protelis/com/bbn/resourcemanagement/resourcetracker.pt", false);
            final Scenario<Node, Link> scenario = NS2Parser.parseFromFile(args[0], args[0], factory);

            scenario.setTerminationCondition(new NeverTerminate<>());

            final BasicNetworkVisualizerFactory<Node, Link> visFactory = new BasicNetworkVisualizerFactory<>();
            final ScenarioVisualizer<DisplayNode, DisplayEdge, Node, Link> visualizer = new ScenarioVisualizer<>(
                    scenario, visFactory);

            final ScenarioRunner<Node, Link> emulation = new ScenarioRunner<>(scenario, visualizer);
            emulation.run();

            System.exit(0);
        } catch (final IOException ioe) {
            LOGGER.error("Error reading the simulation at " + args[0], ioe);
            System.exit(1);
        }

    }

}
