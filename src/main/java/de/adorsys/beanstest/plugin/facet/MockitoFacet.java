package de.adorsys.beanstest.plugin.facet;

import java.io.File;
import java.io.FileNotFoundException;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.jboss.forge.parser.JavaParser;
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

    public void createMockProducer(JavaResource type, String stereotype, PipeOut out) {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        JavaResource alternativesResource = null;
        
        try {
            alternativesResource = getAlternativesProducer();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("AlternativesProducer not found", e);
        }
        
        try {
            type.exists();
        } catch (Exception e) {
            ShellMessages.warn(out, "Type does not exist in source folder (compile error might occur): " + type);
        }
        
        JavaClass alternativesClass = JavaParser.parse(JavaClass.class, alternativesResource.getResourceInputStream());
        
        String className = java.calculateName(type);
        String packageName = java.calculatePackage(type);

        //add import
        alternativesClass.addImport(packageName + "." + className);
        
        //add producer method
        Method<JavaClass> producerMethod = alternativesClass.addMethod("public " + className + " produce" + className + "()"); //:(
        producerMethod.addAnnotation(Produces.class);
        producerMethod.setBody("mock(" + className + ".class);");
        
        //handle alternative annotation and beans.xml entry
        BeansDescriptor beansDescriptor = project.getFacet(CDITestFacet.class).getConfig();
        if (stereotype == null) {
            producerMethod.addAnnotation(Alternative.class);
            
            if (!beansDescriptor.getAlternativeClasses().contains(alternativesResource.getFullyQualifiedName())) { //TODO does not work :(
                beansDescriptor.alternativeClass(alternativesResource.getFullyQualifiedName());
            }
        }
        
        alternativesResource.setContents(alternativesClass);
    }

    private JavaResource getAlternativesProducer() throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        return java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", File.separator)+ "/AlternativesProducer.java");
    }
}
