package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn("environment: production;system=webapp;priority=major");
        when(configuration.getString("alertmanager_custom_labels")).thenReturn("environmentlabel: productionlabel;systemlabel=webapplabel;prioritylabel=majorlabel");

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
        assertEquals("productionlabel", alertManagerPayload.getLabels().get("environmentlabel"));
        assertEquals("webapplabel", alertManagerPayload.getLabels().get("systemlabel"));
        assertEquals("majorlabel", alertManagerPayload.getLabels().get("prioritylabel"));

        // - annotations
        assertEquals("StreamTitle", alertManagerPayload.getAnnotations().get("stream_title"));
        assertEquals(triggeredAt.toString(), alertManagerPayload.getAnnotations().get("triggered_at"));
        assertEquals("aDescription", alertManagerPayload.getAnnotations().get("triggered_rule_description"));
        assertEquals("aTitle", alertManagerPayload.getAnnotations().get("triggered_rule_title"));
        assertEquals("production", alertManagerPayload.getAnnotations().get("environment"));
        assertEquals("webapp", alertManagerPayload.getAnnotations().get("system"));
        assertEquals("major", alertManagerPayload.getAnnotations().get("priority"));

        // - startsAt
        assertEquals(triggeredAt.toString(), alertManagerPayload.getStartsAt());

        // - endsAt
        assertEquals(triggeredAt.plusMinutes(1337).plusSeconds(10).toString(), alertManagerPayload.getEndsAt());

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
        DateTime triggeredAt = new DateTime();
        when(checkResult.getTriggeredAt()).thenReturn(triggeredAt);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - endsAt
        assertNotNull(alertManagerPayload.getEndsAt());
        assertNotEquals("", alertManagerPayload.getEndsAt());
        assertEquals(triggeredAt.plusMinutes(1).plusSeconds(10).toString(), alertManagerPayload.getEndsAt());
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

    @Test
    public void buildWithCustomLabels() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn("Test234");
        when(configuration.getString("alertmanager_custom_labels")).thenReturn("environment: production;system=webapp;priority=major");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("Test234", alertManagerPayload.getLabels().get("alertname"));
        assertEquals("production", alertManagerPayload.getLabels().get("environment"));
        assertEquals("webapp", alertManagerPayload.getLabels().get("system"));
        assertEquals("major", alertManagerPayload.getLabels().get("priority"));
        assertEquals(4, alertManagerPayload.getLabels().size());
    }

    @Test
    public void buildWithoutCustomLabels() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn("Test234");
        when(configuration.getString("alertmanager_custom_labels")).thenReturn("");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("Test234", alertManagerPayload.getLabels().get("alertname"));
        assertEquals(1, alertManagerPayload.getLabels().size());
    }

    @Test
    public void buildWithOneCustomLabel() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn("Test234");
        when(configuration.getString("alertmanager_custom_labels")).thenReturn("level: critical");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("Test234", alertManagerPayload.getLabels().get("alertname"));
        assertEquals("critical", alertManagerPayload.getLabels().get("level"));
        assertEquals(2, alertManagerPayload.getLabels().size());
    }

    @Test
    public void buildWithStrangeNotatedCustomLabels() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_alert_name")).thenReturn("Test234");
        when(configuration.getString("alertmanager_custom_labels")).thenReturn(";level;;;system=production=staging;;");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - labels
        assertEquals("Test234", alertManagerPayload.getLabels().get("alertname"));
        assertEquals("", alertManagerPayload.getLabels().get("level"));
        assertEquals("production=staging", alertManagerPayload.getLabels().get("system"));
        assertEquals(3, alertManagerPayload.getLabels().size());
    }

    @Test
    public void buildWithCustomAnnotations() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn("environment: production;system=webapp;priority=major");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertEquals("production", alertManagerPayload.getAnnotations().get("environment"));
        assertEquals("webapp", alertManagerPayload.getAnnotations().get("system"));
        assertEquals("major", alertManagerPayload.getAnnotations().get("priority"));
        assertEquals(3, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithCustomAnnotationsInAdditionToDefaultOnes() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn("environment: production;system=webapp;priority=major");

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
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .withStream(stream)
                                                                            .withCheckResult(checkResult)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertEquals("production", alertManagerPayload.getAnnotations().get("environment"));
        assertEquals("webapp", alertManagerPayload.getAnnotations().get("system"));
        assertEquals("major", alertManagerPayload.getAnnotations().get("priority"));
        assertEquals("StreamTitle", alertManagerPayload.getAnnotations().get("stream_title"));
        assertEquals(triggeredAt.toString(), alertManagerPayload.getAnnotations().get("triggered_at"));
        assertEquals("aDescription", alertManagerPayload.getAnnotations().get("triggered_rule_description"));
        assertEquals("aTitle", alertManagerPayload.getAnnotations().get("triggered_rule_title"));
        assertEquals(7, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithoutCustomAnnotations() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn("");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertEquals(0, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithOneCustomAnnotation() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn("level: critical");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertEquals("critical", alertManagerPayload.getAnnotations().get("level"));
        assertEquals(1, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithStrangeNotatedCustomAnnotations() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations")).thenReturn(";level;;;system=production=staging;;");


        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                                                                            .withConfiguration(configuration)
                                                                            .build();

        // expect: correct values set
        // - annotations
        assertEquals("", alertManagerPayload.getAnnotations().get("level"));
        assertEquals("production=staging", alertManagerPayload.getAnnotations().get("system"));
        assertEquals(2, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithCustomTemplateAnnotations() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations"))
                .thenReturn(
                        "mystreamtitle=${stream.title};"
                                + "myresultdesc=${check_result.resultDescription};"
                                + "mytriggeredat=${check_result.triggeredAt};"
                                + "mystreamid=${stream.id};"
                                + "mystreamdescription=${stream.description};"
                                + "myalertconditiontitle=${alertCondition.title};"
                                + "myalertconditiondesc=${alertCondition.description};"
                                + "mytriggeredcondition=${check_result.triggeredCondition};"
                                + "myMessageBacklog=${if backlog}${foreach backlog message}${message.message} ${end}${else}<No Backlog>${end};"
                );

        // and: stream mock
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn("StreamTitle");
        when(stream.getId()).thenReturn("StreamId");
        when(stream.getDescription()).thenReturn("StreamDescription");

        // and: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        DateTime triggeredAt = new DateTime();
        when(checkResult.getTriggeredAt()).thenReturn(triggeredAt);
        when(checkResult.getResultDescription()).thenReturn("CheckResultResultDescription");
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.toString()).thenReturn("TriggeredConditionString");
        when(alertCondition.getDescription()).thenReturn("AlertDescription");
        when(alertCondition.getTitle()).thenReturn("AlertTitle");
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);


        // Message Mocks
        Message messageOne = new Message("messageOne", "source", new DateTime(2015, 1, 1, 0, 0, DateTimeZone.UTC));
        Message messageTwo = new Message("messageTwo", "source", new DateTime(2015, 1, 1, 0, 0, DateTimeZone.UTC));
        MessageSummary messageSummaryOne = mock(MessageSummary.class);
        MessageSummary messageSummaryTwo = mock(MessageSummary.class);
        List<MessageSummary> messageSummaryList = Arrays.asList(messageSummaryOne, messageSummaryTwo);
        when(messageSummaryOne.getRawMessage()).thenReturn(messageOne);
        when(messageSummaryTwo.getRawMessage()).thenReturn(messageTwo);
        when(checkResult.getMatchingMessages()).thenReturn(messageSummaryList);



        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                .withConfiguration(configuration)
                .withStream(stream)
                .withCheckResult(checkResult)
                .build();

        // expect: correct values set
        // - annotations
        assertEquals("StreamTitle", alertManagerPayload.getAnnotations().get("mystreamtitle"));
        assertEquals("CheckResultResultDescription", alertManagerPayload.getAnnotations().get("myresultdesc"));
        assertNotNull(alertManagerPayload.getAnnotations().get("mytriggeredat"));
        assertNotEquals("", alertManagerPayload.getAnnotations().get("mytriggeredat"));
        assertEquals("StreamId", alertManagerPayload.getAnnotations().get("mystreamid"));
        assertEquals("StreamDescription", alertManagerPayload.getAnnotations().get("mystreamdescription"));
        assertEquals("AlertTitle", alertManagerPayload.getAnnotations().get("myalertconditiontitle"));
        assertEquals("AlertDescription", alertManagerPayload.getAnnotations().get("myalertconditiondesc"));
        assertEquals("TriggeredConditionString", alertManagerPayload.getAnnotations().get("mytriggeredcondition"));
        assertEquals("messageOne messageTwo ", alertManagerPayload.getAnnotations().get("myMessageBacklog"));
        assertEquals(13, alertManagerPayload.getAnnotations().size());
    }

    @Test
    public void buildWithCustomTemplateAnnotationsWithoutMessageBacklog() {
        // given: configuration mock
        Configuration configuration = mock(Configuration.class);
        when(configuration.getString("alertmanager_custom_annotations"))
                .thenReturn(
                        "mystreamtitle=${stream.title};"
                                + "myresultdesc=${check_result.resultDescription};"
                                + "mytriggeredat=${check_result.triggeredAt};"
                                + "mystreamid=${stream.id};"
                                + "mystreamdescription=${stream.description};"
                                + "myalertconditiontitle=${alertCondition.title};"
                                + "myalertconditiondesc=${alertCondition.description};"
                                + "mytriggeredcondition=${check_result.triggeredCondition};"
                                + "myMessageBacklog=${if backlog}${foreach backlog message}${message.message} ${end}${else}<No Backlog>${end};"
                );

        // and: stream mock
        Stream stream = mock(Stream.class);
        when(stream.getTitle()).thenReturn("StreamTitle");
        when(stream.getId()).thenReturn("StreamId");
        when(stream.getDescription()).thenReturn("StreamDescription");

        // and: checkResult mock
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        DateTime triggeredAt = new DateTime();
        when(checkResult.getTriggeredAt()).thenReturn(triggeredAt);
        when(checkResult.getResultDescription()).thenReturn("CheckResultResultDescription");
        AlertCondition alertCondition = mock(AlertCondition.class);
        when(alertCondition.toString()).thenReturn("TriggeredConditionString");
        when(alertCondition.getDescription()).thenReturn("AlertDescription");
        when(alertCondition.getTitle()).thenReturn("AlertTitle");
        when(checkResult.getTriggeredCondition()).thenReturn(alertCondition);

        // and: instance with set mocks as values
        AlertManagerPayload alertManagerPayload = AlertManagerPayloadBuilder.newInstance()
                .withConfiguration(configuration)
                .withStream(stream)
                .withCheckResult(checkResult)
                .build();

        // expect: correct values set
        // - annotations
        assertEquals("StreamTitle", alertManagerPayload.getAnnotations().get("mystreamtitle"));
        assertEquals("CheckResultResultDescription", alertManagerPayload.getAnnotations().get("myresultdesc"));
        assertNotNull(alertManagerPayload.getAnnotations().get("mytriggeredat"));
        assertNotEquals("", alertManagerPayload.getAnnotations().get("mytriggeredat"));
        assertEquals("StreamId", alertManagerPayload.getAnnotations().get("mystreamid"));
        assertEquals("StreamDescription", alertManagerPayload.getAnnotations().get("mystreamdescription"));
        assertEquals("AlertTitle", alertManagerPayload.getAnnotations().get("myalertconditiontitle"));
        assertEquals("AlertDescription", alertManagerPayload.getAnnotations().get("myalertconditiondesc"));
        assertEquals("TriggeredConditionString", alertManagerPayload.getAnnotations().get("mytriggeredcondition"));
        assertEquals("<No Backlog>", alertManagerPayload.getAnnotations().get("myMessageBacklog"));
        assertEquals(13, alertManagerPayload.getAnnotations().size());
    }
}
