package challenge.types;

public enum RadTypes {
    Bike("bike"),
    Mountain("mountain"),
    EBike("e-bike");

    private String name;

    RadTypes(String name) {
        this.name = name;
    }

    public static RadTypes getByName(String name) {
        for (RadTypes r : RadTypes.values()) {
            if (r.name.equals(name)) {
                return r;
            }
        }
        return null;
    }
}
