package com.fashionstore.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.Random;

import com.fashionstore.ai.OutfitMatcher;
import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.Outfit;
import com.fashionstore.models.Product;
import com.fashionstore.storage.DataManager;
import com.fashionstore.ui.components.ClothingItemView;
import com.fashionstore.utils.SceneManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.application.Platform;

public class AIRecommendationsController implements Initializable {

    @FXML
    private VBox recommendationsContainer;
    @FXML
    private Button generateButton;
    @FXML
    private ComboBox<String> styleComboBox;
    @FXML
    private ComboBox<String> seasonComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private DataManager dataManager;
    private OutfitMatcher outfitMatcher;
    private List<Outfit> currentRecommendations = new ArrayList<>();
    private Map<String, String> categoryIconMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataManager = FashionStoreApp.getDataManager();
        outfitMatcher = new OutfitMatcher();

        if (dataManager.getCurrentUser() == null) {
            return;
        }
        
        // Initialize style preferences
        setupStyleOptions();
        
        // Initialize season options
        setupSeasonOptions();
        
        // Set up category icons
        initializeCategoryIcons();
        
        // Set initial status
        statusLabel.setText("Select preferences and click Generate to create outfit recommendations");
        progressIndicator.setVisible(false);
    }
    
    private void setupStyleOptions() {
        styleComboBox.getItems().addAll(
            "Any Style",
            "Casual",
            "Formal",
            "Business",
            "Athletic",
            "Evening",
            "Streetwear",
            "Vintage",
            "Bohemian",
            "Minimalist",
            "Preppy"
        );
        styleComboBox.setValue("Any Style");
    }
    
    private void setupSeasonOptions() {
        seasonComboBox.getItems().addAll(
            "Current Season",
            "Spring",
            "Summer",
            "Fall",
            "Winter"
        );
        seasonComboBox.setValue("Current Season");
    }
    
    private void initializeCategoryIcons() {
        // Map categories to icon paths - these should exist in your resources folder
        categoryIconMap.put("Tops", "/icons/top.png");
        categoryIconMap.put("Bottoms", "/icons/bottom.png");
        categoryIconMap.put("Dresses", "/icons/dress.png");
        categoryIconMap.put("Footwear", "/icons/shoes.png");
        categoryIconMap.put("Outerwear", "/icons/jacket.png");
        categoryIconMap.put("Accessories", "/icons/accessory.png");
    }

    @FXML
    private void generateRecommendations() {
        // Clear current recommendations
        currentRecommendations.clear();
        recommendationsContainer.getChildren().clear();
        
        // Show progress indicator
        progressIndicator.setVisible(true);
        statusLabel.setText("Generating outfit recommendations...");
        generateButton.setDisable(true);
        
        // Get user's wardrobe items
        List<Product> wardrobeItems = dataManager.getUserWardrobe(
                dataManager.getCurrentUser().getUserId());

        if (wardrobeItems.isEmpty()) {
            showEmptyWardrobeMessage();
            progressIndicator.setVisible(false);
            generateButton.setDisable(false);
            return;
        }

        // Process in background to avoid freezing UI
        Thread generationThread = new Thread(() -> {
            try {
                List<Outfit> generatedOutfits = new ArrayList<>();
                
                try {
                    // Try to generate outfits using OutfitMatcher
                    int maxAttempts = 10; // Allow more attempts to find diverse outfits
                    int outfitsGenerated = 0;
                    
                    for (int i = 0; i < maxAttempts && outfitsGenerated < 5; i++) {
                        Outfit recommendation = outfitMatcher.generateOutfit(
                                dataManager.getCurrentUser(), wardrobeItems);
                        
                        if (recommendation != null && !recommendation.getProductIds().isEmpty()) {
                            // Check if this recommendation is sufficiently different from existing ones
                            if (isUnique(recommendation, generatedOutfits)) {
                                generatedOutfits.add(recommendation);
                                outfitsGenerated++;
                            }
                        }
                    }
                } catch (Error e) {
                    // OutfitMatcher has compilation issues, use fallback method
                    System.err.println("Error using OutfitMatcher: " + e.getMessage());
                    e.printStackTrace();
                    generatedOutfits = generateFallbackOutfits(wardrobeItems);
                }
                
                // Store the recommendations
                currentRecommendations.addAll(generatedOutfits);
                
                // Update UI on the JavaFX thread
                final List<Outfit> finalOutfits = generatedOutfits;
                Platform.runLater(() -> {
                    if (finalOutfits.isEmpty()) {
                        statusLabel.setText("Couldn't generate recommendations. Try adding more variety to your wardrobe.");
                    } else {
                        // Display all recommendations
                        for (int i = 0; i < finalOutfits.size(); i++) {
                            addOutfitRecommendation(finalOutfits.get(i), i + 1);
                            
                            // Add separator unless it's the last recommendation
                            if (i < finalOutfits.size() - 1) {
                                recommendationsContainer.getChildren().add(new Separator());
                            }
                        }
                        statusLabel.setText("Created " + finalOutfits.size() + " outfit recommendations");
                    }
                    
                    // Hide progress indicator and re-enable button
                    progressIndicator.setVisible(false);
                    generateButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error generating recommendations: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    generateButton.setDisable(false);
                });
                e.printStackTrace();
            }
        });
        
        generationThread.setDaemon(true);
        generationThread.start();
    }
    
    /**
     * Check if the new outfit is sufficiently different from existing ones
     */
    private boolean isUnique(Outfit newOutfit, List<Outfit> existingOutfits) {
        for (Outfit existing : existingOutfits) {
            int commonItems = 0;
            for (String productId : newOutfit.getProductIds()) {
                if (existing.getProductIds().contains(productId)) {
                    commonItems++;
                }
            }
            
            // If more than 50% of items are the same, consider it a duplicate
            if ((double) commonItems / newOutfit.getProductIds().size() > 0.5) {
                return false;
            }
        }
        return true;
    }

    private void addOutfitRecommendation(Outfit outfit, int index) {
        BorderPane outfitBox = new BorderPane();
        outfitBox.setPadding(new Insets(15));
        outfitBox.getStyleClass().add("recommendation-box");
        
        // Create header with outfit name
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label numberLabel = new Label("#" + index);
        numberLabel.getStyleClass().add("recommendation-number");
        
        Label titleLabel = new Label(outfit.getName());
        titleLabel.getStyleClass().add("recommendation-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        String occasionText = outfit.getOccasion().toString().charAt(0) + 
                              outfit.getOccasion().toString().substring(1).toLowerCase();
        String seasonText = outfit.getSeason().toString().charAt(0) + 
                            outfit.getSeason().toString().substring(1).toLowerCase();
        
        Label detailsLabel = new Label(occasionText + " • " + seasonText);
        detailsLabel.getStyleClass().add("recommendation-details");
        
        headerBox.getChildren().addAll(numberLabel, titleLabel, detailsLabel);
        outfitBox.setTop(headerBox);
        
        // Items container
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(15, 0, 10, 0));
        
        // Categorized items display
        Map<String, List<Product>> categorizedItems = categorizeOutfitItems(outfit);
        
        for (Map.Entry<String, List<Product>> entry : categorizedItems.entrySet()) {
            String category = entry.getKey();
            List<Product> items = entry.getValue();
            
            if (!items.isEmpty()) {
                // Create category header
                HBox categoryHeader = new HBox(8);
                categoryHeader.setAlignment(Pos.CENTER_LEFT);
                
                // Add category icon if available
                if (categoryIconMap.containsKey(category)) {
                    try {
                        Image icon = new Image(getClass().getResourceAsStream(categoryIconMap.get(category)));
                        ImageView iconView = new ImageView(icon);
                        iconView.setFitHeight(16);
                        iconView.setFitWidth(16);
                        categoryHeader.getChildren().add(iconView);
                    } catch (Exception e) {
                        System.out.println("Could not load icon for category: " + category);
                    }
                }
                
                Label categoryLabel = new Label(category);
                categoryLabel.getStyleClass().add("category-label");
                categoryHeader.getChildren().add(categoryLabel);
                
                contentBox.getChildren().add(categoryHeader);
                
                // Create flow pane for items in this category
                FlowPane itemsPane = new FlowPane(10, 10);
                itemsPane.setAlignment(Pos.CENTER_LEFT);
                
                for (Product product : items) {
                    ClothingItemView itemView = new ClothingItemView(product);
                    itemView.setSelected(true);  // Show as selected to highlight in the outfit
                    itemsPane.getChildren().add(itemView);
                }
                
                contentBox.getChildren().add(itemsPane);
                contentBox.getChildren().add(new Separator());
            }
        }
        
        outfitBox.setCenter(contentBox);
        
        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(5, 0, 0, 0));

        Button saveButton = new Button("Save Outfit");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(e -> saveRecommendation(outfit));
        
        Button tryOnButton = new Button("Try On");
        tryOnButton.getStyleClass().add("secondary-button");
        tryOnButton.setOnAction(e -> openOutfitCreator(outfit));

        actionBox.getChildren().addAll(tryOnButton, saveButton);
        outfitBox.setBottom(actionBox);

        // Add to container
        recommendationsContainer.getChildren().add(outfitBox);
    }
    
    /**
     * Categorize outfit items for display
     */
    private Map<String, List<Product>> categorizeOutfitItems(Outfit outfit) {
        Map<String, List<Product>> categorized = new HashMap<>();
        categorized.put("Tops", new ArrayList<>());
        categorized.put("Bottoms", new ArrayList<>());
        categorized.put("Dresses", new ArrayList<>());
        categorized.put("Footwear", new ArrayList<>());
        categorized.put("Outerwear", new ArrayList<>());
        categorized.put("Accessories", new ArrayList<>());
        
        for (String productId : outfit.getProductIds()) {
            Product product = dataManager.getProduct(productId);
            if (product != null) {
                String category = determineCategory(product);
                categorized.get(category).add(product);
            }
        }
        
        return categorized;
    }
    
    /**
     * Determine which category a product belongs to
     */
    private String determineCategory(Product product) {
        String category = product.getCategory().toLowerCase();
        String subcategory = product.getSubcategory() != null ? product.getSubcategory().toLowerCase() : "";
        String description = product.getDescription() != null ? product.getDescription().toLowerCase() : "";
        
        if (category.contains("dress") || subcategory.contains("dress") || description.contains("dress")) {
            return "Dresses";
        } else if (category.contains("top") || category.contains("shirt") || category.contains("blouse") || 
                category.contains("sweater") || category.contains("tee") || subcategory.contains("top")) {
            return "Tops";
        } else if (category.contains("pant") || category.contains("jean") || category.contains("skirt") || 
                category.contains("short") || category.contains("bottom") || subcategory.contains("bottom")) {
            return "Bottoms";
        } else if (category.contains("shoe") || category.contains("boot") || category.contains("sneaker") || 
                category.contains("sandal") || category.contains("footwear")) {
            return "Footwear";
        } else if (category.contains("jacket") || category.contains("coat") || category.contains("hoodie") || 
                category.contains("blazer") || category.contains("cardigan")) {
            return "Outerwear";
        } else if (category.contains("accessory") || category.contains("jewelry") || category.contains("hat") || 
                category.contains("scarf") || category.contains("belt") || category.contains("bag") || 
                category.contains("watch") || category.contains("glasses")) {
            return "Accessories";
        } else {
            // Try to determine from description
            if (description.contains("shirt") || description.contains("top") || description.contains("blouse")) {
                return "Tops";
            } else if (description.contains("pant") || description.contains("jean") || description.contains("skirt")) {
                return "Bottoms";
            } else if (description.contains("boot") || description.contains("sneaker") || description.contains("shoe")) {
                return "Footwear";
            } else {
                return "Accessories";  // Default when we can't determine
            }
        }
    }

    private void saveRecommendation(Outfit outfit) {
        // Save the outfit to user's collection
        dataManager.addOutfit(outfit);
        dataManager.getCurrentUser().addOutfit(outfit.getOutfitId());
        dataManager.saveAllData();

        SceneManager.showAlert("Outfit Saved",
                "The outfit \"" + outfit.getName() + "\" has been saved to your collection!");
    }
    
    private void openOutfitCreator(Outfit outfit) {
        // TODO: Implement opening the outfit in the outfit creator
        // This would require passing the outfit to the outfit creator window
        SceneManager.showAlert("Coming Soon",
                "Try-on feature will be available in the next update.");
    }

    private void showEmptyWardrobeMessage() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(40));
        messageBox.getStyleClass().add("empty-message-box");
        
        Label emptyLabel = new Label("Your wardrobe is empty!");
        emptyLabel.getStyleClass().add("warning-title");
        
        Label infoLabel = new Label("Add some items to your wardrobe to get AI outfit recommendations.");
        infoLabel.getStyleClass().add("info-text");
        
        Button shopButton = new Button("Shop Now");
        shopButton.getStyleClass().add("primary-button");
        shopButton.setOnAction(e -> backToShopping());
        
        messageBox.getChildren().addAll(emptyLabel, infoLabel, shopButton);
        recommendationsContainer.getChildren().add(messageBox);
        
        statusLabel.setText("Add items to your wardrobe to generate outfits");
    }

    /**
     * Handles the Back to Shopping button click
     */
    @FXML
    public void backToShopping() {
        // Close this window
        javafx.stage.Stage stage = (javafx.stage.Stage) recommendationsContainer.getScene().getWindow();
        stage.close();
    }

    /**
     * Generate simple outfits when OutfitMatcher has issues
     */
    private List<Outfit> generateFallbackOutfits(List<Product> wardrobeItems) {
        List<Outfit> outfits = new ArrayList<>();
        
        if (wardrobeItems.size() < 3) {
            return outfits; // Not enough items to create outfits
        }
        
        // Categorize items
        Map<String, List<Product>> categorizedItems = new HashMap<>();
        categorizedItems.put("Tops", new ArrayList<>());
        categorizedItems.put("Bottoms", new ArrayList<>());
        categorizedItems.put("Footwear", new ArrayList<>());
        categorizedItems.put("Outerwear", new ArrayList<>());
        categorizedItems.put("Accessories", new ArrayList<>());
        
        for (Product item : wardrobeItems) {
            String category = determineCategory(item);
            if (categorizedItems.containsKey(category)) {
                categorizedItems.get(category).add(item);
            }
        }
        
        // Create the outfits
        String[] themes = {"Casual", "Formal", "Business", "Athletic", "Streetwear"};
        String[] seasons = {"Spring", "Summer", "Fall", "Winter"};
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) { // Try to create up to 5 outfits
            if (!categorizedItems.get("Tops").isEmpty() && 
                !categorizedItems.get("Bottoms").isEmpty() && 
                !categorizedItems.get("Footwear").isEmpty()) {
                
                // Create a new outfit
                String theme = themes[random.nextInt(themes.length)];
                String season = seasons[random.nextInt(seasons.length)];
                String outfitName = theme + " " + season + " Outfit";
                
                Outfit outfit = new Outfit(dataManager.getCurrentUser().getUserId(), outfitName);
                outfit.setOutfitId(UUID.randomUUID().toString());
                outfit.setAiGenerated(true);
                
                // Set season
                Outfit.OutfitSeason outfitSeason = Outfit.OutfitSeason.ALL_SEASON;
                if (season.equals("Spring")) outfitSeason = Outfit.OutfitSeason.SPRING;
                else if (season.equals("Summer")) outfitSeason = Outfit.OutfitSeason.SUMMER;
                else if (season.equals("Fall")) outfitSeason = Outfit.OutfitSeason.FALL; 
                else if (season.equals("Winter")) outfitSeason = Outfit.OutfitSeason.WINTER;
                outfit.setSeason(outfitSeason);
                
                // Set occasion
                Outfit.OutfitOccasion outfitOccasion = Outfit.OutfitOccasion.CASUAL;
                if (theme.equals("Formal")) outfitOccasion = Outfit.OutfitOccasion.FORMAL;
                else if (theme.equals("Business")) outfitOccasion = Outfit.OutfitOccasion.WORK;
                else if (theme.equals("Athletic")) outfitOccasion = Outfit.OutfitOccasion.SPORT;
                outfit.setOccasion(outfitOccasion);
                
                // Add a random product from each category
                List<Product> tops = categorizedItems.get("Tops");
                List<Product> bottoms = categorizedItems.get("Bottoms");
                List<Product> footwear = categorizedItems.get("Footwear");
                
                outfit.addProduct(tops.get(random.nextInt(tops.size())).getProductId());
                outfit.addProduct(bottoms.get(random.nextInt(bottoms.size())).getProductId());
                outfit.addProduct(footwear.get(random.nextInt(footwear.size())).getProductId());
                
                // Add outerwear if available (50% chance)
                if (!categorizedItems.get("Outerwear").isEmpty() && random.nextBoolean()) {
                    List<Product> outerwear = categorizedItems.get("Outerwear");
                    outfit.addProduct(outerwear.get(random.nextInt(outerwear.size())).getProductId());
                }
                
                // Add accessory if available (50% chance)
                if (!categorizedItems.get("Accessories").isEmpty() && random.nextBoolean()) {
                    List<Product> accessories = categorizedItems.get("Accessories");
                    outfit.addProduct(accessories.get(random.nextInt(accessories.size())).getProductId());
                }
                
                // Add tags
                outfit.addTag(theme);
                outfit.addTag(season);
                
                // Add to list if it's unique
                if (isUnique(outfit, outfits)) {
                    outfits.add(outfit);
                }
            }
        }
        
        return outfits;
    }
}