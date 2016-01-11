
package org.tfabric.samples;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.DriverManager;

public class OracleSqlServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(200);
        PrintWriter writer = response.getWriter();
        writer.println("Oracle with Java \n");

        String vcap_services = System.getenv("VCAP_SERVICES");

        Connection dbConnection = null;

        if (vcap_services != null && vcap_services.length() > 0) {
            try {
                // Use a JSON parser to get the info we need from  the
                // VCAP_SERVICES environment variable. This variable contains
                // credentials for all services bound to the application.
                // In this case, MySQL is the only bound service.
                JsonRootNode root = new JdomParser().parse(vcap_services);

                JsonNode oracleNode = root.getNode("oracle1");
                JsonNode credentials = oracleNode.getNode(0).getNode("credentials");

                // Grab login info for MySQL from the credentials node
                String hostname = credentials.getStringValue("hostname");
                String user = credentials.getStringValue("username");
                String password = credentials.getStringValue("password");
                String port = credentials.getStringValue("port");
                String oraService = credentials.getStringValue("servicename");
                
                System.out.println("hostname: " + hostname);
                System.out.println("username: " + user);
                System.out.println("password: " + password);
                System.out.println("port: " + port);
                System.out.println("servicename: " + oraService);
                
                String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST="+hostname+") (PORT="+port+"))(CONNECT_DATA=(SERVICE_NAME="+oraService+")))";

                // Connect to Oracle
                writer.println("Connecting to Oracle...");

                Class.forName("oracle.jdbc.driver.OracleDriver");
                dbConnection = DriverManager.getConnection(dbUrl, user, password);
            } catch (Exception e) {
                System.out.println("Caught error: ");
                e.printStackTrace();
            }
        }

        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                writer.println("Connected to Oracle!");

                // creating a database table and populating some values
                Statement statement = dbConnection.createStatement();

                ResultSet rs = statement.executeQuery("SELECT \"Hello World!\"");
                writer.println("Executed query \"SELECT \"Hello World!\"\".");

                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();

                while (rs.next()) {
                    for (int i = 1; i <= columnsNumber; i++) {
                        if (i > 1) System.out.print(",  ");
                        String columnValue = rs.getString(i);

                        // Since we are selecting a string literal, the column
                        // value and column name are both the same. The values
                        // could be retrieved with the line commented out below.
                        //writer.println("Column value: " + columnValue + " column name " + rsmd.getColumnName(i));

                        writer.println("Result: " + columnValue);
                    }
                }

                statement.close();
            } else {
                writer.println("Failed to connect to Oracle");
            }
        }
        catch (Exception e) {
            writer.println("Exception caught while executing DB queries.");
        }

        writer.close();
    }
}
