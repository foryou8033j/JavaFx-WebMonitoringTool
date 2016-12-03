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

	
	//MainApp 과의 연동 데이터
	private MainApp mainApp;
	
	@FXML
	private FlowPane overView;
	
	public RootLayoutController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
     * 비어 있는 주소록을 만든다.
     */
    @FXML
    private void handleNew() {
        mainApp.getSiteData().clear();
        mainApp.setSiteFilePath(null);
        mainApp.addBrowserToOverview();
    }
	
    /**
     * FileChooser를 열어서 사용자가 가져올 주소록을 선택하게 한다.
     */
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();

        // 확장자 필터를 설정한다.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Save File Dialog를 보여준다.
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadSiteDataFromFile(file);
            mainApp.addBrowserToOverview();
        }
    }
    
    /**
     * 현재 열려 있는 파일에 저장한다.
     * 만일 열려 있는 파일이 없으면 "save as" 다이얼로그를 보여준다.
     *
     */
    @FXML
    private void handleSave() {
        File personFile = mainApp.getSiteFilePath();
        if (personFile != null) {
            mainApp.saveSiteDataToFile(personFile);
        } else {
            handleSaveAs();
        }
    }

    /**
     * FileChooser를 열어서 사용자가 저장할 파일을 선택하게 한다.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();

        // 확장자 필터를 설정한다.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Save File Dialog를 보여준다.
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // 정확한 확장자를 가져야 한다.
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveSiteDataToFile(file);
        }
    }
	
    /**
     * Setting창을 새로 연다.
     */
	@FXML
	private void handleSettingButton()
	{
		mainApp.showSettingDialog();
	}
	
	/**
	 * 모든 브라우저 새로고침 명령을 수행한다.
	 */
	@FXML
	private void handleRefreshAllButton()
	{
		for(int i=0; i< mainApp.getBrowserControllers().size(); i++)
		{
			mainApp.getBrowserControllers().get(i).loadURL();
		}
	}
	
	/**
	 * About 다이얼로그를 보여준다.
	 */
	@FXML
	private void handleAboutButton()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("about");
		alert.setHeaderText("정보보호 통합 모니터링 도구 v1.0");
		alert.setContentText("개발 : 해상병 626기 상병 서정삼");
		
		alert.showAndWait();
	}
	
	/**
	 * MainApp에서 Overview에 브라우저 추가 작업을 지시한다.
	 */
	private void showSiteDataToOverview()
	{
		mainApp.addBrowserToOverview();
	}
	
	/**
	 * 애플리케이션 종료.
	 */
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
