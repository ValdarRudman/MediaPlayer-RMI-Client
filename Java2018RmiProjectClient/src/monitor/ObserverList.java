package monitor;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * class that will update an Observable list
 * @author valdar
 *
 */
public class ObserverList implements Observer {

	//Observable list that will be updated
	private ObservableList<String> list = null;
	
	/**
	 * Assigns an ObservableList to its on on creation
	 * @param list
	 */
	public ObserverList(ObservableList<String> list) {
		
		if(this.list == null) {
			
			this.list = list;
		}
		
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {

				File[] files = (File[])arg1;
				
				list.clear();
				
				if(files != null) {
					
					for (File file : files) {
						
						list.add(file.getName());
						
					}
				}
				
			}
		});
		
	}

}
