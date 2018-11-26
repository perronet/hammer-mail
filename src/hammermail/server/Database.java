/*
 * Copyright (C) 2018 gaet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hammermail.server;

import hammermail.core.Mail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Database {
    private static final String URL = "src/test/hammer.db";
    private static final String DB_URL = "jdbc:sqlite:" + URL;

    public Database(boolean dropOld) {
        if (!Files.exists(Paths.get(URL))) {
            createDB();
            createTables();
        } else {
            if (dropOld) {
                dropDB();
                System.out.println("Delete " + URL + " database");
                createDB();
                createTables();
            }
        }
    }

    private void createDB() {
        Connection conn = null;
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            conn = DriverManager.getConnection(DB_URL);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void createTables() {
        String sqlUser = "CREATE TABLE IF NOT EXISTS users (\n"
                // + "	user_id integer AUTO_INCREMENT PRIMARY KEY,\n"
                + " username varchar(255) PRIMARY KEY,\n"
                + " password varchar(255) NOT NULL\n"
                + ");";

        String sqlEmail = "CREATE TABLE IF NOT EXISTS email (\n"
                + " sender varchar(255) NOT NULL,\n"
                + " receiver varchar(255) NOT NULL,\n"
                + " title varchar(255),\n"
                + " email_text text NOT NULL, \n"
                + " time TIMESTAMP NOT NULL,\n"
                + " PRIMARY KEY (sender, receiver, time), \n"
                + " FOREIGN KEY (sender) REFERENCES users(username) ON DELETE CASCADE, \n"
                + " FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE CASCADE\n"
                + ");";
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            stmt.execute(sqlUser);
            stmt.execute(sqlEmail);

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void dropDB() {
        try {
            Files.deleteIfExists(Paths.get(URL));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    protected boolean checkPassword(String userN, String passW) {
        String dbPsw = "";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();
            if (rs.isClosed()) {
                dbPsw = null;
                return false;
            }
            dbPsw = rs.getString("password");

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                if (dbPsw != null)
                    return dbPsw.equals(passW);
                else
                    return false;
            }
        }
    }

    protected void dbAddUser(String userN, String psw) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();

            if (rs.isClosed()) {
                sql = "INSERT INTO users VALUES (?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userN);
                pstmt.setString(2, psw);
                pstmt.executeUpdate();
            } else {
                System.out.println("User already exists");
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    protected void dbRemoveUser(String userN) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "DELETE FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }

    }

    protected void dbAddMail(Mail mail) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "INSERT INTO email VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mail.getSender());
            pstmt.setString(2, mail.getReceiver());
            pstmt.setString(3, mail.getTitle());
            pstmt.setString(4, mail.getText());
            pstmt.setTimestamp(5, mail.getDate());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    protected void dbRemoveMail(Mail mail) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "DELETE FROM email WHERE sender = ? AND "
                    + "receiver = ? AND time = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, mail.getSender());
            pstmt.setString(2, mail.getReceiver());
            pstmt.setTimestamp(3, mail.getDate());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    protected ArrayList<Mail> getRiceivedMail(String userN) {
        ArrayList<Mail> mailList = new ArrayList<>();;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM email WHERE receiver = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Mail m = new Mail(1, 
                        /*rs.getInt("user_id"), */
                        rs.getString("sender"), 
                        rs.getString("receiver"),
                        rs.getString("title"), 
                        rs.getString("email_text"), 
                        rs.getTimestamp("time"));
                mailList.add(m);
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            } finally {
                return mailList;
            }
        }
    }

    protected ArrayList<Mail> getSentMail(String userN) {
        ArrayList<Mail> mailList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL);
            String sql = "SELECT * FROM email WHERE sender = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userN);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Mail m = new Mail(1,
                        /*rs.getInt("user_id"), */
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getString("title"),
                        rs.getString("email_text"),
                        rs.getTimestamp("time"));
                mailList.add(m);
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                rs.close();
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            } finally {
                return mailList;
            }
        }
    }

    protected void resetTables() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM email");

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace(System.out);

        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

}