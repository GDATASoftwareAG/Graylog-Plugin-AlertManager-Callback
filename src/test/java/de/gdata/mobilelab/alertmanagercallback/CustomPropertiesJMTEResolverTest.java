package de.gdata.mobilelab.alertmanagercallback;

import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomPropertiesJMTEResolverTest {

    @Test
    public void testBuilder() {
        // when: building
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().build();

        // then: should not be null
        assertNotNull(customPropertiesJMTEResolver);
    }

    @Test
    public void testBuilderWithUrl() throws Exception {
        // given: An URL
        String url = "https://somealertmanager.alert/";

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given url
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withUrl(url).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with URL
        assertEquals(url, templateModel.get("stream_url"));
    }

    @Test
    public void testBuilderWithStream() throws Exception {
        // given: A Stream
        Stream stream = mock(Stream.class);

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given stream
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withStream(stream).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with stream
        assertEquals(stream, templateModel.get("stream"));
    }

    @Test
    public void testBuilderWithCheckResult() throws Exception {
        // given: A CheckResult
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given checkResult
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withCheckResult(checkResult).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with checkResult
        assertEquals(checkResult, templateModel.get("check_result"));
    }

    @Test
    public void testBuilderWithCheckResultContainingMessages() throws Exception {
        // given: A CheckResult
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);

        // and: Message Mocks
        Message messageOne = new Message("messageOne", "source", new DateTime(2015, 1, 1, 0, 0, DateTimeZone.UTC));
        Message messageTwo = new Message("messageTwo", "source", new DateTime(2015, 1, 1, 0, 0, DateTimeZone.UTC));
        MessageSummary messageSummaryOne = mock(MessageSummary.class);
        MessageSummary messageSummaryTwo = mock(MessageSummary.class);
        List<MessageSummary> messageSummaryList = Arrays.asList(messageSummaryOne, messageSummaryTwo);
        when(messageSummaryOne.getRawMessage()).thenReturn(messageOne);
        when(messageSummaryTwo.getRawMessage()).thenReturn(messageTwo);
        when(checkResult.getMatchingMessages()).thenReturn(messageSummaryList);

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given checkResult
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withCheckResult(checkResult).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with checkResult
        assertEquals(checkResult, templateModel.get("check_result"));

        // and: templateModel contains those messages from above
        assertEquals(2, templateModel.get("backlog_size"));
        assertEquals(2, ((List<Message>) templateModel.get("backlog")).size());
        assertTrue(((List<Message>) templateModel.get("backlog")).contains(messageOne));
        assertTrue(((List<Message>) templateModel.get("backlog")).contains(messageTwo));
    }

    @Test
    public void testBuilderWithCheckResultContainingNoMatchingMessages() throws Exception {
        // given: A CheckResult which returns null as matching messages
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        when(checkResult.getMatchingMessages()).thenReturn(null);

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given checkResult
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withCheckResult(checkResult).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with checkResult
        assertEquals(checkResult, templateModel.get("check_result"));

        // and: templateModel contains those messages from above
        assertEquals(0, templateModel.get("backlog_size"));
        assertTrue(((List<Message>) templateModel.get("backlog")).isEmpty());
    }

    @Test
    public void testBuilderWithCheckResultContainingTriggeredCondition() throws Exception {
        // given: A CheckResult which returns a triggered condition
        AlertCondition.CheckResult checkResult = mock(AlertCondition.CheckResult.class);
        AlertCondition triggeredCondition = mock(AlertCondition.class);
        when(checkResult.getTriggeredCondition()).thenReturn(triggeredCondition);

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // when: building with given checkResult
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().withCheckResult(checkResult).build();
        Map<String, Object> templateModel = (Map<String, Object>) templateModelField.get(customPropertiesJMTEResolver);

        // then: templateModel should contain entry with checkResult
        assertEquals(checkResult, templateModel.get("check_result"));

        // and: templateModel contains an alertCondition
        assertEquals(triggeredCondition, templateModel.get("alertCondition"));
    }

    @Test
    public void testTransformTemplateValuesWithEmptyTemplateModelMap() throws Exception {
        // given: an empty Map
        Map<String, Object> emptyMap = Collections.emptyMap();

        // and: a CustomPropertiesJMTEResolver instance
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().build();

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // and: resolver has given map
        templateModelField.set(customPropertiesJMTEResolver, emptyMap);

        // and: A custom value map
        Map<String, Object> customValueMap = new HashMap<>();
        customValueMap.put("test1", "${a_unknown_key_for_template}");
        customValueMap.put("test2", "${a_unknown_key_for_template2}sometext${another_one}");
        customValueMap.put("test3", 1337);

        // when: calling transformTemplateValues
        Map<String, Object> result = customPropertiesJMTEResolver.transformTemplateValues(customValueMap);

        // then: values which do not exist will be replaced with an empty string
        assertTrue(result.containsKey("test1"));
        assertTrue(result.containsKey("test2"));
        assertTrue(result.containsKey("test3"));
        assertEquals("", result.get("test1"));
        assertEquals("sometext", result.get("test2"));
        assertEquals(1337, result.get("test3"));
    }

    @Test
    public void testTransformTemplateValuesWithCustomTemplateModelMap() throws Exception {
        // given: a custom templateModelMap
        Map<String, Object> customTemplateModelMap = new HashMap<>();
        customTemplateModelMap.put("a_known_key_for_template", "bla");
        customTemplateModelMap.put("a_known_key_for_template2", "blubb");
        customTemplateModelMap.put("another_one", "hello");
        customTemplateModelMap.put("backlog", Arrays.asList("Message 1: Hello", "Message2: World"));
        customTemplateModelMap.put("existing_value", "Hello World!");

        // and: a CustomPropertiesJMTEResolver instance
        CustomPropertiesJMTEResolver customPropertiesJMTEResolver = CustomPropertiesJMTEResolver
                .Builder.newBuilder().build();

        // and: access to private field for templateModel
        Field templateModelField = CustomPropertiesJMTEResolver.class.getDeclaredField("templateModel");
        templateModelField.setAccessible(true);

        // and: resolver has given map
        templateModelField.set(customPropertiesJMTEResolver, customTemplateModelMap);

        // and: A custom value map
        Map<String, Object> customValueMap = new HashMap<>();
        customValueMap.put("test1", "${a_known_key_for_template}");
        customValueMap.put("test2", "${a_known_key_for_template2}sometext${another_one}");
        customValueMap.put("test3", 1337);
        customValueMap.put("test4", "${an_unknown_key_for_template}");
        customValueMap.put("foreachTest", "${foreach backlog message}${message} ${end}");
        customValueMap.put("ifTest", "${if not_existing_value}${not_existing_value}${else}unknown${end}");
        customValueMap.put("ifTest2", "${if existing_value}${existing_value}${else}unknown${end}");

        // when: calling transformTemplateValues
        Map<String, Object> result = customPropertiesJMTEResolver.transformTemplateValues(customValueMap);

        // then: values should be replaced if they exist inside the templateModelMap
        assertTrue(result.containsKey("test1"));
        assertTrue(result.containsKey("test2"));
        assertTrue(result.containsKey("test3"));
        assertTrue(result.containsKey("test4"));
        assertTrue(result.containsKey("foreachTest"));
        assertTrue(result.containsKey("ifTest"));
        assertTrue(result.containsKey("ifTest2"));
        assertEquals("bla", result.get("test1"));
        assertEquals("blubbsometexthello", result.get("test2"));
        assertEquals(1337, result.get("test3"));
        assertEquals("", result.get("test4"));
        assertEquals("Message 1: Hello Message2: World ", result.get("foreachTest"));
        assertEquals("unknown", result.get("ifTest"));
        assertEquals("Hello World!", result.get("ifTest2"));
    }

}
