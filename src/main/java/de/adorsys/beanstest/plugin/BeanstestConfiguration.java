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

import javax.enterprise.context.ApplicationScoped;

import org.jboss.forge.project.dependencies.Dependency;

@ApplicationScoped
public class BeanstestConfiguration {
    public static final String PACKAGESUFFIX = ".beanstest";
    
    private Dependency weldseDependency;

    public Dependency getWeldseDependency() {
        return weldseDependency;
    }

    public void setWeldseDependency(Dependency weldseDependency) {
        this.weldseDependency = weldseDependency;
    }
    
    public String persistenceExtensionTemplate() {
        if (weldseDependency != null && weldseDependency.getVersion() != null && !weldseDependency.getVersion().startsWith("2")) {
            return "/de/adorsys/beanstest/PersistenceExtension.jv";
        }
        return "/de/adorsys/beanstest/PersistenceExtension_weld20.jv"; //default
    }
    
    public String mockJpaInjectionServicesTemplate() {
        if (weldseDependency != null && weldseDependency.getVersion() != null && !weldseDependency.getVersion().startsWith("2")) {
            return "/de/adorsys/beanstest/MockJpaInjectionServices.jv";
        }
        return "/de/adorsys/beanstest/MockJpaInjectionServices_weld20.jv"; //default
    }    
}
