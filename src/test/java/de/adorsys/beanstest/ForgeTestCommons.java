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
package de.adorsys.beanstest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	
	private MyInputStream myInputStream;
	
	public void init(String projectName, String packageName, boolean print2stout) throws Exception {
	    myInputStream = new MyInputStream();
	    
		shell.setOutputStream(new MyOutputStream(print2stout));
		shell.setInputStream(myInputStream);
		shell.setAnsiSupported(false);
		
		beanManager.fireEvent(new Startup(), new Annotation[0]);
		beanManager.fireEvent(new PostStartup(), new Annotation[0]);
		
		localproject = initializeProject(PackagingType.JAR, projectName, packageName);
		
		shell.setVerbose(true);
		shell.setExceptionHandlingEnabled(false);
	}
	
	public void cleanUp() throws Exception {
		localproject.getProjectRoot().delete(true);
	}
	
	/**
	 * input as string. e.g. newline 10 newline will be "\n10\n"
	 * 
	 * @param input
	 * @throws IOException
	 */
	public void setNewInput(String input) {
	    myInputStream.setInput(input);
	}

	/**
	 * @param type
	 * @param projectDir
	 * @return
	 * @throws Exception
	 */
	protected Project initializeProject(PackagingType type, String projectName, String packageName) throws Exception {
		DirectoryResource directoryResource = (DirectoryResource) this.factory
				.getResourceFrom(new File("target")).reify(DirectoryResource.class);
		shell.setCurrentResource(directoryResource);
		shell.execute("new-project --named " + projectName + " --topLevelPackage " + packageName + " --type "
				+ type.toString());
		return project.get();
	}
	
	class MyInputStream extends InputStream {
	    byte [] ba = "\n".getBytes();
	    int c = 0;
	    
	    void setInput(String input) {
	        ba = input.getBytes();
	        c = 0;
	    }

        @Override
        public int read() throws IOException {
            if(ba == null) {
                throw new IllegalStateException("input not initialized");
            }
            
            try {
                return ba[c++];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("not enough input set");
            }
        }	    
	}

	class MyOutputStream extends OutputStream {
	    boolean print2stdout;

        public MyOutputStream(boolean print2stout) {
            this.print2stdout = print2stout;
        }

        @Override
        public void write(int b) throws IOException {
            if (print2stdout) {
                System.out.write(b);
            }
        }
	    
	}
}
