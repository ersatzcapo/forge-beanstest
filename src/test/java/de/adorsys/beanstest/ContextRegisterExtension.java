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


public class ContextRegisterExtension implements Extension {
    
    private Set<Class<? extends Annotation>> missingScopes = new HashSet<Class<? extends Annotation>>();

    public void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat, BeanManager beanManager)  {
        for (Annotation a: pat.getAnnotatedType().getAnnotations()) {
            if (!beanManager.isNormalScope(a.annotationType())) {
                continue;
            }
            try {
                beanManager.getContext(a.annotationType());
            } catch (ContextNotActiveException x) {
                missingScopes.add(a.annotationType());
            }
        }
    }
    
    protected void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        for (Class<? extends Annotation> a : missingScopes) {
            try {
                beanManager.getContext(a);
            } catch (ContextNotActiveException x) {
                afterBeanDiscovery.addContext(new ContextImpl(a)); // nur wenn es ihn noch nicht gibt
            }
        }
    }
    
    private static class ContextImpl implements Context {
        private final Class<? extends Annotation> scopeAnno;
        private Map<Contextual<?>, Object> instances;
        
        ContextImpl(Class<? extends Annotation> scopeAnno) {
            this.scopeAnno = scopeAnno;
            instances = new HashMap<Contextual<?>, Object>();
        }
        
        @Override
        public Class<? extends Annotation> getScope() {
            return scopeAnno;
        }

        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            T tee = contextual.create(creationalContext);
            instances.put(contextual, tee);
            return tee;
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
