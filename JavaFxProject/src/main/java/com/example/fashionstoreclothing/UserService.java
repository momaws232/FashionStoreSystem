package com.example.fashionstoreclothing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserService {
    private static UserService instance;
    private Map<String, User> users;
    private ObservableList<LoginRecord> loginHistory;
    private ObservableList<User> activeUsers;
    private static final String DATA_PATH = "data/";
    private static final String USER_FILE = DATA_PATH + "users.dat";
    private static final String LOGIN_FILE = DATA_PATH + "logins.dat";

    private UserService() {
        // Create data directory if it doesn't exist
        new File(DATA_PATH).mkdirs();

        users = new HashMap<>();
        loginHistory = FXCollections.observableArrayList();
        activeUsers = FXCollections.observableArrayList();

        loadUsersFromDisk();
        loadLoginHistoryFromDisk();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    private void loadUsersFromDisk() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            users = (Map<String, User>) ois.readObject();
            System.out.println("Loaded " + users.size() + " users");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Creating default users");
            // Create default users if file doesn't exist
            createDefaultUsers();
        }
    }

    private void saveUsersToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
            System.out.println("Saved " + users.size() + " users");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLoginHistoryFromDisk() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LOGIN_FILE))) {
            List<LoginRecord> records = (List<LoginRecord>) ois.readObject();
            loginHistory.addAll(records);
            System.out.println("Loaded " + loginHistory.size() + " login records");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous login history found");
        }
    }

    private void saveLoginHistoryToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LOGIN_FILE))) {
            oos.writeObject(new ArrayList<>(loginHistory));
            System.out.println("Saved " + loginHistory.size() + " login records");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultUsers() {
        users.put("admin", new User("admin", "admin123", true));
        users.put("user1", new User("user1", "password", false));
        saveUsersToDisk();
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            LoginRecord record = new LoginRecord(username, LocalDateTime.now(), "127.0.0.1");
            loginHistory.add(record);
            saveLoginHistoryToDisk();

            if (!activeUsers.contains(user)) {
                user.setActive(true);
                activeUsers.add(user);
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public void recordLogout(String username) {
        User user = users.get(username);
        if (user != null) {
            user.setActive(false);
            activeUsers.remove(user);

            loginHistory.stream()
                    .filter(r -> r.getUsername().equals(username) && r.getLogoutTime() == null)
                    .findFirst()
                    .ifPresent(r -> {
                        r.setLogoutTime(LocalDateTime.now());
                        saveLoginHistoryToDisk();
                    });
        }
    }

    public boolean registerUser(String username, String password, boolean isAdmin) {
        if (users.containsKey(username)) return false;

        users.put(username, new User(username, password, isAdmin));
        saveUsersToDisk();
        return true;
    }

    public ObservableList<LoginRecord> getLoginHistory() {
        return FXCollections.unmodifiableObservableList(loginHistory);
    }

    public ObservableList<User> getActiveUsers() {
        return FXCollections.unmodifiableObservableList(activeUsers);
    }
}