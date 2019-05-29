package de.gdata.mobilelab.alertmanagercallback;

import com.floreysoft.jmte.Engine;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.streams.Stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CustomPropertiesJMTEResolver {

    private Engine templateEngine;
    private Map<String, Object> templateModel;

    private CustomPropertiesJMTEResolver(Map<String, Object> templateModel) {
        templateEngine = Engine.createEngine();
        this.templateModel = templateModel;
    }

    Map<String, Object> transformTemplateValues(Map<String, Object> customValueMap) {
        final Map<String, Object> transformedCustomValueMap = new HashMap<>();
        customValueMap.forEach((key, value) -> {
            if (value instanceof String) {
                transformedCustomValueMap.put(key, templateEngine.transform((String) value, templateModel));
            } else {
                transformedCustomValueMap.put(key, value);
            }
        });
        return transformedCustomValueMap;
    }

    static final class Builder {

        private String url;
        private Stream stream;
        private AlertCondition.CheckResult checkResult;

        private Builder() {
            // private constructor to hide the implicit one
        }

        static Builder newBuilder() {
            return new Builder();
        }

        Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        Builder withStream(Stream stream) {
            this.stream = stream;
            return this;
        }

        Builder withCheckResult(AlertCondition.CheckResult checkResult) {
            this.checkResult = checkResult;
            return this;
        }

        CustomPropertiesJMTEResolver build() {
            return new CustomPropertiesJMTEResolver(createModel());
        }

        // parts copied from org.graylog2.alerts.FormattedEmailAlertSender#getModel()
        private Map<String, Object> createModel() {

            final Map<String, Object> model = new HashMap<>();
            model.put("stream", stream);
            model.put("stream_url", url);

            if (checkResult != null) {
                model.put("check_result", checkResult);
                model.put("alertCondition", checkResult.getTriggeredCondition());

                final List<Message> matchingMessages = checkResult.getMatchingMessages() != null ?
                        checkResult.getMatchingMessages()
                                .stream()
                                .map(MessageSummary::getRawMessage)
                                .collect(Collectors.toList()) :
                        Collections.emptyList();
                model.put("backlog", matchingMessages);
                model.put("backlog_size", matchingMessages.size());
            }

            return model;
        }
    }
}
