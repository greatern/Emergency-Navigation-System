/**
 * @author K Maremela 221030105
 * @author KS Mothoagae 220022690
 * @author NF Maluleke 221063322
 * @author TG Nenwali 222001364
 */
import gui.GUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
    	 GUI visualizer = new GUI();
         visualizer.setStage(primaryStage);
         primaryStage.setTitle("Guardian Route");
         primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
