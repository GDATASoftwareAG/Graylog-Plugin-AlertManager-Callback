package de.gdata.mobilelab.alertmanagercallback;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class AlertManagerPostRequestSender {

    private ObjectMapper objectMapper;
    private final String alertManagerApiUrl;

    /**
     * Initialized a new POST request sender for the AlertManager callback with given AlertManager API URL.
     *
     * @param alertManagerApiUrl the AlertManager API URL to use
     */
    AlertManagerPostRequestSender(String alertManagerApiUrl) {
        this.alertManagerApiUrl = alertManagerApiUrl;
        objectMapper = new ObjectMapper();
    }

    /**
     * Sends the HTTP-POST request to the AlertManager with given payload.
     *
     * @param alertManagerPayload the payload to send
     * @throws AlarmCallbackException if sending POST fails
     */
    void sendPostRequestToAlertManager(AlertManagerPayload alertManagerPayload) throws AlarmCallbackException {
        Object[] wrapper = new Object[1];
        wrapper[0] = alertManagerPayload;
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
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder sb = new StringBuilder();
        int HttpResult = connection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            connection.disconnect();
            return sb.toString();
        } else {
            throw new IOException("Could not get a valid response for POST-request to '" + targetUrl
                                          + "'. ResponseCode: " + connection.getResponseCode()
                                          + " ResponseMessage: '" + connection.getResponseMessage() + "'.");
        }
    }

}
