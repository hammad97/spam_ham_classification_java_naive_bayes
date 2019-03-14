/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spamorhamfx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author Dell
 */
public class FXMLDocumentController implements Initializable {
    
    private Label label;
    @FXML
    private TextField targetMessage;
    @FXML
    private Button classifyMessageButton;
    @FXML
    private Rectangle spamResult;
    @FXML
    private Rectangle hamResult;
    @FXML
    private Rectangle resultWindow;
    @FXML
    private Button accuracyButton;
    public double windowX;
    public double windowY;
    public spamorham classifierObject;              //this one for showing 70-30 test data result
    public spamorham classifierObjectSearch;        //this one for actually showing your string result from 100% data
    @FXML
    private AnchorPane rootPane;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        Alert alert = new Alert(AlertType.INFORMATION, "Please wait for confirmation of application started status", ButtonType.OK);
        alert.showAndWait();
           final Thread threadSS = new Thread() {
            @Override
            public void run() {
        classifierObjectSearch = new spamorham("SMSSpamCollectionFull.txt","NOTESTFILE");
            };
        };
        threadSS.start();

        final Thread threadS = new Thread() {
            @Override
            public void run() {
                classifierObject = new spamorham("SMSSpamCollection.txt","SMSSpamCollection_test.txt");
            };
        };
        threadS.start();
        windowX= resultWindow.getLayoutX();
        windowY= resultWindow.getLayoutY();
    }    

    @FXML
    private void handleClassifyButtonAction(ActionEvent event) {
        this.resultWindow.setLayoutX(windowX);
        this.resultWindow.setLayoutY(windowY);
        if(!this.targetMessage.getText().isEmpty()){
            String ss= this.targetMessage.getText();
            String resultt = this.classifierObjectSearch.getStringResult(ss);
            if(resultt.compareToIgnoreCase("SPAM")==0){
                TranslateTransition trans= new TranslateTransition();
                trans.setDuration(Duration.seconds(2));
                trans.setToX(this.spamResult.getLayoutX()-270);
                trans.setNode(this.resultWindow);
                trans.play();    
            }
            else{
                TranslateTransition trans= new TranslateTransition();
                trans.setDuration(Duration.seconds(2));
                trans.setToX(this.hamResult.getLayoutX()-270);
                trans.setNode(this.resultWindow);
                trans.play();                    
            }
        }
    }

    @FXML
    private void handleAccuracyButtonAction(ActionEvent event) {
        if(!this.classifierObject.evaluationResult.isEmpty()){
            Alert alert = new Alert(AlertType.INFORMATION, this.classifierObject.evaluationResult, ButtonType.OK);
            alert.showAndWait();
        }
            
    }
    
}
