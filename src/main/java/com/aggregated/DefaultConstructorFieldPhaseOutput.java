package com.aggregated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultConstructorFieldPhaseOutput extends ChainedOutput {
    private Map<Field, String>                 fieldToDefaultValue;
    private DefaultConstructorFieldPhaseOutput toReturn;

    @JsonCreator
    public DefaultConstructorFieldPhaseOutput(@JsonProperty("fieldToDefaultValue") Map<Field, String> fieldToDefaultValue) {

        this.fieldToDefaultValue = fieldToDefaultValue;
        toReturn = this;
    }
    public Map<Field, String> getFieldToDefaultValue() {
        return fieldToDefaultValue;
    }
    @Override
    public boolean failVerify() {
        //False is good
        return false;
    }

    @Override
    public void reset() {
        if (Objects.nonNull(this.fieldToDefaultValue)) {
            this.fieldToDefaultValue.clear();
        } else {
            this.fieldToDefaultValue = new HashMap<>();
        }
    }

    @Override
    public ChainedOutput getValues() {
        if (Objects.isNull(this.toReturn)) {
            return new DefaultConstructorFieldPhaseOutput(fieldToDefaultValue);
        }
        return this.toReturn;
    }

    @Override
    public Object getRawValues() {
        return this.fieldToDefaultValue;
    }

    @Override
    public String toString() {
        return "DefaultConstructorFieldPhaseOutput{" +
                "fieldToDefaultValue=" + fieldToDefaultValue +
                ", toReturn=" + toReturn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultConstructorFieldPhaseOutput)) return false;
        DefaultConstructorFieldPhaseOutput that = (DefaultConstructorFieldPhaseOutput) o;
        return Objects.equals(getFieldToDefaultValue(), that.getFieldToDefaultValue()) && Objects.equals(
                toReturn,
                that.toReturn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFieldToDefaultValue(), toReturn);
    }
}














