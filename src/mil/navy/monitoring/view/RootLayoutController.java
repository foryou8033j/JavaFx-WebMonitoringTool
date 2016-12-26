package mil.navy.monitoring.view;

import java.io.File;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import mil.navy.monitoring.MainApp;
import mil.navy.monitoring.model.Site;

/**
 * 
 * @author navy
 *
 */
public class RootLayoutController {

	private MainApp mainApp;
	
	@FXML
	private FlowPane overView;
	
	public RootLayoutController() {
		
	}
	
    @FXML
    private void handleNew() {
        mainApp.getSiteData().clear();
        mainApp.setSiteFilePath(null);
        mainApp.addBrowserToOverview();
    }
	
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadSiteDataFromFile(file);
            mainApp.addBrowserToOverview();
        }
    }
    

    @FXML
    private void handleSave() {
        File personFile = mainApp.getSiteFilePath();
        if (personFile != null) {
            mainApp.saveSiteDataToFile(personFile);
        } else {
            handleSaveAs();
        }
    }

 
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveSiteDataToFile(file);
        }
    }
	

	@FXML
	private void handleSettingButton()
	{
		mainApp.showSettingDialog();
	}
	

	@FXML
	private void handleRefreshAllButton()
	{
		for(int i=0; i< mainApp.getBrowserControllers().size(); i++)
		{
			mainApp.getBrowserControllers().get(i).loadURL();
		}
	}
	

	@FXML
	private void handleAboutButton()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("about");
		alert.setHeaderText("정보보호체계 통합 모니터링 도구 v1.0");
		alert.setContentText("해상병 626기 상병 서정삼");
		
		alert.showAndWait();
	}
	

	private void showSiteDataToOverview()
	{
		mainApp.addBrowserToOverview();
	}
	

	@FXML
	private void handleExitButton()
	{
		System.exit(0);
	}
	
	public void setMainApp(MainApp mainApp)
	{
		this.mainApp = mainApp;
		
		showSiteDataToOverview();
	}
	
	public FlowPane getOverView()
	{
		return overView;
	}
	
	
	
	
	
}
