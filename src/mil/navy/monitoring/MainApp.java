package mil.navy.monitoring;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sun.javafx.scene.traversal.TopMostTraversalEngine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mil.navy.monitoring.model.Site;
import mil.navy.monitoring.model.SiteListWrapper;
import mil.navy.monitoring.util.FileUtil;
import mil.navy.monitoring.util.Regedit;
import mil.navy.monitoring.view.BrowserLayoutController;
import mil.navy.monitoring.view.RootLayoutController;
import mil.navy.monitoring.view.SettingsLayoutController;

public class MainApp extends Application {

	private String titleString = "�� ���� ����͸� ��";
	
	private ObservableList<Site> siteData = FXCollections.observableArrayList();
	private ObservableList<BrowserLayoutController> controllers = FXCollections.observableArrayList();
	private ObservableList<BorderPane> browsers = FXCollections.observableArrayList();
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	private RootLayoutController rootController;
	
	private final double WIDTH = 600;
	private final double HEIGHT = 400;
	
	private int BrowserColumn = 0;
	private int BrowserRow = 0;
	
	private boolean isRefreshing;
	private boolean isRotate;
	private int rotateTime;
	
	private int lastStartIndex = 1;
	
	LongProperty startTime = new SimpleLongProperty(0);
	
	/**
	 * ���� �������� ������ �����Ѵ�.
	 * �� ���������� �������� �����ϱ� ������ synchronized�� ������ �Ͽ� �浹�� ���� �Ѵ�.
	 * @return
	 */
	public synchronized boolean isRefresh()
	{
		return isRefreshing;
	}
	public synchronized void setRefresh(boolean set)
	{
		isRefreshing = set;
	}
	
	public synchronized boolean isRotate()
	{
		return isRotate;
	}
	
	public synchronized void setRotate(boolean set)
	{
		isRotate = set;
	}
	
	public synchronized int getRotateTime()
	{
		return rotateTime;
	}
	
	public synchronized void setRotateTime(int set)
	{
		rotateTime = set;
	}
	
	/**
	 * MainApp�� �ʱ⼳��
	 */
	public MainApp() {
		
		
		//siteData.add(new Site("Naver", "http://naver.com/", true));
		/*siteData.add(new Site("Google", "http://google.com/"));
		siteData.add(new Site("Bing", "http://bing.com/"));
		siteData.add(new Site("���ѹα� �ر�", "http://navy.mil.kr/"));*/
		// ���������� ������ ����ó ������ �����´�.

		try
		{
			if(Regedit.getRegistry("isRefresh") == null)
			{
				Regedit.addRegistry("isRefresh", "true");
				isRefreshing = true;
			}
			else
				isRefreshing = Boolean.valueOf(Regedit.getRegistry("isRefresh"));
			
			if(Regedit.getRegistry("isRotate") == null)
			{
				Regedit.addRegistry("isRotate", "true");
				isRotate = true;
			}
			else
				isRotate = Boolean.valueOf(Regedit.getRegistry("isRotate"));
			
			
			if(Regedit.getRegistry("rotateTime") == null)
			{
				Regedit.addRegistry("rotateTime", "10");
				rotateTime = 10;
			}
			else
				rotateTime = Integer.valueOf(Regedit.getRegistry("rotateTime"));
		}catch(Exception e)
		{
			isRefreshing = true;
			isRotate = true;
			rotateTime = 10;
			Regedit.addRegistry("isRefresh", "true");
			Regedit.addRegistry("isRotate", "true");
			Regedit.addRegistry("rotateTime", "10");
		}
		
	    
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		primaryStage.setTitle(titleString);
		primaryStage.setWidth(WIDTH);
		primaryStage.setHeight(HEIGHT);
		primaryStage.setMinWidth(WIDTH);
		primaryStage.setMinHeight(HEIGHT);
		
		File file = getSiteFilePath();
	    if (file != null) {
	        loadSiteDataFromFile(file);
	    }
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override public void handle(WindowEvent t) {
		        System.out.println("Closing...");
		        System.exit(0);
		    }
		});
		
		startTime.set(System.nanoTime());
		
		initRootLayout();

	}
	
	/**
	 * ȭ��� ��Ʈ �޴��ٸ� �ε� �ϸ鼭 �������� Stage�� �����ش�.
	 */
	private void initRootLayout()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			rootController= loader.getController();
			rootController.setMainApp(this);
			
			
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
			//ȭ�� ȸ�� ������ �����ϴ� ������, ���α׷� ���۰� �Բ� �����Ѵ�.
			new Timer().scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(() -> {
		            	if(isRotate) tryRotate();
		            });
		        }
		    }, 0, 2000);
			
			
		}catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ȭ�� ȸ�� �õ�
	 */
	private void tryRotate()
	{
		
		//�����ε������� ���� �ð� ����
		LongProperty besideTime = new SimpleLongProperty();
		besideTime.bind(Bindings.subtract(System.nanoTime(), startTime));
				
		long besideTimeLong =  besideTime.divide(1_000_000).longValue();
		long rotateTimeLong = (long) rotateTime * 1000;
		
		if(besideTimeLong > rotateTimeLong)
		{
			//�ӽ� ���� ���� ����
			FileUtil.deleteAllFiles("C:/Users/navy/AppData/Local/Temp");
			startTime.set(System.nanoTime());
			printRotateScreen();
		}
		
	}
	
	/**
	 * ���� ȭ�� ǥ��
	 */
	public void showSettingDialog()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/SettingsLayout.fxml"));
			BorderPane layout = (BorderPane) loader.load();
			
			Stage dialogStage = new Stage();
			dialogStage.setTitle("ȯ�漳��");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			
			SettingsLayoutController controller = loader.getController();
			controller.setMainApp(this);
			
			Scene scene = new Scene(layout);
			dialogStage.setScene(scene);
			
			dialogStage.show();
			
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Overview ��Ʈ�ѷ����� ������ �߰� ���ø� �����Ѵ�.
	 * SiteData�� �������� overview�� �������� �׸���.
	 */
	public void addBrowserToOverview()
	{
		
		try
		{
			rootController.getOverView().getChildren().removeAll(browsers);
			rootController.getOverView().getChildren().clear();
			
			//�� ��Ʈ�ѷ��� Ÿ�̸� ����.
			for(int i=0; i<controllers.size(); i++)
				controllers.get(i).shutdownThis();
			
			controllers = null;
			browsers = null;
			
			controllers = FXCollections.observableArrayList();
			browsers = FXCollections.observableArrayList();
			
			//�߰��� �������� column, row�� ���Ѵ�.
			BrowserColumn = 1;
			BrowserRow = 1;
			for(int i=0; i<=siteData.size(); i++)
			{
				if(i >= (siteData.size() / 2) - 1)
				{
					BrowserRow++;
					break;
				}
				else
					BrowserColumn++;
			}
			
			//������ ������ Ȧ �� �϶��� ��ġ
			if(siteData.size() % 2 != 0)
				BrowserColumn++;
			
			//������ ������ 1�� �϶��� ��ġ
			if(siteData.size() == 1)
			{
				BrowserColumn = 1;
				BrowserRow = 1;
			}
				
			//������ ������ 2���� ���� Vertical ��ġ�� Horizontal ��ġ�� ����
			if(siteData.size() == 2)
			{
				BrowserColumn = 2;
				BrowserRow = 1;
			}
			
			
			
			for(int i=0; i<siteData.size(); i++)
			{
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("view/BrowserLayout.fxml"));
				BorderPane browser = (BorderPane) loader.load();
				controllers.add(loader.getController());
				controllers.get(i).setMainApp(this);
				controllers.get(i).setSiteData(siteData.get(i));
				browsers.add(browser);
				
				browser.setPrefHeight((rootLayout.heightProperty().get() / BrowserRow) - 30);
				browser.setPrefWidth((rootLayout.widthProperty().get() / BrowserColumn) - 10);
				
				
				//�� �������� Height ������ ���
				rootLayout.heightProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						
						browser.setPrefHeight((newValue.intValue() / BrowserRow)-30);
						
					}
				});
				
				//�� �������� Width ������ ���
				rootLayout.widthProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,Number newValue) {
						browser.setPrefWidth((newValue.doubleValue() / BrowserColumn) - 10);
						
					}
				});

				
			}
			

			if(siteData.size() > 3)
			{
				int i = lastStartIndex+1;
				
				while(true)
				{
					
					if(i == siteData.size())
					{
						i = 0;
						continue;
					}
					
					rootController.getOverView().getChildren().add(browsers.get(i));
					//System.out.println(i + "in");
					
					i++;
					
					if(i == lastStartIndex+1) 
						break;
					
				}
				
				lastStartIndex++;
				
				if(lastStartIndex > siteData.size()-1)
					lastStartIndex = 0;
			}
			else if(siteData.size() < 4)
			{
				for(int i=0; i<siteData.size(); i++)
					rootController.getOverView().getChildren().add(browsers.get(i));
			}
			else if(siteData.size() == 0)
			{
				FXMLLoader loader = new FXMLLoader(getClass().getResource("view/VoidLayout.fxml"));
				BorderPane browser = (BorderPane) loader.load();
				rootController.getOverView().getChildren().add(browser);
				//�� �������� Height ������ ���
				rootLayout.heightProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						
						browser.setPrefHeight(newValue.intValue() -50);
						
					}
				});
				
				//�� �������� Width ������ ���
				rootLayout.widthProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,Number newValue) {
						browser.setPrefWidth(newValue.doubleValue() - 30);
						
					}
				});
			}

			
		}catch (IOException e)
		{
		e.printStackTrace();
			
		}
		
	}
	
	/**
	 * ���� ��µ� ȭ�� �迭�� �������� BrowserLayout�� �ѷ��ش�.
	 */
	private void printRotateScreen()
	{
		try
		{
			rootController.getOverView().getChildren().removeAll(browsers);
			rootController.getOverView().getChildren().clear();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		if(browsers.size() > 3)
		{
			int i = lastStartIndex+1;
			
			while(true)
			{
				
				if(i == browsers.size())
				{
					i = 0;
					continue;
				}
				
				rootController.getOverView().getChildren().add(browsers.get(i));
				
				i++;
				
				if(i == lastStartIndex+1) 
					break;
				
			}
			
			lastStartIndex++;
			
			if(lastStartIndex > browsers.size()-1)
				lastStartIndex = 0;
		}
		else if(browsers.size() < 4)
		{
			for(int i=0; i<browsers.size(); i++)
				rootController.getOverView().getChildren().add(browsers.get(i));
		}
	}
	
	
	/**
	 * ����Ʈ ���� ȯ�漳���� ��ȯ�Ѵ�.
	 * �� ������ ���������� ���� ���̰�, ȯ�漳���� OS Ư�� ������Ʈ���κ��� �д´�.
	 * ���� preference�� ã�� ���ϸ� null�� ��ȯ�Ѵ�.
	 *
	 * @return
	 */
	public File getSiteFilePath() {
	    Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    String filePath = prefs.get("filePath", null);
	    if (filePath != null) {
	        return new File(filePath);
	    } else {
	        return null;
	    }
	}
	
	/**
	 * ���� �ҷ��� ������ ��θ� �����Ѵ�. �� ��δ� OS Ư�� ������Ʈ���� ����ȴ�.
	 *
	 * @param file the file or null to remove the path
	 */
	public void setSiteFilePath(File file) {
	    Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    if (file != null) {
	        prefs.put("filePath", file.getPath());

	        // Stage Ÿ��Ʋ�� ������Ʈ�Ѵ�.
	        primaryStage.setTitle(titleString + " - " + file.getName());
	    } else {
	        prefs.remove("filePath");

	        // Stage Ÿ��Ʋ�� ������Ʈ�Ѵ�.
	        primaryStage.setTitle("AddressApp");
	    }
	}
	
	/**
	 * ������ ���Ϸκ��� ����Ʈ �����͸� �����´�. ���� ����Ʈ �����ͷ� ��ü�ȴ�.
	 *
	 * @param file
	 */
	public void loadSiteDataFromFile(File file) {
	    try {
	        JAXBContext context = JAXBContext.newInstance(SiteListWrapper.class);
	        Unmarshaller um = context.createUnmarshaller();

	        // ���Ϸκ��� XML�� ���� ���� �� �������Ѵ�.
	        SiteListWrapper wrapper = (SiteListWrapper) um.unmarshal(file);

	        siteData.clear();
	        siteData.addAll(wrapper.getSites());

	        // ���� ��θ� ������Ʈ���� �����Ѵ�.
	        setSiteFilePath(file);

	    } catch (Exception e) { // ���ܸ� ��´�
	    	e.printStackTrace();
	    	
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Could not load data");
	        alert.setContentText("Could not load data from file:\n" + file.getPath());

	        alert.showAndWait();
	    }
	}
	
	/**
	 * ���� ����Ʈ �����͸� ������ ���Ͽ� �����Ѵ�.
	 *
	 * @param file
	 */
	public void saveSiteDataToFile(File file) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(SiteListWrapper.class);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	        // ����ó �����͸� ���Ѵ�.
	        SiteListWrapper wrapper = new SiteListWrapper();
	        wrapper.setSites(siteData);

	        // ������ �� XML�� ���Ͽ� �����Ѵ�.
	        m.marshal(wrapper, file);

	        // ���� ��θ� ������Ʈ���� �����Ѵ�.
	        setSiteFilePath(file);
	    } catch (Exception e) { // ���ܸ� ��´�.
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Could not save data");
	        alert.setContentText("Could not save data to file:\n" + file.getPath());

	        alert.showAndWait();
	    }
	}
	
	/**
	 * ����Ʈ�����͸� ��ȯ�Ѵ�. 
	 */
	public ObservableList<Site> getSiteData()
	{
		return siteData;
	}
	
	/**
	 * ���� ������ ���������� ��Ʈ�ѷ����� ��ȯ�Ѵ�.
	 * (����) �������� �����;��� ��������.
	 * @return
	 */
	public ObservableList<BrowserLayoutController> getBrowserControllers()
	{
		return controllers;
	}
	
	/**
	 * primaryStage�� ��ȯ�Ѵ�.
	 * @return
	 */
	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}



