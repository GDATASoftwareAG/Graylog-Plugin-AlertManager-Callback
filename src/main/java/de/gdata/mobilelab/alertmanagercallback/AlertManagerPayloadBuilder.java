package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AlertManagerPayloadBuilder {

    private static final String ALERTNAME_KEY = "alertname";

    private Stream stream;
    private AlertCondition.CheckResult checkResult;
    private Configuration configuration;
    private CustomPropertiesTextFieldParser customPropertiesTextFieldParser;

    private AlertManagerPayloadBuilder() {
        // Private constructor to hide the implicit one
        customPropertiesTextFieldParser = new CustomPropertiesTextFieldParser();
    }

    static AlertManagerPayloadBuilder newInstance() {
        return new AlertManagerPayloadBuilder();
    }

    AlertManagerPayloadBuilder withStream(Stream stream) {
        this.stream = stream;

        return this;
    }

    AlertManagerPayloadBuilder withCheckResult(AlertCondition.CheckResult checkResult) {
        this.checkResult = checkResult;

        return this;
    }

    AlertManagerPayloadBuilder withConfiguration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    AlertManagerPayload build() {
        AlertManagerPayload alertManagerPayload = new AlertManagerPayload();
        alertManagerPayload.setAnnotations(extractAnnotations());
        alertManagerPayload.setLabels(extractLabels());
        alertManagerPayload.setGeneratorURL(extractStreamUrl());
        alertManagerPayload.setStartsAt(extractStartsAt());
        alertManagerPayload.setEndsAt(extractEndsAt());

        return alertManagerPayload;
    }

    private Map<String, Object> extractLabels() {
        Map<String, Object> labels = new HashMap<>();
        if(configuration != null && configuration.getString(AlertManagerAlarmCallback.CONFIGURATION_KEY_ALERT_NAME) != null) {
            labels.put(ALERTNAME_KEY, configuration.getString(AlertManagerAlarmCallback.CONFIGURATION_KEY_ALERT_NAME));
        } else {
            labels.put(ALERTNAME_KEY, "Please add a valid configuration object to AlertManager plugin.");
        }

        // custom labels
        final String customLabelString = configuration != null ? configuration.getString(AlertManagerAlarmCallback.CONFIGURATION_KEY_CUSTOM_LABELS) : null;
        try {
            labels.putAll(customPropertiesTextFieldParser.extractKeyValuePairsFromCustomField(customLabelString));
        } catch (IOException e) {
            // damaged configuration, so we'll not put any additional label into the map
        }

        return labels;
    }

    private Map<String, Object> extractAnnotations() {
        Map<String, Object> annotations = new HashMap<>();

        if(stream != null && stream.getTitle() != null) {
            annotations.put("stream_title", stream.getTitle());
        }

        if(checkResult != null) {
            annotations.put("triggered_at", checkResult.getTriggeredAt() != null ? checkResult.getTriggeredAt().toString() : null);
            if(checkResult.getTriggeredCondition() != null) {
                annotations.put("triggered_rule_description", checkResult.getTriggeredCondition().getDescription());
                annotations.put("triggered_rule_title", checkResult.getTriggeredCondition().getTitle());
            }
        }

        // custom annotations
        final String customAnnotationString = configuration != null ? configuration.getString(AlertManagerAlarmCallback.CONFIGURATION_KEY_CUSTOM_ANNOTATIONS) : null;
        try {
            annotations.putAll(customPropertiesTextFieldParser.extractKeyValuePairsFromCustomField(customAnnotationString));
        } catch (IOException e) {
            // damaged configuration, so we'll not put any additional annotation into the map
        }

        return annotations;
    }

    private String extractEndsAt() {
        if(checkResult == null || checkResult.getTriggeredAt() == null || checkResult.getTriggeredCondition() == null) {
            return new DateTime().plusMinutes(1).toString();
        }

        return checkResult.getTriggeredAt().plusMinutes(checkResult.getTriggeredCondition().getGrace()).toString();
    }

    private String extractStartsAt() {
        if(checkResult == null || checkResult.getTriggeredAt() == null) {
            return new DateTime().toString();
        }

        return checkResult.getTriggeredAt().toString();
    }

    private String extractStreamUrl() {
        if(checkResult == null || checkResult.getTriggeredCondition() == null || checkResult.getTriggeredCondition().getParameters() == null) {
            return null;
        }

        return String.valueOf(checkResult.getTriggeredCondition().getParameters().get("stream_url"));
    }
}
