/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarfx;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author blj0011
 */
public class FXMLDocumentController implements Initializable
{
    
    @FXML private Label lblMonthYear;
    @FXML private GridPane gpMain;   
    
    LocalDateTime ldtControl;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
        ldtControl = LocalDateTime.now();        
        loadMonth(ldtControl);
        
    }    
 
    int getColumn(LocalDateTime ldt)
    {
        int i = 0;
        while(ldt.getDayOfWeek() != DayOfWeek.SUNDAY)
        {
            i++;
            ldt = ldt.plusDays(1);
        }
        
        return i;
    }
    
    @FXML private void handleBTNMonthChange(ActionEvent event)
    {
        if(((Button)event.getSource()).getId().equals("leftButtonWithImage"))
        {
            ldtControl = ldtControl.minusMonths(1);
            loadMonth(ldtControl);
        }
        else if(((Button)event.getSource()).getId().equals("rightButtonWithImage"))
        {
            ldtControl = ldtControl.plusMonths(1);
            loadMonth(ldtControl);
        }
    }
    
    private void loadMonth(LocalDateTime ldt)
    {
        if(gpMain.getChildren().size() > 0)
        {
            gpMain.getChildren().clear();
        }
        
        loadGridPaneFirstRow();
        lblMonthYear.setText(ldt.getMonth() + " " + ldt.getYear());
        
        LocalDateTime ldtIterator = ldt.minusDays(ldt.getDayOfMonth() - 1);        
        int control = getColumn(ldtIterator);
        int control2 = 0;
        int i = 0;
        while(ldtIterator.getMonth() == ldt.getMonth())
        {
            if( i == 0 || i == 1 && control2 <= control)
            {
                i = 1;
                control2++;
            }
            else 
            {
                System.out.println((control2 + (control)));
                i = ((control2 - (control + 1)) / 7) + 2;
                control2++;
            }
            
            Label tempLabel = new Label(Integer.toString(ldtIterator.getDayOfMonth()));
            GridPane.setHalignment(tempLabel, HPos.CENTER);
            
            gpMain.add(createCell(tempLabel), ldtIterator.getDayOfWeek().getValue() - 1, i);
            
            ldtIterator = ldtIterator.plusDays(1);
        }        
    }
    
    private void loadGridPaneFirstRow()
    {
        String[] string = {"Monday", "Tuesday", "Wednesday", "Thrusday", "Friday", "Saturday", "Sunday"};
        for(int i = 0; i < string.length; i++)
        {
            Label tempLabel = new Label(string[i]);
            GridPane.setHalignment(tempLabel, HPos.CENTER);
            gpMain.add(tempLabel, i, 0);
        }       
    }
    
    private BorderPane createCell(Label label) {

        BorderPane cell = new BorderPane();
        cell.setOnMouseClicked(e -> System.out.println("cell containing " + label.getText() + " was clicked!"));
        label.setOnMouseClicked(e -> System.out.println(((Label)e.getSource()).getText()));
        
//        Circle circle = new Circle(10, Color.CORNFLOWERBLUE);
//
//        circle.visibleProperty().bind(cellSwitch);

        BorderPane.setAlignment(label, Pos.TOP_RIGHT);
        cell.setTop(label);
        cell.getStyleClass().add("cell");
        
        return cell;
    }
}
