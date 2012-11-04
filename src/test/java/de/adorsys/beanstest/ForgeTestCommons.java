package de.adorsys.beanstest;

import java.io.ByteArrayInputStream;
import java.io.File;
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

/**
 * Handling of forge shell, project, directory 
 * @author Brandenstein
 */
public class ForgeTestCommons {

	@Inject
	Shell shell;

	@Inject
	BeanManager beanManager;

	@Inject
	private Instance<Project> project;

	@Inject
	private ResourceFactory factory;
	
	private Project localproject;
	
	public void init(String completeInput, String projectName) throws Exception {
		shell.setOutputStream(System.out);
		shell.setInputStream(new ByteArrayInputStream(completeInput.getBytes()));
		shell.setAnsiSupported(false);
		
		beanManager.fireEvent(new Startup(), new Annotation[0]);
		beanManager.fireEvent(new PostStartup(), new Annotation[0]);
		
		localproject = initializeProject(PackagingType.BASIC, projectName);
		
		shell.setVerbose(true);
		shell.setExceptionHandlingEnabled(false);
	}
	
	public void cleanUp() throws Exception {
		localproject.getProjectRoot().delete(true);
	}

	/**
	 * TODO control directory, enable other packages etc.
	 * @param type
	 * @param projectDir
	 * @return
	 * @throws Exception
	 */
	protected Project initializeProject(PackagingType type, String projectName) throws Exception {
		DirectoryResource directoryResource = (DirectoryResource) this.factory
				.getResourceFrom(new File("target")).reify(DirectoryResource.class);
		shell.setCurrentResource(directoryResource);
		shell.execute("new-project --named " + projectName + " --topLevelPackage de.adorsys.test --type "
				+ type.toString());
		return project.get();
	}

}
