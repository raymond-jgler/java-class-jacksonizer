# Welcome to Java Class Annotatable Constructor Templater !

## Overview:

_ This method will eventually annotate contructor (existing or write new) and its parameters(s) using client's annotation inputs.

## Constructor's qualification :
_ its body contains all declared serialiazable fields (assigning )
_ its param list covers all declared serialiazable fields

## Field's qualification :
-  non-static
-  non-transient

## How to use:
+ package.txt is the entry point together with ScanPackage.java, supporting multi single files or package lines.
+ ScanPackage.java is the entry class that starts everything.
+ Driver.java is the class that starts the main logic.

# This contains 4 flags :
 - processDomainFieldBFS(bool x)                 : to add annotations for fields explored in class ( if domain class )

 - processDomainImportBFS(bool x)                : (not recommended) to add annotations for imported class ( if domain class)

 - processTopDownHierarchicalClasses(bool x)       : (recommended for a complete list of fields) to process/add annotations from top-level #parent class down to the designated one, possibly transfer super 

 - processDomainParamBFS(bool x)                 : to add annotations for parameters explored in constructor ( if domain class)
 - addSuperConstructor(bool x)                    : (if processTopDownHierarchicalClasses is enabled, then it's recommended to enable this as well) to add a default

_ Basic flow - phase-pattern-based algorithm :
+ When processing a class, the method will explore -process all fields declared in a class if they're domain-specific, this procedure is repeated.
+ Providing client-based import flag - processImportDomainsLayerWise, to process all dommain classes found in the import section or not.
+ for libraries such as Jackson, custom (De)serializers annotations are supported on a given field's type, and a scope-rule to only annotate on the field, if that field is used/assigned in constructor body or in getter (TODO)
+ Additional skipping / choosing base class, class name's logic , etc. please refer to Driver.java for more details.

![basic logic flow](https://raw.githubusercontent.com/trgpnt/java-class-decorator/83497b1acd425ead5b7011210d4431244adc2e81/src/main/resources/imgs/basic_flow.png)

Example: supposed this hierarchy :
```
BaseClass > Child01 > Child02 > Child03
```

before - BaseClass:
```
import java.util.Map;
import java.util.SortedSet;

public abstract class BaseClass<T> {
    protected Integer x;
    protected String text;
    protected boolean y;
    protected Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary;
}
```

before - Child03:
```
public class Child03 extends Child02 {
    private Integer myField01;
    private String myField02;
}
```
after BaseClass :
```
import java.util.Map;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseClass<T> {
    protected Integer x;
    protected String text;
    protected boolean y;
    protected Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary;

    @YourConstructorLevelAnnotation
    protected BaseClass(@YourParamLevelAnnotation("x") Integer x,
                        @YourParamLevelAnnotation("text") String text,
                        @YourParamLevelAnnotation("y") boolean y,
                        @YourParamLevelAnnotation("amIScary") Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary) {
        this.x = x;
        this.text = text;
        this.y = y;
        this.amIScary = amIScary;
    }
}
```
after Child03:
```
import java.util.Map;
import java.util.SortedSet;

public class Child03 extends Child02 {
    private Integer myField01;
    private String myField02;

    @YourConstructorLevelAnnotation
    private Child03(@YourParamLevelAnnotation("x") Integer x,
                    @YourParamLevelAnnotation("text") String text,
                    @YourParamLevelAnnotation("y") boolean y,
                    @YourParamLevelAnnotation("amIScary") Map<String, Map<String, Map<SortedSet<Integer>, String>>> amIScary,
                    @YourParamLevelAnnotation("abc") int abc,
                    @YourParamLevelAnnotation("myField01") Integer myField01,
                    @YourParamLevelAnnotation("myField02") String myField02) {
        super(x, text, y, amIScary, abc);
        this.myField01 = myField01;
        this.myField02 = myField02;
    }
}
```

# Algorithsm & D.Ss - not yet scary bit manipulation :
 - DFS / BFS
 - Trie
 - Stack
 - Queue
 - Linked List
