package org.wordcorr.gui.input;

/**
 * Enumerated type for input data types.
 * @author Keith Hamasaki
 **/
public class Type {

    public static final Type TEXT    = new Type(0);
    public static final Type INTEGER = new Type(1);
    public static final Type FLOAT   = new Type(2);

    public static Type getType(String typename) {
        if (typename.equals("text")) return TEXT;
        if (typename.equals("integer")) return INTEGER;
        if (typename.equals("float")) return FLOAT;
        throw new IllegalArgumentException("Unknown type: " + typename + ". Valid types are text, integer, float, date, time");
    }

    private Type(int value) {
        _value = value;
    }

    public String toString() {
        return String.valueOf(_value);
    }

    private final int _value;
}
