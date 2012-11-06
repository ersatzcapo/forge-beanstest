/**
 * Copyright (C) 2012 Christian Brandenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.beanstest.plugin.cdi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

/**
 * Facet managing the Weld SE dependency
 * 
 * @author Brandenstein
 */
@Alias("cdiTestFacet")
@RequiresFacet({ DependencyFacet.class, ResourceFacet.class, JavaSourceFacet.class })
public class CDITestFacet extends BaseFacet {

	@Inject
	private Shell shell;

	@Inject
	private ShellPrompt prompt;

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
		if (!descriptor.createNewFile()) {
			throw new RuntimeException("Failed to create required ["
					+ descriptor.getFullyQualifiedName() + "]");
		}
		descriptor.setContents(getClass().getResourceAsStream(
				"/de/adorsys/beanstest/beans.xml"));

		// create Runner
		final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

		JavaClass javaResource = JavaParser.parse(JavaClass.class, getClass()
				.getResourceAsStream("/de/adorsys/beanstest/SimpleRunner.jv"));
		javaResource.setPackage(java.getBasePackage());

		try {
			if (!java.getJavaResource(javaResource).exists()
					|| prompt.promptBoolean("Runner ["
							+ javaResource.getQualifiedName()
							+ "] already, exists. Overwrite?")) {
				java.saveTestJavaSource(javaResource);
			} else {
				return false; //TODO is this ok?
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Runner could not be created", e);
		}

		return true;
	}

	private FileResource<?> getConfigFile(final Project project) {
		return (FileResource<?>) project.getFacet(ResourceFacet.class)
				.getTestResourceFolder()
				.getChild("META-INF" + File.separator + "beans.xml");
	}

	@Override
	public boolean isInstalled() { // TODO
		DependencyFacet DependencyFacet = getProject().getFacet(
				DependencyFacet.class);
		return DependencyFacet.hasEffectiveDependency(DependencyBuilder
				.create("org.jboss.weld.se:weld-se"));
	}
}
