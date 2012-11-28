package de.adorsys.beanstest.plugin.facet;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Stereotype;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaAnnotation;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.Method;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.shrinkwrap.descriptor.api.spec.cdi.beans.BeansDescriptor;

import de.adorsys.beanstest.plugin.BeanstestConfiguration;

@Alias("beanstest.MockitoFacet")
@RequiresFacet({ DependencyFacet.class, JavaSourceFacet.class, CDITestFacet.class })
public class MockitoFacet extends BaseFacet {
    public static final Dependency MOCKITO = DependencyBuilder.create("org.mockito:mockito-all:1.9.5:test");

    @Override
    public boolean install() {
        // add mockito dependency
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        dependencyFacet.addDirectDependency(MOCKITO);
        
        // create AlternativesProducer
        JavaClass alternativesProducer = JavaParser.parse(JavaClass.class, getClass().getResourceAsStream("/de/adorsys/beanstest/AlternativesProducer.jv"));
        try {
            JavaResource altResource = getAlternativesProducer();
            altResource.setContents(alternativesProducer);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("AlternativesProducer could not be created", e);
        }
        
        return true;
    }

    @Override
    public boolean isInstalled() {
        DependencyFacet dependencyFacet = getProject().getFacet(DependencyFacet.class);
        boolean mockitoDep = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(MOCKITO));  
        boolean alternativesProducer = false;
        try {
            alternativesProducer = getAlternativesProducer().exists();
        } catch (Exception e) {}
        return mockitoDep && alternativesProducer;
    }

    public void createMockProducer(JavaResource type, String stereotype, PipeOut out) throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        final String beanstestPackage = java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX;
        JavaResource alternativesResource = getAlternativesProducer();
        
        try {
            type.exists();
        } catch (Exception e) {
            ShellMessages.warn(out, "Type does not exist in source folder (compile error might occur): " + type);
        }
        
        JavaClass alternativesClass = JavaParser.parse(JavaClass.class, alternativesResource.getResourceInputStream());
        alternativesClass.setPackage(beanstestPackage);
        
        String className = java.calculateName(type);
        String packageName = java.calculatePackage(type);

        //add import
        alternativesClass.addImport(packageName + "." + className);
        
        //add producer method
        Method<JavaClass> producerMethod = alternativesClass.addMethod("public " + className + " produce" + className + "()"); //:(
        producerMethod.addAnnotation(Produces.class);
        producerMethod.setBody("return mock(" + className + ".class);");
        
        //handle alternative annotation and beans.xml entry
        BeansDescriptor beansDescriptor = project.getFacet(CDITestFacet.class).getConfig();
        if (stereotype == null) {
            if(alternativesClass.getAnnotation(Alternative.class) == null) {
                alternativesClass.addAnnotation(Alternative.class);
            }
            
            if (!beansDescriptor.getAlternativeClasses().contains(alternativesClass.getCanonicalName())) { 
                beansDescriptor.alternativeClass(alternativesClass.getCanonicalName());
                project.getFacet(CDITestFacet.class).saveConfig(beansDescriptor);
            }
        } else {
            //handle stereotype
            createStereotype(beanstestPackage, stereotype);
            producerMethod.addAnnotation(stereotype);
            
        }
        
        alternativesResource.setContents(alternativesClass);
    }

    private void createStereotype(String beanstestPackage, String stereotype) throws FileNotFoundException {
//        @Stereotype
//        @Alternative
//        @Target(ElementType.METHOD)
//        @Retention(RetentionPolicy.RUNTIME)
//        public @interface ProduceReceiverAlternative {
//        }
        
        //TODO how do i create a nested annotation with Forge ?
        
        JavaResource stereotypeResource = getStereotype(stereotype);
        
        if (!stereotypeResource.exists()) {
            JavaAnnotation javaAnnotation = JavaParser.create(JavaAnnotation.class);
            javaAnnotation.setName(stereotype);
            javaAnnotation.setPackage(beanstestPackage);
            javaAnnotation.addAnnotation(Stereotype.class);
            javaAnnotation.addAnnotation(Alternative.class);
            javaAnnotation.addAnnotation(Target.class).setEnumValue(ElementType.METHOD);
            javaAnnotation.addAnnotation(Retention.class).setEnumValue(RetentionPolicy.RUNTIME);
            
            stereotypeResource.setContents(javaAnnotation);
            
            BeansDescriptor beansDescriptor = project.getFacet(CDITestFacet.class).getConfig();
            if (!beansDescriptor.getAlternativeStereotypes().contains(javaAnnotation.getCanonicalName())) { 
                beansDescriptor.alternativeStereotype(javaAnnotation.getCanonicalName());
                project.getFacet(CDITestFacet.class).saveConfig(beansDescriptor);
            }
        }
    }

    private JavaResource getAlternativesProducer() throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        return java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/")+ "/AlternativesProducer.java");
    }
    
    private JavaResource getStereotype(String stereotype) throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        return java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", "/")+ "/"+ stereotype + ".java");
    }
}
