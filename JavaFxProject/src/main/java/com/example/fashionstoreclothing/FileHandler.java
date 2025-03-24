package com.example.fashionstoreclothing;


import java.io.*;

public class FileHandler {

    private static final String FILE_PATH = "inventory.dat";

    public boolean saveInventory(Inventory inventory) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(inventory);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Inventory loadInventory() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Inventory) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}