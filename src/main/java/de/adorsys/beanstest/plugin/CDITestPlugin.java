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

import java.util.Arrays;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeIn;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.SetupCommand;

import de.adorsys.beanstest.plugin.cdi.CDITestFacet;

/**
 * CDI test plugin
 * 
 * Enables a project to use Weld SE for junit testing
 */
@Alias("beanstest")
@RequiresFacet(CDITestFacet.class)
public class CDITestPlugin implements Plugin {
	@Inject
	private ShellPrompt prompt;

	@Inject
	private Project project;

	@Inject
	private Event<InstallFacets> installFaEvent;

	@SetupCommand
	public void setup(PipeOut out) {
		if (!project.hasFacet(CDITestFacet.class)) {
			installFaEvent.fire(new InstallFacets(CDITestFacet.class));
		} else {
			ShellMessages.info(out, "is installed");
		}
	}

	@DefaultCommand //TODO
	public void defaultCommand(@PipeIn String in, PipeOut out) {
		out.println("Executed default command.");
	}

	@Command("mockito") //TODO
	public void command(@PipeIn String in, PipeOut out, @Option String... args) {
		if (args == null)
			out.println("Executed named command without args.");
		else
			out.println("Executed named command with args: "
					+ Arrays.asList(args));
	}

	@Command //TODO
	public void prompt(@PipeIn String in, PipeOut out) {
		if (prompt.promptBoolean("Do you like writing Forge plugins?"))
			out.println("I am happy.");
		else
			out.println("I am sad.");
	}
}
