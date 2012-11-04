package de.adorsys.beanstest.plugin.cdi;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.events.PostStartup;
import org.jboss.forge.shell.events.Startup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.adorsys.beanstest.plugin.SimpleRunner;

@Deprecated
@RunWith(SimpleRunner.class)
public class FirstCDIFacetTest {

	@Inject
	Shell shell;

	@Inject
	BeanManager beanManager;

	@Inject
	private Instance<Project> project;

	@Inject
	private ResourceFactory factory;
	
	private DirectoryResource directoryResource;

	private InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(
			"\n10\n".getBytes()));

	@Before
	public void beforeTest() throws Exception {
		shell.setOutputStream(System.out);
		shell.setInputStream(inputStream);
		shell.setAnsiSupported(false);
		
		beanManager.fireEvent(new Startup(), new Annotation[0]);
		beanManager.fireEvent(new PostStartup(), new Annotation[0]);
		
		initializeProject(PackagingType.BASIC);
		
		shell.setVerbose(true);
		shell.setExceptionHandlingEnabled(false);
	}
	
	@After
	public void afterTest() throws Exception {
		directoryResource.delete(true);
	}

	@Test
	public void testFacet() throws Exception {
		shell.execute("beanstest setup");
	}

	protected Project initializeProject(PackagingType type) throws Exception {
		File f = new File("target/tmp");
		f.mkdir();
		directoryResource = (DirectoryResource) this.factory
				.getResourceFrom(f).reify(DirectoryResource.class);
		shell.setCurrentResource(directoryResource);
		shell.execute("new-project --named test --topLevelPackage de.adorsys.test --type "
				+ type.toString());
		return project.get();
	}

}
