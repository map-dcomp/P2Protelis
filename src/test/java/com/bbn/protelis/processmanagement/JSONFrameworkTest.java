package com.bbn.protelis.processmanagement;

import static org.danilopianini.lang.LangUtils.stackTraceToString;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.protelis.lang.datatype.Tuple;

import com.bbn.protelis.processmanagement.testbed.StandaloneExecution;
import com.bbn.protelis.processmanagement.testbed.daemon.DaemonWrapper;
import com.bbn.protelis.processmanagement.testbed.daemon.LocalDaemon;
import com.cedarsoftware.util.io.JsonReader;

public class JSONFrameworkTest {
	private static final int portOffsetWrap = 5000;
	static boolean nonTerminating = false; // Set this to true if you want to have tests wait for the user
	

	/**
	 * Common pattern of running a 
	 * @param program	Protelis program to load
	 * @param module	Boolean indicating whether the program is anonymous or in a class
	 * @param network	JSON file containing serialization of an array of DaemonWrappers
	 * @param rounds	How long the test should run for
	 * @param expectedResult
	 * @throws IOException
	 */
	protected void runTest(String program, boolean module, String network, int rounds, String expectedResult) throws IOException {
		runTest(program,module,network,rounds,expectedResult,new String[0]);
	}
	
	protected void runTest(String program, boolean module, String network, int rounds, String expectedResult, String[] extraArgs) {
		Object[] expected = {};
		try {
			expected = readExpectedResult(expectedResult);
			// Turn parameters into arguments
			String[] baseArgs = {"-f",network,module?"-c":"-a",program};
			if(!nonTerminating) baseArgs = ArrayUtils.addAll(baseArgs, new String[]{"-t","rounds",Integer.toString(rounds)});
			String[] args = ArrayUtils.addAll(baseArgs, extraArgs);
			// Run the simulation
			DaemonWrapper[] returns = StandaloneExecution.runStandalone(args);
			// Reorganize the return values into a map and check if they are correct
			Map<Long,Object> results = new HashMap<>();
			for(DaemonWrapper d : returns) { results.put(d.getUID(), d.getValue()); }
			compareValues(expected,results);
		} catch (Exception e) {
			failException(e);
		} finally {
			// shift testing port offset by size of network
			LocalDaemon.testPortOffset = (LocalDaemon.testPortOffset + expected.length)%portOffsetWrap ;
		}
	}

	private Object[] readExpectedResult(String resource) throws IOException {
		// Read the specification
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		if(in==null) throw new FileNotFoundException("Couldn't locate JSON file: "+resource);
		JsonReader jr = new JsonReader(in);
		Object[] objects = (Object[])jr.readObject();
		jr.close();
		return objects;
	}

	private boolean tupleEquals(Object expected, Object actual) {
		// Test if comparing array and Tuple
		if(!(expected instanceof Object[])) return false;
		if(!(actual instanceof Tuple)) return false;
		Object[] expectedA = ((Object[])expected);
		Tuple actualT = (Tuple)actual;
		// Perform the actual element-by-element comparison
		if(expectedA.length != actualT.size()) return false;
		for(int i=0;i<expectedA.length;i++) {
			Object e_v = expectedA[i];
			Object a_v = actualT.get(i);
			if(e_v==null) {
				if(a_v!=null) return false;
			} else {
				if(!(e_v.equals(a_v) || tupleEquals(e_v,a_v))) return false;
			}
		}
		return true;
	}

	private void compareValues(Object[] expected, Map<Long,Object> actual) {
		Assert.assertEquals("Number of daemons does not match number of results:", expected.length, actual.size());
		String mismatches = "Mismatches:";
		boolean allMatches = true;
		for(int i=0;i<expected.length;i++) {
			Object e_i = ((Object[])expected[i])[0];
			Object e_v = ((Object[])expected[i])[1];
			Object a_v = actual.get((Long)e_i);
			String e_vStr = (e_v==null)?"null":(e_v instanceof Object[]) ? Arrays.deepToString((Object[])e_v) : e_v.toString();
			if(e_v==null) {
				if(a_v!=null) { allMatches = false; mismatches+= " Daemon "+e_i+": expected null but observed "+a_v; }
			} else {
				if(a_v==null) { allMatches = false; mismatches+= " Daemon "+e_i+": expected "+e_vStr+" but observed null"; }
				else if(!(e_v.equals(a_v) || tupleEquals(e_v,a_v))) {
					allMatches = false;
					mismatches+= " Daemon "+e_i+": expected "+e_vStr+" but observed "+a_v;
				}
			}
		}
		if(!allMatches)
			throw new AssertionError(mismatches);
	}

	private void failException(Exception e) {
		fail(stackTraceToString(e));
	}

}