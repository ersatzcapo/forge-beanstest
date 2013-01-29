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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author Brandenstein
 */
public class SimpleRunner extends BlockJUnit4ClassRunner {
    private final Class<?> clazz;
    private static Weld bootloader;
    private static WeldContainer weldContainer;
 
    private static volatile int counter = 0;
 
    public SimpleRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
        this.clazz = clazz;
 
        if (counter == 0) {
            bootloader = new Weld();
            weldContainer = bootloader.initialize();
        }
        counter++;
    }
 
    @Override
    protected Object createTest() throws Exception {
        return weldContainer.instance().select(clazz).get();
    }
 
    @Override
    public void run(RunNotifier notifier) {
        try {
            super.run(notifier);
        } finally {
            counter--;
            if (counter == 0) {
                bootloader.shutdown();
            }
        }
    }
}
