package challenge;

import challenge.types.Person;
import challenge.types.SearchOptions;


import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class Fahrradverleih {

    List<Stellplatz> stellplaetze;

    public Fahrradverleih() {
        this.stellplaetze = new ArrayList<Stellplatz>();
    }

    public void addStellplatz(Stellplatz stellplatz) {
        this.stellplaetze.add(stellplatz);
    }

    public List<Fahrrad> search(SearchOptions options) {
        switch (options.type()) {
            case MARKE -> {
                return Database.searchByMarke(options.text());
            }
            case TYP -> {
                return Database.searchByTyp(options.text());
            }
            case RAHMEN -> {
                return Database.searchByRahmen(options.text());
            }
            case AKKU -> {
                return Database.searchByAkku(Integer.parseInt(options.text()));
            }
            default -> {return null;}
        }
    }

    public boolean rent(Person p, Fahrrad fahrrad) {
        return Database.ausleihen(p, fahrrad);
    }

    public boolean giveBack(Person p, Fahrrad fahrrad, Stellplatz stellplatz) {
        return Database.giveBack(p, fahrrad, stellplatz.getId());
    }

    public int count() {
        int counter = 0;
        for (Stellplatz stellplatz : stellplaetze) {
            counter += Database.countFahrraeder(stellplatz.getId());
        }
        return counter;
    }
}
