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
	
	//데이터
	private MainApp mainApp;
	
	private Site curClickedSite = null;
	
	//initialize 보다 먼저 시행
	public SettingsLayoutController() {
		

	}
	
	/**
	 * 노드를 한칸 위로 올리는 핸들
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
	 * 노드를 한칸 아래로 내리는 핸들
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
	 * 새로고침 동작 유무 버튼
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
	 * 화면 회전 동작 유무 버튼
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
	 * 사이트 추가
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
	 * 수정 내용 저장
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
	 * 수정 내용 적용
	 */
	@FXML
	private void handleActionButton()
	{
		mainApp.addBrowserToOverview();
		actionButton.setTextFill(Color.BLACK);
	}
	
	/**
	 * 현재 선택된 사이트 삭제
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
	 * 화면 회전 시간을 적용한다.
	 * @param time
	 */
	private void changeRotateTimeValue(int time)
	{
		
		mainApp.setRotateTime(time);
		
		Regedit.addRegistry("rotateTime", String.valueOf(mainApp.getRotateTime()));
	}
	
	/**
	 * 화살표 방향을 선택 인덱스에 따라 비활성화 한다.
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
	 * 리스트 클릭을 반영해서 우측 패널에 정보를 출력한다.
	 * 수정이 불가능한경우의 상황을 반영한다.
	 * @param site
	 */
	private void showSiteDetailData(Site site)
	{
		//현재 보고있는 리스트 정보 저장
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
			
			name.setText("사이트 이름을 입력하세요.");
			address.setText("사이트 주소를 입력하세요.");
			zoom.setValue(0);
			refreshTime.setValue(0);
			zoomLabel.setText("-");
			refreshLabel.setText("- 초");
			
			removeButton.setDisable(true);
		}
	}
	

	/**
	 * 시간값에 따라 라벨 내용이 변경되도록 하는 메소드.
	 * @param label 변경대상 라벨
	 * @param newValue 값
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
				label.setText(String.valueOf(minute) + "분");
			else
				label.setText(String.valueOf(minute) + "분 " + String.valueOf(second) + "초");
		}
		else
		{
			label.setText(String.valueOf(newValue) + "초");
		}
	}
	
	/**
	 * 대상 토글 버튼의 액션 상태에 따라 폰트 색상을 변경한다.
	 * @param button 버튼
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
		// 연락처 테이블의 두 열을 초기화한다.
		columnName.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		columnAddress.setCellValueFactory(cellData -> cellData.getValue().getAddressProperty());
		
		//사이트 정보 지우기
		showSiteDetailData(null);
		
		//선택 감지 후 그 때 마다 새로운 데이터를 필드에 표시한다.
		siteTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showSiteDetailData(newValue));
		
		zoom.valueProperty().addListener((observable, oldValue, newValue) -> zoomLabel.setText(String.valueOf(newValue.doubleValue())));
		
		refreshTime.valueProperty().addListener((observable, oldValue, newValue) -> changeLabelByTime(refreshLabel, newValue.intValue()));
		
		rotate.valueProperty().addListener((observable, oldValue, newValue) -> changeLabelByTime(rotateLabel, newValue.intValue()));
		rotate.valueProperty().addListener((observable, oldValue, newValue) -> changeRotateTimeValue(newValue.intValue() ));	//화면회전 값은 변경과 동시에 적용한다.

		refreshingButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeToggleButtonColorBySelect(refreshingButton));
		rotateButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeToggleButtonColorBySelect(rotateButton));
		
		siteTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> moveButtonChanger(newValue.intValue()));
		
	}
	
	/**
	 * mainApp과 기본 데이터 연동
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp)
	{
		//MainApp과 연동.
		this.mainApp = mainApp;
		
		//사이트 데이터를 연동한다.
		siteTable.setItems(mainApp.getSiteData());
		
		//화면회전 슬라이더와 토글버튼을 초기화 한다.
		rotate.valueProperty().set((int)mainApp.getRotateTime());
		rotateButton.setSelected(mainApp.isRotate());
		
		//화면 리프레싱 토글 버튼을 초기화 한다.
		refreshingButton.setSelected(mainApp.isRefresh());
		
		addSiteDataToListener(mainApp.getSiteData());
	}
	
	/**
	 * 사이트데이터 추가/삭제 감지 리스너
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

