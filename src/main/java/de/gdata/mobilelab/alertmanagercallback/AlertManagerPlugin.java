package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class AlertManagerPlugin implements Plugin {

    @Override
    public PluginMetaData metadata() {
        return new AlertManagerPluginMetaData();
    }

    @Override
    public Collection<PluginModule> modules() {
        return Collections.singleton(new AlertManagerCallbackPluginModule());
    }
}
