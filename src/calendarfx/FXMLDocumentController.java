/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarfx;

import com.pepperonas.fxiconics.FxIconicsLabel;
import com.pepperonas.fxiconics.MaterialColor;
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        ldtIterator.format(DateTimeFormatter.ISO_DATE);
        
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
                i = ((control2 - (control + 1)) / 7) + 2;
                control2++;
            }
            
            Label tempLabel = new Label(Integer.toString(ldtIterator.getDayOfMonth()));
            
            
            gpMain.add(createCell(tempLabel, ldtIterator), ldtIterator.getDayOfWeek().getValue() - 1, i);
            
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
    
    private BorderPane createCell(Label label, LocalDateTime ldt) {

        BorderPane cell = new BorderPane();
        label.setOnMouseClicked(e -> addNewNote(ldt));
        
        VBox vbox = new VBox();
        vbox.getChildren().addAll(loadNotesForDate(ldt));
        BorderPane.setAlignment(vbox, Pos.CENTER);
        cell.setCenter(vbox);

        BorderPane.setAlignment(label, Pos.TOP_RIGHT);
        cell.setTop(label);
        cell.getStyleClass().add("cell");
        
        return cell;
    }
    
    private List<HBox> loadNotesForDate(LocalDateTime ldt)
    {  
        List<HBox> tempList = new ArrayList();
        
        String sql = "Select * FROM Notes WHERE local_date_time = ?";
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:calendarFx.db");
             PreparedStatement pstmt = conn.prepareStatement(sql);
             )
        {
            
            pstmt.setString(1, ldt.toLocalDate().toString());
            ResultSet rs = pstmt.executeQuery();  
            while(rs.next())
            {
                FxIconicsLabel labelTextDefault =
                (FxIconicsLabel) new FxIconicsLabel.Builder(FxFontGoogleMaterial.Icons.gmd_cancel)
                        .size(24)
                        .text("")
                        .color(MaterialColor.ORANGE_500)
                        .build();
                
                String tempNote = rs.getString("note");
                Label tempLabel = new Label(tempNote);
                //new Label(rs.getString("note")));
                labelTextDefault.setOnMouseClicked(e -> {deleteNote(ldt, tempNote); loadMonth(ldt);});
                tempLabel.setOnMouseClicked(e -> {editNote(ldt, tempNote); loadMonth(ldt);});
                HBox hbox = new HBox();
                hbox.getChildren().add(labelTextDefault);
                hbox.getChildren().add(tempLabel);
                tempList.add(hbox);
            }
        }     
        catch (SQLException ex) {    
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return tempList;
    }
    
    
    
    private void addNewNote(LocalDateTime ldt)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("New Note Dialog");
        alert.setHeaderText("Add New Note");
        alert.setContentText("Enter note!");

        TextArea textArea = new TextArea();
        textArea.setEditable(true);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setContent(expContent);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        {
            if(textArea.getText().length() > 0)
            {
                createNoteForDate(textArea.getText(), ldt);
                loadMonth(ldt);
            }            
        } 
    }
    
    private void deleteNote(LocalDateTime ldt, String note)
    {
        if(deleteConfirmation(note).get() == ButtonType.OK)
        {
            String sql = "DELETE FROM Notes WHERE note = ? and  local_date_time = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:calendarFx.db");
                    PreparedStatement pstmt = conn.prepareStatement(sql))
            {
                pstmt.setString(1, note);
                pstmt.setString(2, ldt.toLocalDate().toString());

                pstmt.executeUpdate();

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Delete Note Dialog");
                alert.setHeaderText("Delete Note");
                alert.setContentText("Note was successfully deleted!");
                alert.show();           
            }
            catch (SQLException ex)
            {
                exceptionDialog(ex.getMessage());
            }
        }
    }
    
    private void editNote(LocalDateTime ldt, String note)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Edit Note Dialog");
        alert.setHeaderText("Edit Note");
        alert.setContentText("Enter note!");

        TextArea textArea = new TextArea();
        
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setContent(expContent);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
        {
            if(textArea.getText().length() > 0)
            {
                editNoteForDate(note, textArea.getText(), ldt);
                loadMonth(ldt);
            }            
        } 
    }
    
    private void createNoteForDate(String note, LocalDateTime ldt)
    {
        
        String sql = "INSERT INTO Notes(local_date_time, note) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:calendarFx.db");
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, ldt.toLocalDate().toString());
            pstmt.setString(2, note);
            
            pstmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            exceptionDialog(ex.getMessage());
        }
    }
    
    private void editNoteForDate(String oldNote, String newNote, LocalDateTime ldt)
    {
        String sql = "UPDATE Notes SET note = ? WHERE note = ? and  local_date_time = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:calendarFx.db");
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            
            pstmt.setString(1, newNote);
            pstmt.setString(2, oldNote);
            pstmt.setString(3, ldt.toLocalDate().toString());
           
            pstmt.executeUpdate();
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Update Note Dialog");
            alert.setHeaderText("Update Note");
            alert.setContentText("Note was successfully updated!");
            alert.show();           
        }
        catch (SQLException ex)
        {
            exceptionDialog(ex.getMessage());
        }
    }
    
    private void exceptionDialog(String exceptionText)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Error!");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }
    
    private  Optional<ButtonType> deleteConfirmation(String note)
    {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Note Dialog");
        alert.setHeaderText("Delete Note");
        alert.setContentText("Are you sure you want to delete this note!");

        TextArea textArea = new TextArea();
        textArea.setText(note);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setContent(expContent);
        Optional<ButtonType> result = alert.showAndWait();
        
        ObservableList<String> optionList = FXCollections.observableArrayList();
        return result;
    }
}


//if (result.get() == ButtonType.OK)
//        {
//            if(textArea.getText().length() > 0)
//            {
//                editNoteForDate(note, textArea.getText(), ldt);
//                loadMonth(ldt);
//            }            
//        } 