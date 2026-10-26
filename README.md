# Was habe ich umgesetzt, und wie starte ich das Programm?
Ich habe den Fahrrad verleih als Konsolen Programm mit einer persistenten SQLite Datenbank umgesetzt. In der Main.java können Stellplätzen Fahrräder hinzugefügt. Diese können dann über den verleih.rent() ausgeliehen und über das verleih.giveBack() zurückgegeben werden.
Die Fahrräder können über verleich.search() mit den Jeweiligen SearchOptions nach Marke, Typ, Rahmengröße und Akku gesucht werden.
Man kann das Programm mit `./gradlew run` starten.

# Welche Entscheidungen habe ich getroffen, und warum?
Ich habe mich dazu entschieden den Verleih Persistent zu halten, damit neue Änderungen auch bestehen bleiben. (Stand abgabe ist in der Main.java eine `        Database.insertSampleData();` welche die Alten Daten löscht und Testdaten hinzufügt, damit das Testen einfacher gestaltet ist.)
über die JavaKlassen können Personen eine Fahrrad aus einen Stellplatz nehmen und wieder zurück geben. Zudem können Stellplätze von sich aus Fahrräder hinzufügen welche dann ausgeliehen werden können. Dies deckt die Basis Funktionen ab. 
Zudem kann dann noch die Anzahl aller belegten Stellplätze ausgegeben werden.


# Was würde ich als Nächstes machen?
Das Datenbankenmodell und die Javaklassen haben aus Zeitgründen leichte ungleichheiten. Hier würde ich die Javaklassen nochmal nach den Datenbankenmodellen aufbauen. Mir ist auch erst später aufgefallen das die Stellplätzee auch noch ein extra Objekt sind, weshab ich diese gerne überarbeiten würde aber leider aufgrund der Zeit dies nicht schaffen konnte.