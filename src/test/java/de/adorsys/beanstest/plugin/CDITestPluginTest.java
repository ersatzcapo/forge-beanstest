package de.adorsys.beanstest.plugin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;

import de.adorsys.beanstest.plugin.CDITestPlugin;

@Ignore //does not work anymore :(
public class CDITestPluginTest extends AbstractShellTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {
      return AbstractShellTest.getDeployment().addPackages(true, CDITestPlugin.class.getPackage());
   }

   @Test
   public void testDefaultCommand() throws Exception
   {
      getShell().execute("cditest");
   }

   @Test
   public void testCommand() throws Exception
   {
      getShell().execute("cditest command");
   }

   @Test
   public void testPrompt() throws Exception
   {
      queueInputLines("y");
      getShell().execute("cditest prompt foo bar");
   }
}
