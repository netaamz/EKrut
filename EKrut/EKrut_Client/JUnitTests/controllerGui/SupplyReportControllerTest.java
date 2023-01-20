package controllerGui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import client.ChatClient;
import client.ClientController;
import common.Message;
import entity.SupplyReportEntity;
import enums.TaskType;

class SupplyReportControllerTest {
	private Method initDetailsMethod;
	private Field actualResult;
	private SupplyReportController supplyReportController;
	private ClientController chatService;
	private int machineID;

	private boolean compareReports(SupplyReportEntity compareReport, SupplyReportEntity toReport) {
		ArrayList<Boolean> boolArray = new ArrayList<>();
		boolArray.add(compareReport.getId() == toReport.getId());
		boolArray.add(compareReport.getYear() == toReport.getYear());
		boolArray.add(compareReport.getMonth() == toReport.getMonth());
		boolArray.add(compareReport.getMachine_id() == toReport.getMachine_id());
		for (String[] itemSet : compareReport.getReportsList())
			boolArray.add(toReport.getReportsList().contains(itemSet));
		if (boolArray.contains(false)) return false;
		return true;
	}

	@BeforeEach
	void setUp() throws Exception {
		supplyReportController = new SupplyReportController();
		// Handle this differently later, currently you need to run the server
		chatService = new ClientController("localhost", 5555);
		supplyReportController.setChatService(chatService);
	}

	@Test
	void testSuccessfulSupplyReport() throws Exception {

		SupplyReportController.setReport("2022", "12", "1");
		SupplyReportEntity actualResult = supplyReportController.getSupplyReportFromDB(1);

		SupplyReportEntity expectedResult = setExpectedResult(63, 1, 7,
				"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26",
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0",
				"10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10", "12", "2022", "1");

		assertTrue(compareReports(actualResult, expectedResult));

	}

	private SupplyReportEntity setExpectedResult(int id, int machine_id, int min_stock, String item_id,
			String times_under_min, String end_stock, String month, String year, String region) {
		return new SupplyReportEntity(id, machine_id, min_stock, item_id, times_under_min, end_stock, month, year,
				region);
	}

}
