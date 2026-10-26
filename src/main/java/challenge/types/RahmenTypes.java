package challenge.types;

public enum RahmenTypes {
    S("S"),
    M("M"),
    L("L");

    private String type;

    RahmenTypes(String type) {
        this.type = type;
    }

    public static RahmenTypes getByName(String name) {
        for (RahmenTypes t : RahmenTypes.values()) {
            if (t.type.equals(name)) {
                return t;
            }
        }
        return null;
    }}
