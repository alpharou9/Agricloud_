package esprit.rania.config;

public class DatabaseConfig {
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "3306";
    public static final String DB_NAME = "javablog";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    public static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
                    "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
}
