package project.DevView.cat_service.question.entity;

import lombok.Data;
import lombok.Getter;

@Getter
public enum Field {
    OS(1), DATABASE(2), NETWORK(3), JAVA(4),
    ALGORITHM(5), DATASTRUCTURE(6), SW(7), WEB(8);

    private final int value;

    Field(int value) {
        this.value = value;
    }

    public static Field fromValue(int value) {
        for (Field field : Field.values()) {
            if (field.getValue() == value) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
    public static Field fromName(String name) {
        for (Field field : Field.values()) {
            if (field.name().equalsIgnoreCase(name)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid name: " + name);
    }
}
