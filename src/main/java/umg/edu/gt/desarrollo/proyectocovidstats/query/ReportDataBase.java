package umg.edu.gt.desarrollo.proyectocovidstats.query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class ReportDataBase {
    private static final Logger logger = LogManager.getLogger(ReportDataBase.class);

    public static void main(String[] args) {
        Properties props = new Properties();

        try (InputStream input = ReportDataBase.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("The config.properties file could not be found");
                return;
            }
            props.load(input);
        } catch (IOException ex) {
            logger.error("Error loading configuration file", ex);
            return;
        }

        String url = props.getProperty("spring.datasource.url");
        String user = props.getProperty("spring.datasource.username");
        String password = props.getProperty("spring.datasource.password");
        String countryIso = props.getProperty("app.country-iso");
        String reportDate = props.getProperty("app.report-date");



        String query = "SELECT province, SUM(confirmed) as total_confirmed, " +
                "SUM(deaths) as total_deaths, SUM(recovered) as total_recovered " +
                "FROM report " +
                "WHERE iso = ? AND date = ? " +
                "GROUP BY province " +
                "ORDER BY province";

        try (
                Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, countryIso);
            stmt.setString(2, reportDate);

            ResultSet rs = stmt.executeQuery();

            System.out.println("\n〰️️〰️〰️〰️〰️〰 COVID-19 REPORT 〰️️〰️〰️〰️〰️〰");
            System.out.printf("%-15s: %s%n", "Date", reportDate);
            System.out.println("〰️〰️〰️〰️〰️〰️〰️️〰️〰️〰️〰️〰️〰️️〰️〰️〰️〰️〰️〰");
            System.out.printf("%-15s | %-18s | %-8s | %-11s%n",
                    "PROVINCE", "CONFIRMED CASES", "DEATHS", "RECOVERED");
            System.out.println("〰️〰️〰️〰️〰️〰️〰️️〰️〰️〰️〰️〰️〰️️〰️〰️〰️〰️〰️〰️️〰️〰️〰️〰️〰️〰️");

            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                String province = rs.getString("province");
                int confirmedCases = rs.getInt("total_confirmed");
                int deaths = rs.getInt("total_deaths");
                int recovered = rs.getInt("total_recovered");

                System.out.printf("%-15s | %-18d | %-8d | %-11d%n",
                        province, confirmedCases, deaths, recovered);
            }

            if (!hasResults) {
                System.out.println("No data found for the specified date and country.");
            }

            System.out.println("❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎❎");

        } catch (SQLException e) {
            logger.error("Error querying the database:" + e.getMessage());
        }
    }
}