package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlertManagerPayloadBuilderTest {

    @Test
    public void newInstance() {
        assertNotNull(AlertManagerPayloadBuilder.newInstance());
        assertEquals(AlertManagerPayloadBuilder.class, AlertManagerPayloadBuilder.newInstance().getClass());
    }

    @Test
    public void withStream() throws NoSuchFieldException, IllegalAccessException {
        // given: Access to private field stream
        Field streamField = AlertManagerPayloadBuilder.class.getDeclaredField("stream");
        streamField.setAccessible(true);

        // and: instance
        AlertManagerPayloadBuilder alertManagerPayloadBuilder = AlertManagerPayloadBuilder.newInstance();

        // and: a stream mock
        Stream stream = mock(Stream.class);

        // when: calling withStream with given stream
        alertManagerPayloadBuilder = alertManagerPayloadBuilder.withStream(stream);

        // then: stream has been set
        assertEquals(stream, streamField.get(alertManagerPayloadBuilder));
    }

    @Test
    public void withCheckResult() throws IllegalAccessException, NoSuchFieldException {
        // given: Access to private field checkResult
        Field checkResultField = AlertManagerPayloadBuilder.class.getDeclaredField("checkResult");
        checkResultField.setAccessible(true);

        // and: instance
        AlertManagerPayloadBuilder alertManagerPayloadBuilder = AlertManagerPayloadBuilder.newInstance();

        // and: a checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);

        // when: calling withCheckResult with given checkResult
        alertManagerPayloadBuilder = alertManagerPayloadBuilder.withCheckResult(checkResult);

        // then: checkResult has been set
        assertEquals(checkResult, checkResultField.get(alertManagerPayloadBuilder));
    }

    @Test
    public void withConfiguration() throws NoSuchFieldException, IllegalAccessException {
        // given: Access to private field configuration
        Field configurationField = AlertManagerPayloadBuilder.class.getDeclaredField("configuration");
        configurationField.setAccessible(true);

        // and: instance
        AlertManagerPayloadBuilder alertManagerPayloadBuilder = AlertManagerPayloadBuilder.newInstance();

        // and: a configuration mock
        Configuration configuration = mock(Configuration.class);

        // when: calling withConfiguration with given configuration
        alertManagerPayloadBuilder = alertManagerPayloadBuilder.withConfiguration(configuration);

        // then: configuration has been set
        assertEquals(configuration, configurationField.get(alertManagerPayloadBuilder));
    }

    @Test
    public void buildWithNoInformation() {
        // given: object with no information given
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance().build();

        // expect: default values have been set if any
        assertNotNull(alertManagerPayload);
        assertTrue(alertManagerPayload.getAnnotations().isEmpty());
        assertFalse(alertManagerPayload.getLabels().isEmpty());
        assertTrue(alertManagerPayload.getLabels().containsKey("alertname"));
        assertEquals("Please add a valid configuration object to AlertManager plugin.", alertManagerPayload.getLabels().get("alertname"));
        assertNull(alertManagerPayload.getGeneratorURL());
        assertNotNull(alertManagerPayload.getStartsAt());
        assertNotNull(alertManagerPayload.getEndsAt());
    }

    @Test
    public void buildWithFullInformation() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn("AlertName");

        // and: stream mock
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn("StreamTitle");

        // and: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        DateTime triggeredAt = new DateTime();
        when(checkResult.getTriggeredAt()).thenReturn(triggeredAt);
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getDescription()).thenReturn("aDescription");
        when(alertCondition.getTitle()).thenReturn("aTitle");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stream_url", "aStreamUrl");
        when(alertCondition.getParameters()).thenReturn(parameters);
        when(alertCondition.getGrace()).thenReturn(1337);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withStream(stream)
                                                                            .withConfiguration(configuration)
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("AlertName", alertManagerPayload.getLabels().get("alertname"));

        // - annotations
        assertEquals("StreamTitle", alertManagerPayload.getAnnotations().get("stream_title"));
        assertEquals(triggeredAt.toString(), alertManagerPayload.getAnnotations().get("triggered_at"));
        assertEquals("aDescription", alertManagerPayload.getAnnotations().get("triggered_rule_description"));
        assertEquals("aTitle", alertManagerPayload.getAnnotations().get("triggered_rule_title"));

        // - startsAt
        assertEquals(triggeredAt.toString(), alertManagerPayload.getStartsAt());

        // - endsAt
        assertEquals(triggeredAt.plusMinutes(1337).toString(), alertManagerPayload.getEndsAt());

        // - generatorUrl
        assertEquals("aStreamUrl", alertManagerPayload.getGeneratorURL());
    }

    @Test
    public void buildWithNoInformationForLabels() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn(null);


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("Please add a valid configuration object to AlertManager plugin.", alertManagerPayload.getLabels().get("alertname"));
    }

    @Test
    public void buildWithNoInformationForAnnotations() {
        // given: stream mock
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn(null);

        // and: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getTriggeredAt()).thenReturn(null);
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getDescription()).thenReturn(null);
        when(alertCondition.getTitle()).thenReturn(null);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withStream(stream)
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertFalse(alertManagerPayload.getAnnotations().containsKey("stream_title"));
        assertNull(alertManagerPayload.getAnnotations().get("triggered_at"));
        assertNull(alertManagerPayload.getAnnotations().get("triggered_rule_description"));
        assertNull(alertManagerPayload.getAnnotations().get("triggered_rule_title"));
    }

    @Test
    public void buildWithNoInformationForEndsAt() {
        // given: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getTriggeredAt()).thenReturn(null);
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getGrace()).thenReturn(0);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - endsAt
        assertNotNull(alertManagerPayload.getEndsAt());
        assertNotEquals("", alertManagerPayload.getEndsAt());
    }

    @Test
    public void buildWithNoInformationForStartsAt() {
        // given: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getTriggeredAt()).thenReturn(null);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - startsAt
        assertNotNull(alertManagerPayload.getStartsAt());
        assertNotEquals("", alertManagerPayload.getStartsAt());
    }

    @Test
    public void buildWithNoInformationForStreamUrl() {
        // given: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        AlertCondition alertCondition = mock(AlertCondition.class);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stream_url", null);
        when(alertCondition.getParameters()).thenReturn(parameters);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - generatorUrl
        assertEquals("null", alertManagerPayload.getGeneratorURL());
    }

    @Test
    public void buildWithNoInformationForStreamUrlParameters() {
        // given: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.getParameters()).thenReturn(null);
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - generatorUrl
        assertNull(alertManagerPayload.getGeneratorURL());
    }
}