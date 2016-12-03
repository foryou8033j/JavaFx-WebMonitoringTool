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

	private String titleString = "웹 통합 모니터링 툴";
	
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
	 * 메인 데이터의 접근을 설정한다.
	 * 각 브라우저에서 전역으로 접근하기 때문에 synchronized를 설정을 하여 충돌을 방지 한다.
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
	 * MainApp의 초기설정
	 */
	public MainApp() {
		
		
		//siteData.add(new Site("Naver", "http://naver.com/", true));
		/*siteData.add(new Site("Google", "http://google.com/"));
		siteData.add(new Site("Bing", "http://bing.com/"));
		siteData.add(new Site("대한민국 해군", "http://navy.mil.kr/"));*/
		// 마지막으로 열었던 연락처 파일을 가져온다.

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
	 * 화면상 루트 메뉴바를 로드 하면서 마지막에 Stage를 보여준다.
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
			
			
			//화면 회전 동작을 제어하는 쓰레드, 프로그램 동작과 함께 시작한다.
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
	 * 화면 회전 시도
	 */
	private void tryRotate()
	{
		
		//다음로딩까지의 남은 시간 측정
		LongProperty besideTime = new SimpleLongProperty();
		besideTime.bind(Bindings.subtract(System.nanoTime(), startTime));
				
		long besideTimeLong =  besideTime.divide(1_000_000).longValue();
		long rotateTimeLong = (long) rotateTime * 1000;
		
		if(besideTimeLong > rotateTimeLong)
		{
			//임시 폴더 전부 삭제
			FileUtil.deleteAllFiles("C:/Users/navy/AppData/Local/Temp");
			startTime.set(System.nanoTime());
			printRotateScreen();
		}
		
	}
	
	/**
	 * 설정 화면 표시
	 */
	public void showSettingDialog()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/SettingsLayout.fxml"));
			BorderPane layout = (BorderPane) loader.load();
			
			Stage dialogStage = new Stage();
			dialogStage.setTitle("환경설정");
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
	 * Overview 컨트롤러에서 브라우저 추가 지시를 수행한다.
	 * SiteData를 바탕으로 overview에 브라우저를 그린다.
	 */
	public void addBrowserToOverview()
	{
		
		try
		{
			rootController.getOverView().getChildren().removeAll(browsers);
			rootController.getOverView().getChildren().clear();
			
			//각 컨트롤러의 타이머 종료.
			for(int i=0; i<controllers.size(); i++)
				controllers.get(i).shutdownThis();
			
			controllers = null;
			browsers = null;
			
			controllers = FXCollections.observableArrayList();
			browsers = FXCollections.observableArrayList();
			
			//추가된 브라우저의 column, row를 구한다.
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
			
			//브라우저 갯수가 홀 수 일때의 배치
			if(siteData.size() % 2 != 0)
				BrowserColumn++;
			
			//브라우저 갯수가 1개 일때의 배치
			if(siteData.size() == 1)
			{
				BrowserColumn = 1;
				BrowserRow = 1;
			}
				
			//브라우저 갯수가 2개일 때의 Vertical 배치를 Horizontal 배치로 변경
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
				
				
				//각 브라우저의 Height 리스너 등록
				rootLayout.heightProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						
						browser.setPrefHeight((newValue.intValue() / BrowserRow)-30);
						
					}
				});
				
				//각 브라우저의 Width 리스너 등록
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
				//각 브라우저의 Height 리스너 등록
				rootLayout.heightProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						
						browser.setPrefHeight(newValue.intValue() -50);
						
					}
				});
				
				//각 브라우저의 Width 리스너 등록
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
	 * 현재 출력될 화면 배열을 바탕으로 BrowserLayout을 뿌려준다.
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
	 * 사이트 파일 환경설정을 반환한다.
	 * 즉 파일은 마지막으로 열린 것이고, 환경설정은 OS 특정 레지스트리로부터 읽는다.
	 * 만일 preference를 찾지 못하면 null을 반환한다.
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
	 * 현재 불러온 파일의 경로를 설정한다. 이 경로는 OS 특정 레지스트리에 저장된다.
	 *
	 * @param file the file or null to remove the path
	 */
	public void setSiteFilePath(File file) {
	    Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
	    if (file != null) {
	        prefs.put("filePath", file.getPath());

	        // Stage 타이틀을 업데이트한다.
	        primaryStage.setTitle(titleString + " - " + file.getName());
	    } else {
	        prefs.remove("filePath");

	        // Stage 타이틀을 업데이트한다.
	        primaryStage.setTitle("AddressApp");
	    }
	}
	
	/**
	 * 지정한 파일로부터 사이트 데이터를 가져온다. 현재 사이트 데이터로 대체된다.
	 *
	 * @param file
	 */
	public void loadSiteDataFromFile(File file) {
	    try {
	        JAXBContext context = JAXBContext.newInstance(SiteListWrapper.class);
	        Unmarshaller um = context.createUnmarshaller();

	        // 파일로부터 XML을 읽은 다음 역 마샬링한다.
	        SiteListWrapper wrapper = (SiteListWrapper) um.unmarshal(file);

	        siteData.clear();
	        siteData.addAll(wrapper.getSites());

	        // 파일 경로를 레지스트리에 저장한다.
	        setSiteFilePath(file);

	    } catch (Exception e) { // 예외를 잡는다
	    	e.printStackTrace();
	    	
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Could not load data");
	        alert.setContentText("Could not load data from file:\n" + file.getPath());

	        alert.showAndWait();
	    }
	}
	
	/**
	 * 현재 사이트 데이터를 지정한 파일에 저장한다.
	 *
	 * @param file
	 */
	public void saveSiteDataToFile(File file) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(SiteListWrapper.class);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	        // 연락처 데이터를 감싼다.
	        SiteListWrapper wrapper = new SiteListWrapper();
	        wrapper.setSites(siteData);

	        // 마샬링 후 XML을 파일에 저장한다.
	        m.marshal(wrapper, file);

	        // 파일 경로를 레지스트리에 저장한다.
	        setSiteFilePath(file);
	    } catch (Exception e) { // 예외를 잡는다.
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Could not save data");
	        alert.setContentText("Could not save data to file:\n" + file.getPath());

	        alert.showAndWait();
	    }
	}
	
	/**
	 * 사이트데이터를 반환한다. 
	 */
	public ObservableList<Site> getSiteData()
	{
		return siteData;
	}
	
	/**
	 * 현재 지정된 브라우저들의 컨트롤러들을 반환한다.
	 * (주의) 브라우저의 데이터양은 변동적임.
	 * @return
	 */
	public ObservableList<BrowserLayoutController> getBrowserControllers()
	{
		return controllers;
	}
	
	/**
	 * primaryStage를 반환한다.
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



