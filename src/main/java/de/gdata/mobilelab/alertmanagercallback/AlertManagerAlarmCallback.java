package de.gdata.mobilelab.alertmanagercallback;

import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AlertManagerAlarmCallback implements AlarmCallback {

    private Configuration configuration;
    private ObjectMapper objectMapper;

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        configuration = config;
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        AlertManagerPayload alertManagerPayload = new AlertManagerPayload();
        alertManagerPayload.setStartsAt(result.getTriggeredAt().toString());
        alertManagerPayload.setEndsAt(result.getTriggeredAt().plusMinutes(result.getTriggeredCondition().getGrace()).toString());
        Object streamUrl = result.getTriggeredCondition().getParameters().get("stream_url");
        alertManagerPayload.setGeneratorURL(streamUrl != null ? (String) streamUrl : null);

        Map<String, Object> labels = new HashMap<>();
        labels.put("alertname", configuration.getString("alertmanager_alert_name"));
        alertManagerPayload.setLabels(labels);

        Map<String, Object> annotations = new HashMap<>();
        annotations.put("stream_title", stream.getTitle());
        annotations.put("triggered_at", result.getTriggeredAt().toString());
        annotations.put("triggered_rule_description", result.getTriggeredCondition().getDescription());
        annotations.put("triggered_rule_title", result.getTriggeredCondition().getTitle());
        alertManagerPayload.setAnnotations(annotations);

        Object[] wrapper = new Object[1];
        wrapper[0] = alertManagerPayload;

        final String alertManagerApiUrl = configuration.getString("alertmanager_api_url");
        try {
            String responseAsString = postForResponseAsString(alertManagerApiUrl, objectMapper.writeValueAsString(wrapper));
            AlertManagerResponse alertManagerResponse = objectMapper.readValue(responseAsString, AlertManagerResponse.class);
            if (!AlertManagerResponse.STATUS_SUCCESS.equals(alertManagerResponse.getStatus())) {
                throw new AlarmCallbackException("Response from AlertManager for Alert failed. Response-Status: '"
                                                         + alertManagerResponse.getStatus() + "'.");
            }
        } catch (Exception e) {
            throw new AlarmCallbackException("Could not send Alert to AlertManager (" + alertManagerApiUrl + ").", e);
        }
    }

    /**
     * Sends the POST-request to the given targetUrl with given payload as body.
     *
     * @param targetUrl the target url of POST-request
     * @param payload the payload (JSON body)
     * @return the response
     * @throws IOException if request fails
     */
    private String postForResponseAsString(String targetUrl, String payload) throws IOException {
        URL apiUrl = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "application/json;");
        connection.setRequestProperty("Accept", "application/json,text/plain");
        connection.setRequestProperty("Method", "POST");
        OutputStream os = connection.getOutputStream();
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.close();

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            connection.disconnect();
            return sb.toString();
        } else {
            throw new IOException("Could not get a valid response for POST-request to '" + targetUrl
                                          + "'. ResponseCode: " + connection.getResponseCode()
                                          + " ResponseMessage: '" + connection.getResponseMessage() + "'.");
        }
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
