package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.PluginModule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AlertManagerCallbackPluginModuleTest {

    @Test
    public void inheritance() {
        assertTrue(PluginModule.class.isAssignableFrom(AlertManagerCallbackPluginModule.class));
    }
    
}