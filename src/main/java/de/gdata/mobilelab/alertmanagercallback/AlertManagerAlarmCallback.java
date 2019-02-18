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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class AlertManagerAlarmCallback implements AlarmCallback {

    static final String CONFIGURATION_KEY_API_URL = "alertmanager_api_url";
    static final String CONFIGURATION_KEY_ALERT_NAME = "alertmanager_alert_name";
    static final String CONFIGURATION_KEY_CUSTOM_LABELS = "alertmanager_custom_labels";
    static final String CONFIGURATION_KEY_CUSTOM_ANNOTATIONS = "alertmanager_custom_annotations";

    private Configuration configuration;
    private AlertManagerPostRequestSender alertManagerPostRequestSender;

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        configuration = config;
        alertManagerPostRequestSender = new AlertManagerPostRequestSender(config.getString(CONFIGURATION_KEY_API_URL));
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
                CONFIGURATION_KEY_API_URL,
                "AlertManager API URL",
                "http://localhost:9093/api/v1/alerts",
                "This callback sends a POST-Request to an AlertManager API. It converts the information into a format which is readable by the AlertManager.",
                Optional.NOT_OPTIONAL
        );
        configurationRequest.addField(alertmanagerApiUrl);

        // Alert Name
        ConfigurationField alertName = new TextField(
                CONFIGURATION_KEY_ALERT_NAME,
                "AlertManager Alert Name",
                "TestAlert",
                "The name for the specific AlertManager alert (will be transmitted as 'alertname'-label).",
                Optional.NOT_OPTIONAL
        );
        configurationRequest.addField(alertName);

        // Custom labels
        ConfigurationField customLabels = new TextField(
                CONFIGURATION_KEY_CUSTOM_LABELS,
                "Custom AlertManager labels",
                "",
                "The custom AlertManager label key-value-pairs separated by '" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "' to set for each alert. Please use the following notation: 'label1=value1" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "label2=value2'",
                Optional.OPTIONAL
        );
        configurationRequest.addField(customLabels);

        // Custom annotations
        ConfigurationField customAnnotations = new TextField(
                CONFIGURATION_KEY_CUSTOM_ANNOTATIONS,
                "Custom AlertManager annotations",
                "",
                "The custom AlertManager annotation key-value-pairs separated by '" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "' to set for each alert. Please use the following notation: 'annotation1=value1" + CustomPropertiesTextFieldParser.KEY_VALUE_PAIR_SEPARATOR + "annotation2=value2'",
                Optional.OPTIONAL
        );
        configurationRequest.addField(customAnnotations);

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
        final String apiUrl = configuration.getString(CONFIGURATION_KEY_API_URL);
        if (apiUrl == null) {
            throw new ConfigurationException("AlertManager API URL has to be set.");
        }

        try {
            new URI(apiUrl);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("The URL: '" + apiUrl + "' is not a valid URL. " + e.getMessage());
        }


        CustomPropertiesTextFieldParser customPropertiesTextFieldParser = new CustomPropertiesTextFieldParser();

        final String customLabels = configuration.getString(CONFIGURATION_KEY_CUSTOM_LABELS);
        try {
            customPropertiesTextFieldParser.extractKeyValuePairsFromCustomField(customLabels);
        } catch (IOException e) {
            // Not a valid configuration for custom labels
            throw new ConfigurationException("The format for given custom labels is invalid. " + e.getMessage());
        }

        final String customAnnotations = configuration.getString(CONFIGURATION_KEY_CUSTOM_ANNOTATIONS);
        try {
            customPropertiesTextFieldParser.extractKeyValuePairsFromCustomField(customAnnotations);
        } catch (IOException e) {
            // Not a valid configuration for custom labels
            throw new ConfigurationException("The format for given custom annotations is invalid. " + e.getMessage());
        }
    }
}
