# java-class-decorator

Overview:

_ This method will eventually annotate contructor (existing or write new) and its parameters(s) using client's annotation inputs.

constructor's qualification :
_ its body contains all declared serialiazable fields (assigning )
_ its param list covers all declared serialiazable fields

field's qualification :
_ non-static
_ non-transient

_ How to use:
+ package.txt is the entry point together with ScanPackage.java, supporting multi single files or package lines.

_ Basic flow:
+ When processing a class, the method will explore -process all fields declared in a class if they're domain-specific, this procedure is repeated.
+ Providing client-based import flag - processImportDomainsLayerWise, to process all dommain classes found in the import section or not.
+ for libraries such as Jackson, custom (De)serializers annotations are supported on a given field's type, and a scope-rule to only annotate on the field, if that field is used/assigned in constructor body or in getter (TODO)
+ Additional skipping / choosing base class, class name's logic , etc. please refer to Driver.java for more details.

![basic logic flow](https://raw.githubusercontent.com/trgpnt/java-class-decorator/83497b1acd425ead5b7011210d4431244adc2e81/src/main/resources/imgs/basic_flow.png)




