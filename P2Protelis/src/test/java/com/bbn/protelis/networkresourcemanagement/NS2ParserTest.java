package com.bbn.protelis.networkresourcemanagement;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ProtelisProgram;

import com.bbn.protelis.networkresourcemanagement.ns2.NS2Parser;
import com.bbn.protelis.networkresourcemanagement.testbed.Scenario;
import com.bbn.protelis.networkresourcemanagement.visualizer.ScenarioVisualizer;

public class NS2ParserTest {

	@Test
	public void testSimpleGraph() throws IOException {
		final ProtelisProgram program = ProtelisLoader.parseAnonymousModule("true");

		final String filename = "ns2/multinode.ns";
		try (final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
			Assert.assertNotNull("Couldn't find ns2 file: " + filename, stream);

			try (final Reader reader = new InputStreamReader(stream)) {
				final Scenario scenario = NS2Parser.parse(filename, reader, program);
				Assert.assertNotNull("Parse didn't create a scenario", scenario);

				scenario.setVisualize(true);

				// ScenarioRunner emulation = new ScenarioRunner(scenario);
				// emulation.run();
				final ScenarioVisualizer visualizer = new ScenarioVisualizer(scenario);
				visualizer.waitForClose();
			} // reader
		} // stream
	}

}
