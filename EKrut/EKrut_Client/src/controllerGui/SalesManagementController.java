package controllerGui;

import java.time.LocalTime;
import java.util.ArrayList;

import Store.NavigationStoreController;
import client.ClientController;
import common.Message;
import entity.SaleEntity;
import entity.SaleEntity.SaleStatus;
import enums.PopupTypeEnum;
import enums.RolesEnum;
import enums.SaleType;
import enums.ScreensNamesEnum;
import enums.TaskType;
import interfaces.IScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import utils.PopupSetter;
import utils.TooltipSetter;

/**
 * Sales Management GUI controller, implements Screen interface
 * creates new sale pattern 
 * @author Lidor
 *
 */
public class SalesManagementController  implements IScreen {


    @FXML
    private TableColumn<SaleEntity, LocalTime> endTimeCol;
 
    @FXML
    private TableColumn<SaleEntity, LocalTime> startTimeCol;

    @FXML
    private TableColumn<SaleEntity, SaleStatus> statusCol;

    @FXML
    private TableView<SaleEntity> salesTable;

    @FXML
    private TableColumn<SaleEntity, SaleType> saleTypeCol;

    @FXML
    private TableColumn<SaleEntity, String> daysCol;
    
    @FXML
    private Button refreshBtn;

    @FXML
    private Button returnBtn;
    
    @FXML
    private Button saveBtn;
    
    @FXML
    private GridPane btnGridPane;
    
    @FXML
    private Label regionLbl;


    private static ClientController chat = HostClientController.getChat(); // define the chat for the controller
   	private ArrayList<SaleEntity> salesToUpdate = new ArrayList<>();
   	public static ObservableList<SaleEntity> sales=FXCollections.observableArrayList();
   	private TooltipSetter tooltip;
   	
   	
	/**
	 *  Setup screen before launching view
	 */
   	@Override
	public void initialize()  {
   		try { 
    	if (sales != null)
    		sales.clear();
    	String region =NavigationStoreController.connectedUser.getRegion();
    	regionLbl.setText(region);
		chat.acceptObj(new Message(TaskType.RequestSalesFromServer, region));
		setupTable(); // setup columns connection
		if(NavigationStoreController.connectedUser.getRole_type().equals(RolesEnum.marketingManager)) {
			saveBtn.setVisible(false);
			GridPane.setColumnSpan(refreshBtn, 2);
		}
			
		tooltip = new TooltipSetter("Save changes");
		saveBtn.setTooltip(tooltip.getTooltip());
		tooltip = new TooltipSetter("Refresh");
		refreshBtn.setTooltip(tooltip.getTooltip()); 
   		}catch(Exception e) {
   			e.printStackTrace();
   		}
		
	}
   	
   	/**
   	 * refresh this page
   	 * @param event
   	 */
    @FXML
    void refresh(ActionEvent event) {
    	NavigationStoreController.getInstance().refreshStage(ScreensNamesEnum.SalesManagement);
    }
    /**
	 * Send to server the sales for update
	 * @param event
	 */
    @FXML
    void save(ActionEvent event) {
    	if (salesToUpdate.size() > 0) {
			chat.acceptObj(new Message(TaskType.RequestUpdateSales, salesToUpdate));
			salesToUpdate.clear();
			PopupSetter.createPopup(PopupTypeEnum.Success, "The discount was successfully updated.");
		}
    }
    /**
    * Sets up the table of sales data and makes it editable based on the user's role.
    * Connects the columns of the table to the appropriate fields of a SaleEntity object.
    * Sets up an event handler for when the user commits an edit on the status column of the table,
    * updates the SaleStatus of the SaleEntity object and adds it to a list of sales that need to be updated.
    */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupTable() {
		if(NavigationStoreController.connectedUser.getRole_type().equals(RolesEnum.marketingWorker))
			salesTable.setEditable(true); // make table editable
		salesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		salesTable.setItems(sales);
		// factory
		saleTypeCol.setCellValueFactory((Callback) new PropertyValueFactory<SaleEntity, String>("saleType"));
		daysCol.setCellValueFactory((Callback) new PropertyValueFactory<SaleEntity, String>("days"));
		startTimeCol.setCellValueFactory((Callback) new PropertyValueFactory<SaleEntity, LocalTime>("startTime"));
		endTimeCol.setCellValueFactory((Callback) new PropertyValueFactory<SaleEntity, LocalTime>("endTime"));
		statusCol.setCellValueFactory((Callback) new PropertyValueFactory<SaleEntity, SaleStatus>("saleStatus"));
		
		
		// define the editable cells- sale status
		ObservableList<SaleStatus> statusLst = FXCollections.observableArrayList();
		statusLst.addAll(SaleEntity.SaleStatus.values());
		statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(statusLst));
		
		statusCol.setOnEditCommit(new EventHandler<CellEditEvent<SaleEntity, SaleStatus>>() {
			@Override
			public void handle(CellEditEvent<SaleEntity, SaleStatus> event) {
				SaleEntity saleEntity = event.getRowValue();
				SaleStatus oldStatus=saleEntity.getSaleStatus();
				SaleStatus newStatus=event.getNewValue();
				if(!oldStatus.equals(newStatus)) {
					saleEntity.setSaleStatus(newStatus);
					if(!salesToUpdate.contains(saleEntity))
						salesToUpdate.add(saleEntity);
				}
			}
			
		});
	}
	
	/** gets all the sales by region  */
	public static void getSalesEntityFromServer(ArrayList<SaleEntity> saleArr) {
		sales.addAll(saleArr);
	}
	
}
