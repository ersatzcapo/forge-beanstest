JBoss Forge Beanstest Plugin
============================

This plugin provides a simple junit runner, that starts up a weld se container and registers
the test class itself as CDI bean. Thus injection can be used in the unit test. Additionally there
is support for mocking missing scope contexts, the generation of test classes and mockito mocks.

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
	
This will add junit and weld se dependency to your pom and add a beans.xml to src/test/resources/META-INF.
It will also copy a class named "SimpleRunner" to your test folder. If not already done, it will also cause 
a "beans setup" for general CDI support.

Hide missing scopes
-------------------

Weld se does not support some web specific scopes such as request scope or session scope. Thus it
will throw an exception, when it detects a request or session scope annotation. To prevent this:

	beanstest hide-missing-scopes
	
This will add an extension, that will mock every missing scope context during startup of the weld container.

New test class
--------------

To generate a test class stub that uses the SimpleRunner:

	beanstest new-test --type <complete type>
	
New mockito mock
----------------

To generate a mock for a class in your project:

	beanstest new-mockito --type <class to mock>
	
This adds all necessary dependencies and creates the class AlternativeProducer right next to the SimpleRunner.
The AlternetiveProducer class will contain a CDI producer method that produces a mockito mock alterntative for the given type.
If you prefer to use alternative stereotypes instead of a plain alternative class just add the stereotype name like this:

	beanstest new-mockito --type <class to mock> --stereotype <name of stereotype>

This will additionally create a alternative stereotype annotation and add this to the producer method.	
	
Thx for your interest and thx to all that shared their ideas concerning this topic...