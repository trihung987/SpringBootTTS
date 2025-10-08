package me.trihung.enums;

public enum RoleType {
    ADMIN("ADMIN"),
    USER("USER"),
    MANAGER("MANAGER");

    public final String value;

    RoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
