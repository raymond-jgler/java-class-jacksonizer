











package com.aggregated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;

public class Driver {
    private static final ClassDecorator decorator = ClassDecorator.emptyInstance();

    public static void JacksonModePackageExecution(String packageName) {

        decorator.reset();
        decorator
                .withInputType(InputReceiver.STRING_BASED_INPUT, packageName)
                .withStrategyModeAndRules(DecorationStrategyMode.ANNOTATE_PARAMERTISED_CONSTRUCTOR, ExecutionRule.ALL)
                .withSubPackages(Boolean.FALSE)
                /**
                 * This will skip runtime exceptions.
                 * if true.
                 */
                .skipError(Boolean.FALSE)
                /**
                 * Will only skip the top-level class,
                 * inner ones will be evaluated again
                 * with these names.
                 */
                .skipClassContainOrEndWith("Parser",
                        "Handler",
                        "Utilities",
                        "Utility",
                        "Transaction",
                        "Processor",
                        "Connection",
                        "Util",
                        "Utils",
                        "QueryGrammarParser",
                        "Factory",
                        "Converter",
                        "Builder")
                /**
                 * Important flags,
                 * defaulted to FALSE
                 */
//        .processDomainParamBFS(true)
                /**
                 * if need to process instance fields
                 */
//              .processDomainFieldBFS(true)
                /**
                 * these 2 flags are highly recommended for error's mitigation
                 */
                .processTopDownHierarchicalClasses(true)
//              .addSuperConstructor(true)
                /**
                 * if need to process instance fields
                 */
//              .processDomainFieldBFS(true)
                .skipAnnotatedWith(JsonDeserialize.class, JsonSerialize.class)
                .skipWhenBaseClass(InvocationHandler.class,
                        Exception.class,
                        RuntimeException.class)
                .withBaseClass(Serializable.class)
                .withAccessModifier("private") //if write new, which access mod ?
                .annotateConstructorWith("ClassLevelAnnotation")
                .annotateParamsWith("PropertyLevelAnnotation")
                .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package ctor's anntation
                .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package field
                .stripFinalClass(false)
                /**
                 * Besides, constructor-based annotations,
                 * this will annotate the declared fields in the class content.
                 */
                .addCustomSerialization(RawClientRuleInput.CUSTOM_SERIALIZER, "com.domain.ExampleSerializer", "com.fasterxml.jackson.databind.annotation.JsonSerialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"))
                .addCustomSerialization(RawClientRuleInput.CUSTOM_DESERIALIZER, "com.domain.ExampleDeserializer", "com.fasterxml.jackson.databind.annotation.JsonDeserialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"))
                .execute();
    }

    public static void JacksonModeSingleJavaExecution(String singleJavaName) {
        ClassDecorator singleMode = ClassDecorator.emptyInstance();

        singleMode.execSingleJava(singleJavaName)
                .skipClassContainOrEndWith("Parser",
                        "Handler",
                        "Utilities",
                        "Utility",
                        "Transaction",
                        "Processor",
                        "Connection",
                        "Util",
                        "Utils",
                        "QueryGrammarParser",
                        "Factory",
                        "Converter",
                        "Builder")
                /**
                 * Important flags,
                 * defaulted to FALSE
                 */
                /**
                 * if need to process ctor's params
                 */
//              .processDomainParamBFS(true)
                /**
                 * these 2 flags are highly recommended for error's mitigation
                 */
                .processTopDownHierarchicalClasses(true)
//              .addSuperConstructor(true)
                /**
                 * if need to process instance fields
                 */
//              .processDomainFieldBFS(true)
                .skipAnnotatedWith(JsonDeserialize.class, JsonSerialize.class)
                .skipWhenBaseClass(InvocationHandler.class,
                        Exception.class,
                        RuntimeException.class)
                .withBaseClass(Serializable.class)
                .withAccessModifier("private") //if write new, which access mod ?
                .annotateConstructorWith("ClassLevelAnnotation")
                .annotateParamsWith("ParamLevelAnnotation")
//                .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package ctor's anntation
//                .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package field
                .addAnnotationPackage("com.module.to.custom.class.annotation") //-> package ctor's anntation
                .addAnnotationPackage("com.module.to.custom.param.annotation") //-> package field
                .stripFinalClass(false)
                /**
                 * Besides, constructor-based annotations,
                 * this will annotate the declared fields in the class content.
                 */
                .addCustomSerialization(RawClientRuleInput.CUSTOM_SERIALIZER, "com.domain.ExampleSerializer", "com.fasterxml.jackson.databind.annotation.JsonSerialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"))
                .addCustomSerialization(RawClientRuleInput.CUSTOM_DESERIALIZER, "com.domain.ExampleDeserializer", "com.fasterxml.jackson.databind.annotation.JsonDeserialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"))
                .execute();
    }
}
