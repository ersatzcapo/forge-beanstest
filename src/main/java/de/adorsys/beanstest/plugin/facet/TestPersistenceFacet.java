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
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import de.adorsys.beanstest.ExtensionsServicesFileResource;
import de.adorsys.beanstest.plugin.BeanstestConfiguration;

/**
 * 
 * @author Brandenstein
 */
@Alias("beanstest.TestPersistenceFacet")
@RequiresFacet({ DependencyFacet.class, ResourceFacet.class, JavaSourceFacet.class, CDITestFacet.class })
public class TestPersistenceFacet extends BaseFacet {
//    public static final Dependency WELDSEDEFAULT = DependencyBuilder.create("org.jboss.weld.se:weld-se:1.1.10.Final:test");
    
    @Override
    public boolean install() {
        // TODO add dependencies: hibernate * 2 + hsqldb
        /*DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency dependency = configuration.getWeldseDependency();
        dependencyFacet.addDirectDependency(dependency);*/
        
        //Create PersistenceExtension
        JavaClass persistenceExtension = createClassFromTemplate("/de/adorsys/beanstest/PersistenceExtension.jv");
        createServiceEntry(persistenceExtension);
        
        //Create MockJpaInjectionServices
        createClassFromTemplate("/de/adorsys/beanstest/MockJpaInjectionServices.jv");
        
        //Copy persistence.xml
        //TODO
        return true;
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


    //TODO
    @Override
    public boolean isInstalled() {
      /*  DependencyFacet dependencyFacet = getProject().getFacet(DependencyFacet.class);
        boolean weldse = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create("org.jboss.weld.se:weld-se"));
        boolean junit = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(JUNIT));
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        boolean simplerunner = false;
        try {
            simplerunner = java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/") + "/SimpleRunner.java").exists();
        } catch (FileNotFoundException e) {}
        return weldse && junit && simplerunner;*/
        return true;
    }
}
