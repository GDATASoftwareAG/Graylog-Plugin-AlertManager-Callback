package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class AlertManagerPluginMetaData implements PluginMetaData {

    @Override
    public String getUniqueId() {
        return AlertManagerAlarmCallback.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "AlertManager Callback";
    }

    @Override
    public String getAuthor() {
        return "G DATA CyberDefense AG";
    }

    @Override
    public URI getURL() {
        return URI.create("https://www.gdata.de");
    }

    @Override
    public Version getVersion() {
        return Version.from(2, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Plugin for AlertManager HTTP-Callbacks.";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.from(3, 3, 5);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
