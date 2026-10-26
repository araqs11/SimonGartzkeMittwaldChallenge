package challenge;

import challenge.types.*;

import java.sql.*;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        Database.init();
        Database.insertSampleData();
        Fahrradverleih verleih = new Fahrradverleih();

        Stellplatz stellplatz1 = new Stellplatz(1,3);
        Stellplatz stellplatz2 = new Stellplatz(2,2);

        verleih.addStellplatz(stellplatz1);
        verleih.addStellplatz(stellplatz2);

        Person max = new Person("Max","Muster",1);
        Person ben = new Person("Ben","Beispiel",2);

        // 1) Suche nach Marke -> Cube (id 1 und 4)
        List<Fahrrad> cubes = verleih.search(new SearchOptions(SearchOptionsTypes.MARKE, "Cube"));

        // 2) Suche nach Typ -> mountain (id 2 und 4)
        List<Fahrrad> mountain = verleih.search(new SearchOptions(SearchOptionsTypes.TYP, "mountain"));

        // 3) Suche nach Rahmengröße -> M (id 1 und 4)
        List<Fahrrad> mittel = verleih.search(new SearchOptions(SearchOptionsTypes.RAHMEN, "M"));

        // 4) Suche nach Akku (mindestens 50 %) -> nur id 1
        List<Fahrrad> starkeAkkus = verleih.search(new SearchOptions(SearchOptionsTypes.AKKU, "50"));

        System.out.println("Cube count: " + cubes.size());
        System.out.println("mountain count: " + mountain.size());
        System.out.println("M count: " + mittel.size());
        System.out.println("Akku 50+ count: " + starkeAkkus.size());

        for (Fahrrad fahrrad : mittel) {
            System.out.println(fahrrad);
        }

        System.out.println("Anzahl räder: " + verleih.count());
        System.out.println(verleih.rent(max, starkeAkkus.get(0)));
        System.out.println("Anzahl räder: " + verleih.count());
        System.out.println(verleih.rent(ben, starkeAkkus.get(0)));
        System.out.println("Anzahl räder: " + verleih.count());
        System.out.println(verleih.giveBack(ben, starkeAkkus.get(0), stellplatz1));
        System.out.println("Anzahl räder: " + verleih.count());
        System.out.println(verleih.giveBack(max, starkeAkkus.get(0), stellplatz1));
        System.out.println("Anzahl räder: " + verleih.count());
        stellplatz1.addFahrrad(new Fahrrad("Test", RadTypes.Bike, RahmenTypes.M,0)); // sollte nicht gehen, da stellplatz voll ist.
        System.out.println("Anzahl räder: " + verleih.count());

    }
}
