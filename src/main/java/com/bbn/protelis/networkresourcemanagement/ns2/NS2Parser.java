/*BBN_LICENSE_START -- DO NOT MODIFY BETWEEN LICENSE_{START,END} Lines
Copyright (c) <2017,2018,2019,2020>, <Raytheon BBN Technologies>
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
package com.bbn.protelis.networkresourcemanagement.ns2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

import com.bbn.protelis.common.testbed.termination.NeverTerminate;
import com.bbn.protelis.networkresourcemanagement.BasicNetworkFactory;
import com.bbn.protelis.networkresourcemanagement.BasicResourceManagerFactory;
import com.bbn.protelis.networkresourcemanagement.DelegateRegionLookup;
import com.bbn.protelis.networkresourcemanagement.DnsNameIdentifier;
import com.bbn.protelis.networkresourcemanagement.NetworkClient;
import com.bbn.protelis.networkresourcemanagement.NetworkLink;
import com.bbn.protelis.networkresourcemanagement.NetworkServer;
import com.bbn.protelis.networkresourcemanagement.NodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;
import com.bbn.protelis.networkresourcemanagement.testbed.ScenarioRunner;
import com.bbn.protelis.networkresourcemanagement.visualizer.BasicNetworkVisualizerFactory;
import com.bbn.protelis.networkresourcemanagement.visualizer.DisplayEdge;
import com.bbn.protelis.networkresourcemanagement.visualizer.DisplayNode;
import com.bbn.protelis.networkresourcemanagement.visualizer.ScenarioVisualizer;
import com.bbn.protelis.utils.SimpleClock;
import com.bbn.protelis.utils.VirtualClock;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.google.common.collect.ImmutableMap;

/**
 * Read NS2 files in and create a network for protelis.
 */
public final class NS2Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NS2Parser.class);

    private static final double MEGABITS_IN_KILOBIT = 1 / 1000D;

    /**
     * Name of the file that defines the network topology for a configuration.
     * This file is in the NS2 file format.
     */
    public static final String TOPOLOGY_FILENAME = "topology.ns";

    private NS2Parser() {
    }

    private static final int SET_MIN_ARGUMENTS = 2;
    private static final int SET_TYPE_ARGUMENT_INDEX = 1;
    private static final int LINK_MIN_ARGUMENTS = 7;
    private static final int LINK_NODE1_ARGUMENT_INDEX = 2;
    private static final int LINK_NODE2_ARGUMENT_INDEX = 3;
    private static final int LINK_BANDWIDTH_ARGUMENT_INDEX = 4;
    private static final int LINK_DELAY_ARGUMENT_INDEX = 5;
    // private static final int LINK_QTYPE_ARGUMENT_INDEX = 6;

    /**
     * Parse an NS2 file into a scenario.
     * 
     * @param scenarioName
     *            name of the scenario to create
     * @param baseDirectory
     *            the directory that contains the data.
     * @return the network topology
     * @throws IOException
     *             if there is an error reading from the reader
     */
    public static Topology parse(final String scenarioName, final Path baseDirectory) throws IOException {
        final Map<String, Link> links = new HashMap<>();
        final Map<String, Node> nodesByName = new HashMap<>();
        final Map<String, Switch> lans = new HashMap<>();

        final Path topologyPath = baseDirectory.resolve(TOPOLOGY_FILENAME);

        String simulator = null;

        try (Reader reader = Files.newBufferedReader(topologyPath, StandardCharsets.UTF_8)) {
            try (BufferedReader bufReader = new BufferedReader(reader)) {

                final Pattern setRegExp = Pattern.compile("^set\\s+(\\S+)\\s+\\[([^]]+)\\]$");

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
                                            "Cannot construct nodes and links without a simulator");
                                }
                                if (!self.equals(simulator)) {
                                    throw new NS2FormatException(
                                            "Only creating simulated objects is supported line: " + line);
                                }

                                if (tokens.length < SET_MIN_ARGUMENTS) {
                                    throw new NS2FormatException("Expecting at least 2 arguments line: " + line);
                                }

                                final String objectType = tokens[SET_TYPE_ARGUMENT_INDEX];

                                final Map<String, Object> extraData = getNodeData(baseDirectory, name);

                                if ("node".equals(objectType)) {
                                    final Node node = new Node(name, extraData);
                                    nodesByName.put(name, node);
                                } else if ("duplex-link".equals(objectType)) {
                                    if (tokens.length < LINK_MIN_ARGUMENTS) {
                                        throw new NS2FormatException(
                                                "Expecting at least 7 arguments for duplex-link on line: " + line);
                                    }

                                    if (!tokens[LINK_NODE1_ARGUMENT_INDEX].startsWith("$")
                                            || !tokens[LINK_NODE2_ARGUMENT_INDEX].startsWith("$")) {
                                        throw new NS2FormatException(
                                                "Expecting nodes for link to start with $ on line: " + line);
                                    }

                                    final String leftNodeName = tokens[LINK_NODE1_ARGUMENT_INDEX].substring(1);
                                    final String rightNodeName = tokens[LINK_NODE2_ARGUMENT_INDEX].substring(1);

                                    if (!nodesByName.containsKey(leftNodeName)) {
                                        throw new NS2FormatException(
                                                "Unknown node " + leftNodeName + " on line: " + line);
                                    }

                                    if (!nodesByName.containsKey(rightNodeName)) {
                                        throw new NS2FormatException(
                                                "Unknown node " + rightNodeName + " on line: " + line);
                                    }

                                    final String bandwidthStr = tokens[LINK_BANDWIDTH_ARGUMENT_INDEX];
                                    final double bandwidth = parseBandwidth(bandwidthStr);

                                    final String delayStr = tokens[LINK_DELAY_ARGUMENT_INDEX];
                                    final double delayMs = parseDelay(delayStr);

                                    // final String queueBehavior =
                                    // tokens[LINK_QTYPE_ARGUMENT_INDEX];

                                    final Node leftNode = nodesByName.get(leftNodeName);
                                    final Node rightNode = nodesByName.get(rightNodeName);

                                    final Link link = new Link(name, leftNode, rightNode, bandwidth, delayMs);
                                    links.put(name, link);
                                } else if ("make-lan".equals(objectType)) {
                                    final String bandwidthStr = tokens[tokens.length - 2];
                                    final double bandwidth = parseBandwidth(bandwidthStr);

                                    final String delayStr = tokens[tokens.length - 1];
                                    final double delayMs = parseDelay(delayStr);

                                    final Set<Node> nodes = new HashSet<>();
                                    for (int idx = 2; idx < tokens.length - 2; ++idx) {
                                        final String str = tokens[idx].replace("\"", "").replace("$", "").trim();
                                        if (str.length() > 0) {
                                            if (!nodesByName.containsKey(str)) {
                                                throw new NS2FormatException(
                                                        "Unknown node " + str + " on line: " + line);
                                            }
                                            nodes.add(nodesByName.get(str));
                                        }
                                    }

                                    final Switch lan = new Switch(name, nodes, bandwidth, delayMs);
                                    lans.put(name, lan);
                                } else {
                                    throw new NS2FormatException(
                                            "Unsupported object type: " + objectType + " on line: " + line);
                                }
                            } else {
                                throw new NS2FormatException(
                                        "set arguments must reference an object (doesn't start with $): " + line);
                            }
                        } // set that is not the simulator
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

                        final Node node = nodesByName.get(nodeName);
                        node.setOperatingSystem(tokens[2]);
                    } else if (line.startsWith("tb-set-hardware")) {
                        final String[] tokens = line.split("\\s");
                        if (tokens.length != 3) {
                            throw new NS2FormatException("Expecting tb-set-hardware to have 3 tokens: " + line);
                        }

                        if (!tokens[1].startsWith("$")) {
                            throw new NS2FormatException("Expecting node name to start with $ on line: " + line);
                        }
                        final String nodeName = tokens[1].substring(1);
                        if (!nodesByName.containsKey(nodeName)) {
                            throw new NS2FormatException("Unknown node " + nodeName + " on line: " + line);
                        }

                        final Node node = nodesByName.get(nodeName);

                        final String hardware = tokens[2];
                        node.setHardware(hardware);
                    } else if (line.startsWith("tb-set-ip-link") || line.startsWith("tb-set-ip-lan")
                            || line.startsWith("tb-set-ip-interface")) {
                        final String[] tokens = line.split("\\s");
                        if (tokens.length != 4) {
                            throw new NS2FormatException("Expecting tb-set-ip-* to have 4 tokens: " + line);
                        }

                        if (!tokens[1].startsWith("$")) {
                            throw new NS2FormatException("Expecting node name to start with $ on line: " + line);
                        }
                        final String nodeName = tokens[1].substring(1);
                        if (!nodesByName.containsKey(nodeName)) {
                            throw new NS2FormatException("Unknown node " + nodeName + " on line: " + line);
                        }
                        final Node node = nodesByName.get(nodeName);

                        if (!tokens[2].startsWith("$")) {
                            throw new NS2FormatException(
                                    "Expecting link/node/lan name to start with $ on line: " + line);
                        }
                        final String selectorName = tokens[2].substring(1);

                        final Link link;
                        if ("tb-set-ip-link".equals(tokens[0])) {
                            if (!links.containsKey(selectorName)) {
                                throw new NS2FormatException("Unknown link " + selectorName + " on line: " + line);
                            }
                            link = links.get(selectorName);
                        } else {
                            final NetworkDevice selectorDevice;
                            if ("tb-set-ip-lan".equals(tokens[0])) {
                                selectorDevice = lans.get(selectorName);
                                if (null == selectorDevice) {
                                    throw new NS2FormatException(
                                            "Unable to find lan " + selectorName + " referenced on line " + line);
                                }
                            } else if ("tb-set-ip-interface".equals(tokens[0])) {
                                selectorDevice = nodesByName.get(selectorName);
                                if (null == selectorDevice) {
                                    throw new NS2FormatException(
                                            "Unable to find lan " + selectorName + " referenced on line " + line);
                                }
                            } else {
                                throw new RuntimeException("Internal error, unknown action: " + tokens[0]);
                            }

                            final Set<Link> nodeLinks = node.getLinks();
                            link = nodeLinks.stream().filter(
                                    l -> selectorDevice.equals(l.getLeft()) || selectorDevice.equals(l.getRight()))
                                    .findFirst().orElse(null);

                        }

                        if (null == link) {
                            throw new NS2FormatException("Unable to find link for " + selectorName + " on " + nodeName
                                    + " referenced on line " + line);
                        }

                        final String ip = tokens[3];
                        try {
                            final InetAddress addr = Address.getByAddress(ip);
                            node.setIpAddress(link, addr);
                        } catch (final UnknownHostException e) {
                            throw new NS2FormatException("Invalid IP address: " + ip);
                        }
                    } else if (line.startsWith("tb-set-ip")) {
                        final String[] tokens = line.split("\\s");
                        if (tokens.length != 3) {
                            throw new NS2FormatException("Expecting tb-set-ip to have 3 tokens: " + line);
                        }

                        if (!tokens[1].startsWith("$")) {
                            throw new NS2FormatException("Expecting node name to start with $ on line: " + line);
                        }
                        final String nodeName = tokens[1].substring(1);
                        if (!nodesByName.containsKey(nodeName)) {
                            throw new NS2FormatException("Unknown node " + nodeName + " on line: " + line);
                        }
                        final Node node = nodesByName.get(nodeName);

                        final Set<Link> nodeLinks = node.getLinks();
                        if (nodeLinks.isEmpty()) {
                            throw new NS2FormatException(
                                    "No link on node " + nodeName + " cannot assign IP referenced on line " + line);
                        } else if (nodeLinks.size() > 1) {
                            throw new NS2FormatException("Multiple links on node " + nodeName
                                    + " cannot assign IP referenced on line " + line);
                        }

                        final Link link = nodeLinks.iterator().next();
                        final String ip = tokens[2];
                        try {
                            final InetAddress addr = Address.getByAddress(ip);
                            node.setIpAddress(link, addr);
                        } catch (final UnknownHostException e) {
                            throw new NS2FormatException("Invalid IP address: " + ip);
                        }
                    } else if (line.startsWith("tb-set-node-failure-action")) {
                        LOGGER.debug("Ignoring tb-set-node-failure-action line: {}", line);
                    } else if (line.contains("rtproto")) {
                        LOGGER.debug("Ignoring routing specification line: {}", line);
                    } else if (line.endsWith("run")) {
                        LOGGER.debug("Ignoring run line: {}", line);
                    } else {
                        LOGGER.info("Ignoring unknown line '{}'", line);
                    }
                }

            } // bufReader
        } // input stream reader

        final Topology topology = new Topology(scenarioName, ImmutableMap.copyOf(nodesByName));
        return topology;
    }

    /**
     * Parse the string as a delay.
     * 
     * @param delayStr
     *            the string to parse
     * @return delay in milliseconds
     */
    private static double parseDelay(final String delayStr) {
        final Pattern delayRegExp = Pattern.compile("^([0-9]*\\.?[0-9]+)(ms)$");

        final Matcher delayMatch = delayRegExp.matcher(delayStr);
        if (!delayMatch.matches()) {
            throw new NS2FormatException("String doesn't match expected format for delay: '" + delayStr + "'");
        }
        // if there are other delay units, then this needs to handle the
        // conversion
        try {
            final double delayMs = Double.parseDouble(delayMatch.group(1));
            return delayMs;
        } catch (final NumberFormatException e) {
            throw new NS2FormatException("Delay value is not parseable as a floating point number: '" + delayStr + "'");
        }
    }

    private static double parseBandwidth(final String bandwidthStr) {
        final Pattern bandwidthExp = Pattern.compile("^(\\d+\\.?\\d*)(\\S+)$");
        final Matcher bandwidthMatch = bandwidthExp.matcher(bandwidthStr);
        if (!bandwidthMatch.matches()) {
            throw new NS2FormatException("Bandwidth spec doesn't match expected format: '" + bandwidthStr + "'");
        }
        final double bandwidthValue = Double.parseDouble(bandwidthMatch.group(1));
        final String bandwidthUnits = bandwidthMatch.group(2);
        final double bandwidthMultiplier;
        if ("mb".equalsIgnoreCase(bandwidthUnits)) {
            // megabits per second
            bandwidthMultiplier = 1;
        } else if ("kb".equalsIgnoreCase(bandwidthUnits)) {
            // kilobits per second
            bandwidthMultiplier = MEGABITS_IN_KILOBIT;
        } else {
            throw new NS2FormatException("Unknown bandwidth units: " + bandwidthUnits);
        }
        final double bandwidth = bandwidthValue * bandwidthMultiplier;

        return bandwidth;
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
    public static Map<String, Object> getNodeDataFromResource(final String baseDirectory, final String nodeName)
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
    private static Map<String, Object> getNodeData(final Path baseDirectory, final String nodeName) throws IOException {

        final Path nodePath = baseDirectory.resolve(nodeName + ".json");
        if (Files.exists(nodePath)) {
            try (InputStream stream = Files.newInputStream(nodePath)) {
                @SuppressWarnings("unchecked")
                final JsonObject<String, Object> obj = (JsonObject<String, Object>) JsonReader.jsonToJava(stream,
                        Collections.singletonMap(JsonReader.USE_MAPS, true));
                return obj;
            }
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
        String scenarioFile = "./src/test/resources/ns2/multinode/";
        try {
            if (args.length < 1) {
                LOGGER.warn("No file specified; using default: " + scenarioFile + "\n");
            } else {
                scenarioFile = args[0];
            }

            final Path baseDirectory = Paths.get(scenarioFile);
            final NodeLookupService nodeLookupService = new LocalNodeLookupService(5000);

            final DelegateRegionLookup regionLookupService = new DelegateRegionLookup();

            final VirtualClock clock = new SimpleClock();
            final BasicResourceManagerFactory managerFactory = new BasicResourceManagerFactory(clock);
            final BasicNetworkFactory factory = new BasicNetworkFactory(nodeLookupService, regionLookupService,
                    managerFactory, "/protelis/com/bbn/resourcemanagement/example_resourcetracker.pt", false);
            final Topology topology = NS2Parser.parse(scenarioFile, baseDirectory);

            final Scenario<NetworkServer, NetworkLink, NetworkClient> scenario = new Scenario<>(topology, factory,
                    DnsNameIdentifier::new);

            regionLookupService.setDelegate(scenario);

            scenario.setTerminationCondition(new NeverTerminate<>());

            final BasicNetworkVisualizerFactory visFactory = new BasicNetworkVisualizerFactory();
            final ScenarioVisualizer<DisplayNode, DisplayEdge, NetworkLink, NetworkServer, NetworkClient> visualizer = new ScenarioVisualizer<>(
                    visFactory);

            final ScenarioRunner<NetworkServer, NetworkLink, NetworkClient> emulation = new ScenarioRunner<>(scenario,
                    visualizer);
            emulation.run();

            System.exit(0);
        } catch (final IOException ioe) {
            LOGGER.error("Error reading the simulation at " + scenarioFile, ioe);
            System.exit(1);
        }

    }

}
