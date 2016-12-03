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

	
	//MainApp ���� ���� ������
	private MainApp mainApp;
	
	@FXML
	private FlowPane overView;
	
	public RootLayoutController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
     * ��� �ִ� �ּҷ��� �����.
     */
    @FXML
    private void handleNew() {
        mainApp.getSiteData().clear();
        mainApp.setSiteFilePath(null);
        mainApp.addBrowserToOverview();
    }
	
    /**
     * FileChooser�� ��� ����ڰ� ������ �ּҷ��� �����ϰ� �Ѵ�.
     */
    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();

        // Ȯ���� ���͸� �����Ѵ�.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Save File Dialog�� �����ش�.
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            mainApp.loadSiteDataFromFile(file);
            mainApp.addBrowserToOverview();
        }
    }
    
    /**
     * ���� ���� �ִ� ���Ͽ� �����Ѵ�.
     * ���� ���� �ִ� ������ ������ "save as" ���̾�α׸� �����ش�.
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
     * FileChooser�� ��� ����ڰ� ������ ������ �����ϰ� �Ѵ�.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();

        // Ȯ���� ���͸� �����Ѵ�.
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Save File Dialog�� �����ش�.
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            // ��Ȯ�� Ȯ���ڸ� ������ �Ѵ�.
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            mainApp.saveSiteDataToFile(file);
        }
    }
	
    /**
     * Settingâ�� ���� ����.
     */
	@FXML
	private void handleSettingButton()
	{
		mainApp.showSettingDialog();
	}
	
	/**
	 * ��� ������ ���ΰ�ħ ����� �����Ѵ�.
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
	 * About ���̾�α׸� �����ش�.
	 */
	@FXML
	private void handleAboutButton()
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("about");
		alert.setHeaderText("������ȣ ���� ����͸� ���� v1.0");
		alert.setContentText("���� : �ػ� 626�� �� ������");
		
		alert.showAndWait();
	}
	
	/**
	 * MainApp���� Overview�� ������ �߰� �۾��� �����Ѵ�.
	 */
	private void showSiteDataToOverview()
	{
		mainApp.addBrowserToOverview();
	}
	
	/**
	 * ���ø����̼� ����.
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
