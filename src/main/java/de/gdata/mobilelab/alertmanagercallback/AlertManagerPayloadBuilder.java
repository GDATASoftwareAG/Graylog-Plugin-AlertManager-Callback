package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

class AlertManagerPayloadBuilder {

    private static final String STREAM_TITLE_KEY = "stream_title";
    private static final String ALERTMANAGER_ALERT_NAME_KEY = "alertmanager_alert_name";
    private static final String ALERTNAME_KEY = "alertname";
    private Stream stream;
    private AlertCondition.CheckResult checkResult;
    private Configuration configuration;

    private AlertManagerPayloadBuilder() {
        // Private constructor to hide the implicit one
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
        if(configuration != null && configuration.getString(ALERTMANAGER_ALERT_NAME_KEY) != null) {
            labels.put(ALERTNAME_KEY, configuration.getString(ALERTMANAGER_ALERT_NAME_KEY));
        } else {
            labels.put(ALERTNAME_KEY, "Please add a valid configuration object to AlertManager plugin.");
        }

        return labels;
    }

    private Map<String, Object> extractAnnotations() {
        Map<String, Object> annotations = new HashMap<>();

        if(stream != null && stream.getTitle() != null) {
            annotations.put(STREAM_TITLE_KEY, stream.getTitle());
        }

        if(checkResult != null) {
            annotations.put("triggered_at", checkResult.getTriggeredAt() != null ? checkResult.getTriggeredAt().toString() : null);
            if(checkResult.getTriggeredCondition() != null) {
                annotations.put("triggered_rule_description", checkResult.getTriggeredCondition().getDescription());
                annotations.put("triggered_rule_title", checkResult.getTriggeredCondition().getTitle());
            }
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
