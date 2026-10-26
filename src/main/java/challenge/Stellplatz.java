package challenge;

public class Stellplatz {

    private int limit;
    private int id;

    public Stellplatz(int limit) {
        this.id = Database.addStellplatz(limit);
        this.limit = limit;
    }

    public Stellplatz(int id, int limit) {
        this.id = id;
        this.limit = limit;
    }

    public void addFahrrad(Fahrrad fahrrad) {
        Database.addFahrrad(fahrrad.getMarke(),fahrrad.getTyp(),fahrrad.getRahmen(),fahrrad.getAkku(),id);
    }

    public int getLimit() {
        return limit;
    }

    public int getId() {
        return id;
    }
}
