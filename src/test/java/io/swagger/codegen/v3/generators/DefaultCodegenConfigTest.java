package io.swagger.codegen.v3.generators;

import io.swagger.codegen.v3.CodegenArgument;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

public class DefaultCodegenConfigTest {

    @Test
    public void testInitialValues() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), null);
        Assert.assertEquals(codegen.apiPackage, "");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), null);
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.TRUE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.TRUE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.TRUE);
    }

    @Test
    public void testSetters() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.setModelPackage("xxx.yyyyy.zzzzzzz.model");
        codegen.setApiPackage("xxx.yyyyy.zzzzzzz.api");
        codegen.setSortParamsByRequiredFlag(false);
        codegen.setHideGenerationTimestamp(false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xxx.yyyyy.zzzzzzz.model");
        Assert.assertEquals(codegen.apiPackage, "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xxx.yyyyy.zzzzzzz.api");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    @Test
    public void testPutAdditionalProperties() throws Exception {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.additionalProperties().put(CodegenConstants.MODEL_PACKAGE, "xx.yyyyy.model");
        codegen.additionalProperties().put(CodegenConstants.API_PACKAGE, "xx.yyyyy.api");
        codegen.additionalProperties().put(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG, false);
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.modelPackage, "xx.yyyyy.model");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.MODEL_PACKAGE), "xx.yyyyy.model");
        Assert.assertEquals(codegen.apiPackage, "xx.yyyyy.api");
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.API_PACKAGE), "xx.yyyyy.api");
        Assert.assertEquals(codegen.sortParamsByRequiredFlag, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG), Boolean.FALSE);
        Assert.assertEquals(codegen.hideGenerationTimestamp, Boolean.FALSE);
        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    @Test
    public void testNumberSchemaMinMax() {
        Schema schema = new NumberSchema()
                .minimum(BigDecimal.valueOf(50))
                .maximum(BigDecimal.valueOf(1000));

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        CodegenProperty codegenProperty = codegen.fromProperty("test", schema);

        Assert.assertEquals(codegenProperty.minimum, "50");
        Assert.assertEquals(codegenProperty.maximum, "1000");
    }

    @Test
    public void testFromOperation_BodyParamsUnique() {
        PathItem dummyPath = new PathItem()
            .post(new Operation())
            .get(new Operation());
      
        OpenAPI openAPI = new OpenAPI()
            .path("dummy", dummyPath);

        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        codegen.setEnsureUniqueParams(false);
        final Operation operation = new Operation();

        RequestBody body = new RequestBody();
        body.setDescription("A list of list of values");
        body.setContent(new Content().addMediaType("application/json", new MediaType().schema(new ArraySchema().items(new ArraySchema().items(new IntegerSchema())))));
        operation.setRequestBody(body);
        Parameter param = new Parameter().in("query").name("testParameter");
        operation.addParametersItem(param);
        
        CodegenOperation codegenOperation = codegen.fromOperation("/path", "GET", operation, null, openAPI);

        Assert.assertEquals(true, codegenOperation.allParams.get(0).getVendorExtensions().get("x-has-more"));
        Assert.assertEquals(false, codegenOperation.bodyParams.get(0).getVendorExtensions().get("x-has-more"));

        codegenOperation.allParams.get(0).getVendorExtensions().put("x-has-more", false);
        codegenOperation.bodyParams.get(0).getVendorExtensions().put("x-has-more", true);

        Assert.assertEquals(false, codegenOperation.allParams.get(0).getVendorExtensions().get("x-has-more"));
        Assert.assertEquals(true, codegenOperation.bodyParams.get(0).getVendorExtensions().get("x-has-more"));
    }

    @Test(dataProvider = "testGetCollectionFormatProvider")
    public void testGetCollectionFormat(Parameter.StyleEnum style, Boolean explode, String expectedCollectionFormat) {
        final DefaultCodegenConfig codegen = new P_DefaultCodegenConfig();
        
        ArraySchema paramSchema = new ArraySchema()
                .items(new IntegerSchema());
        Parameter param = new Parameter()
                .in("query")
                .name("testParameter")
                .schema(paramSchema)
                .style(style)
                .explode(explode);
        
        CodegenParameter codegenParameter = codegen.fromParameter(param, new HashSet<>());
        
        Assert.assertEquals(codegenParameter.collectionFormat, expectedCollectionFormat);
    }
    
    @DataProvider(name = "testGetCollectionFormatProvider")
    public Object[][] provideData_testGetCollectionFormat() {
        // See: https://swagger.io/docs/specification/serialization/#query
        return new Object[][] {
            { null,                                 null,           "multi" },
            { Parameter.StyleEnum.FORM,             null,           "multi" },
            { null,                                 Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.FORM,             Boolean.TRUE,   "multi" },
            
            { null,                                 Boolean.FALSE,  "csv" },
            { Parameter.StyleEnum.FORM,             Boolean.FALSE,  "csv" },
            
            { Parameter.StyleEnum.SPACEDELIMITED,   Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.SPACEDELIMITED,   Boolean.FALSE,  "space" },
            { Parameter.StyleEnum.SPACEDELIMITED,   null,           "multi" },
            
            { Parameter.StyleEnum.PIPEDELIMITED,    Boolean.TRUE,   "multi" },
            { Parameter.StyleEnum.PIPEDELIMITED,    Boolean.FALSE,  "pipe" },
            { Parameter.StyleEnum.PIPEDELIMITED,    null,           "multi" },
        };
    }
    
    /**
     * Tests that {@link DefaultCodegenConfig#fromOperation(String, String, Operation, java.util.Map, OpenAPI)} correctly
     * resolves the consumes list when the request body is specified via reference rather than inline.
     */
    @Test
    public void testRequestBodyRefConsumesList() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/3_0_0/requestBodyRefTest.json");
        final P_DefaultCodegenConfig codegen = new P_DefaultCodegenConfig(); 
        final String path = "/test/requestBodyRefTest";
        final Operation op = openAPI.getPaths().get(path).getPost();
        final CodegenOperation codegenOp = codegen.fromOperation(path, "post", op, openAPI.getComponents().getSchemas(), openAPI);

        Assert.assertTrue(codegenOp.getHasConsumes());
        Assert.assertNotNull(codegenOp.consumes);
        Assert.assertEquals(codegenOp.consumes.size(), 2);
        Assert.assertEquals(codegenOp.consumes.get(0).get("mediaType"), "application/json");
        Assert.assertEquals(codegenOp.consumes.get(1).get("mediaType"), "application/xml");
    }

    private static class P_DefaultCodegenConfig extends DefaultCodegenConfig{
        @Override
        public String getArgumentsLocation() {
            return null;
        }

        @Override
        public String getDefaultTemplateDir() {
            return null;
        }

        @Override
        public CodegenType getTag() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public List<CodegenArgument> readLanguageArguments() {
            return null;
        }
    }
}
