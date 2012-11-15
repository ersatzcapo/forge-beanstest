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

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.adorsys.beanstest.ForgeTestCommons;

@RunWith(SimpleRunner.class)
public class NewTestTest {
    private static final String TESTPROJECTNAME = NewTestTest.class.getSimpleName();
    private static final String TESTPACKAGENAME = "de.adorsys.testproject";

    @Inject
    Shell shell;

    @Inject
    ForgeTestCommons forgeTestCommons;

    @Before
    public void init() throws Exception {
        forgeTestCommons.init(TESTPROJECTNAME, TESTPACKAGENAME, true);
    }

    @After
    public void destroy() throws Exception {
        forgeTestCommons.cleanUp();
    }

    @Test
    public void testNewTest() throws Exception {
        forgeTestCommons.setNewInput("\n10\n");

        shell.execute("beanstest setup");
        
        shell.execute("beanstest new-test --type " + TESTPACKAGENAME + ".FirstTest");
 
        // test test :)
        assertTrue("FirstTest was not created", new File("target/" 
                + TESTPROJECTNAME 
                + "/src/test/java/" + TESTPACKAGENAME.replaceAll("\\.", "/") + "/FirstTest.java").exists());

    }
    

}
