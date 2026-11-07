package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Automatically initialize database and tables on startup
        DatabaseInitializer.initializeDatabase();

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // Start the application authentication and routing menu
        Authentication auth = new Authentication(userDao, scanner);
        auth.start();

        scanner.close();
    }
}