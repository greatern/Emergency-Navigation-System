/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
package gui;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import graph.Graph;
import graph.Point;
import graph.ZoneType;

public class GUI {
	 private Scene mainScene;
	    private Stage primaryStage;
    private BorderPane root;
    private String buildingType = "";
    private Image image;
    private Canvas canvas;
    private GraphicsContext gc;
    private Text statusText;
    private Point start = null;
    private Point end = null;
    private Graph graph;
    private List<Image> images = new ArrayList<>();
    private List<Graph> graphs = new ArrayList<>();
    private int currentImageIndex = 0;
    // color constants for zone highlighting
    private static final Color EXIT_HIGHLIGHT = Color.rgb(144, 238, 144, 0.7); // Light green
    private static final Color DANGER_HIGHLIGHT = Color.rgb(255, 182, 193, 0.7); // Sweet pink
    
 // RGB color scheme for Gui Scene
    private static final Color DARK_BLUE = Color.rgb(30, 58, 138);     
    private static final Color BABY_BLUE = Color.rgb(137, 207, 240);    
    private static final Color WHITE = Color.rgb(255, 255, 255);       
    private static final Color LIGHT_GRAY = Color.rgb(243, 244, 246);   
    private static final Color DARK_GRAY = Color.rgb(55, 65, 81);      

    // Add a checkbox for toggling zone highlighting
    private CheckBox highlightZonesCheckbox;
    
    // For animation
    private AnimationTimer animationTimer;
    private List<Point> currentPath;
    private int currentPathIndex;
    private long lastUpdateTime;
    private static final double MOVER_SPEED = 89.22; // pixels per second


    //Loading Welcome Scene 
    private void showWelcomeScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgb(243, 244, 246); -fx-padding: 20;");

        Text title = new Text("Guardian Route");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setFill(DARK_BLUE);

        VBox description = new VBox(10);
        description.setAlignment(Pos.CENTER);
        Text[] points = {
            new Text("• Find safe paths quickly"),
            new Text("• Shows Exits and Danger zones"),
            new Text("• Works for any building type"),
            new Text("• Uses A* pathfinding")
        };
        for (Text point : points) {
            point.setFont(Font.font("Arial", 14));
            point.setFill(DARK_GRAY);
            description.getChildren().add(point);
        }

        Button proceedButton = createStyledButton("Proceed");
        proceedButton.setOnAction(e -> showBuildingTypeScene());

        layout.getChildren().addAll(title, description, proceedButton);
        mainScene = new Scene(layout, 800, 600);
        primaryStage.setScene(mainScene);
    }
    
//Shows the Building Scene
    private void showBuildingTypeScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgb(243, 244, 246); -fx-padding: 20;");

        Text title = new Text("Choose Building Type");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(DARK_BLUE);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        String[] types = {"School Building", "Residential Building", "Service Building", "Corporate Building"};
        for (String type : types) {
            Button btn = createStyledButton(type);
            btn.setOnAction(e -> {
                buildingType = type;
                showFloorSelectionScene();
            });
            buttonBox.getChildren().add(btn);
        }

        Button backButton = createStyledButton("Back");
        backButton.setOnAction(e -> showWelcomeScene());

        layout.getChildren().addAll(title, buttonBox, backButton);
        mainScene = new Scene(layout, 800, 600);
        primaryStage.setScene(mainScene);
    }

    //Shows Floor Selection Scene
    private void showFloorSelectionScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgb(243, 244, 246); -fx-padding: 20;");

        Text title = new Text("Select Floors");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(DARK_BLUE);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button singleFloorBtn = createStyledButton("Select Floor");
        singleFloorBtn.setOnAction(e -> {
            if (loadImages(false)) {
                showPathfindingScene();
            }
        });
      
        buttonBox.getChildren().addAll(singleFloorBtn);

        Button backButton = createStyledButton("Back");
        backButton.setOnAction(e -> showBuildingTypeScene());

        layout.getChildren().addAll(title, buttonBox, backButton);
        mainScene = new Scene(layout, 800, 600);
        primaryStage.setScene(mainScene);
    }
    
    //Loads Images to the GUI Scene
    private boolean loadImages(boolean multiple) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("images/"));
        List<File> files;
        if (multiple) {
            files = fc.showOpenMultipleDialog(null);
        } else {
            File file = fc.showOpenDialog(null);
            files = file != null ? List.of(file) : List.of();
        }

        if (files != null && !files.isEmpty()) {
            images.clear();
            graphs.clear();
            for (File file : files) {
                Image img = new Image(file.toURI().toString());
                images.add(img);
                graphs.add(new Graph(img.getPixelReader(), (int) img.getWidth(), (int) img.getHeight(), false));
            }
            currentImageIndex = 0;
            start = null;
            end = null;
            return true;
        }
        return false;
    }
    
    //Displays the Scene for the GUI
    private void showPathfindingScene() {
        BorderPane root = new BorderPane();
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (!images.isEmpty()) {
            canvas.setWidth(images.get(0).getWidth());
            canvas.setHeight(images.get(0).getHeight());
        }

        statusText = new Text("Click on the image to set Start & End points");
        statusText.setFill(Color.WHITE);
        statusText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        StackPane statusContainer = new StackPane(statusText);
        statusContainer.setPadding(new Insets(5));
        statusContainer.setStyle("-fx-background-color: transparent;");
        
        
        HBox controls = new HBox(10);
        Button loadBtn = createStyledButton("Load New Image");
        Button resetBtn = createStyledButton("Reset Points");
       
        highlightZonesCheckbox = new CheckBox("Display Zones");
        highlightZonesCheckbox.setTextFill(WHITE);

        ComboBox<String> floorSelector = new ComboBox<>();
        for (int i = 0; i < images.size(); i++) {
            floorSelector.getItems().add("Floor " + (i + 1));
        }
        floorSelector.setValue("Floor 1");
        floorSelector.setOnAction(e -> {
            currentImageIndex = floorSelector.getSelectionModel().getSelectedIndex();
            if (!images.isEmpty()) {
                canvas.setWidth(images.get(currentImageIndex).getWidth());
                canvas.setHeight(images.get(currentImageIndex).getHeight());
            }
            drawImage();
        });

        controls.getChildren().addAll(loadBtn, resetBtn, highlightZonesCheckbox, statusContainer);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: rgb(30, 58, 138);");

        loadBtn.setOnAction(e -> {
            if (loadImages(images.size() > 1)) {
                floorSelector.getItems().clear();
                for (int i = 0; i < images.size(); i++) {
                    floorSelector.getItems().add("Floor " + (i + 1));
                }
                floorSelector.setValue("Floor 1");
                currentImageIndex = 0;
                if (!images.isEmpty()) {
                    canvas.setWidth(images.get(0).getWidth());
                    canvas.setHeight(images.get(0).getHeight());
                }
                drawImage();
            }
        });
        
        
        resetBtn.setOnAction(e -> {
            start = null;
            end = null;
            stopAnimation();
            statusText.setText("Click on the image to set Start & End points");
            statusText.setFill(Color.WHITE);
            drawImage();
        });

        highlightZonesCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> drawImage());

        canvas.setOnMouseClicked(this::handleClick);

        Button backButton = createStyledButton("Back");
        backButton.setOnAction(e -> showFloorSelectionScene());

        root.setTop(controls);
        root.setCenter(canvas);
        root.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(10));

        mainScene = new Scene(root, 1000, 800);
        primaryStage.setScene(mainScene);
        drawImage();
    }
    
    public void setStage(Stage stage) {
        this.primaryStage = stage;
        showWelcomeScene();
    }

    //Styles the Color for Buttons
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: rgb(137, 207, 240); -fx-text-fill: rgb(255, 255, 255); " +
            "-fx-font-size: 14; -fx-padding: 8 16; -fx-background-radius: 5;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: rgb(100, 170, 200); -fx-text-fill: rgb(255, 255, 255); " +
            "-fx-font-size: 14; -fx-padding: 8 16; -fx-background-radius: 5;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: rgb(137, 207, 240); -fx-text-fill: rgb(255, 255, 255); " +
            "-fx-font-size: 14; -fx-padding: 8 16; -fx-background-radius: 5;"
        ));
        return button;
    }
    
    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

  //Draws the image to the Graph Scene
    private void drawImage() {
        // Early return if graphics context isn't ready
        if (canvas == null || gc == null) {
            return;
        }

        // Clear the canvas with a light gray background
        gc.setFill(LIGHT_GRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Only proceed if we have valid images loaded
        if (!images.isEmpty() && currentImageIndex < images.size()) {
            try {
                // Draw the base floor plan image
                Image currentImage = images.get(currentImageIndex);
                gc.drawImage(currentImage, 0, 0, canvas.getWidth(), canvas.getHeight());
                
                // Update the current graph reference
                graph = graphs.get(currentImageIndex);

                // Highlight zones if checkbox is selected
                if (highlightZonesCheckbox != null && highlightZonesCheckbox.isSelected()) {
                    highlightZones();
                }

             

            } catch (Exception e) {
                System.err.println("Error drawing image: " + e.getMessage());
                gc.setFill(Color.RED);
                gc.fillText("Error displaying image", 10, 20);
            }
        }

        // Always draw points if they exist (will appear on top of everything)
        if (start != null || end != null) {
            drawPoints();
        }

        // If animation is running, draw the mover at its current position
        if (animationTimer != null && currentPath != null && !currentPath.isEmpty()) {
            Point moverPosition = getCurrentMoverPosition();
            if (moverPosition != null) {
                drawMover(moverPosition);
            }
        }
    }
 // Helper method to get current mover position
    private Point getCurrentMoverPosition() {
        if (currentPath == null || currentPath.isEmpty()) {
            return null;
        }

        if (currentPathIndex >= currentPath.size() - 1) {
            return currentPath.get(currentPath.size() - 1);
        }

        Point current = currentPath.get(currentPathIndex);
        Point next = currentPath.get(currentPathIndex + 1);
        
        // Calculate progress between current and next point
        double totalDistance = distance(current, next);
        double distanceCovered = MOVER_SPEED * ((System.nanoTime() - lastUpdateTime) / 1_000_000_000.0);
        double ratio = Math.min(1.0, distanceCovered / totalDistance);
        
        return new Point(
            (int) (current.x + (next.x - current.x) * ratio),
            (int) (current.y + (next.y - current.y) * ratio)
        );
    }

    private void highlightZones() {
        int highlightSize = 10; // Increased size for better visibility
        
        // Highlight exits (safe zones)
        List<Point> exits = graph.getAllZonesOfType(ZoneType.EXIT);
        gc.setFill(EXIT_HIGHLIGHT);
        for (Point exit : exits) {
            // Draw a circle at each exit point
            gc.fillOval(
                exit.x - highlightSize/2, 
                exit.y - highlightSize/2, 
                highlightSize, 
                highlightSize
            );
        }
        
        // Highlight danger zones with sweet pink
        List<Point> dangers = graph.getAllZonesOfType(ZoneType.DANGER);
        gc.setFill(DANGER_HIGHLIGHT);
        for (Point danger : dangers) {
            // Draw a circle at each danger point
            gc.fillOval(
                danger.x - highlightSize/2, 
                danger.y - highlightSize/2, 
                highlightSize, 
                highlightSize
            );
        }
    }

    private void handleClick(MouseEvent e) {
        if (graph == null)
            return;

        int x = (int) e.getX();
        int y = (int) e.getY();

        if (start == null) {
            start = new Point(x, y);
            stopAnimation();
            drawImage();
        } else if (end == null) {
            end = new Point(x, y);
            stopAnimation();
            drawImage();
            updatePath("A*");
        } else {
            start = new Point(x, y);
            end = null;
            stopAnimation();
            drawImage();
        }
    }

    private void drawPoints() {
    	
        if (start != null) {
            // Draw a blue location pin (cartoon style)
            gc.setFill(Color.DODGERBLUE);
            
            // Pin body (teardrop shape)
            gc.fillOval(start.x - 8, start.y - 8, 16, 16); // Round base
            gc.fillPolygon(
                new double[]{start.x - 5, start.x, start.x + 5}, // X points
                new double[]{start.y - 3, start.y - 15, start.y - 3}, // Y points
                3 // Number of points (triangle tip)
            );
            
            // White center dot
            gc.setFill(Color.WHITE);
            gc.fillOval(start.x - 4, start.y - 4, 8, 8);
            
            // "START" text below
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText("START", start.x - 15, start.y + 20);
            
            //Reset status text
            //this.statusText.setText("");
        }
        
        if (end != null) {
            // Draw a red location pin (cartoon style)
            gc.setFill(Color.CRIMSON);
            
            // Pin body (teardrop shape)
            gc.fillOval(end.x - 8, end.y - 8, 16, 16); // Round base
            gc.fillPolygon(
                new double[]{end.x - 5, end.x, end.x + 5}, // X points
                new double[]{end.y - 3, end.y - 15, end.y - 3}, // Y points
                3 // Triangle tip
            );
            
            // White "X" mark
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(end.x - 4, end.y - 4, end.x + 4, end.y + 4);
            gc.strokeLine(end.x + 4, end.y - 4, end.x - 4, end.y + 4);
            
            // "GOAL" text below
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText("GOAL", end.x - 12, end.y + 20);
        }
    }

	
    private void updatePath(String algorithm) {
    	 
        if (start == null || end == null || graph == null) {
            return;
        }

        currentPath = graph.aStar(start, end);
        
        if (currentPath == null || currentPath.isEmpty()) {
        	
        	statusText.setText("No Path Found!");
            
            statusText.setFill(Color.RED);
            return;
        }

        boolean isSafe = true;
        boolean reachesSafeZone = false;
        
        // Check if path ends in a safe zone
        ZoneType endZone = graph.getZoneType(end);
        if (endZone == ZoneType.EXIT) {
            reachesSafeZone = true;
        }
        
     
        // Set status text based on path safety
        if (reachesSafeZone) {
        	
            statusText.setText("Reached safe zone successfully!");
            statusText.setFill(Color.LIMEGREEN);
            drawPath(currentPath, DARK_BLUE);
        } else if (isSafe) {
        	
            statusText.setText("Safe Path Found!");
            statusText.setFill(Color.LIMEGREEN);
            drawPath(currentPath, Color.GREEN);
        } else {
        	
            statusText.setText("Warning: Path Goes Through Danger Zones!");
            statusText.setFill(Color.ORANGE);
            drawPath(currentPath, Color.ORANGE);
        }
        
        startAnimation();
    }	
    private void drawPath(List<Point> path, Color pathColor) {
        if (path == null || path.isEmpty()) return;
        gc.setStroke(pathColor);
        gc.setLineWidth(4);
        gc.beginPath();
        gc.moveTo(path.get(0).x, path.get(0).y);
        for (int i = 1; i < path.size(); i++) {
            Point p = path.get(i);
            gc.lineTo(p.x, p.y);
        }
        gc.stroke();
        // Add start and end markers
        drawPathEndpoint(path.get(0), "START", Color.BLUE);
        drawPathEndpoint(path.get(path.size()-1), "END", Color.GREEN);
        
        // Add distance information
        double totalDistance = calculatePathDistance(path);
      //  gc.setFill(Color.BLACK);
        /*
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(String.format("Path Distance: %.1f pixels", totalDistance), 
                   path.get(0).x, path.get(0).y - 10); */
    }
    


   
    private void startAnimation() {
        stopAnimation();
        
        if (currentPath == null || currentPath.isEmpty()) {
            return;
        }

        currentPathIndex = 0;
        lastUpdateTime = System.nanoTime();
        final Point[] currentPosition = {currentPath.get(0)}; // Store current position

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate time since last frame in seconds
                double elapsedSeconds = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                // Smooth movement using interpolation
                if (currentPathIndex < currentPath.size() - 1) {
                    Point current = currentPath.get(currentPathIndex);
                    Point next = currentPath.get(currentPathIndex + 1);
                    
                    // Calculate direction vector
                    double dx = next.x - current.x;
                    double dy = next.y - current.y;
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    
                    // Normalize direction
                    if (distance > 0) {
                        dx /= distance;
                        dy /= distance;
                    }
                    
                    // Calculate movement for this frame
                    double moveDistance = MOVER_SPEED * elapsedSeconds;
                    double remainingDistance = distance - distance(current, currentPosition[0]);
                    
                    // Update position
                    if (moveDistance >= remainingDistance) {
                        // Move to next point
                        currentPosition[0] = new Point(next.x, next.y);
                        currentPathIndex++;
                    } else {
                        // Move partway along segment
                        double ratio = moveDistance / distance;
                        currentPosition[0] = new Point(
                            (int)(currentPosition[0].x + dx * moveDistance),
                            (int)(currentPosition[0].y + dy * moveDistance)
                        );
                    }
                }
                
                // Redraw everything
                drawImage();
                drawPath(currentPath, getPathColor());
                drawMover(currentPosition[0]);
                
                // Check if animation is complete
                if (currentPathIndex >= currentPath.size() - 1) {
                    Point endPoint = currentPath.get(currentPath.size() - 1);
                    if (graph.getZoneType(endPoint) == ZoneType.EXIT) {
                        statusText.setText("✓ Reached safe exit successfully!");
                        statusText.setFill(Color.LIMEGREEN);
                    }
                    stopAnimation();
                }
            }
        };
        
        animationTimer.start();
    }

    // Helper method to get appropriate path color
    private Color getPathColor() {
        if (statusText.getText().contains("Warning")) {
            return Color.ORANGERED;
        } else if (statusText.getText().contains("Reached")) {
            return Color.LIMEGREEN;
        }
        return Color.GREEN;
    }

    // Updated drawMover to accept position
    private void drawMover(Point position) {
        // Draw mover with smooth edges
        gc.setFill(Color.rgb(255, 105, 180)); // Hot pink
        gc.fillOval(position.x - 10, position.y - 10, 20, 20);
        
        // Add shadow for depth
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(position.x - 8, position.y - 6, 16, 16);
        
        // Add face details
        gc.setFill(Color.WHITE);
        gc.fillOval(position.x - 6, position.y - 8, 4, 4); // Left eye
        gc.fillOval(position.x + 2, position.y - 8, 4, 4); // Right eye
        
        // Smile
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeArc(position.x - 4, position.y - 2, 8, 6, 180, 180, ArcType.OPEN);
    }
    private Point getCurrentPosition() {
        if (currentPathIndex >= currentPath.size() - 1) {
            return currentPath.get(currentPath.size() - 1);
        }
        
        Point current = currentPath.get(currentPathIndex);
        Point next = currentPath.get(currentPathIndex + 1);
        
        // Calculate progress between current and next point
        double totalDistance = distance(current, next);
        double distanceCovered = MOVER_SPEED * ((System.nanoTime() - lastUpdateTime) / 1_000_000_000.0);
        double ratio = Math.min(1.0, distanceCovered / totalDistance);
        
        return new Point(
            (int) (current.x + (next.x - current.x) * ratio),
            (int) (current.y + (next.y - current.y) * ratio)
        );
    }

    private double calculatePathDistance(List<Point> path) {
        double distance = 0;
        for (int i = 1; i < path.size(); i++) {
            distance += distance(path.get(i-1), path.get(i));
        }
        return distance;
    }

    private void drawPathEndpoint(Point point, String label, Color color) {
        // Draw endpoint circle
        gc.setFill(color);
        gc.fillOval(point.x - 6, point.y - 6, 12, 12);
        
        // Draw white border
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(point.x - 6, point.y - 6, 12, 12);
        
        // Draw label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText(label, point.x - 15, point.y - 10);
    }

    // Helper method to calculate distance between two points
    private double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }
}