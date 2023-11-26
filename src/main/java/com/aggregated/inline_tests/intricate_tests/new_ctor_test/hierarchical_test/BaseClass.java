package com.aggregated.inline_tests.intricate_tests.new_ctor_test.hierarchical_test;

import com.aggregated.inline_tests.intricate_tests.new_ctor_test.fuzzy_class.*;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

public abstract class BaseClass<T> {
    protected Integer x;
    protected String text;
    protected boolean y;
    protected Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary;
    protected Map<String, Map<String, Map<SortedSet<Integer>, DummyObject02>>> amIScary02;
    private DummyObject01 dummyObject01;
    private DummyObject dummyObject;
    private Collection<DummyObject> listDummy;
    private Map<String, HelloIImAnotherDummy> ok;
}
