package com.aggregated;

import java.util.Objects;

import static com.aggregated.CharacterRepository.CLOSE_BRACKET;
import static com.aggregated.CharacterRepository.OPEN_BRACKET;

public  class ConstructorCodeAssembler {
    private String ctorPrototype;
    private String methodBody;
    private final static ConstructorCodeAssembler INSTANCE = new ConstructorCodeAssembler();
    private ConstructorCodeAssembler() {}

    public static ConstructorCodeAssembler current() {
        return INSTANCE;
    }
    public ConstructorCodeAssembler assemblePrototype(String ctorPrototype) {
        this.ctorPrototype = ctorPrototype;
        return this;
    }

    public ConstructorCodeAssembler assembleMethodBody(String methodBody) {
        this.methodBody  = methodBody;
        return this;
    }

    public String transformComplete() {
        return StringUtils.appendIndentableBracketTo(ctorPrototype, String.valueOf(OPEN_BRACKET), "")
                + StringUtils.appendIndentableBracketTo(methodBody,
                String.valueOf(CLOSE_BRACKET),
                IndentationUtils.get(IndentationUtils.OUTER_BLOCK_TAB));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstructorCodeAssembler)) return false;
        ConstructorCodeAssembler that = (ConstructorCodeAssembler) o;
        return Objects.equals(ctorPrototype, that.ctorPrototype) && Objects.equals(methodBody,
                that.methodBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ctorPrototype, methodBody);
    }
}
