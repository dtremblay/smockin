package com.smockin.mockserver.service;

import com.smockin.admin.persistence.entity.RestfulMock;
import com.smockin.admin.persistence.entity.RestfulMockDefinitionRule;
import com.smockin.admin.persistence.entity.RestfulMockDefinitionRuleGroup;
import com.smockin.admin.persistence.entity.RestfulMockDefinitionRuleGroupCondition;
import com.smockin.admin.persistence.enums.RuleComparatorEnum;
import com.smockin.admin.persistence.enums.RuleDataTypeEnum;
import com.smockin.admin.persistence.enums.RuleMatchingTypeEnum;
import com.smockin.mockserver.service.dto.RestfulResponseDTO;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import spark.Request;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgallina.
 */
@RunWith(MockitoJUnitRunner.class)
public class RuleEngineTest {

    @Mock
    private RuleResolver ruleResolver;

    @Mock
    private Request req;

    @Mock
    private List<RestfulMockDefinitionRule> rules;

    @Spy
    @InjectMocks
    private RuleEngineImpl ruleEngine = new RuleEngineImpl();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void process_nullRules_Test() {

        // Assertions
        thrown.expect(NullPointerException.class);

        // Setup
        rules = null;

        // Test
        ruleEngine.process(req, rules);

    }

    @Test
    public void process_emptyRules_Test() {

        // Setup
        rules = new ArrayList<RestfulMockDefinitionRule>();

        // Test
        final RestfulResponseDTO result = ruleEngine.process(req, rules);

        // Assertions
        Assert.assertNull(result);

    }

    @Test
    public void process_Test() {

        // Setup
        rules = new ArrayList<>();

        final RestfulMock mock = new RestfulMock();
        mock.setPath("/person/{name}");
        final RestfulMockDefinitionRule rule = new RestfulMockDefinitionRule(mock, 1, 200, MediaType.APPLICATION_JSON_VALUE, "{ \"msg\" : \"foobar\" }", 0, false);
        final RestfulMockDefinitionRuleGroup group = new RestfulMockDefinitionRuleGroup(rule, 1);
        final RestfulMockDefinitionRuleGroupCondition condition = new RestfulMockDefinitionRuleGroupCondition(group, "name", RuleDataTypeEnum.TEXT, RuleComparatorEnum.EQUALS, "joe", RuleMatchingTypeEnum.REQUEST_BODY, false);

        group.getConditions().add(condition);
        rule.getConditionGroups().add(group);
        rules.add(rule);

        Mockito.when(req.body()).thenReturn("{ \"name\" : \"joe\" }");
        Mockito.when(ruleResolver.processRuleComparison(Mockito.any(RestfulMockDefinitionRuleGroupCondition.class), Mockito.anyString())).thenReturn(true);

        // Test
        final RestfulResponseDTO result = ruleEngine.process(req, rules);

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals(rule.getHttpStatusCode(), result.getHttpStatusCode());
        Assert.assertEquals(rule.getResponseContentType(), result.getResponseContentType());
        Assert.assertEquals(rule.getResponseBody(), result.getResponseBody());
    }

    @Test
    public void extractInboundValue_nullRuleMatchingType_Test() {

        // Assertions
        thrown.expect(NullPointerException.class);

        // Test
        ruleEngine.extractInboundValue(null, "", req, "/person/{name}");

    }

    @Test
    public void extractInboundValue_reqHeader_Test() {

        // Setup
        final String fieldName = "name";
        final String reqResponse = "Hey Joe";
        Mockito.when(req.headers(fieldName)).thenReturn(reqResponse);

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_HEADER, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals(reqResponse, result);

    }

    @Test
    public void extractInboundValue_reqParam_Test() {

        // Setup
        final String fieldName = "name";
        final String reqResponse = "Hey Joe";
        Mockito.when(req.queryParams(fieldName)).thenReturn(reqResponse);

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_PARAM, "name", req, "/person/{name}");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals(reqResponse, result);

    }

    @Test
    public void extractInboundValue_reqBody_Test() {

        // Setup
        final String reqResponse = "Hey Joe";
        Mockito.when(req.body()).thenReturn(reqResponse);

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_BODY, "", req, "/person/{name}");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals(reqResponse, result);

    }

    @Test
    public void extractInboundValue_pathVariable_Test() {

        // Setup
        final String fieldName = "name";
        Mockito.when(req.pathInfo()).thenReturn("/person/Joe");

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.PATH_VARIABLE, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals("Joe", result);

    }

    @Test
    public void extractInboundValue_jsonReqBody_Test() {

        // Setup
        final String fieldName = "username";
        final String fieldValue = "admin";
        Mockito.when(req.body()).thenReturn("{\"username\":\"" + fieldValue + "\"}");

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_BODY_JSON_ANY, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals(fieldValue, result);

    }

    @Test
    public void extractInboundValue_jsonReqBody_NotFound_Test() {

        // Setup
        final String fieldName = "username";
        Mockito.when(req.body()).thenReturn("{\"foo\":\"bar\"}");

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_BODY_JSON_ANY, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNull(result);

    }

    @Test
    public void extractInboundValue_jsonReqBody_invalidJson_Test() {

        // Setup
        final String fieldName = "username";
        Mockito.when(req.body()).thenReturn("username = admin");

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_BODY_JSON_ANY, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNull(result);

    }

    @Test
    public void extractInboundValue_jsonReqBody_null_Test() {

        // Setup
        final String fieldName = "username";
        Mockito.when(req.body()).thenReturn(null);

        // Test
        final String result = ruleEngine.extractInboundValue(RuleMatchingTypeEnum.REQUEST_BODY_JSON_ANY, fieldName, req, "/person/{name}");

        // Assertions
        Assert.assertNull(result);

    }

    @Test
    public void extractRequestParamByNameTest() {

        // Setup
        Mockito.when(req.contentType()).thenReturn(null);
        Mockito.when(req.queryParams("name")).thenReturn("bob");

        // Test
        final String result = ruleEngine.extractRequestParamByName(req, "name");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals("bob", result);

    }

    @Test
    public void extractRequestParamByName_formPost_Test() {

        // Setup
        Mockito.when(req.contentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        Mockito.when(req.body()).thenReturn("name=jane;age=28;");

        // Test
        final String result = ruleEngine.extractRequestParamByName(req, "name");

        // Assertions
        Assert.assertNotNull(result);
        Assert.assertEquals("jane", result);

    }

    @Test
    public void extractRequestParamByName_formWithRandomReqBody_Test() {

        // Setup
        Mockito.when(req.contentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        Mockito.when(req.body()).thenReturn("adasdasdadasd");

        // Test
        final String result = ruleEngine.extractRequestParamByName(req, "name");

        // Assertions
        Assert.assertNull(result);
    }

    @Test
    public void extractRequestParamByName_formWithBlankReqBody_Test() {

        // Setup
        Mockito.when(req.contentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        Mockito.when(req.body()).thenReturn(" ");

        // Test
        final String result = ruleEngine.extractRequestParamByName(req, "name");

        // Assertions
        Assert.assertNull(result);
    }

}
