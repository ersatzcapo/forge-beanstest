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

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Hides CDI scopes that are not implemented in Weld SE, e.g. RequestScope, SessionScope.
 * Does not emulate the real behaviour of these scopes, but prevents the missing scope exception.
 */
public class MockMissingScopesExtension implements Extension {
    private Set<Class<? extends Annotation>> missingScopes = new HashSet<Class<? extends Annotation>>();

    public void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat, BeanManager manager)  {
        for (Annotation a: pat.getAnnotatedType().getAnnotations()) {
            if (!manager.isNormalScope(a.annotationType())) {
                continue;
            }
            try {
                manager.getContext(a.annotationType());
            } catch (ContextNotActiveException x) {
                missingScopes.add(a.annotationType());
            }
        }
    }
    
    protected void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager manager) {
        for (Class<? extends Annotation> a : missingScopes) {
            try {
                manager.getContext(a);
            } catch (ContextNotActiveException x) {
                afterBeanDiscovery.addContext(new CDIContextMock(a)); 
            }
        }
    }
    
    private static class CDIContextMock implements Context {
        private final Class<? extends Annotation> scopeAnnotation;
        private Map<Contextual<?>, Object> instances;
        
        CDIContextMock(Class<? extends Annotation> scopeAnnotation) {
            this.scopeAnnotation = scopeAnnotation;
            instances = new HashMap<Contextual<?>, Object>();
        }
        
        @Override
        public Class<? extends Annotation> getScope() {
            return scopeAnnotation;
        }

        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            T instance = contextual.create(creationalContext);
            instances.put(contextual, instance);
            return instance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get(Contextual<T> contextual) {
            return (T) instances.get(contextual);
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
 }
