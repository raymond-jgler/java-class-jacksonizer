package com.aggregated;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Modifier;
import java.util.*;

import static com.aggregated.CharacterRepository.*;

public class BuildAnnotatableCodePhase extends BaseConstructorPhaseAlgorithm {
    private final String CONSTRUCTOR_ANNOTATION;
    private final String FIELD_ANNOTATION;
    private final String CONSTRUCTOR_ANNOTATION_PACKAGE;
    private final String PARAM_ANNOTATION_PACKAGE;
    private static final Set<Character> PARAM_BASED_CHARACTERS = new HashSet<>();
    private static final String CONSTRUCTOR_BODY = "CONSTRUCTOR_BODY";
    private static final String DECLARED_FIELDS = "DECLARED_FIELDS";

    /**
     * For annotated constructors/params/ This must be the constructor with maximum number of
     * parameters.
     */
    private int STARTING_CTOR_IDX = -1;

    private int ENDING_CTOR_IDX = -1;
    private List<String> ANNOTATABLE_ARGS = new ArrayList<>();
    private List<DecorationLocalField> serializableFields = new ArrayList<>();
    private List<DecorationLocalField> customSerRequiredFields = new ArrayList<>();
    private List<DecorationLocalField> parentFields = new ArrayList<>();
    private List<String> missingImportClassString = new ArrayList<>();
    private boolean isBuildNew = false;
    private List<String> rawCustomSerImportStrings;
    private String postCtorString = "";
    private String superLine = "";
    private List<String> customSerAnnotStrings;
    private List<String> customSerClassNames;
    private Map<String, String> filterRules;
    public static final List<String> COMMON_PACKAGE_LIST =
            Arrays.asList(
                    "java.lang",
                    "java.util",
                    "java.io",
                    "java.math",
                    "java.time",
                    "java.nio",
                    "java.net",
                    "java.util.concurrent");

    static {
        PARAM_BASED_CHARACTERS.add(',');
        PARAM_BASED_CHARACTERS.add('<');
        PARAM_BASED_CHARACTERS.add('>');
    }

    /**
     * This flag controls if custom annotation is added b/c custom imports come from diffrent modules
     * / packages.
     */
    private boolean isNeedToAddCustomImports;

    public BuildAnnotatableCodePhase(RawClientRuleInput rawInput) {
        super(rawInput);
        CONSTRUCTOR_ANNOTATION = AT + rawInput.getCtorAnnotation();
        FIELD_ANNOTATION = AT + rawInput.getFieldAnnotation();
        CONSTRUCTOR_ANNOTATION_PACKAGE =
                StringUtils.buildAnnotationPackage(
                        rawInput.getCtorAnnotationPackage(), rawInput.getCtorAnnotation());
        PARAM_ANNOTATION_PACKAGE =
                StringUtils.buildAnnotationPackage(
                        rawInput.getFieldAnnotationPackage(), rawInput.getFieldAnnotation());

        this.rawCustomSerImportStrings = rawInput.getImportStrings();
        if (!Objects.isNull(this.rawCustomSerImportStrings)) {
            /** extract annotation strings and import strings separated by space. */
            extractCustomSerStrings();
        }
        this.filterRules = this.rawInput.getFilterRules();
    }


    private void extractCustomSerStrings() {
        if (Objects.isNull(this.customSerClassNames)) {
            this.customSerClassNames = new ArrayList<>();
        }
        if (Objects.isNull(this.customSerAnnotStrings)) {
            this.customSerAnnotStrings = new ArrayList<>();
        }
        for (String each : rawCustomSerImportStrings) {
            String[] splitted = each.split(SPACE);
            customSerClassNames.add(splitted[0]);
            this.customSerAnnotStrings.add(splitted[1]);
        }
    }

    private void reset() {
        STARTING_CTOR_IDX = -1;
        ENDING_CTOR_IDX = -1;
        ANNOTATABLE_ARGS = new ArrayList<>();
        serializableFields = new ArrayList<>();
        missingImportClassString = new ArrayList<>();
        isBuildNew = false;
        isNeedToAddCustomImports = false;
        parentFields = new ArrayList<>();
        postCtorString = "";
        superLine = "";
    }

    /**
     * Build inheritable parent's params.
     *
     * @param source
     * @return
     */
    private List<DecorationLocalField> buildListFromSpacedStrings(List<String> source) {
        List<DecorationLocalField> res = new ArrayList<>();
        try {
            for (String each : source) {
                if (StringUtils.isEmpty(each)) {
                    continue;
                }
                DecorationLocalField generatedField = StringUtils.makeFieldFromFieldTypeAndName(each);
                res.add(generatedField);
                String fieldType = generatedField.getTypeFullName();
                String fullImportString = "";
                for (String eachRawType : StringUtils.makeNonAlphaStringsFrom(fieldType, Boolean.TRUE)) {
                    generatedField.addImportString(getExactFullPathFor(StringUtils.stripDoubleEndedNonAlphaNumeric(eachRawType)));
                    /** if bfs param is true Enqueue this class, for the full bfs flow */
                    if (rawInput.getBfsParams() && StringUtils.isNotEmpty(fullImportString)) {
                        try {
                            AnnotatableConstructorDecorator.enqueueWith(
                                    ReflectionUtils.getClass(fullImportString));
                        } catch (Throwable t) {
                            // chill
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private boolean isAllNonFinalFields() {
        for (DecorationLocalField field : serializableFields) {
            if (field.getFinal()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public PhaseChainedResult execute(PhaseChainedResult previousInput) {
        reset();
        /** Fetch previous phase's result. */
        AnnotatableConstructorFieldPhaseOutput prevResult =
                (AnnotatableConstructorFieldPhaseOutput) previousInput;
        CustomSerializationCollectedField collectedFields =
                (CustomSerializationCollectedField) prevResult.getRawValues();

        this.serializableFields = collectedFields.getSerializableFields();
        this.customSerRequiredFields = collectedFields.getCustomRequiredFields();

        List<CustomSerializationIndexedField> customSerializationIndexedFields = null;
        if (!CollectionUtils.isEmpty(this.rawCustomSerImportStrings)) {
            customSerializationIndexedFields = buildCustomSerIndexedFields(this.customSerRequiredFields);
        }
        prevResult.reset();
        /** Abrupty skip if need be, to process innner classes. */
        if (shouldSkipCurrentClass()) {
            /** Preserve super field list if present. */
            return new BuildConstructorPhaseOutput(SKIP_MARKER);
        }
        /** Just to build what is needed */
        boolean isFromExisting = findExistingParaCtor() != -1;
        boolean isClassContentChanged = false;
        /**
         * Annotate custom serialization annotations on fields before building the constructor's code
         */
        if (STARTING_CTOR_IDX > 0
                && !CollectionUtils.isEmpty(customSerializationIndexedFields)
                && !CollectionUtils.isEmpty(this.customSerAnnotStrings)) {
            String previousContent = CLASS_CONTENT;
            CLASS_CONTENT = annotateFields(customSerializationIndexedFields);
            isClassContentChanged = !previousContent.equalsIgnoreCase(CLASS_CONTENT);
        }
        /** Only update string indexes if class content is changed ( new annotation is added , etc) */
        if (isClassContentChanged) {
            reprocessVitals();
            isFromExisting = findExistingParaCtor() != -1;
        }
        if (!isFromExisting
                && CollectionUtils.isEmpty(ANNOTATABLE_ARGS)
                && CollectionUtils.isEmpty(serializableFields)
                && MapUtils.isEmpty(classToMergeableParams)) {
            return new BuildConstructorPhaseOutput(SKIP_MARKER);
        }
        /**
         * Important phase: Eval-merge fields to validize all constructors in current class's string
         * content.
         */
        final String key = CLAZZ.getName();
        if (MapUtils.isNotEmpty(classToMergeableParams) && classToMergeableParams.containsKey(key)) {
            parentFields = classToMergeableParams.get(key);
            /** Simultaneously build missing import classes to add as imports at the end of this phase. */
            if (CollectionUtils.isNotEmpty(parentFields)) {
                /** Build missing import */
                for (DecorationLocalField each : parentFields) {
                    for (String eachImport : each.getFullImportStringList(false)) {
                        if (missingImportClassString.contains(eachImport)) {
                            continue;
                        }
                        missingImportClassString.add(eachImport);
                    }
                }
            }
        }
        /** BUILD CONSTRUCTOR CODE. */
        String annotatedConstructorCode =
                buildConstructorCode(isFromExisting, Boolean.TRUE, Boolean.FALSE);
        /** if the universe doesn't want to process this class. */
        if (StringUtils.isEmpty(annotatedConstructorCode)
                || SKIP_MARKER.equalsIgnoreCase(annotatedConstructorCode)) {
            return new BuildConstructorPhaseOutput(SKIP_MARKER);
        }

        /**
         * Set param list for children, instead of relying on declared instance fields to build param
         * line.
         */
        if (!isBuildNew) {
            addFieldListToMap(
                    CLAZZ.getName(), buildListFromSpacedStrings(ANNOTATABLE_ARGS), Boolean.TRUE);
        }

        /** Alright need to bail here if just need to preserve inheritable fields. */
        if (ReflectionUtils.hardCodeIsJackson(CLAZZ)
                || !ValidateClassPhase.hardCodeIsGoodClass(CLAZZ)) {
            return new BuildConstructorPhaseOutput(SKIP_MARKER);
        }

        /** isFromExisting is true when STARTING_CTOR_IDX, and ENDING_CTOR_IDX != -1 */
        if (!isFromExisting) {
            if (STARTING_CTOR_IDX > 1 || ENDING_CTOR_IDX > 1) {
                throw new RuntimeException("Oh my, we're doomed");
            }
            STARTING_CTOR_IDX = WRITABLE_CTOR_IDX;
            ENDING_CTOR_IDX = WRITABLE_CTOR_IDX + annotatedConstructorCode.length();
        }
        StringBuilder defaultCtorCode = new StringBuilder();
        if (isBuildNew) {
            STARTING_CTOR_IDX = WRITABLE_CTOR_IDX + 1;
            /** Not creepy as it seems. */
            ENDING_CTOR_IDX = STARTING_CTOR_IDX;
        }
        if ((rawInput.getWithSuperConstructor()
                && hasStringLevelDefaultCtor()
                && !hasStringLevelDefaultCtor()
                && (isAllNonFinalFields() || (serializableFields.size() == 0)))) {
            defaultCtorCode.append(buildConstructorCode(isFromExisting, Boolean.FALSE, Boolean.TRUE));
        }
        /** Add constructor code to existing class content ( on-the-fly-modification as well) */
        String firstHalf = CLASS_CONTENT.substring(0, STARTING_CTOR_IDX) + defaultCtorCode;
        /** If required to strip final "keyword". */
        if (rawInput.isStripFinalClass()) {
            firstHalf = stripFinalClass(firstHalf);
            /**
             * By default, prepend this @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include =
             * As.WRAPPER_OBJECT) on final classes.
             */
        } else if (Modifier.isFinal(CLAZZ.getModifiers())) {
            firstHalf = prependWrapperObject(firstHalf);
            /** Append import lines to missingImports. */
            missingImportClassString.add("com.fasterxml.jackson.annotation.JsonTypeInfo");
            missingImportClassString.add("com.fasterxml.jackson.annotation.JsonTypeInfo.As");
        }
        final String secondHalf = CLASS_CONTENT.substring(ENDING_CTOR_IDX + 1, CLASS_CONTENT.length());

        StringBuilder decorated =
                new StringBuilder()
                        .append(firstHalf)
                        .append(SINGLE_BREAK)
                        .append(annotatedConstructorCode)
                        .append(secondHalf);

        BuildConstructorPhaseOutput result =
                new BuildConstructorPhaseOutput(addImports(decorated).toString());

        try {
            pushStack((PhaseChainedResult) result.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String prependWrapperObject(String firstHalf) {
        StringBuilder finalRes = new StringBuilder();
        final String toPrepend =
                "@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.WRAPPER_OBJECT)";
        int beforeClassIdx =
                StringUtils.firstIndexOf(firstHalf, '\r', CLASS_KEYWORD_N_NAME_IDX, Boolean.TRUE);
        final String resultFirstHalf = firstHalf.substring(0, beforeClassIdx);
        final String resultSecondHalf = firstHalf.substring(beforeClassIdx + 1, firstHalf.length());

        finalRes
                .append(resultFirstHalf)
                .append(SINGLE_BREAK)
                .append(isInnerClass() ? IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB) : "")
                .append(toPrepend)
                .append(resultSecondHalf);

        return finalRes.toString();
    }

    /**
     * Only annotate custom serialization on designated fields, given by param.
     *
     * @param customSerializationIndexedFields
     * @return
     */
    private String annotateFields(
            List<CustomSerializationIndexedField> customSerializationIndexedFields) {
        if (CollectionUtils.isEmpty(customSerializationIndexedFields)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String runningContent = CLASS_CONTENT;
        String fieldName = "";
        for (CustomSerializationIndexedField field : customSerializationIndexedFields) {
            /** Don't annotate if field doesn't pass the given rules. */
            fieldName = field.getField().getFieldName();
            if (!isValidForCustomSerAnnotation(fieldName)) {
                continue;
            }
            if (Boolean.FALSE == this.isNeedToAddCustomImports) {
                this.isNeedToAddCustomImports = Boolean.TRUE;
            }
            sb.setLength(0);
            int correspondingFieldIndex =
                    Math.max(field.getIndexInClass(), findFieldIndexByName(fieldName, runningContent));

            int end =
                    Math.max(
                            StringUtils.firstIndexOf(runningContent, SEMICOLON, field.getIndexInClass(), true),
                            StringUtils.firstIndexOf(runningContent, '\r', correspondingFieldIndex, true));

            String toPrepend = runningContent.substring(0, end);
            sb.append(toPrepend)
                    .append(SINGLE_BREAK)
                    .append(field.getTransformed(true))
                    .append(
                            runningContent.substring(
                                    StringUtils.firstIndexOf(runningContent, SEMICOLON, end, false) + 1,
                                    runningContent.length()));

            runningContent = sb.toString();
        }
        return runningContent;
    }

    /**
     * Not yet handle if comments.
     *
     * @param fieldName
     * @return
     */
    private boolean isValidForCustomSerAnnotation(String fieldName) {
        if (MapUtils.isEmpty(this.filterRules)) { // if no rule is defined.
            return true;
        }
        boolean containsFieldInTheCheckScope = false;
        List<Integer> filterRuleIndexes = new ArrayList<>();
        if (this.filterRules.containsKey(CONSTRUCTOR_BODY)) {
            String[] rawRuleList = this.filterRules.get(CONSTRUCTOR_BODY).split(COMMA_STRING);
            String partToCheck = rawRuleList[rawRuleList.length - 1];
            String[] withPrefix = new String[0];
            if (partToCheck.contains(SPACE)) {
                withPrefix = partToCheck.split(SPACE);
            }
            String existingCtorBody =
                    CLASS_CONTENT.substring(
                            CLASS_CONTENT.indexOf(OPEN_BRACKET, STARTING_CTOR_IDX) + 1, ENDING_CTOR_IDX + 1);
            for (int i = 0, n = existingCtorBody.length(); i < n; i++) {
                int internIdx =
                        forceFindExactPrefixedString(
                                STARTING_CTOR_IDX, CLASS_CONTENT, withPrefix[1], withPrefix[0], i, CLOSE_PAREN);
                if (internIdx == -1 || filterRuleIndexes.contains(internIdx)) {
                    continue;
                }
                filterRuleIndexes.add(internIdx);
            }

            if (CollectionUtils.isEmpty(filterRuleIndexes)) {
                return false;
            }
            for (int idx : filterRuleIndexes) {
                containsFieldInTheCheckScope =
                        isFieldWithinScope(
                                fieldName, CLASS_CONTENT.substring(idx, CLASS_CONTENT.indexOf(SEMICOLON, idx)));
                if (containsFieldInTheCheckScope) {
                    break;
                }
            }
        }
        return containsFieldInTheCheckScope;
    }

    private boolean isFieldWithinScope(String fieldName, String scope) {
        return scope.contains(fieldName);
    }

    private List<CustomSerializationIndexedField> buildCustomSerIndexedFields(
            List<DecorationLocalField> customSerRequiredFields) {
        List<CustomSerializationIndexedField> result = new ArrayList<>();
        for (int i = 0, n = customSerRequiredFields.size(); i < n; i++) {
            DecorationLocalField field = customSerRequiredFields.get(i);
            String fieldName = field.getFieldName();

            int internIdx = findFieldIndexByName(fieldName, CLASS_CONTENT);
            if (internIdx == -1) {
                continue;
            }
            String stringForm =
                    CLASS_CONTENT.substring(internIdx, CLASS_CONTENT.indexOf(SEMICOLON, internIdx) + 1);
            stringForm =
                    StringUtils.stripDoubleEndedNonAlphaNumeric(
                            StringUtils.findPrependablePieceFrom(CLASS_CONTENT, internIdx, null, false))
                            + stringForm;

            CustomSerializationIndexedField indexedField =
                    new CustomSerializationIndexedField(internIdx, field, stringForm);
            indexedField.transformDecorate(buildCompleteCustomSerAnnotation("using"));
            result.add(indexedField);
        }
        return result;
    }

    private List<String> buildCompleteCustomSerAnnotation(String annotationProp) {
        List<String> annotationLines = new ArrayList<>();
        for (int i = 0, n = customSerClassNames.size(); i < n; i++) {
            StringBuilder annotationLine = new StringBuilder();
            String customSerClass = AT + StringUtils.getLastWord(this.customSerAnnotStrings.get(i), DOT);
            annotationLine
                    .append(customSerClass)
                    .append(OPEN_PAREN)
                    .append(
                            StringUtils.formKeyValuePair(
                                    annotationProp,
                                    StringUtils.getLastWord(customSerClassNames.get(i), DOT) + DOT + CLASS_KEYWORD,
                                    EQUAL))
                    .append(CLOSE_PAREN);

            annotationLines.add(annotationLine.toString());
        }
        return annotationLines;
    }

    private int findFieldIndexByName(String fieldName, String content) {
        int internIdx =
                Math.max(
                        forceFindExactPrefixedString(
                                CLASS_KEYWORD_N_NAME_IDX, content, fieldName, "", 1, EQUAL.charAt(1)),
                        forceFindExactPrefixedString(
                                CLASS_KEYWORD_N_NAME_IDX, content, fieldName, "", 1, SEMICOLON));

        return internIdx;
    }

    private String extractCtorBodyFrom(String content, int start, int end, char extractAfter) {
        return content.substring(content.indexOf(extractAfter, start) + 1, end);
    }

    private String stripFinalClass(String firstHalf) {
        String theRest = firstHalf.substring(firstHalf.indexOf(OPEN_BRACKET, CLASS_KEYWORD_N_NAME_IDX));
        int stop =
                Math.min(
                        StringUtils.lastIndexOf(firstHalf, '\r', CLASS_KEYWORD_N_NAME_IDX, 1, null),
                        StringUtils.lastIndexOf(firstHalf, '\n', CLASS_KEYWORD_N_NAME_IDX, 1, null));
        String splittedFirstPartIdx = firstHalf.substring(0, stop);
        int bridgedIdx = firstHalf.indexOf(OPEN_BRACKET, stop);
        String classDeclaredPart = firstHalf.substring(stop, bridgedIdx);
        classDeclaredPart =
                StringUtils.resolveReplaces(classDeclaredPart, FINAL_KEYWORD, "", SPACE + SPACE, SPACE);

        return splittedFirstPartIdx + classDeclaredPart + theRest;
    }

    /**
     * Serves List<String> or List<DecorationLocalField>
     *
     * @param inp
     * @return
     */
    private List<String> removeDups(List<DecorationLocalField> inp) {
        List<String> uniqueRes = new ArrayList<>();
        for (int i = 0, n = inp.size(); i < n; i++) {
            Object each = inp.get(i);
            String toEval = "";
            if (each instanceof String) {
                toEval = String.valueOf(each);
            } else if (each instanceof DecorationLocalField) {
                DecorationLocalField eachField = (DecorationLocalField) each;
                toEval = eachField.getFieldName();
            }
            if (uniqueRes.contains(toEval)) {
                continue;
            }
            uniqueRes.add(toEval);
        }
        return uniqueRes;
    }

    /**
     * If existing ctor's body doesn't match the expected serializable fieldStrings, then build new.
     *
     * @return
     */
    private String buildConstructorBody(boolean isFromExisting) {
        StringBuilder body = new StringBuilder();
        isBuildNew = false;
        StringBuilder superLine = new StringBuilder();
        if (CollectionUtils.isNotEmpty((parentFields))) {
            superLine =
                    new StringBuilder(IndentationUtils.get(IndentationUtils.INNER_BLOCK_TAB) + "super(");
            List<String> uniqueCleansed = removeDups(parentFields);
            for (int i = 0, n = uniqueCleansed.size(); i < n; i++) {
                superLine.append(uniqueCleansed.get(i).trim());
                if (i < n - 1) {
                    superLine.append(", ");
                }
            }
            superLine.append(");").append(SINGLE_BREAK);
            this.superLine = superLine.toString();
        }
        if (isFromExisting) {
            try {
                String existingCtorBody =
                        extractCtorBodyFrom(
                                CLASS_CONTENT, STARTING_CTOR_IDX, ENDING_CTOR_IDX + 1, OPEN_BRACKET);
                body.append(existingCtorBody);
            } catch (Throwable t) {
                if (rawInput.isSkipError()) {
                    return SKIP_MARKER;
                }
                t.printStackTrace();
                throw new RuntimeException(t.getMessage());
            }
        }

        if (parentFields.size() > 0 && StringUtils.isAnagram(body.toString(), superLine.toString())) {
            /** on the fly reset. */
            parentFields = new ArrayList<>();
            return body.toString();
        }
        if ( // 1st expression
                (!CollectionUtils.isEmpty(ANNOTATABLE_ARGS)
                        && ANNOTATABLE_ARGS.size()
                        >= ReflectionUtils.merge(parentFields, serializableFields).size())
                        &&
                        // 2nd expression
                        (StringUtils.containsAllFields(
                                body.toString(), ReflectionUtils.merge(parentFields, serializableFields))
                                || (body.toString().contains("super(")))) {

            return body.toString();
        }
        /** Then build new */
        isBuildNew = true;
        body.setLength(0);
        body.append(SINGLE_BREAK);

        /** Add the super field list in the ctor body. */
        body.append(superLine);

        List<String> builtFields = buildFields(Boolean.FALSE);

        if (CollectionUtils.isEmpty(builtFields)) {

            if (superLine.length() > 0) {
                return body.toString();
            }
            return SKIP_MARKER;
        }
        for (int i = 0, n = builtFields.size(); i < n; i++) {
            String field = builtFields.get(i);
            final String extracted = extractVarName(field);
            if (StringUtils.isEmpty(field)
                    || StringUtils.isEmpty(extracted)
                    || field.contains(WEIRD_FIELD)) {
                continue;
            }
            if (i > 0) {
                body.append(SINGLE_BREAK);
            }
            body.append(IndentationUtils.get(IndentationUtils.INNER_BLOCK_TAB))
                    .append(THIS_KEYWORD)
                    .append(extracted)
                    .append(EQUAL)
                    .append(extracted)
                    .append(SEMICOLON);
        }

        return body.append(SINGLE_BREAK).toString();
    }

    /**
     * If not should merge,
     *
     * @param shouldMerge
     * @return
     */
    private List<String> buildFields(boolean shouldMerge) {
        List<String> res = new ArrayList<>();
        List<DecorationLocalField> mergeable = new ArrayList<>(serializableFields);
        if (shouldMerge) {
            mergeable = ReflectionUtils.merge(mergeable, parentFields);
        }
        String fieldName = "";
        for (DecorationLocalField field : mergeable) {
            fieldName = field.getFieldName();
            if (Objects.isNull(field) || fieldName.contains(WEIRD_FIELD)) {
                continue;
            }
            String toAdd =
                    new StringBuilder()
                            .append(evalFieldString(field))
                            .append(field.getFieldName().contains(SPACE) ? "" : SPACE)
                            .append(fieldName)
                            .toString();
            if (res.contains(toAdd)) {
                continue;
            }
            res.add(toAdd);
        }
        return res;
    }

    private String evalFieldString(DecorationLocalField field) {
        String fieldType = "";
        if (field.getTypeFullName().equalsIgnoreCase("java.sql.Date")) {
            fieldType = "java.sql.Date";
        } else {
            fieldType = field.getGenericTypeName();
            for (String each : COMMON_PACKAGE_LIST) {
                fieldType = StringUtils.resolveReplaces(fieldType, each + DOT, "");
            }
            if (fieldType.contains(DOT)) {
                fieldType =
                        StringUtils.bulkCascadeRemoveSuffixedString(fieldType, DOT.charAt(0), '<', ',', '>');
                fieldType = StringUtils.resolveReplaces(fieldType, "$", DOT);
            }
            /**
             * Hotfix for: https://github.com/trgpnt/Java-Class-Annotatable-Constructor-Templater/issues/8
             * Verify If Need To Add / Ignore Java Static Import #8
             */
            if (fieldType.contains(DOT)) {
                final String importRegion = getImportRegion();
                if (StringUtils.isEmpty(importRegion)) {
                    return fieldType;
                }
                if (importRegion.contains(fieldType)) {
                    fieldType = fieldType.substring(fieldType.indexOf(DOT) + 1, fieldType.length());
                }
            }
        }
        return fieldType;
    }

    /**
     * if parameterized Build constructor code from serializable fieldStrings, with or without
     * existing constructor body.
     *
     * <p>else build a default ctor
     *
     * @param
     * @return
     */
    private String buildConstructorCode(
            boolean isFromExisting, boolean shouldAnnotateCtor, boolean isDefaultCtor) {
        /** Build ctor prototype / signature */
        StringBuilder ctorPrototype = new StringBuilder();
        if (shouldAnnotateCtor) {
            ctorPrototype
                    .append(IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB))
                    .append(CONSTRUCTOR_ANNOTATION);
        }
        /** Reuse existing access modifier or take from client's. */
        String accessModMaybeClassName = "";
        if (isDefaultCtor) {
            if (Modifier.isAbstract(CLAZZ.getModifiers())
                    || CLAZZ.getSimpleName().startsWith(BASE_KEYWORD)
                    || parentToChild.containsKey(CLAZZ.getName())) {
                accessModMaybeClassName = PROTECTED_MOD + SPACE;
            } else {
                accessModMaybeClassName = PUBLIC_MOD + SPACE;
            }
        } else if (isFromExisting) {
            accessModMaybeClassName =
                    CLASS_CONTENT.substring(
                            STARTING_CTOR_IDX, CLASS_CONTENT.indexOf(OPEN_PAREN, STARTING_CTOR_IDX));
        } else if (!isFromExisting && parentToChild.containsKey(CLAZZ.getName())) {
            accessModMaybeClassName = PROTECTED_MOD + SPACE;
        } else {
            if (parentToChild.containsKey(CLAZZ.getName())) {
                accessModMaybeClassName = PROTECTED_MOD + SPACE;
            } else {
                accessModMaybeClassName =
                        CLAZZ.getSimpleName().startsWith(BASE_KEYWORD)
                                ? PROTECTED_MOD + SPACE
                                : rawInput.getAccessModifier() + SPACE;
            }
        }
        /**
         * lastly,..
         */
        if (MapUtils.isNotEmpty(classToMergeableParams) && classToMergeableParams.containsKey(CLAZZ.getName())) {
            accessModMaybeClassName = PROTECTED_MOD + SPACE;
        }
        ctorPrototype
                .append(SINGLE_BREAK)
                .append(IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB))
                .append(accessModMaybeClassName)
                .append(
                        ctorPrototype.toString().contains(CLAZZ.getSimpleName()) ? "" : CLAZZ.getSimpleName())
                .append(OPEN_PAREN);
        /** Eval and build ctor's body */
        //TODO verify this as well
        String ctorBody = "";
        final String spaces =
                IndentationUtils.genCharsWithLen(
                        SPACE.charAt(0),
                        accessModMaybeClassName.length() + CLAZZ.getSimpleName().length() + "\\t".length() + 2);

        if (!isDefaultCtor) {
            ctorBody = buildConstructorBody(isFromExisting);
        } else {
            StringBuilder sb =
                    new StringBuilder()
                            .append(SINGLE_BREAK)
                            .append(IndentationUtils.get(IndentationUtils.INNER_BLOCK_TAB))
                            .append("super();")
                            .append(SINGLE_BREAK)
                            .append(IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB))
                            .append(CLOSE_BRACKET);
            ctorBody = sb.toString();
        }
        /** If exception occurs, go to next class (maybe inner). */
        if (!isDefaultCtor
                && (StringUtils.isEmpty(ctorBody) || SKIP_MARKER.equalsIgnoreCase(ctorBody))) {
            return SKIP_MARKER;
        }
        /** annotate fieldStrings. */
        /** If need to merge from parent fields */
        List<String> parent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parentFields)
                && !ctorBody
                .substring(ctorBody.indexOf("super"), ctorBody.indexOf(SEMICOLON))
                .contains(StringUtils.stripDoubleEndedNonAlphaNumeric(this.superLine))) {
            for (DecorationLocalField field : parentFields) {
                boolean flag = false;
                String fieldName = field.getFieldName();
                // TODO optimize
                for (String each : ANNOTATABLE_ARGS) {
                    if (StringUtils.isNotEmpty(fieldName) && fieldName.equalsIgnoreCase(each)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
                //TODO verify the need
                String toAdd =
                        new StringBuilder(evalFieldString(field))
                                .append(field.getFieldName().contains(SPACE) ? "" : SPACE)
                                .append(field.getFieldName())
                                .toString();
                if (parent.contains(toAdd)) {
                    continue;
                }
                parent.add(toAdd);
            }
        }
        List<String> fieldStrings = parent;
        List<String> currentFields = ANNOTATABLE_ARGS;
        if (isBuildNew) {
            currentFields = buildFields(Boolean.TRUE);
        }
        /**
         * ONLY IF THE bfs param flag IS TRUE Enqueue params if domain-related in case fields are not
         * the same type.
         */
        if (rawInput.getBfsParams()) {
            for (String curr : currentFields) {
                try {
                    String className = curr.split(SPACE)[0];
                    for (String possibleClassName :
                            StringUtils.makeNonAlphaStringsFrom(className, Boolean.TRUE)) {
                        Class clazz =
                                ReflectionUtils.getClass(
                                        StringUtils.stripDoubleEndedNonAlphaNumeric(
                                                getExactFullPathFor(possibleClassName)));
                        if (ReflectionUtils.isForbidden(clazz, rawInput)) {
                            continue;
                        }
                        AnnotatableConstructorDecorator.enqueueWith(clazz);
                    }
                } catch (Throwable t) {
                    // ok
                }
            }
        }
        StringBuilder annotatedArgs = new StringBuilder();
        if (!isDefaultCtor) {
            if (CollectionUtils.isEmpty(fieldStrings)) {
                fieldStrings = currentFields;
            } else {
                /** Stricly-ordered merging */
                for (String currField : currentFields) {
                    if (fieldStrings.contains(currField)) {
                        continue;
                    }
                    fieldStrings.add(currField);
                }
            }

            if (isBuildNew
                    && Objects.nonNull(this.customSerAnnotStrings)
                    && this.customSerAnnotStrings.size() > 0) {
                FileUtils.writeContentToFile(
                        "class_report.txt",
                        "class is built with new ctor, please double check" + "\nat " + CLAZZ.getName() + "\n",
                        true);
            }
            for (String rawArgString : fieldStrings) {
                if (StringUtils.isEmpty(rawArgString) || rawArgString.contains(WEIRD_FIELD)) {
                    continue;
                }
                if (annotatedArgs.length() > 0) {
                    annotatedArgs.append(COMMA).append(SINGLE_BREAK).append(spaces);
                }
                final String extracted = extractVarName(rawArgString);
                annotatedArgs.append(
                        annotateArg(
                                rawArgString,
                                !isBuildNew ? findExactSerializableFieldString(extracted) : extracted));
            }
            /** put to map to act as children's legacy. */
            addFieldListToMap(CLAZZ.getName(), buildListFromSpacedStrings(fieldStrings), Boolean.TRUE);
        }

        /** Append method prototype with list of jackson-annotated params */
        ctorPrototype.append(annotatedArgs).append(CLOSE_PAREN).append(SPACE);
        String postCtorString = "";
        if (STARTING_CTOR_IDX != -1) {
            postCtorString =
                    CLASS_CONTENT.substring(CLASS_CONTENT.indexOf(CLOSE_PAREN, STARTING_CTOR_IDX) + 1);
        }
        if (StringUtils.isNotEmpty(postCtorString)) {
            this.postCtorString = postCtorString.substring(0, postCtorString.indexOf(OPEN_BRACKET));
            ctorPrototype.append(this.postCtorString);
        }

        return ConstructorCodeAssembler.current()
                .assemblePrototype(ctorPrototype.toString())
                .assembleMethodBody(ctorBody.toString())
                .transformComplete();
    }

    private String findExactSerializableFieldString(String intern) {
        String exactFieldName = "";
        try {
            String existingCtorBody =
                    CLASS_CONTENT.substring(
                            CLASS_CONTENT.indexOf(OPEN_BRACKET, STARTING_CTOR_IDX) + 1, ENDING_CTOR_IDX + 1);
            final String[] dummyEnds =
                    new String[]{String.valueOf(COLON), String.valueOf(SEMICOLON), " !=", " =="};
            int idxOfParam = -1;
            for (int i = 0, n = dummyEnds.length; i < n && idxOfParam == -1; i++) {
                idxOfParam = existingCtorBody.indexOf(intern + dummyEnds[i]);
            }
            String prefixReportMsg = "";
            int equalSignIdx =
                    StringUtils.lastIndexOf(
                            existingCtorBody,
                            StringUtils.resolveReplaces(EQUAL, SPACE, "").charAt(0),
                            idxOfParam,
                            -1,
                            null);
            if (equalSignIdx != idxOfParam && Math.min(equalSignIdx, idxOfParam) != -1) {
                /** loop backward, reverse-build a string. */
                exactFieldName =
                        StringUtils.findPrependablePieceFrom(
                                existingCtorBody.substring(0, equalSignIdx), equalSignIdx - 1, DOT.charAt(0), true);
            }
            if (!StringUtils.isEmpty(exactFieldName) && !exactFieldName.equalsIgnoreCase(intern)) {
                if (StringUtils.isEmpty(exactFieldName)) {
                    prefixReportMsg = "!!ALERT, param is not assigned to any declared field !\n";
                } else {
                    prefixReportMsg = "already added mismatched fields = \n";
                }
                FileUtils.writeContentToFile(
                        "class_report.txt",
                        prefixReportMsg
                                + "\t\t param = "
                                + intern
                                + "\n\t\t field = "
                                + exactFieldName
                                + "\n ===> at "
                                + CLAZZ.getName()
                                + "\n",
                        true);
            }
        } catch (Throwable t) {
            throw new RuntimeException("rip");
        }
        return StringUtils.isEmpty(exactFieldName) ? intern : exactFieldName;
    }

    private int findExistingParaCtor() {
        int maxNbrOfParams = Integer.MIN_VALUE;
        for (int i = 0, n = internCtorIdxes.size(); i < n && internCtorIdxes.get(i) != -1; i++) {
            int existingConstructor = internCtorIdxes.get(i);
            int openParenIdx = CLASS_CONTENT.indexOf(OPEN_PAREN, existingConstructor);
            int closeParenIdx = CLASS_CONTENT.indexOf(CLOSE_PAREN, openParenIdx);

            String declaredParm = CLASS_CONTENT.substring(openParenIdx + 1, closeParenIdx);

            if (StringUtils.isEmpty(declaredParm)) {
                continue;
            }

            List<String> declardParmList = normalizeParams(StringUtils.stripComments(declaredParm));
            /** Gradually record the ctor with number of max params in string. */
            if (CollectionUtils.isEmpty(declardParmList)) {
                continue;
            }
            /**
             * will update exsiting idxes in 2 cases: _ current param list is greater _ current param list
             * is equal, but current ctor's body contains all serializable fields, while the max_so_far
             * doesn't.
             */
            if ((maxNbrOfParams < declardParmList.size())
                    || (maxNbrOfParams == declardParmList.size()
                    && StringUtils.containsAllFields(
                    extractCtorBodyFrom(
                            CLASS_CONTENT,
                            existingConstructor,
                            findMatchingCloseBrckt(
                                    CLASS_CONTENT.indexOf(OPEN_BRACKET, existingConstructor) - 1)
                                    + 1,
                            OPEN_BRACKET),
                    ReflectionUtils.merge(parentFields, serializableFields)))) {

                ANNOTATABLE_ARGS = declardParmList;
                maxNbrOfParams = declardParmList.size();
                STARTING_CTOR_IDX = existingConstructor;
                /** Strictly find the last }'s index with stack. */
                ENDING_CTOR_IDX =
                        findMatchingCloseBrckt(CLASS_CONTENT.indexOf(OPEN_BRACKET, STARTING_CTOR_IDX) - 1);
            }
        }
        if (!CollectionUtils.isEmpty(ANNOTATABLE_ARGS)
                && STARTING_CTOR_IDX > 0
                && ENDING_CTOR_IDX > 0) {
            return STARTING_CTOR_IDX;
        }
        return -1;
    }

    /**
     * get full class path for param.
     *
     * @param className
     * @return
     */
    private String getFullPathFor(String className) {
        try {
            if (StringUtils.isNotEmpty(getFullPathForClass(className))) {
                return getFullPathForClass(className);
            }
            final String zone = getImportRegion();
            int endIdx = zone.indexOf(SEMICOLON, zone.indexOf(className));
            if (endIdx == -1) {
                return getFullPathForClass(className);
            }
            int startIdx = StringUtils.lastIndexOf(zone, SPACE.charAt(0), endIdx, 1, null);
            if (startIdx == -1) {
                return getFullPathForClass(className);
            }
            final String path = zone.substring(startIdx, endIdx);
            addPathToClass(className, path);
            return getFullPathForClass(className);
        } catch (Throwable t) {
            return "";
        }
    }

    private String getExactFullPathFor(String className) {
        try {
            if (StringUtils.isNotEmpty(getFullPathForClass(className))) {
                return getFullPathForClass(className);
            }
            final String zone = getImportRegion();
            for (String line : zone.split(String.valueOf(SEMICOLON))) {
                if (!line.contains(className)) {
                    continue;
                }
                final int res = TrieRepository.go().resetTrie().with(line, false).search(className);
                if (res >= 1) {
                    addPathToClass(
                            className,
                            StringUtils.stripDoubleEndedNonAlphaNumeric(
                                    line.substring(line.indexOf(IMPORT_KEYWORD) + IMPORT_KEYWORD.length() + 1)));
                    break;
                }
            }
            return getFullPathForClass(className);
        } catch (Throwable t) {
            throw new RuntimeException("rip");
        }
    }

    /**
     * A minor stack-based approach to eval open-close matching brackets (of the same type).
     *
     * @return
     */
    private int findMatchingCloseBrckt(int from) {
        int stackSize = 0;
        for (int i = from, n = CLASS_CONTENT.length(); i < n; i++) {
            if (OPEN_BRACKET == CLASS_CONTENT.charAt(i)) {
                stackSize++;
            } else if (CLOSE_BRACKET == CLASS_CONTENT.charAt(i)) {
                stackSize--;
                if (stackSize == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static List<String> normalizeParams(String inp) {
        try {
            int left = 0;
            int right = -1;
            int stackSimulated = 0;
            List<String> res = new ArrayList<>();
            char[] arr = inp.toCharArray();
            final Set<Character> preserved = new HashSet<>(Arrays.asList(GREATER_SIGN, LESSER_SIGN, COMMA, DOT.charAt(0)));
            for (int i = 0, n = arr.length; i < n; i++) {
                right++;
                if (i == n - 1) {
                    res.add(StringUtils.ensureOneWhitespace(new String(arr, left, right + 1), preserved));
                    break;
                }
                char c = arr[i];
                if (LESSER_SIGN == c) {
                    stackSimulated++;
                } else if (GREATER_SIGN == c) {
                    stackSimulated--;
                } else if (COMMA == c) {
                    if (stackSimulated > 0) {
                        continue;
                    }
                    res.add(StringUtils.ensureOneWhitespace(new String(arr, left, right), preserved));
                    right = 0;
                    left = i;
                }
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Only add if lacking Jackson imports.
     *
     * @param decorated
     * @return
     */
    private StringBuilder addImports(StringBuilder decorated) {
        if (decorated.toString().contains(buildAnnotationImports())
                && CollectionUtils.isEmpty(missingImportClassString)) {
            return decorated;
        }
        StringBuilder finalModified = new StringBuilder();
        /**
         * Append import jackson packages Find the last "import com..." ( if any ), otherwise, append as
         * the last.
         */
        int startingImportIdx =
                forceFindPrefixedString(
                        Optional.of(0),
                        IMPORT_KEYWORD,
                        COM_KEYWORD,
                        decorated.toString().substring(0, CLASS_KEYWORD_N_NAME_IDX - 1),
                        Optional.of(-1));

        if (startingImportIdx == -1) {
            /** append as the last. */
            startingImportIdx =
                    forceFindPrefixedString(
                            Optional.of(0),
                            IMPORT_KEYWORD,
                            "",
                            decorated.toString().substring(0, CLASS_KEYWORD_N_NAME_IDX - 1),
                            Optional.of(-1));
        }
        /** lastly, find the semicolon */
        startingImportIdx =
                decorated.indexOf(
                        String.valueOf(SEMICOLON), startingImportIdx == -1 ? 0 : startingImportIdx);

        /** Oops, we entered the comment section let's bring it back */
        if (startingImportIdx > CLASS_KEYWORD_N_NAME_IDX) {
            startingImportIdx =
                    StringUtils.lastIndexOf(CLASS_CONTENT, SEMICOLON, CLASS_KEYWORD_N_NAME_IDX, 1, false);
        }

        finalModified
                .append(decorated.substring(0, startingImportIdx + 1))
                .append(CLASS_CONTENT.contains(buildAnnotationImports()) ? "" : buildAnnotationImports())
                .append(buildMissingImports())
                .append(decorated.substring(startingImportIdx + 1, decorated.length()));

        return finalModified;
    }

    private String buildMissingImports() {
        /** Insert import into Trie */
        String zone = getImportRegion();
        TrieRepository trieRepository = TrieRepository.go().resetTrie();
        if (StringUtils.isNotEmpty(zone)) {
            for (String line : zone.split(String.valueOf(SEMICOLON))) {
                if (line.contains(IMPORT_KEYWORD)) {
                    line =
                            line.substring(line.indexOf(IMPORT_KEYWORD) + IMPORT_KEYWORD.length(), line.length())
                                    .replace(";", "");
                    line =
                            StringUtils.toStringFromList(
                                    StringUtils.makeNonAlphaStringsFrom(line, Boolean.TRUE),
                                    CharacterRepository.SPACE);
                }
                trieRepository.with(line, true);
            }
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0, n = this.missingImportClassString.size(); i < n; i++) {
            String each = this.missingImportClassString.get(i);
            if (StringUtils.isEmpty(each)
                    || each.charAt(0) == '.'
                    || !each.contains(DOT)
                    || (trieRepository.containsData()
                    && trieRepository.search(each) > 0)) { // Use Trie to evaluate strings.
                continue;
            }
            /** Never had expected dollar sign appears here. */
            if (each.contains("$")) {
                /**
                 * By default, this will assume that this field's usage reference has already specified the
                 * inner class within. So just need to preserve the top-level class string.
                 */
                each = each.substring(0, each.indexOf("$"));
            }
            res.append(SINGLE_BREAK).append(IMPORT_KEYWORD).append(SPACE).append(each).append(SEMICOLON);
        }
        return res.toString();
    }

    private String buildAnnotationImports() {
        StringBuilder fixedPart =
                new StringBuilder()
                        .append(SINGLE_BREAK)
                        .append(IMPORT_KEYWORD)
                        .append(CONSTRUCTOR_ANNOTATION_PACKAGE)
                        .append(SINGLE_BREAK)
                        .append(IMPORT_KEYWORD)
                        .append(PARAM_ANNOTATION_PACKAGE);

        /** Process to add the required custom serialization imports... */
        if (!NullabilityUtils.isAllNonEmpty(false, String.valueOf(rawCustomSerImportStrings))) {
            return fixedPart.toString();
        }

        if (Boolean.FALSE == this.isNeedToAddCustomImports) {
            return fixedPart.toString();
        }

        for (int i = 0, n = this.customSerAnnotStrings.size(); i < n; i++) {
            fixedPart
                    .append(SINGLE_BREAK)
                    .append(IMPORT_KEYWORD)
                    .append(SPACE)
                    .append(this.customSerAnnotStrings.get(i))
                    .append(SEMICOLON);
        }

        for (int i = 0, n = this.customSerClassNames.size(); i < n; i++) {
            fixedPart
                    .append(SINGLE_BREAK)
                    .append(IMPORT_KEYWORD)
                    .append(SPACE)
                    .append(this.customSerClassNames.get(i))
                    .append(SEMICOLON);
        }

        return fixedPart.toString();
    }

    private String extractVarName(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return "";
        }
        String[] spaceSplitted = raw.split(SPACE);
        return StringUtils.stripDoubleEndedNonAlphaNumeric(spaceSplitted[spaceSplitted.length - 1]);
    }

    /**
     * Jackson-annotate a given field. input : String field1 output : @JsonProperty("field1") String
     * field OR @JsonProperty String field if varName is not specified.
     *
     * @param raw     : String field1 above.
     * @param varName : field1 above.
     * @return
     */
    private String annotateArg(String raw, String varName) {
        StringBuilder jacksonAnnotated = new StringBuilder();
        StringBuilder middleValue = new StringBuilder(SPACE);
        if (StringUtils.isNoneBlank(varName)) {
            middleValue.setLength(0);
            middleValue
                    .append(OPEN_PAREN)
                    .append(SINGLE_QUOTE)
                    .append(varName)
                    .append(SINGLE_QUOTE)
                    .append(CLOSE_PAREN)
                    .append(SPACE);
        }
        jacksonAnnotated.append(FIELD_ANNOTATION).append(middleValue).append(raw);

        return jacksonAnnotated.toString();
    }
}
