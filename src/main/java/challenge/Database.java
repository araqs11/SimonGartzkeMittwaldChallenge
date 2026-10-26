package challenge;

import challenge.types.Person;
import challenge.types.RadTypes;
import challenge.types.RahmenTypes;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static String DB_FILE = "fahrraeder.db";
    private static String URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    private static boolean isStellplatzVoll(SQLException e) {
        return e.getMessage() != null && e.getMessage().contains("Stellplatz voll");
    }

    // Call once at program start
    public static void init() {
        try (Connection con = connect();
             Statement st = con.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS Person (
                    id       INTEGER PRIMARY KEY,
                    vorname  TEXT NOT NULL,
                    nachname TEXT NOT NULL
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS Stellplatz (
                    id             INTEGER PRIMARY KEY,
                    max_fahrraeder INTEGER NOT NULL CHECK (max_fahrraeder > 0)
                )""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS Fahrrad (
                    id            INTEGER PRIMARY KEY,
                    marke         TEXT NOT NULL,
                    typ           TEXT,
                    rahmen        TEXT,
                    akku          INTEGER,
                    person_id     INTEGER,
                    stellplatz_id INTEGER,
                    FOREIGN KEY (person_id) REFERENCES Person(id)
                        ON UPDATE CASCADE
                        ON DELETE SET NULL,
                    FOREIGN KEY (stellplatz_id) REFERENCES Stellplatz(id)
                        ON UPDATE CASCADE
                        ON DELETE SET NULL
                )""");


            st.execute("CREATE INDEX IF NOT EXISTS idx_fahrrad_person ON Fahrrad(person_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_fahrrad_stellplatz ON Fahrrad(stellplatz_id)");

            // Limit per Stellplatz: reject new bikes / moves into a full Stellplatz
            st.execute("""
                CREATE TRIGGER IF NOT EXISTS trg_stellplatz_limit_insert
                BEFORE INSERT ON Fahrrad
                WHEN NEW.stellplatz_id IS NOT NULL
                BEGIN
                    SELECT RAISE(ABORT, 'Stellplatz voll')
                    WHERE (SELECT COUNT(*) FROM Fahrrad WHERE stellplatz_id = NEW.stellplatz_id)
                       >= (SELECT max_fahrraeder FROM Stellplatz WHERE id = NEW.stellplatz_id);
                END""");

            st.execute("""
                CREATE TRIGGER IF NOT EXISTS trg_stellplatz_limit_update
                BEFORE UPDATE OF stellplatz_id ON Fahrrad
                WHEN NEW.stellplatz_id IS NOT NULL
                 AND NEW.stellplatz_id IS NOT OLD.stellplatz_id
                BEGIN
                    SELECT RAISE(ABORT, 'Stellplatz voll')
                    WHERE (SELECT COUNT(*) FROM Fahrrad WHERE stellplatz_id = NEW.stellplatz_id)
                       >= (SELECT max_fahrraeder FROM Stellplatz WHERE id = NEW.stellplatz_id);
                END""");

            System.out.println("Database: " + new File(DB_FILE).getAbsolutePath());

        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to initialize the database", e);
        }
    }

    public static void insertSampleData() {
        try (Connection con = connect();
             Statement st = con.createStatement()) {

            con.setAutoCommit(false);   // all statements as one transaction

            // Clear old data (bikes first, because they reference Person and Stellplatz)
            st.executeUpdate("DELETE FROM Fahrrad");
            st.executeUpdate("DELETE FROM Stellplatz");
            st.executeUpdate("DELETE FROM Person");

            st.executeUpdate("""
                INSERT INTO Person (id, vorname, nachname) VALUES
                    (1, 'Max', 'Muster'),
                    (2, 'Ben', 'Beispiel')""");

            st.executeUpdate("""
                INSERT INTO Stellplatz (id, max_fahrraeder) VALUES
                    (1, 3),
                    (2, 2)""");

            st.executeUpdate("""
                INSERT INTO Fahrrad (id, marke, typ, rahmen, akku, stellplatz_id) VALUES
                    (1, 'Cube',    'e-bike',   'M', 80, 1),
                    (2, 'Canyon',  'mountain', 'L', 0,  1),
                    (3, 'Giant',   'bike',     'S', 0,  1),
                    (4, 'Cube',    'mountain', 'M', 0,  2),
                    (5, 'Stevens', 'e-bike',   'L', 45, 2)""");

            con.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to insert sample data", e);
        }
    }

    // Creates a Stellplatz and returns its id
    public static int addStellplatz(int maxFahrraeder) {
        String sql = "INSERT INTO Stellplatz (max_fahrraeder) VALUES (?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maxFahrraeder);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding stellplatz with limit: " + maxFahrraeder, e);
        }
    }


    // New bike at a Stellplatz (stellplatzId may be null). Throws if the Stellplatz is full.
    public static Fahrrad addFahrrad(String marke, RadTypes typ, RahmenTypes rahmen, int akku, Integer stellplatzId) {
        String sql = "INSERT INTO Fahrrad (marke, typ, rahmen, akku, stellplatz_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marke);
            ps.setString(2, typ.name());
            ps.setString(3, rahmen.name());
            if (typ.equals(RadTypes.EBike)) {
                ps.setInt(4, akku);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (stellplatzId != null) {
                ps.setInt(5, stellplatzId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Fahrrad(marke, typ, rahmen, akku, keys.getInt(1));
            }
        } catch (SQLException e) {
            if (isStellplatzVoll(e)) {
                throw new IllegalStateException("Stellplatz " + stellplatzId + " is full", e);
            }
            throw new RuntimeException("Error adding fahrrad", e);
        }
    }

    // Number of bikes currently at a Stellplatz
    public static int countFahrraeder(int stellplatzId) {
        String sql = "SELECT COUNT(*) FROM Fahrrad WHERE stellplatz_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stellplatzId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error when counting Fahrrad at Stellplatz: " + stellplatzId, e);
        }
    }

    // Renting frees the Stellplatz spot, because the bike leaves it.
    // Only possible if the bike is free and (for e-bikes) the battery is above 20.
    public static boolean ausleihen(Person p, Fahrrad fahrrad) {
        String sql = """
            UPDATE Fahrrad
            SET person_id = ?, stellplatz_id = NULL
            WHERE id = ?
              AND person_id IS NULL
              AND (typ IS NOT 'e-bike' OR akku > 20)
              AND stellplatz_id IS NOT NULL""";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.id());
            ps.setInt(2, fahrrad.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to assign Fahrrad: " + fahrrad.getId() + " to Person: " + p.vorname(), e);
        }
    }

    // Give back and place the bike at a Stellplatz. false if the bike does not belong
    // to the person or the Stellplatz is full (then the bike stays rented).
    public static boolean giveBack(Person p, Fahrrad fahrrad, int stellplatzId) {
        String sql = "UPDATE Fahrrad SET person_id = NULL, stellplatz_id = ? WHERE id = ? AND person_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stellplatzId);
            ps.setInt(2, fahrrad.getId());
            ps.setInt(3, p.id());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (isStellplatzVoll(e)) {
                return false;
            }
            throw new RuntimeException("Error when trying to giveBack Fahrrad: " + fahrrad.getId()
                    + " to Stellplatz: " + stellplatzId, e);
        }
    }

    public static List<Fahrrad> searchByMarke(String marke) {
        String sql = "SELECT * FROM Fahrrad WHERE marke LIKE ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, marke);
            return readFahrraeder(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to search Fahrrad by marke: " + marke, e);
        }
    }

    public static List<Fahrrad> searchByTyp(String typ) {
        String sql = "SELECT * FROM Fahrrad WHERE typ LIKE ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, typ);
            return readFahrraeder(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to search Fahrrad by typ: " + typ, e);
        }
    }

    public static List<Fahrrad> searchByRahmen(String rahmen) {
        String sql = "SELECT * FROM Fahrrad WHERE rahmen = ? COLLATE NOCASE";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rahmen);
            return readFahrraeder(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to search Fahrrad by rahmen: " + rahmen, e);
        }
    }

    public static List<Fahrrad> searchByAkku(int minAkku) {
        String sql = "SELECT * FROM Fahrrad WHERE akku >= ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, minAkku);
            return readFahrraeder(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error when trying to search Fahrrad by akku: " + minAkku, e);
        }
    }

    // Shared helper: executes the query and maps each row to a Fahrrad
    private static List<Fahrrad> readFahrraeder(PreparedStatement ps) throws SQLException {
        List<Fahrrad> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Fahrrad(
                        rs.getString("marke"),
                        RadTypes.getByName(rs.getString("typ")),
                        RahmenTypes.getByName(rs.getString("rahmen")),
                        rs.getInt("akku"),
                        rs.getInt("id")
                ));
            }
        }
        return result;
    }
}