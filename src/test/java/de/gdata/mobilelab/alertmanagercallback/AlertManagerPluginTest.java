package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AlertManagerPluginTest {

    @Test
    public void inheritance() {
        assertTrue(Plugin.class.isAssignableFrom(AlertManagerPlugin.class));
    }

    @Test
    public void metadata() {
        PluginMetaData pluginMetaData = new AlertManagerPlugin().metadata();
        assertNotNull(pluginMetaData);
        assertEquals(AlertManagerPluginMetaData.class, pluginMetaData.getClass());
    }

    @Test
    public void modules() {
        Collection<PluginModule> modules = new AlertManagerPlugin().modules();
        assertEquals(1, modules.size());
        assertEquals(AlertManagerCallbackPluginModule.class, modules.toArray()[0].getClass());
    }
}