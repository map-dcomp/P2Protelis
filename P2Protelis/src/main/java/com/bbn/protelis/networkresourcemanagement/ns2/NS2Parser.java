package com.bbn.protelis.networkresourcemanagement.ns2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.protelis.lang.ProtelisLoader;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ProtelisProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.common.testbed.termination.NeverTerminate;
import com.bbn.protelis.networkresourcemanagement.Link;
import com.bbn.protelis.networkresourcemanagement.Node;
import com.bbn.protelis.networkresourcemanagement.NodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.LocalNodeLookupService;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;
import com.bbn.protelis.networkresourcemanagement.testbed.ScenarioRunner;
import com.bbn.protelis.utils.StringUID;

/**
 * Read NS2 files in and create a network for protelis.
 */
public final class NS2Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NS2Parser.class);

    private NS2Parser() {
    }

    /**
     * Parse an NS2 file into a map of Nodes.
     * 
     * @param scenarioName
     *            name of the scenario to create
     * @param reader
     *            where to read the data from
     * @param program
     *            the program to run on all of the nodes
     * @param lookupService
     *            how to connect to nodes
     * @return the network scenario
     * @throws IOException
     *             if there is an error reading from the reader
     */
    public static Scenario parse(final String scenarioName,
            final Reader reader,
            final ProtelisProgram program,
            final NodeLookupService lookupService) throws IOException {
        final Map<String, Node> nodesByName = new HashMap<>();
        final Set<Link> links = new HashSet<>();

        String simulator = null;

        try (BufferedReader bufReader = new BufferedReader(reader)) {

            // final Pattern setRegExp = Pattern.compile("set (\\s+)
            // \\[([^]]+)\\]");
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
                                throw new NS2FormatException("Cannot constructor nodes and links without a simulator");
                            }
                            if (!self.equals(simulator)) {
                                throw new NS2FormatException(
                                        "Only creating simulated objects is supported line: " + line);
                            }

                            final String objectType = tokens[1];
                            if ("node".equals(objectType)) {
                                final Node node = new Node(lookupService, program, name);
                                nodesByName.put(name, node);
                            } else if ("duplex-link".equals(objectType)) {
                                if (!tokens[2].startsWith("$") || !tokens[3].startsWith("$")) {
                                    throw new NS2FormatException(
                                            "Expecting nodes for link to start with $ on line: " + line);
                                }

                                final String leftNodeName = tokens[2].substring(1);
                                final String rightNodeName = tokens[3].substring(1);

                                if (!nodesByName.containsKey(leftNodeName)) {
                                    throw new NS2FormatException("Unknown node " + leftNodeName + " on line: " + line);
                                }

                                if (!nodesByName.containsKey(rightNodeName)) {
                                    throw new NS2FormatException("Unknown node " + rightNodeName + " on line: " + line);
                                }

                                final Node leftNode = nodesByName.get(leftNodeName);
                                final Node rightNode = nodesByName.get(rightNodeName);
                                leftNode.addNeighbor(rightNode);
                                rightNode.addNeighbor(leftNode);

                                final Link link = new Link(name, leftNode, rightNode);
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
                    LOGGER.error("Ignoring unknown line '" + line + "'");
                }
            }

        } // bufReader

        final Map<DeviceUID, Node> nodes = nodesByName.entrySet().stream()
                .collect(Collectors.toMap(e -> new StringUID(e.getKey()), Map.Entry::getValue));
        final Scenario scenario = new Scenario(scenarioName, nodes, links);
        return scenario;
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

            final ProtelisProgram program = ProtelisLoader.parseAnonymousModule("true");

            final NodeLookupService lookupService = new LocalNodeLookupService(5000);

            final String filename = "ns2/multinode.ns";
            try (Reader reader = new FileReader(args[0])) {
                final Scenario scenario = NS2Parser.parse(filename, reader, program, lookupService);

                scenario.setVisualize(true);
                scenario.setTerminationCondition(new NeverTerminate<>());

                final ScenarioRunner emulation = new ScenarioRunner(scenario);
                emulation.run();

            } // reader
        } catch (final IOException ioe) {
            LOGGER.error("Error reading the file " + args[0], ioe);
        }

    }

}
