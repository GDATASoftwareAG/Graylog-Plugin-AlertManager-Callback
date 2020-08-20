package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.Version;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlertManagerPluginMetaDataTest {

    @Test
    public void inheritance() {
        assertTrue(PluginMetaData.class.isAssignableFrom(AlertManagerPluginMetaData.class));
    }

    @Test
    public void getUniqueId() {
        assertEquals(AlertManagerAlarmCallback.class.getCanonicalName(), new AlertManagerPluginMetaData().getUniqueId());
    }

    @Test
    public void getName() {
        assertEquals("AlertManager Callback", new AlertManagerPluginMetaData().getName());
    }

    @Test
    public void getAuthor() {
        assertEquals("G DATA CyberDefense AG", new AlertManagerPluginMetaData().getAuthor());
    }

    @Test
    public void getURL() {
        assertEquals(URI.create("https://www.gdata.de"), new AlertManagerPluginMetaData().getURL());
    }

    @Test
    public void getVersion() {
        assertEquals(Version.from(1, 2, 0), new AlertManagerPluginMetaData().getVersion());
    }

    @Test
    public void getDescription() {
        assertEquals("Plugin for AlertManager HTTP-Callbacks.", new AlertManagerPluginMetaData().getDescription());
    }

    @Test
    public void getRequiredVersion() {
        assertEquals(Version.from(3, 3, 5), new AlertManagerPluginMetaData().getRequiredVersion());
    }

    @Test
    public void getRequiredCapabilities() {
        assertTrue(new AlertManagerPluginMetaData().getRequiredCapabilities().isEmpty());
    }
}