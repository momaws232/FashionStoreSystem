package com.example.fashionstoreclothing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_DIR = System.getProperty("user.home") + File.separator + "FashionStore";
    private static final String LOG_FILE = LOG_DIR + File.separator + "application.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final String className;
    
    public Logger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
        initLogFile();
    }
    
    private void initLogFile() {
        try {
            // Create log directory if it doesn't exist
            Files.createDirectories(Paths.get(LOG_DIR));
            
            // Create log file if it doesn't exist
            File logFile = new File(LOG_FILE);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }
    
    public void info(String message) {
        log("INFO", message);
    }
    
    public void warn(String message) {
        log("WARNING", message);
    }
    
    public void error(String message) {
        log("ERROR", message);
    }
    
    public void error(String message, Throwable throwable) {
        log("ERROR", message);
        logThrowable(throwable);
    }
    
    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logMessage = String.format("[%s] [%s] %s - %s", timestamp, level, className, message);
        
        // Print to console
        System.out.println(logMessage);
        
        // Write to file
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    private void logThrowable(Throwable throwable) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            throwable.printStackTrace(out);
        } catch (IOException e) {
            System.err.println("Failed to write stack trace to log file: " + e.getMessage());
        }
        
        // Also print to console
        throwable.printStackTrace(System.err);
    }
}