package com.aggregated.inline_tests.intricate_tests.hierarchical_test;


import com.aggregated.inline_tests.intricate_tests.fuzzy_class.DummyObject01;
import com.aggregated.inline_tests.intricate_tests.fuzzy_class.DummyObject02;
import com.aggregated.inline_tests.intricate_tests.fuzzy_class.DummyObject;
import java.util.Map;
import java.util.SortedSet;

public abstract class BaseClass<T> {
    protected Integer x;
    protected String text;
    protected boolean y;
    protected Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary;
    private DummyObject01 dummyObject01;
    private DummyObject02 dummyObject02;
    private DummyObject dummyObject;
}
