package de.adorsys.beanstest.plugin.cdi;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.shrinkwrap.descriptor.api.DescriptorImporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.cdi.beans.BeansDescriptor;

/**
 * Facet managing the Weld SE dependency
 * 
 * @author Brandenstein
 */
@Alias("weldSEDependencyFacet")
@RequiresFacet({ DependencyFacet.class, ResourceFacet.class })
public class CDITestFacet extends BaseFacet {

	@Inject
	private Shell shell;

	@Override
	public boolean install() {
		// add weld-se dependency
		DependencyFacet dependencyFacet = project
				.getFacet(DependencyFacet.class);
		List<Dependency> versions = dependencyFacet
				.resolveAvailableVersions("org.jboss.weld.se:weld-se:[1.1.2.Final,):test");
		Dependency dependency = shell.promptChoiceTyped("Select version: ",
				versions);
		dependencyFacet.addDirectDependency(dependency);

		// add beans.xml in src/test/resouces
		FileResource<?> descriptor = getConfigFile(project);
        if (!descriptor.createNewFile())
        {
           throw new RuntimeException("Failed to create required [" + descriptor.getFullyQualifiedName() + "]");
        }
        descriptor.setContents(getClass().getResourceAsStream("/de/adorsys/cditest/beans.xml"));

		// create Runner

		return true;
	}

	private FileResource<?> getConfigFile(final Project project) {
		return (FileResource<?>) project.getFacet(ResourceFacet.class)
				.getTestResourceFolder()
				.getChild("META-INF" + File.separator + "beans.xml");
	}

	@Override
	public boolean isInstalled() { //TODO
		DependencyFacet DependencyFacet = getProject().getFacet(
				DependencyFacet.class);
		return DependencyFacet.hasEffectiveDependency(DependencyBuilder
				.create("org.jboss.weld.se:weld-se"));
	}
}
