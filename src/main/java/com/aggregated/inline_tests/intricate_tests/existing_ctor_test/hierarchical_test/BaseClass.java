package com.aggregated.inline_tests.intricate_tests.existing_ctor_test.hierarchical_test;

import com.aggregated.inline_tests.intricate_tests.existing_ctor_test.fuzzy_class.DummyObject;
import com.aggregated.inline_tests.intricate_tests.existing_ctor_test.fuzzy_class.DummyObject01;
import com.aggregated.inline_tests.intricate_tests.existing_ctor_test.fuzzy_class.DummyObject02;
import com.aggregated.inline_tests.intricate_tests.existing_ctor_test.fuzzy_class.HelloIImAnotherDummy;

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

    protected BaseClass(){}
    public BaseClass(Integer x, String text, boolean y, Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary, Map<String, Map<String, Map<SortedSet<Integer>, DummyObject02>>> amIScary02, DummyObject01 dummyObject01, DummyObject dummyObject, Collection<DummyObject> listDummy, Map<String, HelloIImAnotherDummy> ok) {
        this.x = x;
        this.text = text;
        this.y = y;
        this.amIScary = amIScary;
        this.amIScary02 = amIScary02;
        this.dummyObject01 = dummyObject01;
        this.dummyObject = dummyObject;
        this.listDummy = listDummy;
        this.ok = ok;
    }
}
