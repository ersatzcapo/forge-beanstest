JBoss Forge Beanstest Plugin
============================

This plugin provides a simple junit runner, that starts up a Weld SE container and registers
the test class itself as CDI bean. Thus injection can be used in the unit test. Additionally there
is support the generation of test classes and mockito mocks, a JPA persistence test setup and a
extension for mocking missing scope contexts.

Install Beanstest Plugin
------------------------

Install and start jboss forge. In the forge shell type:
	
	forge git-plugin git://github.com/ersatzcapo/forge-beanstest.git
	
Alternatively you can download the source zip, extract it and type:

	forge source-plugin <relative path to source folder>
	
Setup Beanstest
---------------

When you are in the scope of a project you can setup beanstest with:

	beanstest setup
	
This will add junit and Weld SE dependency to your pom and add a beans.xml to src/test/resources/META-INF.
It will also copy a class named "SimpleRunner" to your test folder. If not already done, it will also cause 
a "beans setup" for general CDI support.

New test class
--------------

To generate a test class stub that uses the SimpleRunner:

	beanstest new-test --type <complete type>

Setup test persistence
----------------------

To enable the PersistenceContext annotation:

	beanstest test-persistence
	
This will create and register a PersistenceExtionsion, that will add a Weld JpaInjectionServices implementation. When the Weld container comes across a PersistenceContext
annotation, it will ask the MockJpaInjectionServices for a EntityManager. The default is to use Hibernate and a hsqldb database. 	

Mock missing scopes
-------------------
Weld SE does not support some web specific scopes such as request scope or session scope. Thus it
will throw an exception, when it detects a request or session scope annotation. To prevent this:

	beanstest mock-scopes
	
This will add an extension, that will mock every missing scope context during startup of the weld container.
	
New mockito mock
----------------

To generate a mock for a class in your project:

	beanstest new-mockito --type <class to mock>
	
This adds mockito dependencies and creates the class AlternativeProducer right next to the SimpleRunner.
The AlternetiveProducer class will contain a CDI producer method that produces a mockito mock alterntative for the given type.
It is registered in the test beans.xml with the stereotype BeanstestAlternative. To use a custom sterotype:

	beanstest new-mockito --type <class to mock> --stereotype <name of stereotype>

This will additionally create a alternative stereotype annotation and add this to the producer method.	
	
Thx for your interest and thx to all that shared their ideas concerning this topic...