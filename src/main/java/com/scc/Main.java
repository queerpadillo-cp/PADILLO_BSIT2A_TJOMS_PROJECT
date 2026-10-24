package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database & Tables
        DatabaseInitializer.initializeTables();

        // 2. Instantiate DAOs & Shared Scanner
        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        // 3. Delegate execution to Authentication UI handler
        Authentication auth = new Authentication(userDao, scanner);
        auth.start();

        scanner.close();
    }
}