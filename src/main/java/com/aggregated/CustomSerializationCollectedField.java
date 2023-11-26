package com.aggregated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class CustomSerializationCollectedField implements Cloneable {
    private List<DecorationLocalField> serializableFields;

    private List<DecorationLocalField> customRequiredFields;

    public CustomSerializationCollectedField() {
        this.serializableFields = new ArrayList<>();
        this.customRequiredFields = new ArrayList<>();
    }

    public CustomSerializationCollectedField(List<DecorationLocalField> serializableFields, List<DecorationLocalField> customRequiredFields) {
        this.serializableFields = serializableFields;
        this.customRequiredFields = customRequiredFields;
    }

    public void addSerializableField(DecorationLocalField field) {
        this.serializableFields.add(field);
    }
    public void addCustomRequiredField(DecorationLocalField field) {
        this.customRequiredFields.add(field);
    }
    public List<DecorationLocalField> getSerializableFields() {
        return new ArrayList<>(serializableFields);
    }
    public List<DecorationLocalField> getCustomRequiredFields() {
        return customRequiredFields;
    }
    public void reset() {
        this.serializableFields = new ArrayList<>();
        this.customRequiredFields = new ArrayList<>();
    }

    @Override
    protected Object clone() {
        return deepClone();
    }

    public CustomSerializationCollectedField deepClone() {
        List<DecorationLocalField> serializables = new ArrayList<>(this.serializableFields);
        List<DecorationLocalField> customs = new ArrayList<>(this.customRequiredFields);
        return new CustomSerializationCollectedField(serializables, customs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomSerializationCollectedField)) return false;
        CustomSerializationCollectedField that = (CustomSerializationCollectedField) o;
        return Objects.equals(getSerializableFields(), that.getSerializableFields()) && Objects.equals(getCustomRequiredFields(), that.getCustomRequiredFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSerializableFields(), getCustomRequiredFields());
    }
}














