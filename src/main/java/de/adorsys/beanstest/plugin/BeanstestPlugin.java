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

import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
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

import de.adorsys.beanstest.plugin.facet.CDITestFacet;

/**
 * CDI test plugin
 * 
 * Enables a project to use Weld SE for junit testing
 */
@Alias("beanstest")
@RequiresFacet(CDITestFacet.class)
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
    public void setup(PipeOut out) {
        if (!project.hasFacet(CDITestFacet.class)) {
            // ask for weld se version
            DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
            List<Dependency> versions = dependencyFacet.resolveAvailableVersions("org.jboss.weld.se:weld-se:[1.1.2.Final,):test"); //TODO => plugin
            Dependency dependency = shell.promptChoiceTyped("Select version: ", versions, CDITestFacet.WELDSEDEFAULT);
            
            configuration.setWeldseDependency(dependency);
            
            installFaEvent.fire(new InstallFacets(CDITestFacet.class));
        } else {
            ShellMessages.info(out, "is installed");
        }
    }

    @Command("hide-missing-scopes")
    public void hideMissingScopes(final PipeOut out) {
        CDITestFacet cditest = project.getFacet(CDITestFacet.class);
        
        //TODO handle io
        
        cditest.hideMissingScopes();
    }

    @Command("mock-alternative")
    // mockito plain stereotype
    // TODO
    public void command(@PipeIn String in, PipeOut out, @Option String... args) {
        throw new RuntimeException("not yet implemented");
    }
}
