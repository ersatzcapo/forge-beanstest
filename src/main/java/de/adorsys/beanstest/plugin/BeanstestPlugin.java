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
package de.adorsys.beanstest.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeIn;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.SetupCommand;
import org.junit.runner.RunWith;

import de.adorsys.beanstest.plugin.facet.CDITestFacet;
import de.adorsys.beanstest.plugin.facet.MockitoFacet;

/**
 * CDI test plugin
 * 
 * Enables a project to use Weld SE for junit testing
 */
@Alias("beanstest")
@RequiresFacet({ CDITestFacet.class, JavaSourceFacet.class })
public class BeanstestPlugin implements Plugin {
    @Inject
    private ShellPrompt prompt;

    @Inject
    private Shell shell;

    @Inject
    private Project project;

    @Inject
    private Event<InstallFacets> installFaEvent;

    @Inject
    private BeanstestConfiguration configuration;

    @SetupCommand
    public void setup(PipeOut out) throws Exception {
        if (!project.hasFacet(CDITestFacet.class)) {
            // ask for weld se version
            DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
            List<Dependency> versions = dependencyFacet.resolveAvailableVersions("org.jboss.weld.se:weld-se:[1.1.2.Final,):test");
            Dependency dependency = shell.promptChoiceTyped("Select version: ", versions, CDITestFacet.WELDSEDEFAULT);
            configuration.setWeldseDependency(dependency);

            installFaEvent.fire(new InstallFacets(CDITestFacet.class));
        } else {
            ShellMessages.info(out, "is installed");
        }
    }

    @Command("hide-missing-scopes")
    public void hideMissingScopes() {
        CDITestFacet cditest = project.getFacet(CDITestFacet.class);
        cditest.hideMissingScopes();
    }

    @Command("new-test")
    public void newTest(@Option(required = true, name = "type", shortName = "t") final String type, //JavaResource always maps to main folder, see JavaPathspecParser
            PipeOut out) throws FileNotFoundException {
        JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        DirectoryResource resource = java.getTestSourceFolder();
        
        String path = type.replaceAll("\\.", File.separator) + ".java"; //TODO
        
        JavaResource javaTestResource = resource.getChildOfType(JavaResource.class, path); //TODO eventually . ˜
        
        if (!javaTestResource.exists()) {
            if (javaTestResource.createNewFile()) {
                JavaClass javaTestClass = JavaParser.create(JavaClass.class);
                javaTestClass.setName(java.calculateName(javaTestResource));
                javaTestClass.setPackage(java.calculatePackage(javaTestResource));
                
                //SimpleRunner import
                JavaResource simpleRunnerResource = java.getTestJavaResource(java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX + ".SimpleRunner.java");
                if(simpleRunnerResource != null && simpleRunnerResource.exists()) {
                    javaTestClass.addImport(simpleRunnerResource.getJavaSource());
                } else {
                    throw new RuntimeException("SimpleRunner does not exist: [" + simpleRunnerResource + "]");
                }
                
                javaTestClass.addImport(RunWith.class);
                javaTestClass.addAnnotation(RunWith.class);
                javaTestClass.getAnnotation(RunWith.class).setLiteralValue("SimpleRunner.class");

                javaTestResource.setContents(javaTestClass);
            } else {
                ShellMessages.error(out, "Cannot create test [" + javaTestResource.getFullyQualifiedName() + "]");
            }
        } else {
            ShellMessages.error(out, "Test already exists [" + javaTestResource.getFullyQualifiedName() + "]");
        }
    }


    @Command("new-mockito")
    public void command(@PipeIn String in, PipeOut out,
            @Option(required = false, name = "sterotype", shortName = "s") final String stereotype,
            @Option(required = true, name = "type", shortName = "t") final JavaResource type
            ) {
        if (!project.hasFacet(MockitoFacet.class)) {
            installFaEvent.fire(new InstallFacets(MockitoFacet.class));
        }
        
        MockitoFacet mockito = project.getFacet(MockitoFacet.class);
        
        //TODO 
        mockito.createDies();
        
        mockito.createDas();
    }
}
