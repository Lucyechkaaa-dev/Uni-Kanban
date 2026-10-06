package backend.ui.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.Main;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@Log4j2
public class KanbanFxApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static String[] savedArgs = new String[0];

    public static void setArgs(String[] args) {
        if (args != null) {
            savedArgs = args;
        }
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }

    @Override
    public void init() {
        try {
            log.info("Bootstrapping Spring Boot context for JavaFX...");
            springContext = new SpringApplicationBuilder(Main.class)
                    .headless(false)
                    .run(savedArgs);
            log.info("Spring Boot context successfully initialized.");
        } catch (Exception e) {
            log.warn("Spring Boot context initialization encountered an issue, running JavaFX standalone: {}", e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting JavaFX Kanban UI...");
        KanbanBoardView boardView = new KanbanBoardView();

        Scene scene = new Scene(boardView, 1100, 720);
        primaryStage.setTitle("Uni-Kanban");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.setScene(scene);
        primaryStage.show();
        log.info("JavaFX Kanban UI window displayed.");
    }

    @Override
    public void stop() {
        log.info("Stopping JavaFX application...");
        if (springContext != null && springContext.isActive()) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void launchApp(String[] args) {
        setArgs(args);
        Application.launch(KanbanFxApplication.class, args);
    }
}
