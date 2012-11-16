package de.adorsys.beanstest.plugin.facet;

import java.io.File;
import java.io.FileNotFoundException;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

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
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        JavaClass alternativesProducer = JavaParser.parse(JavaClass.class, getClass().getResourceAsStream("/de/adorsys/beanstest/AlternativesProducer.jv"));
        alternativesProducer.setPackage(java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX);
        try {
            java.saveTestJavaSource(alternativesProducer);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("AlternativesProducer could not be created", e);
        }
        
        return true;
    }

    @Override
    public boolean isInstalled() {
        DependencyFacet dependencyFacet = getProject().getFacet(DependencyFacet.class);
        boolean mockitoDep = dependencyFacet.hasEffectiveDependency(DependencyBuilder.create(MOCKITO));  
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        boolean alternativesProducer = false;
        try {
            alternativesProducer = java.getTestJavaResource((java.getBasePackage() + BeanstestConfiguration.PACKAGESUFFIX).replaceAll("\\.", File.separator) + "/AlternativesProducer.java").exists();
        } catch (FileNotFoundException e) {}
        return mockitoDep && alternativesProducer;
    }
    
    public void createDies() {
        
    }

    public void createDas() {
        
    }
}
