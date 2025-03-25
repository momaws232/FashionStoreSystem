package com.example.fashionstoreclothing;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    // Store in user's home directory or application data directory
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + "FashionStore";
    private static final String FILE_PATH = DATA_DIR + File.separator + "inventory.dat";

    public boolean saveInventory(Inventory inventory) {
        try {
            // Ensure directory exists
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("Created data directory: " + DATA_DIR);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
                oos.writeObject(inventory);
                System.out.println("Inventory saved successfully");
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Inventory loadInventory() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("Inventory file not found, creating new inventory");
            return new Inventory();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Inventory inventory = (Inventory) ois.readObject();
            System.out.println("Inventory loaded successfully with " + inventory.getItems().size() + " items");
            return inventory;
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            e.printStackTrace();
            return new Inventory(); // Return empty inventory on error
        } catch (ClassNotFoundException e) {
            System.err.println("Class definition has changed: " + e.getMessage());
            e.printStackTrace();
            return new Inventory();
        }
    }
}