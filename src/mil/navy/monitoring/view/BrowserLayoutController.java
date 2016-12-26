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

	private MainApp mainApp;
	
	java.net.CookieManager manager = new java.net.CookieManager();
	
	Worker<Void> worker;
	
	Timer timer = null;
	
	public BrowserLayoutController() {

	}
	
	private void tryReloadPage()
	{


		elapsedTime.bind(Bindings.subtract(endTime, startTime));
		

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
		tryReloadPage();
    	title.textFillProperty().set(Color.BLACK);
	}
	
	private void pauseRefresingProcess()
	{
    	title.textFillProperty().set(Color.RED);
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
			return;
		}
		else 
			pauseTime = new SimpleLongProperty(0);
		
		LongProperty besideTime = new SimpleLongProperty();
		besideTime.bind(Bindings.subtract(System.nanoTime(), startTime.add(pauseTime)));
		
		long besideTimeLong =  besideTime.divide(1_000_000).longValue();
		long refreshTimeLong = (long) site.getRefreshTime() * 1000;
		
		double fullPercent =  ((double)besideTimeLong / (double)refreshTimeLong) * 100;
	}
	

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
	

	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
		
	}
	


	public void setSiteData(Site site)
	{
		this.site = site;
		
		try
		{
			startTime.set(System.nanoTime());
			

			title.setText(site.getName());
			
			webEngine = browser.getEngine();
			
			webEngine.load(site.getAddress());

			java.net.CookieHandler.setDefault(new java.net.CookieManager());

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
			

			webEngine.setCreatePopupHandler(null);
			

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (GeneralSecurityException e) {
			}
		      
		    
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
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

			
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		

	}
	
	public void shutdownThis()
	{
		try
		{
			timer.cancel();
			
		}catch (Exception e)
		{
			
		}
		
	}

}

