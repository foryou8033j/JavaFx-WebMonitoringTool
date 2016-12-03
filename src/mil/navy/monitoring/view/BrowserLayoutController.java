package mil.navy.monitoring.view;

import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mil.navy.monitoring.MainApp;
import mil.navy.monitoring.model.Site;

public class BrowserLayoutController{
	
	LongProperty startTime   = new SimpleLongProperty(0);
    LongProperty endTime     = new SimpleLongProperty(0);
    LongProperty elapsedTime = new SimpleLongProperty(0);
    LongProperty pauseTime = new SimpleLongProperty(0);
	
	private Site site;
	
	@FXML
	private Label title;
	
	@FXML
	private WebView browser;
	private WebEngine webEngine;
	//Document doc;
	
	@FXML
	private ToggleButton pauseButton;
	
	//남은시간 표시 진행바
	@FXML
	private ProgressBar progressBar;
	
	private MainApp mainApp;
	
	//@FXML
	//Task task;
	
	java.net.CookieManager manager = new java.net.CookieManager();
	
	Worker<Void> worker;
	
	Timer timer1 = null;
	
	public BrowserLayoutController() {

	}
	
	@FXML
	private void handlePauseButton()
	{
		if(pauseButton.isSelected())
		{
			loadURL();
			site.setRunning(true);
			pauseButton.setText("▶");
			title.textFillProperty().set(Color.BLACK);
		}
		else
		{
			title.textFillProperty().set(Color.RED);
			pauseButton.setText("■");
			site.setRunning(false);
		}
	}
	
	/**
	 * 설정된 시간을 바탕으로 새로고침한다.
	 */
	private void tryReloadPage()
	{
		
		//시작, 끝 시간 조정 -> 추후 그래프에서 로딩 시간 측정용
		elapsedTime.bind(Bindings.subtract(endTime, startTime));
		
		//다음로딩까지의 남은 시간 측정
		LongProperty besideTime = new SimpleLongProperty();
		besideTime.bind(Bindings.subtract(System.nanoTime(), startTime));
		
		long besideTimeLong =  besideTime.divide(1_000_000).longValue();
		long refreshTimeLong = (long) site.getRefreshTime() * 1000;
		
		if( besideTimeLong > refreshTimeLong && (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED))
		{
			loadURL();
		}
		else if(besideTimeLong > refreshTimeLong * 1.5)
		{
			loadURL();
		}

		
	}
	
	private void doRefreshingPrecess()
	{
		progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
		tryReloadPage();
    	pauseButton.setSelected(true);
    	title.textFillProperty().set(Color.BLACK);
	}
	
	private void pauseRefresingProcess()
	{
		progressBar.progressProperty().unbind();
		progressBar.setProgress(-1);
    	title.textFillProperty().set(Color.RED);
    	pauseButton.setSelected(false);
	}
	
	@FXML
	public void initialize()
	{
		
	    
	}
	
	private void removeCache()
	{
		manager.getCookieStore().removeAll();
	
		
	}
	
	private void drawTimeProgressBar()
	{
		
		if(!site.isRunning() || !mainApp.isRefresh()) 
		{
			//progressBar.setStyle("-fx-accent: #A9A9A9;");
			pauseTime.set(System.nanoTime());
			progressBar.setProgress(-1);
			return;
		}
		else 
			pauseTime = new SimpleLongProperty(0);
		
		LongProperty besideTime = new SimpleLongProperty();
		besideTime.bind(Bindings.subtract(System.nanoTime(), startTime.add(pauseTime)));
		
		long besideTimeLong =  besideTime.divide(1_000_000).longValue();
		long refreshTimeLong = (long) site.getRefreshTime() * 1000;
		
		double fullPercent =  ((double)besideTimeLong / (double)refreshTimeLong) * 100;
		
		progressBar.setProgress(fullPercent/100);
	}
	
	/**
	 * 설정된 URL주소를 로드한다.
	 */
	public void loadURL()
	{
		try
		{
			//removeCache();
			startTime.set(System.nanoTime());
			webEngine.reload();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * MainApp과 연동한다.
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
		
	}
	
	/**
	 * 지정된 Site와 연동한다.
	 * @param site
	 */
	public void setSiteData(Site site)
	{
		this.site = site;
		
		try
		{
			startTime.set(System.nanoTime());
			
			//사이트 타이틀 설정
			title.setText(site.getName());
			
			//웹엔진 연동
			webEngine = browser.getEngine();
			
			//org.eclipse.swt.browser.DefaultType=mozilla
			
			/*doc = Jsoup.parse(new URL(site.getAddress()), 5000);
		    doc.select("img").stream().forEach((element) -> {
	            element.remove();
	        });
		    webEngine.loadContent(doc.outerHtml());*/
			
			//URL url = new URL(site.getAddress());
			//webview.getEngine().load(url.toExternalForm());
			//browser.getEngine().load(url.toExternalForm());
			webEngine.load(site.getAddress());
			
			//쿠키 설정
			//java.net.CookieHandler.setDefault(null);
			//java.net.CookieHandler.setDefault(manager);
			
			
			//Desktop.getDesktop().browse(new URI("http://www.example.com"));
			//java.net.CookieHandler.setDefault(null);
			java.net.CookieHandler.setDefault(new java.net.CookieManager());
			//browser.setCache(false);
			
			
			//브라우저 배율 기본 설정
			browser.zoomProperty().set((double) site.getZoom() );
			browser.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
			      @Override public void onChanged(Change<? extends Node> change) {
			        Set<Node> deadSeaScrolls = browser.lookupAll(".scroll-bar");
			        for (Node scroll : deadSeaScrolls) {
			          scroll.setVisible(false);
			        }
			      }
			    });
			
			webEngine.javaScriptEnabledProperty().set(true);
			

			//Progress 설정
			/*webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
				
		        @Override
		        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
		        	
		            if (newValue == Worker.State.SUCCEEDED) {
		            	endTime.set(System.nanoTime());
		            	progressBar.setStyle("-fx-accent: #00BFFF;");
		            }
		            else if(newValue == Worker.State.RUNNING)
		            {
		            	progressBar.setStyle("-fx-accent: #FA6900;");
		            }
		            else if(newValue == Worker.State.FAILED)
		            {
		            	progressBar.setStyle("-fx-accent: RED;");
		            }
		        }
		    });*/
			
			webEngine.setCreatePopupHandler(null);
			
			
			//browser.setStyle("window.scrollTo(0, document.body.scrollWidth / 2)");

			//남은시간 프로그래스 바 지정
			//progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());

			//남은 로딩율 프로그래스 지정
			progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());

		    //최초 브라우저 삽입시에 로드.
		    //if(site.isRunning()) loadURL();
			
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (GeneralSecurityException e) {
			}
		      
		    
			timer1 = new Timer();
			timer1.scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(() -> {
		            	
		                if(mainApp.isRefresh()) 
		                {
		                	if(site.isRunning() && mainApp.isRefresh())
		                		doRefreshingPrecess();
		                	else
		                		pauseRefresingProcess();
		                }
		                else
		                	pauseRefresingProcess();
		            });
		        }
		    }, 0, 1000);
/*			
			new Timer().scheduleAtFixedRate(new TimerTask() {
		        @Override
		        public void run() {
		            Platform.runLater(() -> {
		            	drawTimeProgressBar();
		            });
		        }
		    }, 0, 100);*/
			
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		

	}
	
	public void shutdownThis()
	{
		try
		{
			timer1.cancel();
			
		}catch (Exception e)
		{
			
		}
		
	}

}

