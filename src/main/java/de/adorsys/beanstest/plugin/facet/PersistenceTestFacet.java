/**
 * Copyright (C) 2013 Christian Brandenstein
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
import org.jboss.forge.spec.javaee.PersistenceFacet;

import de.adorsys.beanstest.ExtensionsServicesFileResource;
import de.adorsys.beanstest.plugin.BeanstestConfiguration;

/**
 * Setup hsqldb, and enable @PersistenceContext in Weld SE
 * 
 * @author Brandenstein
 */
@Alias("beanstest.PersistenceTestFacet")
@RequiresFacet({ DependencyFacet.class, ResourceFacet.class, JavaSourceFacet.class, CDITestFacet.class, PersistenceFacet.class })
public class PersistenceTestFacet extends BaseFacet {
    public static final Dependency HSQLDB = DependencyBuilder.create("org.hsqldb:hsqldb:2.2.9:test");
    public static final Dependency HIBERNATE_CORE = DependencyBuilder.create("org.hibernate:hibernate-core:4.1.9.Final:test");
    public static final Dependency HIBERNATE_ENTITYMANAGER = DependencyBuilder.create("org.hibernate:hibernate-entitymanager:4.1.9.Final:test");
    
    @Override
    public boolean install() {
        // add dependencies: hibernate and hsqldb
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        dependencyFacet.addDirectDependency(HSQLDB);
        dependencyFacet.addDirectDependency(HIBERNATE_CORE);
        dependencyFacet.addDirectDependency(HIBERNATE_ENTITYMANAGER);
        
        //Create PersistenceExtension
        JavaClass persistenceExtension = createClassFromTemplate("/de/adorsys/beanstest/PersistenceExtension.jv");
        createServiceEntry(persistenceExtension);
        
        //Create MockJpaInjectionServices
        createClassFromTemplate("/de/adorsys/beanstest/MockJpaInjectionServices.jv");
        
        //Copy persistence.xml
        FileResource<?> descriptor = getPersistenceFile(project);
        if (!descriptor.createNewFile()) {
            throw new RuntimeException("Failed to create required [" + descriptor.getFullyQualifiedName() + "]");
        }
        descriptor.setContents(getClass().getResourceAsStream("/de/adorsys/beanstest/persistence.xml"));
        return true;
    }

    private FileResource<?> getPersistenceFile(final Project project) {
        return (FileResource<?>) project.getFacet(ResourceFacet.class).getTestResourceFolder().getChild("META-INF/persistence.xml");
    }
    
    /**
     * Creates a JavaClass from a template
     * 
     * @param templatepath
     * @return created class
     */
    private JavaClass createClassFromTemplate(String templatepath) {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        JavaClass javaClass = JavaParser.parse(JavaClass.class, getClass().getResourceAsStream(templatepath));
        javaClass.setPackage(java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX);
        try {
            java.saveTestJavaSource(javaClass);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Javaclass " + templatepath +" could not be created", e);
        }
        return javaClass;
    }

    //TODO eventually use for both extensions
    private void createServiceEntry(JavaClass extensionJavaClass) {
        // Create services folder and CDI extensions file
        DirectoryResource services = project.getFacet(ResourceFacet.class).getTestResourceFolder().getChildDirectory("META-INF/services");
        if (!services.exists()) {
            services.mkdirs();
        }
        
        ExtensionsServicesFileResource extensions = (ExtensionsServicesFileResource) services.getChild("javax.enterprise.inject.spi.Extension");
        
        if (extensions.exists()) {
            // read existing, check for PersistenceExtension, if not present add
            if (!extensions.containsExtension(extensionJavaClass)) {
                extensions.addExtension(extensionJavaClass);
            }
        } else {
            extensions.createNewFile();
            extensions.setContents(extensionJavaClass.getQualifiedName());
        }
    }


    @Override
    public boolean isInstalled() {
        DependencyFacet dependencyFacet = getProject().getFacet(DependencyFacet.class);
        boolean hsqldb = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(HSQLDB));
        boolean hibcore = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(HIBERNATE_CORE));
        boolean hibentmgr = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(HIBERNATE_ENTITYMANAGER));
        
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        boolean extension = false;
        try {
            extension = java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/") + "/PersistenceExtension.java").exists();
        } catch (FileNotFoundException e) {}
        
        boolean injectionservice = false;
        try {
            injectionservice = java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/") + "/MockJpaInjectionServices.java").exists();
        } catch (FileNotFoundException e) {}
        return hsqldb && hibcore && hibentmgr && extension && injectionservice;
    }
}
