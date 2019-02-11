package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.ConfigurationField.Optional;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class AlertManagerAlarmCallback implements AlarmCallback {

    private Configuration configuration;
    private AlertManagerPostRequestSender alertManagerPostRequestSender;

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        configuration = config;
        alertManagerPostRequestSender = new AlertManagerPostRequestSender(config.getString("alertmanager_api_url"));
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(result)
                                                                            .withConfiguration(configuration)
                                                                            .withStream(stream)
                                                                            .build();

        alertManagerPostRequestSender.sendPostRequestToAlertManager(alertManagerPayload);
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        ConfigurationRequest configurationRequest = new ConfigurationRequest();

        // API URL
        ConfigurationField alertmanagerApiUrl = new TextField(
                "alertmanager_api_url",
                "AlertManager API URL",
                "http://localhost:9093/api/v1/alerts",
                "This callback sends a POST-Request to an AlertManager API. It converts the information into a format which is readable by the AlertManager.",
                Optional.NOT_OPTIONAL
        );
        configurationRequest.addField(alertmanagerApiUrl);

        // Alert Name
        ConfigurationField alertName = new TextField(
                "alertmanager_alert_name",
                "AlertManager Alert Name",
                "TestAlert",
                "The name for the specific AlertManager alert (will be transmitted as 'alertname'-label).",
                Optional.NOT_OPTIONAL
        );
        configurationRequest.addField(alertName);

        return configurationRequest;
    }

    @Override
    public String getName() {
        return "AlertManager Callback";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return configuration.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        String apiUrl = configuration.getString("alertmanager_api_url");
        if (apiUrl == null) {
            throw new ConfigurationException("AlertManager API URL has to be set.");
        }
        try {
            new URI(apiUrl);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("The URL: '" + apiUrl + "' is not a valid URL. " + e.getMessage());
        }
    }
}
