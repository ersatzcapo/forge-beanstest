package de.adorsys.beanstest.plugin.cdi;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.adorsys.beanstest.ForgeTestCommons;
import de.adorsys.beanstest.plugin.SimpleRunner;

@RunWith(SimpleRunner.class)
public class CDITestFacetTest {
	private static final String INPUT = "\n10\n";
	private static final String TESTPROJECTNAME = "CDITestFacetTest";

	@Inject
	Shell shell;
	
	@Inject
	ForgeTestCommons forgeTestCommons;
	
	@Before
	public void init() throws Exception {
		forgeTestCommons.init(INPUT, TESTPROJECTNAME);
	}
	
	@After
	public void destroy() throws Exception {
		forgeTestCommons.cleanUp();
	}

	@Test
	public void testSetup() throws Exception {
		shell.execute("beanstest setup");
		
		//TODO test pom
		
		//test beans.xml created
		assertTrue(new File("target/" + TESTPROJECTNAME + "/src/test/resources/META-INF/beans.xml").exists());		
	}
}
