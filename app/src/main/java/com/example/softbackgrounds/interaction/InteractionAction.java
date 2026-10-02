package com.example.softbackgrounds.interaction;

public class InteractionAction {

    public enum Type {
        NONE,
        LOG,
        OPEN_APP,
        NAVIGATE,
        ANIMATE
    }

    private final Type type;
    private final String target;

    public InteractionAction(Type type, String target) {
        this.type = type;
        this.target = target;
    }

    public Type getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }
}
