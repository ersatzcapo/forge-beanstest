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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFlag;
import org.jboss.forge.resources.ResourceHandles;
import org.jboss.forge.resources.VirtualResource;

/**
 * Handles CDI extensions file:
 * META-INF/services/javax.enterprise.inject.spi.Extension
 * Entries are represented as JavaClass
 * 
 * @author Brandenstein
 */
@ResourceHandles("javax.enterprise.inject.spi.Extension")
public class ExtensionsServicesFileResource extends FileResource<ExtensionsServicesFileResource> {

    @Inject
    public ExtensionsServicesFileResource(ResourceFactory factory) {
        super(factory, null);
        setFlag(ResourceFlag.Leaf);
    }
    
    public ExtensionsServicesFileResource(ResourceFactory factory, File file) {
        super(factory, file);
        setFlag(ResourceFlag.Leaf);
    }

    @Override
    public Resource<File> createFrom(File file) {
        return new ExtensionsServicesFileResource(getResourceFactory(), file);
    }

    @Override
    protected List<Resource<?>> doListResources() {
        List<Resource<?>> resourceList = new ArrayList<Resource<?>>();
        
        InputStreamReader inputStreamReader = new InputStreamReader(getResourceInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                resourceList.add(new ExtensionServiceResource(this, line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read javax.enterprise.inject.spi.Extension file", e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {}
        }
        return resourceList;
    }

    public boolean containsExtension(JavaClass javaClass) {
        for (Resource<?> line : doListResources()) {
            if (javaClass != null) {
                if (javaClass.getCanonicalName().equals(line.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void addExtension(JavaClass javaClass) {
        List<Resource<?>> list = doListResources();
        list.add(new ExtensionServiceResource(this, javaClass.getCanonicalName()));
        String contents = "";
        for (Resource<?> r : list) {
            contents += r.getName() + "\n";
        }
        setContents(contents);
    }
    
    private class ExtensionServiceResource extends VirtualResource<String> {
        private final String typestring; 
        
        protected ExtensionServiceResource(Resource<?> parent, String type) {
            super(parent);
            this.typestring = type;
        }

        @Override
        public boolean delete() throws UnsupportedOperationException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean delete(boolean paramBoolean) throws UnsupportedOperationException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getName() {
            return typestring;
        }

        @Override
        public String getUnderlyingResourceObject() {
            return null;
        }

        @Override
        protected List<Resource<?>> doListResources() {
            return Collections.emptyList();
        }
    }
}
