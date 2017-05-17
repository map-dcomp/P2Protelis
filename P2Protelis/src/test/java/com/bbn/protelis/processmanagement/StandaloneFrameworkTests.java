package com.bbn.protelis.processmanagement;

import org.junit.Test;

public class StandaloneFrameworkTests extends JSONFrameworkTest {
	@Test
	public void smokeTest() {
		String[] extraArgs = {"-v","false"};
		runTest("3",false,"networks/mini.json",1,"tests/smokeTest.json",extraArgs);
	}

	@Test
	public void debugTest() {
		String[] extraArgs = {"-v","false"};
		runTest("self.putEnvironmentVariable(\"red\",self.getId()-1); self.putEnvironmentVariable(\"blue\",self.getId()==1); self.putEnvironmentVariable(\"debugTest\",self.getId()+4); self.getEnvironmentVariable(\"debugTest\");",false,"networks/mini.json",2,"tests/debugTest.json",extraArgs);
	}

	@Test
	public void progressTest() {
		String[] extraArgs = {"-v","false"};
		runTest("let id = self.getId(); rep(n <- id) { n+1 }",false,"networks/mini.json",10,"tests/progressTest.json",extraArgs);
	}
	
	@Test
	public void idTest() {
		String[] extraArgs = {"-v","false"};
		runTest("self.getId()==1",false,"networks/mini.json",1,"tests/idTest.json",extraArgs);
	}
	
	@Test
	public void networkTest() {
		String[] extraArgs = {"-v","false"};
		runTest("self.putEnvironmentVariable(\"logicalNeighbors\",[1]); minHood PlusSelf(nbr(self.getId()))",false,"networks/mini.json",3,"tests/networkTest.json",extraArgs);
	}
	
	@Test
	public void moduleTestA() {
		String[] extraArgs = {"-v","false"};
		runTest("/test/reference.pt",true,"networks/mini.json",1,"tests/moduleTest.json",extraArgs);
	}

	@Test
	public void moduleTestB() {
		String[] extraArgs = {"-v","false"};
		runTest("import test:reference\n testFun();",false,"networks/mini.json",1,"tests/moduleTest.json",extraArgs);
	}

	@Test
	public void environmentTest() {
		String[] extraArgs = {"-v","false"};
		runTest("self.getEnvironmentVariable(\"foo\").length()+self.getEnvironmentVariable(\"bar\")+self.getEnvironmentVariable(\"baz\");",false,"networks/mini.json",1,"tests/environmentTest.json",extraArgs);
	}
}
