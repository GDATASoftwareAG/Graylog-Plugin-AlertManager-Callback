package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.PluginModule;

public class AlertManagerCallbackPluginModule extends PluginModule {

    @Override
    protected void configure() {
        addAlarmCallback(AlertManagerAlarmCallback.class);
    }
}
