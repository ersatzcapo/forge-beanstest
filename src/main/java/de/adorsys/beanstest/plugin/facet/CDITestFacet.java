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
package de.adorsys.beanstest.plugin.facet;

import java.io.FileNotFoundException;

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
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.spec.javaee.CDIFacet;
import org.jboss.shrinkwrap.descriptor.api.DescriptorImporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.cdi.beans.BeansDescriptor;

import de.adorsys.beanstest.ExtensionsServicesFileResource;
import de.adorsys.beanstest.plugin.BeanstestConfiguration;

/**
 * Facet managing the Weld SE and junit dependency, creates beans.xml in test resources and copies SimpleRunner
 * HideMissingScopes installs the HideMissingScopes CDI extension 
 * 
 * @author Brandenstein
 */
@Alias("beanstest.CDITestFacet")
@RequiresFacet({ DependencyFacet.class, ResourceFacet.class, JavaSourceFacet.class, CDIFacet.class })
public class CDITestFacet extends BaseFacet {
    //public static final Dependency WELDSEDEFAULT = DependencyBuilder.create("org.jboss.weld.se:weld-se:1.1.10.Final:test");
    public static final Dependency WELDSEDEFAULT = DependencyBuilder.create("org.jboss.weld.se:weld-se:2.0.1.Final:test");
    public static final Dependency JUNIT = DependencyBuilder.create("junit:junit:4.10:test");
    
    
    @Inject
    private BeanstestConfiguration configuration;

    @Override
    public boolean install() {
        // add weld-se dependency
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency dependency = configuration.getWeldseDependency();
        dependencyFacet.addDirectDependency(dependency);
        
        // add junit dependency
        dependencyFacet.addDirectDependency(JUNIT);

        // add beans.xml in src/test/resouces
        FileResource<?> descriptor = getConfigFile(project);
        if (!descriptor.createNewFile()) {
            throw new RuntimeException("Failed to create required [" + descriptor.getFullyQualifiedName() + "]");
        }
        descriptor.setContents(getClass().getResourceAsStream("/de/adorsys/beanstest/beans.xml"));
        
        // create SimpleRunner
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        JavaClass simpleRunner = JavaParser.parse(JavaClass.class, getClass().getResourceAsStream("/de/adorsys/beanstest/SimpleRunner.jv"));
        simpleRunner.setPackage(java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX);
        try {
            java.saveTestJavaSource(simpleRunner);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("SimpleRunner could not be created", e);
        }
        
        return true;
    }

    private FileResource<?> getConfigFile(final Project project) {
        return (FileResource<?>) project.getFacet(ResourceFacet.class).getTestResourceFolder().getChild("META-INF/beans.xml");
    }

    @Override
    public boolean isInstalled() {
        DependencyFacet dependencyFacet = getProject().getFacet(DependencyFacet.class);
        boolean weldse = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create("org.jboss.weld.se:weld-se"));
        boolean junit = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(JUNIT));
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        boolean simplerunner = false;
        try {
            simplerunner = java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/") + "/SimpleRunner.java").exists();
        } catch (FileNotFoundException e) {}
        boolean testbeans = getConfigFile(getProject()).exists();
        return weldse && junit && simplerunner && testbeans;
    }
    
    public BeansDescriptor getConfig() {
        DescriptorImporter<BeansDescriptor> importer = Descriptors.importAs(BeansDescriptor.class);
        BeansDescriptor descriptor = importer.from(getConfigFile(getProject()).getUnderlyingResourceObject());
        return descriptor;
    }
    
    public void mockMissingScopes() {        
        // Create HideMissingScopes extension
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

        JavaClass mockMissingScopesJavaClass = JavaParser.parse(JavaClass.class, getClass().getResourceAsStream("/de/adorsys/beanstest/MockMissingScopesExtension.jv"));
        mockMissingScopesJavaClass.setPackage(java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX);

        try {
            java.saveTestJavaSource(mockMissingScopesJavaClass);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("MockMissingScopesExtension cannot be created", e);
        }
        
        // Create services folder and CDI extensions file
        DirectoryResource services = project.getFacet(ResourceFacet.class).getTestResourceFolder().getChildDirectory("META-INF/services");
        if (!services.exists()) {
            services.mkdirs();
        }
        
        ExtensionsServicesFileResource cdiextensions = (ExtensionsServicesFileResource) services.getChild("javax.enterprise.inject.spi.Extension");
        
        if (cdiextensions.exists()) {
            // read existing, check for HMSExtension, if not present add
            if (!cdiextensions.containsExtension(mockMissingScopesJavaClass)) {
                cdiextensions.addExtension(mockMissingScopesJavaClass);
            }
        } else {
            cdiextensions.createNewFile();
            cdiextensions.setContents(mockMissingScopesJavaClass.getQualifiedName());
        }
    }

    public void saveConfig(BeansDescriptor beansDescriptor) {
        getConfigFile(project).setContents(beansDescriptor.exportAsString());
    }
}
