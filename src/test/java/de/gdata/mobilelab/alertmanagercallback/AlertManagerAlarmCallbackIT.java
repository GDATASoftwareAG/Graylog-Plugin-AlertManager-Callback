package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlertManagerAlarmCallbackIT {

    @Rule
    public GenericContainer alertManagerContainer = new GenericContainer("prom/alertmanager:v0.15.3")
            .withExposedPorts(9093)
            .waitingFor(new HostPortWaitStrategy());

    private AlertManagerAlarmCallback alertManagerAlarmCallback;

    @Before
    public void setUp() throws Exception {
        alertManagerAlarmCallback = new AlertManagerAlarmCallback();
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put("alertmanager_api_url", "http://" + alertManagerContainer.getContainerIpAddress()
                + ":" + alertManagerContainer.getMappedPort(9093) + "/api/v1/alerts");
        configurationMap.put("alertmanager_alert_name", "TestAlert1");
        Configuration configuration = new Configuration(configurationMap);
        alertManagerAlarmCallback.initialize(configuration);
    }



    @Test
    public void alertManagerIsRunning() {
        assertTrue(alertManagerContainer.isRunning());
        RestTemplate restTemplate = new RestTemplate();
        assertEquals(
                "AlertManager does not provide success-answer on sending test alert",
                "{\"status\":\"success\"}",
                restTemplate.postForObject(
                        "http://" + alertManagerContainer.getContainerIpAddress()
                                + ":" + alertManagerContainer.getMappedPort(9093)
                                + "/api/v1/alerts",
                        "[{\"labels\":{\"alertname\":\"TestAlert1\"}}]",
                        String.class
                )
        );
    }

    @Test
    public void call() throws AlarmCallbackException {
        // given: Stream stub
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn("StreamTitle");

        // and: AlertCondition stub
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getTitle()).thenReturn("aTitle");
        when(alertCondition.getDescription()).thenReturn("aDescription");
        when(alertCondition.getGrace()).thenReturn(1); // Grace time in minutes
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stream_url", "http://localhost/foo/bar");
        when(alertCondition.getParameters()).thenReturn(parameters);

        // and: AlertCondition.CheckResult stub
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getTriggeredAt()).thenReturn(new DateTime());
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);
        when(checkResult.getMatchingMessages()).thenReturn(Collections.emptyList());

        // expect: No exception thrown
        alertManagerAlarmCallback.call(stream, checkResult);

        // and: Alert has been triggered in AlertManager
        String alertOverview = new RestTemplate().getForObject("http://" + alertManagerContainer.getContainerIpAddress()
                                                                       + ":" + alertManagerContainer.getMappedPort(9093)
                                                                       + "/api/v1/alerts/groups",
                                                               String.class);
        assertNotNull(alertOverview);
        assertTrue(alertOverview.contains("TestAlert1"));
    }

    @Test
    public void callWithNullValues() throws AlarmCallbackException {
        // given: Stream stub
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn(null);

        // and: AlertCondition stub
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getTitle()).thenReturn(null);
        when(alertCondition.getDescription()).thenReturn(null);
        when(alertCondition.getGrace()).thenReturn(1); // Grace time in minutes
        when(alertCondition.getParameters()).thenReturn(null);

        // and: AlertCondition.CheckResult stub
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getTriggeredAt()).thenReturn(null);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);
        when(checkResult.getMatchingMessages()).thenReturn(null);

        // expect: No exception thrown
        alertManagerAlarmCallback.call(stream, checkResult);

        // and: Alert has been triggered in AlertManager
        String alertOverview = new RestTemplate().getForObject("http://" + alertManagerContainer.getContainerIpAddress()
                                                                       + ":" + alertManagerContainer.getMappedPort(9093)
                                                                       + "/api/v1/alerts/groups",
                                                               String.class);
        assertNotNull(alertOverview);
        assertTrue(alertOverview.contains("TestAlert1"));
    }

}
