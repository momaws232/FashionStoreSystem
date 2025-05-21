package com.fashionstore.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.fashionstore.models.Outfit;
import com.fashionstore.models.Product;
import com.fashionstore.models.User;

/**
 * Revamped AI-powered outfit generation and matching algorithm
 * Creates complete outfits with greater variety
 */
public class OutfitMatcher {

    // Style themes for outfit creation
    private static final String[] STYLE_THEMES = {
            "Casual", "Formal", "Business", "Athletic", "Evening", "Streetwear", 
            "Vintage", "Bohemian", "Minimalist", "Preppy"
    };

    // Seasonal categories for matching
    private static final String[] SEASONS = {
            "Spring", "Summer", "Fall", "Winter"
    };

    // Category requirements for complete outfits
    private static final Map<String, String[]> REQUIRED_CATEGORIES = new HashMap<>();
    private static final Map<String, String[]> OPTIONAL_CATEGORIES = new HashMap<>();

    // Color compatibility data
    private static final Map<String, List<String>> COLOR_COMPATIBILITY = new HashMap<>();
    
    // Patterns that work well together
    private static final Map<String, List<String>> PATTERN_COMPATIBILITY = new HashMap<>();

    // Store recently generated outfits to avoid duplicates
    private final Set<String> recentOutfitSignatures = new HashSet<>();
    private final Random random = new Random();

    static {
        // Initialize required outfit components by style theme
        REQUIRED_CATEGORIES.put("Casual", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Formal", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Business", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Athletic", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Evening", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Streetwear", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Vintage", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Bohemian", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Minimalist", new String[]{"Tops", "Bottoms", "Footwear"});
        REQUIRED_CATEGORIES.put("Preppy", new String[]{"Tops", "Bottoms", "Footwear"});

        // Initialize optional outfit components by style theme
        OPTIONAL_CATEGORIES.put("Casual", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Formal", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Business", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Athletic", new String[]{"Accessories"});
        OPTIONAL_CATEGORIES.put("Evening", new String[]{"Accessories"});
        OPTIONAL_CATEGORIES.put("Streetwear", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Vintage", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Bohemian", new String[]{"Outerwear", "Accessories"});
        OPTIONAL_CATEGORIES.put("Minimalist", new String[]{"Outerwear"});
        OPTIONAL_CATEGORIES.put("Preppy", new String[]{"Outerwear", "Accessories"});

        // Initialize color compatibility
        COLOR_COMPATIBILITY.put("Black", Arrays.asList("White", "Red", "Blue", "Gray", "Pink", "Green", "Yellow", "Purple", "Orange"));
        COLOR_COMPATIBILITY.put("White", Arrays.asList("Black", "Blue", "Red", "Brown", "Gray", "Navy", "Purple", "Green"));
        COLOR_COMPATIBILITY.put("Blue", Arrays.asList("White", "Gray", "Brown", "Green", "Black", "Pink", "Orange"));
        COLOR_COMPATIBILITY.put("Red", Arrays.asList("Black", "White", "Gray", "Blue", "Yellow", "Brown"));
        COLOR_COMPATIBILITY.put("Green", Arrays.asList("Blue", "Yellow", "Brown", "Gray", "Black", "White"));
        COLOR_COMPATIBILITY.put("Yellow", Arrays.asList("Green", "Blue", "Black", "Gray", "Red"));
        COLOR_COMPATIBILITY.put("Purple", Arrays.asList("White", "Black", "Gray", "Pink", "Teal"));
        COLOR_COMPATIBILITY.put("Pink", Arrays.asList("Blue", "Gray", "Black", "White", "Purple"));
        COLOR_COMPATIBILITY.put("Brown", Arrays.asList("Blue", "Green", "White", "Gray", "Beige"));
        COLOR_COMPATIBILITY.put("Gray", Arrays.asList("Black", "White", "Blue", "Red", "Pink", "Purple", "Navy"));
        COLOR_COMPATIBILITY.put("Navy", Arrays.asList("White", "Gray", "Black", "Red", "Beige"));
        COLOR_COMPATIBILITY.put("Beige", Arrays.asList("Navy", "Brown", "Green", "Blue", "Gray"));
        COLOR_COMPATIBILITY.put("Orange", Arrays.asList("Blue", "Black", "White", "Gray"));
        COLOR_COMPATIBILITY.put("Teal", Arrays.asList("White", "Gray", "Navy", "Purple"));

        // Initialize pattern compatibility
        PATTERN_COMPATIBILITY.put("Solid", Arrays.asList("Solid", "Striped", "Floral", "Plaid", "Polka Dot", "Geometric"));
        PATTERN_COMPATIBILITY.put("Striped", Arrays.asList("Solid", "Plaid"));
        PATTERN_COMPATIBILITY.put("Floral", Arrays.asList("Solid", "Geometric"));
        PATTERN_COMPATIBILITY.put("Plaid", Arrays.asList("Solid", "Striped"));
        PATTERN_COMPATIBILITY.put("Polka Dot", Arrays.asList("Solid"));
        PATTERN_COMPATIBILITY.put("Geometric", Arrays.asList("Solid", "Floral"));
    }

    /**
     * Generates a complete outfit recommendation based on the user's wardrobe items
     * 
     * @param user          The user to generate outfit for
     * @param wardrobeItems The user's wardrobe items
     * @return A recommended outfit, or null if it couldn't be generated
     */
    public Outfit generateOutfit(User user, List<Product> wardrobeItems) {
        if (wardrobeItems == null || wardrobeItems.isEmpty()) {
            return null;
        }

        try {
            // Choose a style theme based on user's items
            String styleTheme = selectStyleTheme(user, wardrobeItems);
            
            // Determine current season
            String season = determineCurrentSeason();
            
            // Categorize wardrobe items
            Map<String, List<Product>> categorizedItems = categorizeItems(wardrobeItems);

            // Check if wardrobe has enough items for a complete outfit
            if (!hasRequiredCategories(categorizedItems, styleTheme)) {
                System.out.println("Not enough items for a complete " + styleTheme + " outfit");
                styleTheme = findViableStyleTheme(categorizedItems);
                if (styleTheme == null) {
                    // Fall back to random selection if no complete outfit can be created
                    return createFallbackOutfit(user, wardrobeItems, season);
                }
            }
            
            // Start building the outfit
            OutfitBuilder builder = new OutfitBuilder(user, styleTheme, season);
            
            // First, handle special case of dresses (they count as both top and bottom)
            if (categorizedItems.containsKey("Dresses") && !categorizedItems.get("Dresses").isEmpty() 
                    && random.nextBoolean()) {
                // 50% chance to use a dress if available
                Product dress = selectItemFromCategory(categorizedItems, "Dresses", null, null);
                builder.addCoreItem(dress);
                // No need to add tops or bottoms if we have a dress
            } else {
                // Add bottoms first (base item)
                Product bottoms = selectItemFromCategory(categorizedItems, "Bottoms", null, null);
                if (bottoms != null) {
                    builder.addCoreItem(bottoms);
                    
                    // Then add a matching top
                    Product top = selectItemFromCategory(categorizedItems, "Tops", bottoms, styleTheme);
                    if (top != null) {
                        builder.addCoreItem(top);
                    }
                }
            }
            
            // Add footwear
            Product shoes = selectItemFromCategory(categorizedItems, "Footwear", builder.getPrimaryColorItem(), styleTheme);
            if (shoes != null) {
                builder.addCoreItem(shoes);
            }
            
            // Add outerwear if appropriate for the season and style
            if (shouldAddOuterwear(season, styleTheme)) {
                Product outerwear = selectItemFromCategory(categorizedItems, "Outerwear", builder.getPrimaryColorItem(), styleTheme);
                if (outerwear != null) {
                    builder.addOptionalItem(outerwear);
                }
            }
            
            // Add accessories
            if (categorizedItems.containsKey("Accessories") && !categorizedItems.get("Accessories").isEmpty()) {
                Product accessory = selectItemFromCategory(categorizedItems, "Accessories", builder.getPrimaryColorItem(), styleTheme);
            if (accessory != null) {
                    builder.addOptionalItem(accessory);
                }
            }
            
            // Build the final outfit
            Outfit outfit = builder.build();
            
            // Check if this is too similar to recently generated outfits
            String outfitSignature = generateOutfitSignature(outfit, wardrobeItems);
            if (recentOutfitSignatures.contains(outfitSignature)) {
                // Try one more time with different selections
                return generateOutfit(user, wardrobeItems);
            }
            
            // Add to recent outfits to avoid duplication
            recentOutfitSignatures.add(outfitSignature);
            if (recentOutfitSignatures.size() > 10) {
                // Only keep the 10 most recent signatures
                recentOutfitSignatures.clear(); 
            }

            return outfit;
        } catch (Exception e) {
            System.err.println("Error generating outfit: " + e.getMessage());
            e.printStackTrace();
            return createFallbackOutfit(user, wardrobeItems, determineCurrentSeason());
        }
    }

    /**
     * Determine if the wardrobe has enough items to create an outfit for the given style
     */
    private boolean hasRequiredCategories(Map<String, List<Product>> categorizedItems, String styleTheme) {
        if (!REQUIRED_CATEGORIES.containsKey(styleTheme)) {
            return false;
        }
        
        // Check for a dress (which counts as both top and bottom)
        boolean hasDress = categorizedItems.containsKey("Dresses") && !categorizedItems.get("Dresses").isEmpty();
        
        // If we have a dress, we only need to check for footwear
        if (hasDress) {
            return categorizedItems.containsKey("Footwear") && !categorizedItems.get("Footwear").isEmpty();
        }
        
        // Otherwise check for all required categories
        for (String category : REQUIRED_CATEGORIES.get(styleTheme)) {
            if (!categorizedItems.containsKey(category) || categorizedItems.get(category).isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Find a style theme that can be created with the available items
     */
    private String findViableStyleTheme(Map<String, List<Product>> categorizedItems) {
        for (String theme : STYLE_THEMES) {
            if (hasRequiredCategories(categorizedItems, theme)) {
                return theme;
            }
        }
            return null;
    }
    
    /**
     * Determine if outerwear should be added based on season and style
     */
    private boolean shouldAddOuterwear(String season, String styleTheme) {
        // More likely to add outerwear in colder seasons
        double seasonProbability;
        switch(season) {
            case "Winter":
                seasonProbability = 0.9;
                break;
            case "Fall":
                seasonProbability = 0.7;
                break;
            case "Spring":
                seasonProbability = 0.5;
                break;
            case "Summer":
                seasonProbability = 0.2;
                break;
            default:
                seasonProbability = 0.5;
                break;
        }
        
        // Adjust by style
        if (styleTheme.equals("Athletic") || styleTheme.equals("Evening")) {
            seasonProbability *= 0.5; // Less likely for athletic or evening wear
        } else if (styleTheme.equals("Streetwear") || styleTheme.equals("Casual")) {
            seasonProbability *= 1.2; // More likely for streetwear or casual
        }
        
        return random.nextDouble() < seasonProbability;
    }
    
    /**
     * Selects an appropriate style theme based on the user's wardrobe
     */
    private String selectStyleTheme(User user, List<Product> items) {
        // Count items by potential style themes
        Map<String, Integer> themeCount = new HashMap<>();
        for (String theme : STYLE_THEMES) {
            themeCount.put(theme, 0);
        }
        
        // Check each item description and category for style indicators
        for (Product item : items) {
            String description = (item.getDescription() != null) ? item.getDescription().toLowerCase() : "";
            String category = item.getCategory().toLowerCase();
            
            for (String theme : STYLE_THEMES) {
                if (description.contains(theme.toLowerCase()) || category.contains(theme.toLowerCase())) {
                    themeCount.put(theme, themeCount.get(theme) + 1);
                }
            }
            
            // Special cases
            if (description.contains("jean") || category.contains("jean") || description.contains("t-shirt") || category.contains("t-shirt")) {
                themeCount.put("Casual", themeCount.get("Casual") + 1);
            }
            if (description.contains("suit") || category.contains("suit") || description.contains("blazer") || category.contains("blazer")) {
                themeCount.put("Business", themeCount.get("Business") + 1);
                themeCount.put("Formal", themeCount.get("Formal") + 1);
            }
            if (description.contains("sport") || category.contains("sport") || description.contains("running") || category.contains("running")) {
                themeCount.put("Athletic", themeCount.get("Athletic") + 1);
            }
        }
        
        // Find theme with highest count
        String bestTheme = null;
        int maxCount = -1;
        
        for (Map.Entry<String, Integer> entry : themeCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestTheme = entry.getKey();
            }
        }
        
        // If no clear theme found, choose randomly
        if (maxCount <= 3) {
            bestTheme = STYLE_THEMES[random.nextInt(STYLE_THEMES.length)];
        }
        
        return bestTheme;
    }
    
    /**
     * Determine the current season
     */
    private String determineCurrentSeason() {
        int month = LocalDateTime.now().getMonthValue();
        
        if (month >= 3 && month <= 5) {
            return "Spring";
        } else if (month >= 6 && month <= 8) {
            return "Summer";
        } else if (month >= 9 && month <= 11) {
            return "Fall";
        } else {
            return "Winter";
        }
    }

    /**
     * Categorize wardrobe items by product type
     */
    private Map<String, List<Product>> categorizeItems(List<Product> items) {
        Map<String, List<Product>> categorized = new HashMap<>();
        
        // Initialize categories
        categorized.put("Tops", new ArrayList<>());
        categorized.put("Bottoms", new ArrayList<>());
        categorized.put("Dresses", new ArrayList<>());
        categorized.put("Footwear", new ArrayList<>());
        categorized.put("Outerwear", new ArrayList<>());
        categorized.put("Accessories", new ArrayList<>());

        for (Product item : items) {
            String category = item.getCategory().toLowerCase();
            String subcategory = item.getSubcategory() != null ? item.getSubcategory().toLowerCase() : "";
            String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
            
            if (category.contains("dress") || subcategory.contains("dress") || description.contains("dress")) {
                categorized.get("Dresses").add(item);
            } else if (category.contains("top") || category.contains("shirt") || category.contains("blouse") || 
                    category.contains("sweater") || category.contains("tee") || category.contains("t-shirt") ||
                    subcategory.contains("top") || subcategory.contains("shirt")) {
                categorized.get("Tops").add(item);
            } else if (category.contains("pant") || category.contains("jean") || category.contains("skirt") || 
                    category.contains("short") || category.contains("bottom") || category.contains("trouser") ||
                    subcategory.contains("pant") || subcategory.contains("bottom")) {
                categorized.get("Bottoms").add(item);
            } else if (category.contains("shoe") || category.contains("boot") || category.contains("sneaker") || 
                    category.contains("sandal") || category.contains("footwear") || subcategory.contains("shoe") ||
                    description.contains("shoe") || description.contains("footwear")) {
                categorized.get("Footwear").add(item);
            } else if (category.contains("jacket") || category.contains("coat") || category.contains("hoodie") || 
                    category.contains("blazer") || category.contains("sweater") || category.contains("cardigan") ||
                    subcategory.contains("outerwear") || description.contains("outerwear")) {
                categorized.get("Outerwear").add(item);
            } else if (category.contains("accessory") || category.contains("jewelry") || category.contains("hat") || 
                    category.contains("scarf") || category.contains("belt") || category.contains("bag") || 
                    category.contains("watch") || category.contains("glasses") || subcategory.contains("accessory")) {
                categorized.get("Accessories").add(item);
            } else {
                // Try to determine category from description
                if (description.contains("shirt") || description.contains("top") || description.contains("blouse")) {
                    categorized.get("Tops").add(item);
                } else if (description.contains("pant") || description.contains("jean") || description.contains("skirt")) {
                    categorized.get("Bottoms").add(item);
                } else if (description.contains("boot") || description.contains("sneaker") || description.contains("sandal")) {
                    categorized.get("Footwear").add(item);
                } else {
                    // If we can't determine, default to tops
                    categorized.get("Tops").add(item);
                }
            }
        }

        return categorized;
    }

    /**
     * Select an item from a specific category that matches/coordinates with a reference item
     */
    private Product selectItemFromCategory(Map<String, List<Product>> categorizedItems, String category, 
                                          Product referenceItem, String styleTheme) {
        // Check if the category exists and has items
        if (!categorizedItems.containsKey(category) || categorizedItems.get(category).isEmpty()) {
            return null;
        }
        
        List<Product> categoryItems = categorizedItems.get(category);
        
        // If no reference item, just return a random item from the category
        if (referenceItem == null) {
            return categoryItems.get(random.nextInt(categoryItems.size()));
        }
        
        // Try to find items that match the style theme first
        List<Product> styleMatches = new ArrayList<>();
        if (styleTheme != null) {
            for (Product item : categoryItems) {
                String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                String itemCategory = item.getCategory().toLowerCase();
                
                if (description.contains(styleTheme.toLowerCase()) || itemCategory.contains(styleTheme.toLowerCase())) {
                    styleMatches.add(item);
                }
            }
        }
        
        // Get color of reference item
        String referenceColor = extractColor(referenceItem);
        if (referenceColor != null && COLOR_COMPATIBILITY.containsKey(referenceColor)) {
            // Create a list of color-compatible items
            List<String> compatibleColors = COLOR_COMPATIBILITY.get(referenceColor);
            List<Product> colorMatches = new ArrayList<>();
            
            // Source items from style matches if available, otherwise use all category items
            List<Product> sourceItems = styleMatches.isEmpty() ? categoryItems : styleMatches;
            
            for (Product item : sourceItems) {
                String itemColor = extractColor(item);
                if (itemColor != null && compatibleColors.contains(itemColor)) {
                    colorMatches.add(item);
                }
            }
            
            // If we found color matches, return a random one
            if (!colorMatches.isEmpty()) {
                return colorMatches.get(random.nextInt(colorMatches.size()));
            }
        }
        
        // If we have style matches but no color matches, use a style match
        if (!styleMatches.isEmpty()) {
            return styleMatches.get(random.nextInt(styleMatches.size()));
        }
        
        // Fallback to a random item from the category
        return categoryItems.get(random.nextInt(categoryItems.size()));
    }
    
    /**
     * Extract the main color from a product
     */
    private String extractColor(Product product) {
        // First check if the product has a color attribute
        if (product.getColor() != null && !product.getColor().isEmpty()) {
            return matchColorName(product.getColor());
        }
        
        // Check description for color
        if (product.getDescription() != null) {
            return extractColorFromText(product.getDescription());
        }
        
        // Check name for color
        if (product.getName() != null) {
            return extractColorFromText(product.getName());
        }
        
        return null;
    }
    
    /**
     * Extract color from text description or name
     */
    private String extractColorFromText(String text) {
        String lowerText = text.toLowerCase();
        
        // Common colors to check for
        String[] colors = {
            "Black", "White", "Red", "Blue", "Green", "Yellow", "Purple",
            "Pink", "Orange", "Brown", "Gray", "Navy", "Teal", "Beige", "Cream",
            "Maroon", "Turquoise", "Gold", "Silver", "Olive", "Lavender"
        };
        
        for (String color : colors) {
            if (lowerText.contains(color.toLowerCase())) {
                return color;
            }
        }

        return null;
    }

    /**
     * Match a color string to a standard color name
     */
    private String matchColorName(String colorInput) {
        String color = colorInput.toLowerCase();
        
        if (color.contains("black")) return "Black";
        if (color.contains("white")) return "White";
        if (color.contains("red")) return "Red";
        if (color.contains("blue")) return "Blue";
        if (color.contains("green")) return "Green";
        if (color.contains("yellow")) return "Yellow";
        if (color.contains("purple")) return "Purple";
        if (color.contains("pink")) return "Pink";
        if (color.contains("orange")) return "Orange";
        if (color.contains("brown")) return "Brown";
        if (color.contains("gray") || color.contains("grey")) return "Gray";
        if (color.contains("navy")) return "Navy";
        if (color.contains("teal") || color.contains("turquoise")) return "Teal";
        if (color.contains("beige") || color.contains("tan") || color.contains("khaki")) return "Beige";
        
        return colorInput;
    }
    
    /**
     * Create a unique signature for an outfit to avoid duplicates
     */
    private String generateOutfitSignature(Outfit outfit, List<Product> allItems) {
        // Create a list of all product IDs in the outfit
        List<String> productIds = new ArrayList<>(outfit.getProductIds());
        Collections.sort(productIds); // Sort to ensure consistent signature
        
        StringBuilder signature = new StringBuilder();
        for (String productId : productIds) {
            // Find the product and add its category to the signature
            for (Product product : allItems) {
                if (product.getProductId().equals(productId)) {
                    signature.append(product.getCategory()).append(":");
                    break;
                }
            }
            signature.append(productId.substring(0, Math.min(8, productId.length()))).append(",");
        }
        
        return signature.toString();
    }
    
    /**
     * Create a fallback outfit with random items if a proper outfit can't be created
     */
    private Outfit createFallbackOutfit(User user, List<Product> wardrobeItems, String season) {
        if (wardrobeItems.size() < 2) {
            return null;
        }

        // Create a shuffled copy of the items
        List<Product> shuffled = new ArrayList<>(wardrobeItems);
        Collections.shuffle(shuffled);
        
        // Create a simple outfit with random items
        String themeName = STYLE_THEMES[random.nextInt(STYLE_THEMES.length)];
        Outfit outfit = new Outfit(user.getUserId(), generateOutfitName(themeName, season));
        outfit.setOutfitId(UUID.randomUUID().toString());
        outfit.setAiGenerated(true);
        
        // Choose distinct items
        Set<String> addedIds = new HashSet<>();
        int count = 0;
        
        for (Product item : shuffled) {
            if (!addedIds.contains(item.getProductId())) {
                outfit.addProduct(item.getProductId());
                addedIds.add(item.getProductId());
                count++;
                
                if (count >= 4) {
                    break; // Limit to 4 items
                }
            }
        }
        
        return outfit;
    }
    
    /**
     * Generate a creative outfit name
     */
    private String generateOutfitName(String styleTheme, String season) {
        String[] adjectives = {
            "Stylish", "Modern", "Classic", "Trendy", "Chic", "Elegant", "Vibrant",
            "Sophisticated", "Bold", "Minimalist", "Fresh", "Urban", "Relaxed", 
            "Contemporary", "Effortless", "Polished", "Luxurious", "Refined"
        };
        
        // Add some themed descriptors based on the style
        List<String> themedDescriptors = new ArrayList<>();
            switch (styleTheme) {
                case "Casual":
                themedDescriptors.addAll(Arrays.asList("Everyday", "Laid-back", "Comfortable", "Weekend"));
                    break;
                case "Formal":
                themedDescriptors.addAll(Arrays.asList("Sophisticated", "Polished", "Refined", "Evening"));
                    break;
                case "Business":
                themedDescriptors.addAll(Arrays.asList("Professional", "Office", "Corporate", "Executive"));
                    break;
                case "Athletic":
                themedDescriptors.addAll(Arrays.asList("Sporty", "Active", "Dynamic", "Performance"));
                    break;
                case "Evening":
                themedDescriptors.addAll(Arrays.asList("Glamorous", "Elegant", "Sophisticated", "Night-out"));
                break;
            case "Streetwear":
                themedDescriptors.addAll(Arrays.asList("Urban", "Contemporary", "Edgy", "Street-smart"));
                break;
            case "Vintage":
                themedDescriptors.addAll(Arrays.asList("Retro", "Classic", "Timeless", "Throwback"));
                break;
            case "Bohemian":
                themedDescriptors.addAll(Arrays.asList("Free-spirited", "Artistic", "Eclectic", "Earthy"));
                break;
            case "Minimalist":
                themedDescriptors.addAll(Arrays.asList("Clean", "Sleek", "Understated", "Essential"));
                break;
            case "Preppy":
                themedDescriptors.addAll(Arrays.asList("Collegiate", "Polished", "Smart", "Refined"));
                    break;
            }

        // Combine adjectives with themed descriptors
        String[] allAdjectives = new String[adjectives.length + themedDescriptors.size()];
        System.arraycopy(adjectives, 0, allAdjectives, 0, adjectives.length);
        for (int i = 0; i < themedDescriptors.size(); i++) {
            allAdjectives[adjectives.length + i] = themedDescriptors.get(i);
        }
        
        // Select a random adjective
        String adjective = allAdjectives[random.nextInt(allAdjectives.length)];
        
        // Create a more interesting name format
        int nameFormat = random.nextInt(5);
        String result;
        switch (nameFormat) {
            case 0:
                result = adjective + " " + season + " " + styleTheme;
                break;
            case 1:
                result = "The " + adjective + " " + styleTheme;
                break;
            case 2:
                result = season + " " + styleTheme + " " + "Ensemble";
                break;
            case 3:
                result = adjective + " " + styleTheme + " Look";
                break;
            case 4:
                result = "Perfect " + season + " " + styleTheme;
                break;
            default:
                result = adjective + " " + season + " " + styleTheme;
                break;
        }
        return result;
    }
    
    /**
     * Helper class to build outfits
     */
    private class OutfitBuilder {
        private final User user;
        private final String styleTheme;
        private final String season;
        private final List<Product> coreItems = new ArrayList<>();
        private final List<Product> optionalItems = new ArrayList<>();
        private Product primaryColorItem;
        
        public OutfitBuilder(User user, String styleTheme, String season) {
            this.user = user;
            this.styleTheme = styleTheme;
            this.season = season;
        }
        
        public void addCoreItem(Product item) {
            if (item == null) return;
            coreItems.add(item);
            
            // The first core item sets the primary color
            if (primaryColorItem == null) {
                primaryColorItem = item;
            }
        }
        
        public void addOptionalItem(Product item) {
            if (item == null) return;
            optionalItems.add(item);
        }
        
        public Product getPrimaryColorItem() {
            return primaryColorItem;
        }
        
        public Outfit build() {
            // Create outfit with a creative name
            Outfit outfit = new Outfit(user.getUserId(), generateOutfitName(styleTheme, season));
            outfit.setOutfitId(UUID.randomUUID().toString());
            outfit.setAiGenerated(true);
            
            // Set season and occasion
            Outfit.OutfitSeason outfitSeason;
            switch (season) {
                case "Spring":
                    outfitSeason = Outfit.OutfitSeason.SPRING;
                    break;
                case "Summer":
                    outfitSeason = Outfit.OutfitSeason.SUMMER;
                    break;
                case "Fall":
                    outfitSeason = Outfit.OutfitSeason.FALL;
                    break; 
                case "Winter":
                    outfitSeason = Outfit.OutfitSeason.WINTER;
                    break;
                default:
                    outfitSeason = Outfit.OutfitSeason.ALL_SEASON;
                    break;
            }
            outfit.setSeason(outfitSeason);
            
            Outfit.OutfitOccasion occasion;
            if (styleTheme.equals("Casual") || styleTheme.equals("Streetwear") || 
                    styleTheme.equals("Vintage") || styleTheme.equals("Bohemian") || 
                    styleTheme.equals("Minimalist")) {
                occasion = Outfit.OutfitOccasion.CASUAL;
            } else if (styleTheme.equals("Formal") || styleTheme.equals("Evening")) {
                occasion = Outfit.OutfitOccasion.FORMAL;
            } else if (styleTheme.equals("Business") || styleTheme.equals("Preppy")) {
                occasion = Outfit.OutfitOccasion.WORK;
            } else if (styleTheme.equals("Athletic")) {
                occasion = Outfit.OutfitOccasion.SPORT;
            } else {
                occasion = Outfit.OutfitOccasion.CASUAL;
            }
            outfit.setOccasion(occasion);
            
            // Add all items to the outfit
            for (Product item : coreItems) {
                outfit.addProduct(item.getProductId());
            }
            
            for (Product item : optionalItems) {
                outfit.addProduct(item.getProductId());
            }
            
            // Add tags
            outfit.addTag(styleTheme);
            outfit.addTag(season);
            
            return outfit;
        }
    }
}