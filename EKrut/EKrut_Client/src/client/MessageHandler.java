package client;

import java.util.ArrayList;
import java.util.HashMap;

import Store.DataStore;
import Store.NavigationStoreController;
import common.Message;
import controller.ItemsController;
import controller.OrderController;
import controllerGui.ClientsReportController;
import controllerGui.ConfirmOnlineOrderController;
import controllerGui.CreateNewSaleController;
import controllerGui.DeliveryManagementController;
import controllerGui.LoginController;
import controllerGui.OrdersReportController;
import controllerGui.PersonalMessagesController;
import controllerGui.RegistrationFormController;
import controllerGui.ReviewOrderController;
import controllerGui.SalesManagementController;
import controllerGui.SupplyManagementController;
import controllerGui.SupplyReportController;
import controllerGui.SupplyUpdateController;
import controllerGui.UsersManagementController;
import controllerGui.ViewCatalogController;
import entity.ClientsReportEntity;
import entity.DeliveryEntity;
import entity.ItemEntity;
import entity.ItemInMachineEntity;
import entity.MachineEntity;
import entity.OrderReportEntity;
import entity.PersonalMessageEntity;
import entity.PickupEntity;
import entity.SaleEntity;
import entity.SupplyReportEntity;
import entity.UserEntity;
import enums.TaskType;

/**
 * Class handles the navigation of messages from and to server
 *
 */
public class MessageHandler {

	/**
	 * Handle the navigations of messages got from server
	 * 
	 * @param msg the Message class to navigate with
	 */
	public static void Handle(Message msg) {
		Message msgFromServer = (Message) msg;
		TaskType task = msgFromServer.getTask();
		Object obj = msgFromServer.getObject();

		// ---- Messages ---- //
		switch (task) {
		case ServerDisconnect:
			NavigationStoreController.ExitHandler(true); // force closing since server is disconnected
			break;
//--------------------------------DATA_STORE--------------------------------
		case InitRegions:
			DataStore.recieveRegions((ArrayList<String>) obj);
			break;
		case InitMachines:
			DataStore.recieveMachines((ArrayList<MachineEntity>) obj);
			break;
		case InitUsers:
			DataStore.recieveUsers((HashMap<String,String>) obj);
			break;
//--------------------------------USERS--------------------------------
		case ReceiveUserFromServerDB:
			LoginController.validUserFromServer((UserEntity) obj);
			break;
		case ReceiveUnapprovedUsers:
			UsersManagementController.recieveUnapprovedUsers((ArrayList<UserEntity>) obj);
			break;

//--------------------------------Registration Form--------------------------------
		case ReceiveUserInfoFromServerDB:
		case ReceiveUserUpdateInDB:
			RegistrationFormController.receiveDataFromServer(obj);
			break;
		case ReceiveManagerInfoFromServerDB:
			ReviewOrderController.getDataFromServer(obj);
			RegistrationFormController.receiveDataFromServer(obj);
			break;
		case ReceivePersonalMessages:
			PersonalMessagesController.getAllMessagesFromServer((ArrayList<PersonalMessageEntity>) obj);
			break;
//--------------------------------REPORT--------------------------------
		case ReceiveOrderReport:
			OrdersReportController.recieveDataFromServer((OrderReportEntity) obj);
			break;
		case ReceiveClientsReport:
			ClientsReportController.recieveDataFromServer((ClientsReportEntity) obj);
			break;
		case ReceiveSupplyReport:
			SupplyReportController.recieveDataFromServer((SupplyReportEntity) obj);
			break;
		case ReceiveAllItemsNameById:
			SupplyReportController.getAnswerFromServer(obj);
			break;
//--------------------------------DELIVERIES---------------------------------
		case ReceiveDeliveriesFromServer:
			DeliveryManagementController.getDeliveryEntityFromServer((ArrayList<DeliveryEntity>) obj);
			break;
		case ReceiveDeliveryFromServer:
			ConfirmOnlineOrderController.getDeliveryEntityFromServer((DeliveryEntity) obj);
			break;
		case ReceiveUserByOrderIdFromServerDB:
			DeliveryManagementController.getUserEntityFromServer((UserEntity) obj);
			break;
//--------------------------------SALES--------------------------------
		case ReceiveSalesFromServer:
			SalesManagementController.getSalesEntityFromServer((ArrayList<SaleEntity>) obj);
			break;
		case InsertSaleAnswer:
			CreateNewSaleController.isSaleExist((Boolean) obj);
			break;
//--------------------------------SUPPLY--------------------------------
		case ReceiveSupplyWorkersFromServer:
			SupplyManagementController.recevieSupplyWorkers((ArrayList<UserEntity>) obj);
			break;
		case InitMachinesSupplyUpdate:
			SupplyUpdateController.getAllMachines((ArrayList<MachineEntity>) obj);
			break;
		case InitMachinesInRegions:
			SupplyManagementController.getMachinesInRegion((ArrayList<MachineEntity>) obj);
			break;

		case ReceiveItemsInMachine:
			navigateItems((ArrayList<ItemInMachineEntity>) obj);
			break;
//--------------------------------ORDER--------------------------------
		case ReceiveItemsFromServer:
			
			ItemsController.getInstance().getItemsFromServer((ArrayList<ItemEntity>) obj);
			break;
		case ReviewOrderServerAnswer:
			ReviewOrderController.getDataFromServer(obj);
			break;
		case ReceiveActiveSales:
			OrderController.setActiveSales((ArrayList<SaleEntity>) obj);
			break;
		case ValidPickupAnswer:
			ConfirmOnlineOrderController.getPickupAnswer((PickupEntity) obj);
			break;

		
		default:
			break;
		}
	}

	/**
	 * Navigate items based on same calling from server, by current controller
	 * @param obj the array list of ItemInMachineEntity
	 */
	private static void navigateItems(ArrayList<ItemInMachineEntity> obj) {
		Object currentController = NavigationStoreController.getInstance().getController();
		if (currentController instanceof SupplyManagementController)
			SupplyManagementController.recevieItemsInMachine((ArrayList<ItemInMachineEntity>) obj);
		else if (currentController instanceof SupplyUpdateController)
			SupplyUpdateController.recevieItemsInMachine((ArrayList<ItemInMachineEntity>) obj);
		else
			ViewCatalogController.recevieItemsInMachine((ArrayList<ItemInMachineEntity>) obj);
	}

}
