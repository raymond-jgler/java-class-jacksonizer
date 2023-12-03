package com.aggregated;

import java.util.List;
import java.util.Objects;

public class CustomSerializationIndexedField {
    private int indexInClass;
    private final DecorationLocalField field;
    private final String stringForm;
    private String transformed;

    public CustomSerializationIndexedField(int indexInClass, DecorationLocalField field, String stringForm) {
        this.indexInClass = indexInClass;
        this.field = field;
        this.stringForm = stringForm;
    }

    public String getStringForm() {
        return stringForm;
    }

    public String getTransformed(boolean isIndent) {
        StringBuilder stringBuilder = new StringBuilder(this.transformed);
        if (isIndent) {
            stringBuilder = new StringBuilder();
            String[] splitted = this.transformed.split("\n");
            for (int i = 0, n = splitted.length; i < n; i++) {
                if (i > 0) {
                    stringBuilder.append("\n");
                }
                String each = splitted[i];
                stringBuilder.
                        append(IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB))
                        .append(each);
            }
        }
        return stringBuilder.toString();
    }

    public int getLen() {
        return this.transformed.length();
    }

    public String transformDecorate(List<String> annotationLines) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (String eachAnnotLine : annotationLines) {
            if (idx > 0) {
                sb.append("\n");
            }
            sb.append(processAnnotation(eachAnnotLine));
            idx++;
        }
        this.transformed = StringArsenal.current().with(sb.append("\n" ).toString())
                .resolveReplaces(";", "")
                .concatenateWith(stringForm)
                .getInternal();

        return this.transformed;
    }

    private String processAnnotation(String raw) {
        if (!raw.contains("@")) {
            raw = "@" + raw;
        }
        if (!raw.contains(";")) {
            raw += ";";
        }
        return raw;
    }

    public Integer getIndexInClass() {
        return indexInClass;
    }

    public DecorationLocalField getField() {
        return field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomSerializationIndexedField)) return false;
        CustomSerializationIndexedField that = (CustomSerializationIndexedField) o;
        return Objects.equals(getIndexInClass(), that.getIndexInClass()) && Objects.equals(getField(), that.getField()) && Objects.equals(stringForm, that.stringForm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndexInClass(), getField(), stringForm);
    }

    @Override
    public String toString() {
        return "CustomSerializationIndexedField{" +
                "indexInClass=" + indexInClass +
                ", field=" + field +
                ", stringForm='" + stringForm + '\'' +
                '}';
    }
}














