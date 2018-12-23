package upload;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Choose file you want to upload
 * @author valdar
 *
 */
public class FileChooserUpload {
	
	String path = null;

	/*
	 *Choose file to upload. return string[] with file path and name
	 * @return
	 */
	public String upload() {
		
		Stage window = new Stage();

        //Block events to other windows
        window.initModality(Modality.APPLICATION_MODAL);
        
        FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All mp3/mp4", "*.mp3*", "*mp4*"));
        
        File file = fileChooser.showOpenDialog(window);
        
        if(file != null) {
        	// if file is not null - get the name and the path of file to pass 
        	path = file.getAbsolutePath().toString();
        	window.close();
        }
        
        return path;
	}
	
}
