package triviaapp.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import triviaapp.dao.FileQuestionDao;
import triviaapp.dao.PlayerDao;
import triviaapp.domain.GameService;
import triviaapp.domain.Player;


public class TriviaAppUi extends Application{
    
    private GameService gameService;
    private Scene gameScene;
    private Scene startScene;
    private int currentQuestion;
     
    @Override
    public void init() throws Exception {
     
                
        Properties properties = new Properties();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties");
//        properties.load(new FileInputStream("/resources/config.properties"));
        properties.load(stream);
        String topPlayersFile = properties.getProperty("players");
        PlayerDao playerdao = new PlayerDao(topPlayersFile);
        
        InputStream input = TriviaAppUi.class.getResourceAsStream("/questions.txt");
        Player testPlayer = new Player ("Tester");
        FileQuestionDao fileQuestion = new FileQuestionDao(input);
        gameService = new GameService(fileQuestion, testPlayer, playerdao);
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        BorderPane startPane = new BorderPane();
      
        Button startButton = new Button("Start");
        Button scoreButton = new Button("Show top scores");
        
        startPane.setPrefSize(250, 100);
        startPane.setPadding(new Insets(30, 20, 30, 20));
        startPane.setTop(startButton);
        startPane.setBottom(scoreButton);
        startScene = new Scene(startPane);
               
        currentQuestion = -1;
        startButton.setOnAction(e -> {
            
            if(gameService.isOver(currentQuestion)) {
                BorderPane endView = new BorderPane();
                endView.setPrefSize(300, 200);
                endView.setPadding(new Insets(10, 10, 10, 10));
                int playersPoints = gameService.getPoints();
                endView.setTop(new Label("Game Over! You got " + playersPoints + " points!"));
              
                TextField field = new TextField();
                Button addButton = new Button("Add my name to scores!");
                
                addButton.setOnAction(w -> {
                    try {
                        gameService.addScore(field.getText(), playersPoints);
                    } catch (IOException ex) {
                        Logger.getLogger(TriviaAppUi.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
              
                endView.setCenter(field);
                endView.setBottom(addButton);
                
                Scene endScene=new Scene(endView);
                primaryStage.setScene(endScene);
             
            }
               currentQuestion++;
                               
               BorderPane gameView = new BorderPane();
               gameView.setPrefSize(300, 200);
               gameView.setPadding(new Insets(10, 10, 10, 10));
               gameView.setTop(new Label("Question: " + gameService.getNextQuestion(currentQuestion)));

               startButton.setText("Next!!");
               gameView.setRight(startButton);
               
               Label resultText = new Label("");
               
               GridPane options = new GridPane();  
               options.setVgap(10);
               options.setHgap(10);
               
               int optionIndex=0;  
                
            for(int x=0; x<2; x++) {
                for(int y=0; y<2; y++) {
                
                    Button optionButton=new Button();
                    options.add(optionButton, x, y);
                    optionButton.setText(gameService.getOption(currentQuestion, optionIndex));
                    
                    String correct =gameService.getCorrect(currentQuestion);
                 
                    optionButton.setOnAction(eh ->{
                        if(gameService.hasBeenAnswered(currentQuestion) == false) {
                            if(gameService.isCorrect(optionButton.getText(), currentQuestion)) {
                                resultText.setText("Correct!");
                            } else {
                                resultText.setText("Wrong! The correct answer is " + correct);
                            }
                        }
                    });
                    optionIndex++;
                }
            }
            gameView.setBottom(resultText);
            gameView.setCenter(options);
            gameScene = new Scene (gameView);
            primaryStage.setScene(gameScene);
     
        });
        
        scoreButton.setOnAction( e -> {
   
            Label scores = new Label(gameService.getTopScore());
            Scene s = new Scene(scores);
           
            Stage scoresStage = new Stage();
            scoresStage.setScene(s);
            scoresStage.show();
        });
        
        
        primaryStage.setScene(startScene);
        primaryStage.show();   
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
