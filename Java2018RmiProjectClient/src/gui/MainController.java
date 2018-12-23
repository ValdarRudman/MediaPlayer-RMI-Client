package gui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Observable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import monitor.FileShare;
import monitor.Monitor;
import monitor.ObserverList;
import upload.FileChooserUpload;

public class MainController {

	//FXML tags
//---------------------------------------//
	@FXML
	MediaView mediaView;

	@FXML
	ListView<String> serverListView;
	
	@FXML
	ListView<String> localListView;
	
	@FXML
	Button downloadButton;
	
	@FXML
	Button uploadButton;
	
	@FXML
	Button playButton;
	
	@FXML
	Button stopButton;
	
	@FXML
	Text currentFileLoaded;
//---------------------------------------//
	
	//The Path for the directories.
	private final String LOCAL_PATH = "rmi\\local";

	
	private static int PORT_NUMBER = 5555;
	private final String HOSTNAME = "127.0.0.1";
	
	//Monitor for local folder
	FileShare localMonitor;
	
	FileShare serverMonitor;
	
	//keep track if media playing and if loaded
	private boolean playing = false;
	private boolean loaded = false;

	//javafx stage
	Stage stage = null;
	
	@FXML
	public void initialize() {
		
		createfolder();
		
		//Add observers
        Platform.runLater(() -> {
           addObservers();
          
        });
        	
		try {
			
			Registry registry = LocateRegistry.getRegistry(HOSTNAME, PORT_NUMBER);
			
			serverMonitor = (FileShare) registry.lookup("Monitor"); 
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					while(true) {

						try {
					
							ObservableList<String> ol = FXCollections.observableArrayList();
							File[] files = serverMonitor.getFiles();
							
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									
									for (File file : files) {
										
										ol.add(file.getName());
											
									}
									
									serverListView.setItems(ol);
									
								}
							});

							Thread.sleep(500);
	
						} catch (RemoteException | InterruptedException e) {
						
							e.printStackTrace();
						}
					
					}
					
				}
			}).start();
			
		} catch (RemoteException | NotBoundException e) {
		
			e.printStackTrace();
		}
 
	}

	/**
	 * Set stage and add closing rules
	 * @param stage
	 */
	public void setStage(Stage stage) {
			
		this.stage = stage;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
		    public void handle(WindowEvent t) {
				
				closeProgram();
			}
		});
	}
		
	/*
	* Run below code before program exits
	*/
	public void closeProgram() {		
			
		Platform.exit();
		System.exit(0);
			
	}
	
	/**
	 * Creates the folders if they don't exist
	 */
	public void createfolder() {
		
		File f = new File(LOCAL_PATH);
		
		if(!f.exists())
			f.mkdirs();
		
	}
	
	/*
	 * Add observes
	 */
	public void addObservers(){

		this.localMonitor = new Monitor(LOCAL_PATH);
        addObservers(localListView, localMonitor);

    }
	
	/**
	 * Added observer to monitor and create ObservableList
	 * @param listView
	 * @param monitor
	 */
	private void addObservers(ListView<String> listView, FileShare monitor) {
		
		ObservableList<String> list = FXCollections.observableArrayList();
		
		ObserverList ol = new ObserverList(list);
		
		((Observable) monitor).addObserver(ol);
		
		new Thread((Runnable) monitor).start();
		
		listView.setItems(list);
		
	}
	
	/**
	 * Load file to play
	 */
	public void load() {
		
		try {
			
			//Check to see if we have anything in the list
			if(localListView.getItems().size() > 0) {
				
				String choice = localListView.getSelectionModel().getSelectedItem();
				
				//checks to see if choice is null. If so sets choice to first item
				if(choice == null) {
				
					localListView.getSelectionModel().select(0);
					
					choice = localListView.getSelectionModel().getSelectedItem();
				
				}
			
				//Open file
				localMonitor.openFile(LOCAL_PATH + "\\" + choice);
				
				//Get file
				File loading = localMonitor.getFile();
		
				//get file and set it as media
				Media media = new Media(loading.toURI().toURL().toString());
			
				//Close file
				localMonitor.closeFile();
				
				//Create media Player and set media
				MediaPlayer mp = new MediaPlayer(media);
				
				mp.setAutoPlay(false);
				
				//Add media Player to our mediaView
				this.mediaView.setMediaPlayer(mp);
			
				this.loaded = true;
				this.currentFileLoaded.setText(loading.getName());
			
			}
		}
		catch(Exception e) {
			
			e.getMessage();
			
		}
	}
	
	/**
	 * Play the media
	 * @param play
	 */
	public void play(ActionEvent play) {

		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				//Load file if not loaded
				if(!loaded) 
					load();

				//Play media if loaded
				if(!playing && loaded) {
				
					mediaView.getMediaPlayer().play();
		            playing = true;
		            
		            playButton.setText("Pause");
		            Color c = Color.web("#8702f4");
		            playButton.setTextFill(c);
					
				}
				//Pause media
				else if(loaded) {
					
					mediaView.getMediaPlayer().pause();
		            playing = false;
		            
		            playButton.setText("Play");
		            Color c = Color.web("#016521");
		            playButton.setTextFill(c);
					
				}
				
			}
		});
		
	}
	
	/**
	 * Stop media playing
	 * @param stop
	 */
	public void stop(ActionEvent stop) {
		
		if(playing) {
			
			mediaView.getMediaPlayer().stop();
			playing = false;
			
			playButton.setText("Play");
			
			loaded = false;
			
			currentFileLoaded.setText("");
			
		}
		
	}
	
	/**
	 * Upload media to server
	 * @param upload
	 */
	public void upload(ActionEvent upload) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				//choose file
				String in = new FileChooserUpload().upload();
				
				File file = new File(in);
				
				if(in == null)
					return;
				
				try {
					
					//open file get bytes
					localMonitor.openFile(in);
					
					//get the files bytes
					byte[] fileBytes = localMonitor.getFileBytes();
					
					//close file 
					localMonitor.closeFile();
					
					//upload to server
					serverMonitor.upload(file.getName(), fileBytes);

					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
			}
			
		});
		
	}
	
	/**
	 * Download file from sever
	 * @param download
	 */
	public void download(ActionEvent download) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				String choice = serverListView.getSelectionModel().getSelectedItem();
				
				//checks to see if choice is null.
				if(choice == null) 
					return;
				
				try {
					
					serverMonitor.openFile(serverMonitor.getFolderPath() + "\\" + choice);
					
					byte[] fileBytes = serverMonitor.getFileBytes();
					
					serverMonitor.closeFile();
					
					localMonitor.upload(choice, fileBytes);
					
					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
				
				
			}
			
		});
		
	}
	
}
