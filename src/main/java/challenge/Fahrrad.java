package challenge;

import challenge.types.RadTypes;
import challenge.types.RahmenTypes;

public class Fahrrad {


    private String marke;
    private RadTypes typ;
    private RahmenTypes rahmen;
    private int akku;
    private int id;

    public Fahrrad(String marke, RadTypes typ, RahmenTypes rahmen, int akku, int id) {
        this.marke = marke;
        this.typ = typ;
        this.rahmen = rahmen;
        this.akku = akku;
        this.id = id;
    }

    public Fahrrad(String marke, RadTypes typ, RahmenTypes rahmen, int akku) {
        this.marke = marke;
        this.typ = typ;
        this.rahmen = rahmen;
        this.akku = akku;
    }

    public String getMarke() {
        return marke;
    }

    public RadTypes getTyp() {
        return typ;
    }

    public RahmenTypes getRahmen() {
        return rahmen;
    }

    public int getAkku() {
        return akku;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return marke + ", " + typ + ", " + rahmen + ", " + akku + ", " + id;
    }
}
