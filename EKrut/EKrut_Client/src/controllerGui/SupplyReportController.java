package controllerGui;

import java.util.ArrayList;
import java.util.Comparator;
import Store.DataStore;
import Store.NavigationStoreController;
import client.ClientController;
import common.CommonFunctions;
import common.Message;
import common.PopupTypeEnum;
import common.TaskType;
import entity.MachineEntity;
import entity.SupplyReportEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class SupplyReportController implements IScreen {

	@FXML
	private BarChart<String, Integer> supplySBC;

	@FXML
	private CategoryAxis xAxisSBC;

	@FXML
	private NumberAxis yAxisSBC;

	@FXML
	private Label titleLabel;

	@FXML
	private ComboBox<String> machineIdComboBox;

	@FXML
	private PieChart pieChart;

	@FXML
	private Label textConclusionsLbl;

	protected static ClientController chat = HostClientController.getChat();
	protected static SupplyReportEntity reportDetails;
	private SupplyReportEntity currentReport;
	protected static boolean RecievedData = false;
	protected static Object answerFromServer;
	private static ArrayList<MachineEntity> allMachines;
	private static String reportYear, reportMonth, reportRegion;
	private String machineName;
	private int machineID;

	@Override
	public void initialize() {
		titleLabel.setText("Supply Report : " + reportRegion);
		supplySBC.setAnimated(false);
		textConclusionsLbl.setVisible(false);
		allMachines = DataStore.getMachines();
		ObservableList<String> machines = FXCollections.observableArrayList();
		for (MachineEntity machine : allMachines) {
			if (NavigationStoreController.connectedUser != null)
				if (machine.getRegionName().equals(reportRegion)) {
					machines.add(machine.getMachineName());
				}
		}
		machineIdComboBox.setItems(machines);
		machineIdComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			machineName = machineIdComboBox.getValue();
			for (MachineEntity machine : allMachines) {
				if (machine.getMachineName().equals(machineName))
					machineID = machine.getId();
			}
			initBarChart(machineID);

		});
	}

	public static void setReport(String year, String month, String region) {
		reportYear = year;
		reportMonth = month;
		reportRegion = region;
		return;
	}

	public static void recieveDataFromServer(SupplyReportEntity report) {
		reportDetails = report;
		RecievedData = true;
		return;
	}
	
	public static void getAnswerFromServer(Object obj) {
		answerFromServer = obj;
		RecievedData = true;
		return;
	}
	

//	public void checkCurAmount() {
//		supplyMachineTbl.setRowFactory(row -> new TableRow<SupplyReportEntity>(){
//		    @Override
//		    public void updateItem(SupplyReportEntity report, boolean empty){
//		        super.updateItem(report, empty);
//
//		        if (report == null || empty) {
//		            setStyle("");
//		        } else {
//		            if (report.getCur_stock() <= report.getMin_stock()) {
//		                //We apply now the changes in all the cells of the row
//		                for(int i=0; i<getChildren().size();i++){
//		                    ((Labeled) getChildren().get(i)).setTextFill(Color.RED);
//		                    //((Labeled) getChildren().get(i)).setStyle("-fx-background-color: yellow");
//		                }                        
//		            }
//		        }
//		    }
//		});
//	}

	/**
	 * @param machineID
	 */
	private void initBarChart(int machineID) {
		XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
		XYChart.Series<String, Integer> series2 = new XYChart.Series<>();
		ObservableList<javafx.scene.chart.PieChart.Data> list = FXCollections.observableArrayList();
		// yAxisSBC.setAutoRanging(false);
		yAxisSBC.setLowerBound(15);
		
		RecievedData = false; 			// reset each operation
		supplySBC.getData().clear(); 	// clear previous if exists
		pieChart.getData().clear();
		textConclusionsLbl.setText("");

		// sends the user information to server
		chat.acceptObj(new Message(TaskType.RequestReport,
				new String[] { "supply", reportRegion, reportMonth, reportYear, String.valueOf(machineID) }));

		// wait for answer
		while (RecievedData == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		currentReport = reportDetails;
		ArrayList<String[]> itemsArray = reportDetails.getReportsList();
		// [name,min,cur,start,severity]
		if (itemsArray == null) {
			CommonFunctions.createPopup(PopupTypeEnum.Error, "No Report Found!");
			return;
		}

		ArrayList<String> itemsNames = getItemsNamesByID(itemsArray.get(0));
		
		ArrayList<String> startAmount = intersectItems(machineID);
		int i = 0;
		for (String[] item : itemsArray) {
			series1.getData().add(new XYChart.Data<String, Integer>(itemsNames.get(i), Integer.parseInt(startAmount.get(i))));
			series2.getData().add(new XYChart.Data<String, Integer>(itemsNames.get(i), Integer.parseInt(item[3])));
			list.add(new PieChart.Data(item[0], Integer.parseInt(item[4])));
			i++;
		}
		series1.setName("Month Start Amount");
		series2.setName("Month End Amount");
		supplySBC.getData().addAll(series1, series2);

		// set pieChart
		pieChart.setData(list);

		// set textual conclusions
		String conclusions = "";
		String itemsAmount = "";
		list = list.sorted(new Comparator<PieChart.Data>() {
			@Override
			public int compare(javafx.scene.chart.PieChart.Data o1, javafx.scene.chart.PieChart.Data o2) {
				return ((Double) o1.getPieValue()).compareTo(o2.getPieValue());
			}
		});
		itemsAmount += list.size() >= 1
				? list.get(list.size() - 1).getName() + " : " + list.get(list.size() - 1).getPieValue() + "\n"
				: "";
		itemsAmount += list.size() >= 2
				? list.get(list.size() - 2).getName() + " : " + list.get(list.size() - 2).getPieValue() + "\n"
				: "";
		itemsAmount += list.size() >= 3
				? list.get(list.size() - 3).getName() + " : " + list.get(list.size() - 3).getPieValue() + "\n"
				: "";
		conclusions = "Top 3 items that were filled \nthe most this month\n\n" + itemsAmount + "\n";
		conclusions += "The product consumption was\n";
		conclusions += list.get(list.size() - 1).getPieValue() > 10 ? "high"
				: list.get(list.size() - 1).getPieValue() >= 5 ? "medium" : "low";
		textConclusionsLbl.setText(conclusions);
		textConclusionsLbl.setVisible(true);
	}

	/**
	 * Handle sending list of itemsID and getting their names
	 * @param itemsID
	 * @return
	 */
	private ArrayList<String> getItemsNamesByID(String[] itemsID){
		// change itemsID from array of string to list of int
		ArrayList<Integer> listToSend = new ArrayList<>();
		for(String item : itemsID) listToSend.add(Integer.parseInt(item));
		
		RecievedData = false;
		chat.acceptObj(new Message(TaskType.RequestAllItemsNameById, listToSend));
		return (ArrayList<String>)answerFromServer;
	}
	
	/**
	 * match between previous report and current
	 * @param machineID
	 * @return
	 */
	private ArrayList<String> intersectItems(int machineID) {
		ArrayList<String> startAmounts = new ArrayList<>();
		// prepare
		SupplyReportEntity prevSupplyReport = getPrevSupplyReportForMachine(machineID);
		if (prevSupplyReport.getReportsList() == null)
			return startAmounts; // what to do here?
		int j = 0;
		for (String itemID : currentReport.getReportsList().get(0)) // iterate over the current items_id
		{
			// search this itemID in the previous report
			for (String prevReportItemID : prevSupplyReport.getReportsList().get(0)) {
				if (itemID.equals(prevReportItemID)) {
					// match! now get the end amount of this id and add to arraylist
					startAmounts.add(prevSupplyReport.getReportsList().get(2)[j]);
					j++;
					break;
				}
				j++;
			}
		}

		return startAmounts;

	}

	/**
	 * return the previous report
	 * @param machineID
	 * @return
	 */
	private SupplyReportEntity getPrevSupplyReportForMachine(int machineID) {
		// get supply report of last month for this machine
		String month = reportMonth;
		String year = reportYear;
		if (month.equals("01")) {
			month = "12";
			year = String.valueOf(Integer.parseInt(year) - 1);
		} else {
			month = String.valueOf(Integer.parseInt(CommonFunctions.getNumericMonth(month)) - 1);
		}
		RecievedData = false; // reset each operation
		// sends the user information to server
		chat.acceptObj(new Message(TaskType.RequestReport,
				new String[] { "supply", reportRegion, month, year, String.valueOf(machineID) }));

		// iterate over curr_report and prev_report items
		// wait for answer
		while (RecievedData == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return reportDetails;

	}

}
