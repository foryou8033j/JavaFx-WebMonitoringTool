package mil.navy.monitoring.view;

import java.io.File;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import mil.navy.monitoring.MainApp;
import mil.navy.monitoring.model.Site;
import mil.navy.monitoring.util.Regedit;

public class SettingsLayoutController {
	
	
	@FXML
	private TableView<Site> siteTable;
	
	@FXML
	private TableColumn<Site, String> columnName;
	
	@FXML
	private TableColumn<Site, String> columnAddress;
	
	@FXML
	private TextField name;
	
	@FXML
	private TextField address;
	
	@FXML
	private Slider zoom;
	
	@FXML
	private Slider refreshTime;
	
	@FXML
	private Label zoomLabel;
	
	@FXML
	private Label refreshLabel;
	
	@FXML
	private Slider rotate;
	
	@FXML
	private Label rotateLabel;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button actionButton;
	
	@FXML
	private Button addButton;
	
	@FXML
	private Button removeButton;
	
	@FXML
	private ToggleButton refreshingButton;
	
	@FXML
	private ToggleButton rotateButton;
	
	@FXML
	private Button upButton;
	
	@FXML
	private Button downButton;
	
	//������
	private MainApp mainApp;
	
	private Site curClickedSite = null;
	
	//initialize ���� ���� ����
	public SettingsLayoutController() {
		

	}
	
	/**
	 * ��带 ��ĭ ���� �ø��� �ڵ�
	 */
	@FXML
	private void handleUpChange()
	{
		int select = siteTable.getSelectionModel().getSelectedIndex();
		
		if (select == 0)
			return;
		else
		{
			Site tmpSite = mainApp.getSiteData().get(select);
			
			mainApp.getSiteData().set(select, mainApp.getSiteData().get(select-1));
			mainApp.getSiteData().set(select-1,tmpSite);
			siteTable.getSelectionModel().select(select-1);
		}
	}
	
	/**
	 * ��带 ��ĭ �Ʒ��� ������ �ڵ�
	 */
	@FXML
	private void handleDownChange()
	{
		int select = siteTable.getSelectionModel().getSelectedIndex();
		
		if (select == mainApp.getSiteData().size() - 1)
			return;
		else
		{
			Site tmpSite = mainApp.getSiteData().get(select);
			
			mainApp.getSiteData().set(select, mainApp.getSiteData().get(select+1));
			mainApp.getSiteData().set(select+1,tmpSite);
			siteTable.getSelectionModel().select(select+1);
		}
	}
    
	/**
	 * ���ΰ�ħ ���� ���� ��ư
	 */
	@FXML
	private void handleRefreshingButton()
	{
		if(refreshingButton.selectedProperty().get())
			mainApp.setRefresh(true);
		else
			mainApp.setRefresh(false);
		
		Regedit.addRegistry("isRefresh", String.valueOf(mainApp.isRefresh()));
	}
	
	
	
	/**
	 * ȭ�� ȸ�� ���� ���� ��ư
	 */
	@FXML
	private void handleRotateButton()
	{
		if(rotateButton.selectedProperty().get())
			mainApp.setRotate(true);
		else
			mainApp.setRotate(false);
		
		Regedit.addRegistry("isRotate", String.valueOf(mainApp.isRotate()));
	}
	
	/**
	 * ����Ʈ �߰�
	 */
	@FXML
	private void handleAddButton()
	{
		try
		{
			//saveButton.setTextFill(Color.RED);
			mainApp.getSiteData().add(new Site("about:blank", "about:blank", false));
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * ���� ���� ����
	 */
	@FXML
	private void handleSaveButton()
	{
		saveButton.setTextFill(Color.BLACK);
		
		if(curClickedSite == null)
		{
			return;
		}
		try
		{
			curClickedSite.setName(name.getText());
			curClickedSite.setAddress(address.getText());
			curClickedSite.setZoom((double) zoom.getValue());
			curClickedSite.setRefreshTime((int) refreshTime.getValue());
		}catch (Exception e)
		{
			
		}
		
	}
	
	/**
	 * ���� ���� ����
	 */
	@FXML
	private void handleActionButton()
	{
		mainApp.addBrowserToOverview();
		actionButton.setTextFill(Color.BLACK);
	}
	
	/**
	 * ���� ���õ� ����Ʈ ����
	 */
	@FXML
	private void handleRemoveButton()
	{
		if(removeButton.isDisable())
			return;
		if(curClickedSite == null)
			return;
		
		try
		{
			mainApp.getSiteData().remove(curClickedSite);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * ȭ�� ȸ�� �ð��� �����Ѵ�.
	 * @param time
	 */
	private void changeRotateTimeValue(int time)
	{
		
		mainApp.setRotateTime(time);
		
		Regedit.addRegistry("rotateTime", String.valueOf(mainApp.getRotateTime()));
	}
	
	/**
	 * ȭ��ǥ ������ ���� �ε����� ���� ��Ȱ��ȭ �Ѵ�.
	 */
	private void moveButtonChanger(int index)
	{
		
		if(index == 0)
			upButton.setDisable(true);
		else
			upButton.setDisable(false);
		
		if(index == mainApp.getSiteData().size() - 1)
			downButton.setDisable(true);
		else
			downButton.setDisable(false);
		
	}
	
	/**
	 * ����Ʈ Ŭ���� �ݿ��ؼ� ���� �гο� ������ ����Ѵ�.
	 * ������ �Ұ����Ѱ���� ��Ȳ�� �ݿ��Ѵ�.
	 * @param site
	 */
	private void showSiteDetailData(Site site)
	{
		//���� �����ִ� ����Ʈ ���� ����
		curClickedSite = site;
		
		if(curClickedSite == null)
		{
			upButton.setDisable(true);
			downButton.setDisable(true);
		}
		else
		{
			upButton.setDisable(false);
			downButton.setDisable(false);
		}
			
		
		if(site != null)
		{
			name.setDisable(false);
			address.setDisable(false);
			zoom.setDisable(false);
			refreshTime.setDisable(false);
			zoomLabel.setDisable(false);
			refreshLabel.setDisable(false);
			
			name.setText(site.getName());
			address.setText(site.getAddress());
			zoom.setValue(site.getZoom());
			zoomLabel.setText(String.valueOf(site.getZoom()));
			refreshTime.setValue(site.getRefreshTime());
			
			removeButton.setDisable(false);
		}
		else
		{
			name.setDisable(true);
			address.setDisable(true);
			zoom.setDisable(true);
			refreshTime.setDisable(true);
			zoomLabel.setDisable(true);
			refreshLabel.setDisable(true);
			
			name.setText("����Ʈ �̸��� �Է��ϼ���.");
			address.setText("����Ʈ �ּҸ� �Է��ϼ���.");
			zoom.setValue(0);
			refreshTime.setValue(0);
			zoomLabel.setText("-");
			refreshLabel.setText("- ��");
			
			removeButton.setDisable(true);
		}
	}
	

	/**
	 * �ð����� ���� �� ������ ����ǵ��� �ϴ� �޼ҵ�.
	 * @param label ������ ��
	 * @param newValue ��
	 */
	private void changeLabelByTime(Label label, int newValue)
	{
		int minute;
		int second;
		
		if(newValue >= 60)
		{
			minute = newValue / 60;
			second = newValue % 60;
			if(second == 0)
				label.setText(String.valueOf(minute) + "��");
			else
				label.setText(String.valueOf(minute) + "�� " + String.valueOf(second) + "��");
		}
		else
		{
			label.setText(String.valueOf(newValue) + "��");
		}
	}
	
	/**
	 * ��� ��� ��ư�� �׼� ���¿� ���� ��Ʈ ������ �����Ѵ�.
	 * @param button ��ư
	 */
	private void changeToggleButtonColorBySelect(ToggleButton button)
	{
		if(button.isSelected())
			button.setTextFill(Color.RED);
		else
			button.setTextFill(Color.BLACK);
	}
	
	@FXML
	private void initialize()
	{
		// ����ó ���̺��� �� ���� �ʱ�ȭ�Ѵ�.
		columnName.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		columnAddress.setCellValueFactory(cellData -> cellData.getValue().getAddressProperty());
		
		//����Ʈ ���� �����
		showSiteDetailData(null);
		
		//���� ���� �� �� �� ���� ���ο� �����͸� �ʵ忡 ǥ���Ѵ�.
		siteTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSiteDetailData(newValue));
		
		zoom.valueProperty().addListener((observable, oldValue, newValue) -> zoomLabel.setText(String.valueOf(newValue.doubleValue())));
		
		refreshTime.valueProperty().addListener((observable, oldValue, newValue) -> changeLabelByTime(refreshLabel, newValue.intValue()));
		
		rotate.valueProperty().addListener((observable, oldValue, newValue) -> changeLabelByTime(rotateLabel, newValue.intValue()));
		rotate.valueProperty().addListener((observable, oldValue, newValue) -> changeRotateTimeValue(newValue.intValue() ));	//ȭ��ȸ�� ���� ����� ���ÿ� �����Ѵ�.

		refreshingButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeToggleButtonColorBySelect(refreshingButton));
		rotateButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeToggleButtonColorBySelect(rotateButton));
		
		siteTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> moveButtonChanger(newValue.intValue()));
		
	}
	
	/**
	 * mainApp�� �⺻ ������ ����
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp)
	{
		//MainApp�� ����.
		this.mainApp = mainApp;
		
		//����Ʈ �����͸� �����Ѵ�.
		siteTable.setItems(mainApp.getSiteData());
		
		//ȭ��ȸ�� �����̴��� ��۹�ư�� �ʱ�ȭ �Ѵ�.
		rotate.valueProperty().set((int)mainApp.getRotateTime());
		rotateButton.setSelected(mainApp.isRotate());
		
		//ȭ�� �������� ��� ��ư�� �ʱ�ȭ �Ѵ�.
		refreshingButton.setSelected(mainApp.isRefresh());
		
		addSiteDataToListener(mainApp.getSiteData());
	}
	
	/**
	 * ����Ʈ������ �߰�/���� ���� ������
	 * @param siteData
	 */
	private void addSiteDataToListener(ObservableList<Site> siteData)
	{
		siteData.addListener((ListChangeListener.Change<? extends Site> change) -> {

			while(change.next())
			{
				if(change.wasRemoved())
				{
					saveButton.setTextFill(Color.RED);
					actionButton.setTextFill(Color.RED);
				}
				else if(change.wasAdded())
				{
					saveButton.setTextFill(Color.RED);
					actionButton.setTextFill(Color.RED);
				}
			}
			
		});
	}
	
}

