package com.bbn.protelis.ns2;

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

import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.protelis.processmanagement.testbed.Scenario;
import com.bbn.protelis.processmanagement.testbed.ScenarioRunner;
import com.bbn.protelis.processmanagement.testbed.client.DummyMonitorable;
import com.bbn.protelis.processmanagement.testbed.daemon.AbstractDaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.daemon.LocalDaemon;

/**
 * Read NS2 files in and create a network for protelis.
 * 
 * @author jschewe
 *
 */
public class NS2Parser {

	private static final Logger LOGGER = LoggerFactory.getLogger("NS2Parser");

	public static void main(final String... args) throws IOException {

		final ProtelisProgram program = ProtelisLoader.parseAnonymousModule("true");

		Scenario scenario = new Scenario(LOGGER);

		final String filename = "/Users/jschewe/projects/map/multinode.ns";
		try (final Reader reader = new FileReader(filename)) {
			final Map<String, Node> nodes = parse(reader);

			int uid = 0;
			final Map<String, LocalDaemon> daemons = new HashMap<>();

			// create daemons
			for (final Map.Entry<String, Node> entry : nodes.entrySet()) {
				final String name = entry.getKey();

				final LocalDaemon daemon = new LocalDaemon();
				daemon.uid = uid;
				daemon.alias = name;
				daemon.program = program;

				daemons.put(name, daemon);

				++uid;
			}

			// connect daemons
			for (final Map.Entry<String, Node> entry : nodes.entrySet()) {
				final String name = entry.getKey();
				final Node node = entry.getValue();
				final LocalDaemon daemon = daemons.get(name);

				final int[] dependencyPorts = node.neighbors.stream().mapToInt((n) -> {
					final LocalDaemon neighbor = daemons.get(n.name);
					return portNumberForDaemon(neighbor);
				}).toArray();

				final DummyMonitorable client = new DummyMonitorable(portNumberForDaemon(daemon), dependencyPorts);
				daemon.setClient(client);
			}

			scenario.network = daemons.values().toArray(new DaemonWrapper[0]);
			scenario.visualize = true;

			ScenarioRunner emulation = new ScenarioRunner(scenario);
			emulation.run();
		} // reader

	}

	private static int portNumberForDaemon(final AbstractDaemonWrapper daemon) {
		return (int) (5000 + daemon.uid);
	}

	/**
	 * Parse an NS2 file into a map of Nodes.
	 * 
	 * @param reader
	 *            where to read the data from
	 * @return key = name, value = Node
	 * @throws IOException
	 *             if there is an error reading from the reader
	 */
	public static Map<String, Node> parse(final Reader reader) throws IOException {
		final Map<String, Node> nodes = new HashMap<>();

		String simulator = null;

		try (final BufferedReader bufReader = new BufferedReader(reader)) {

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
					// ignore
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
								final Node node = new Node(name);
								nodes.put(name, node);
							} else if ("duplex-link".equals(objectType)) {
								if (!tokens[2].startsWith("$") || !tokens[3].startsWith("$")) {
									throw new NS2FormatException(
											"Expecting nodes for link to start with $ on line: " + line);
								}

								final String leftNodeName = tokens[2].substring(1);
								final String rightNodeName = tokens[3].substring(1);

								if (!nodes.containsKey(leftNodeName)) {
									throw new NS2FormatException("Unknown node " + leftNodeName + " on line: " + line);
								}

								if (!nodes.containsKey(rightNodeName)) {
									throw new NS2FormatException("Unknown node " + rightNodeName + " on line: " + line);
								}

								final Node leftNode = nodes.get(leftNodeName);
								final Node rightNode = nodes.get(rightNodeName);
								leftNode.neighbors.add(rightNode);
								rightNode.neighbors.add(leftNode);
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

					if (!nodes.containsKey(nodeName)) {
						throw new NS2FormatException("Unknown node " + nodeName + " on line: " + line);
					}

					final Node node = nodes.get(nodeName);
					node.operatingSystem = tokens[2];

				} else {
					LOGGER.error("Ignoring unknown line '" + line + "'");
				}
			}

		} // bufReader

		return nodes;
	}
	/*
	 * private static final class Link { public String name = null; public
	 * String type = null; public Node left = null; public Node right = null;
	 * 
	 * 
	 * 
	 * public String queueing = null; }
	 */

	private static final class Node {
		public String name = null;
		public String operatingSystem = null;
		public Set<Node> neighbors = new HashSet<>();

		public Node(final String pName) {
			this.name = pName;
		}
	}

	/**
	 * Thrown when there is an error in the NS2 file format.
	 * 
	 * @author jschewe
	 *
	 */
	public static final class NS2FormatException extends RuntimeException {
		public NS2FormatException(final String message) {
			super(message);
		}
	}
}
