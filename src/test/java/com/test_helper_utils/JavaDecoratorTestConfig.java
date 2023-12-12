package com.test_helper_utils;

import com.aggregated.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;

public class JavaDecoratorTestConfig {
    private final ClassDecorator decorator;
    private static final JavaDecoratorTestConfig CONFIG = new JavaDecoratorTestConfig();
    private JavaDecoratorTestConfig() {
        this.decorator = ClassDecorator.emptyInstance();
    }
    public static JavaDecoratorTestConfig construct() {
        return CONFIG;
    }

    /**
     * This constructs basic setting
     * @param packageName
     * @return
     */
    public JavaDecoratorTestConfig onAnnotatableMode(String packageName) {
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
                .processTopDownHierarchicalClasses(true)
                .skipAnnotatedWith(JsonDeserialize.class, JsonSerialize.class)
                .skipWhenBaseClass(InvocationHandler.class,
                        Exception.class,
                        RuntimeException.class)
                .withBaseClass(Serializable.class)
                .withAccessModifier("private") //if write new, which access mod ?
                .annotateConstructorWith("JsonCreator")
                .annotateParamsWith("JsonProperty")
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
                return this;
    }
}
