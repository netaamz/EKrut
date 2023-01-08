package controllerGui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Store.NavigationStoreController;
import client.ClientController;
import common.CommonData;
import common.Message;
import common.ScreensNames;
import common.TaskType;
import controller.ItemsController;
import controller.OrderController;
import entity.ItemEntity;
import entity.ItemInMachineEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import utils.AppConfig;

public class ViewCatalogController {

	@FXML
	private BorderPane viewCatalogBorderpane;

	@FXML
	private Button placeOrderBtn;

	@FXML
	private Button cancelOrderBtn;

	@FXML
	private Button viewCartBtn;

	@FXML
	private TextField searchTextLabel;

	@FXML
	private Label shipmentMethodLabel;

	@FXML
	private Group cartGroup;

	@FXML
	private Label cartPopupAmountLabel;

	@FXML
	private GridPane catalogViewGridpane;

	@FXML
	private Pane viewCartPane;

	@FXML
	private Label cartSizeLabel;

	@FXML
	private Label totalPriceLabel;
	
	@FXML
	private Label discountTotalLabel;

	@FXML
	private ImageView totalMoneyImage;

	@FXML
	private GridPane cartViewGridpane;

	private double machineDiscount = 1;
	private int machineId = AppConfig.MACHINE_ID;
	private ObservableList<Node> allCatalogItems;
	private String currentSupplyMethod;
	private static ClientController chat = HostClientController.chat; // define the chat for th
	private static boolean recievedData = false;


	public void initialize() throws InterruptedException, ExecutionException {
		OrderController.clearAll();
		checkRequestType();
		while (!recievedData)
			Thread.sleep(100);
		generateCatalog(OrderController.getItemsList());
		cartGroup.setVisible(false);
		viewCartPane.setVisible(false);
		viewCartPane.setMouseTransparent(true);
		searchTextLabel.textProperty().addListener((observable, oldValue, newValue) -> {
			reorderCatalog(newValue);
		});

		if (currentSupplyMethod.equals("Delivery") || !OrderController.isActiveSale()) {
			discountTotalLabel.setVisible(false);
			GridPane.setRowIndex(totalPriceLabel, 0);
			GridPane.setRowSpan(totalPriceLabel, 2);
			GridPane.setFillHeight(totalPriceLabel, true);

		}
		shipmentMethodLabel.setMouseTransparent(true);
		recievedData = false;
	}

	private void checkRequestType() {
		if (OrderController.getCurrentOrder() == null) { // EK
			OrderController.setCurrentOrder(NavigationStoreController.connectedUser.getId(), "On-site");
			OrderController.getCurrentOrder().setMachine_id(AppConfig.MACHINE_ID);
			currentSupplyMethod = OrderController.getCurrentOrder().getSupplyMethod();
			shipmentMethodLabel.setText(CommonData.getCurrentMachine().getMachineName());
			chat.acceptObj(new Message(TaskType.RequestItemsInMachine, machineId));
		} else {
			currentSupplyMethod = OrderController.getCurrentOrder().getSupplyMethod();
			switch (currentSupplyMethod) {
			case "Pickup":
				machineId = OrderController.getCurrentOrder().getMachine_id();
				shipmentMethodLabel.setText("Pickup - " + OrderController.getCurrentMachine().getMachineName());
				chat.acceptObj(new Message(TaskType.RequestItemsInMachine, machineId));
				break;
			case "Delivery":
				shipmentMethodLabel.setText("Delivery");
				generateAllItems();
				break;
			}
		}
	}

	private void generateAllItems() {
		ArrayList<ItemEntity> tempItems = ItemsController.allItems;
		for (ItemEntity item : tempItems) {
			OrderController.putItemInList(new ItemInMachineEntity(item));
		}
		recievedData = true;
	}

	@FXML
	void cancelOrder(ActionEvent event) {
		System.out.println("CANCEL");
	}

	@FXML
	void placeOrder(ActionEvent event) {
		NavigationStoreController.getInstance().refreshStage(ScreensNames.ReviewOrder);
	}

	@FXML
	void viewCart(ActionEvent event) {
		updateCartTotalLabels();
		viewCartPane.setVisible(!viewCartPane.isVisible());
	}

	private GridPane createGridPane(String boundaryName) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundary/" + boundaryName + ".fxml"));
		GridPane gridPane = (GridPane) loader.load();
		return gridPane;
	}

	/**
	 * Receives the items from a specific machine
	 * 
	 * @param obj
	 */
	public static void recevieItemsInMachine(ArrayList<ItemInMachineEntity> obj) {
		OrderController.clearItemsList();
		for (ItemInMachineEntity item : obj) {
			convertImage(item);
			OrderController.putItemInList(item);
		}
		recievedData = true;
	}

	private static void convertImage(ItemInMachineEntity item) {
		InputStream fis = new ByteArrayInputStream(item.getItemImg().mybytearray);
		Image fileImg = new Image(fis);
		item.setItemImage(fileImg);
	}

	private void generateCatalog(Map<String, ItemInMachineEntity> itemsList) throws InterruptedException, ExecutionException {
		int j = 0;
		if (OrderController.isActiveSale())
			machineDiscount = OrderController.getTotalDiscountsPercentage();
		
		ExecutorService executor = Executors.newFixedThreadPool(itemsList.size());
        List<Callable<GridPane>> tasks = new ArrayList<>();
        // Add tasks
        for (int i = 1; i <= itemsList.size(); i++) {
        	int index = i;
        	int col = (i - 1) % 4;
        	int row = i % 4 == 0 ? j++ : j;
        	tasks.add(() -> generateItem((ItemInMachineEntity) itemsList.values().toArray()[index], machineDiscount, col, row));
        }
        // Invoke all the tasks
        executor.invokeAll(tasks);
        // Shutdown the executor
        executor.shutdown();

//		for (ItemInMachineEntity item : itemsList.values()) {
//		//generateItem(item, machineDiscount, (i++) % 4, i % 4 == 0 ? j++ : j);
//	}
	
        
		allCatalogItems = FXCollections.observableArrayList(catalogViewGridpane.getChildren());
	}

	public GridPane generateItem(ItemInMachineEntity item, double discountPrice, int i, int j) {
		GridPane newItem = new GridPane();
		try {
			// Prepare the gridpanes for the items in machine
			newItem = createGridPane("ItemGridBoundary");
			ImageView image = (ImageView) newItem.getChildren().get(0);
			GridPane btnBar = (GridPane) ((ButtonBar) newItem.getChildren().get(1)).getButtons().get(0);
			Button minusBtn = (Button) btnBar.getChildren().get(0);
			Label amountLabel = (Label) btnBar.getChildren().get(1);
			Button plusBtn = (Button) btnBar.getChildren().get(2);
			Button addToCartBtn = (Button) newItem.getChildren().get(2);
			Label priceLabel = (Label) newItem.getChildren().get(3);
			Label productNameLabel = (Label) newItem.getChildren().get(4);
			Text discountPriceLabel = (Text) newItem.getChildren().get(5);
			ImageView salePersentageIconImg = (ImageView) newItem.getChildren().get(6);
			ImageView onePlusOneImg = (ImageView) newItem.getChildren().get(7);

			productNameLabel.setText(item.getName());
			if (OrderController.isOnePlusOneSaleExist() && currentSupplyMethod != "Delivery")
				onePlusOneImg.setVisible(OrderController.isOnePlusOneSaleExist());
			if (OrderController.isPercentageSaleExit() && currentSupplyMethod != "Delivery") {
				salePersentageIconImg.setVisible(OrderController.isPercentageSaleExit());
				discountPriceLabel.setVisible(OrderController.isPercentageSaleExit());
				discountPriceLabel.setText(item.getPrice() + "₪");
				discountPriceLabel.setStrikethrough(true);
				priceLabel.getStyleClass().clear();
				priceLabel.getStyleClass().add("Label-list-red");
				priceLabel.setText(String.format("%.2f₪", (OrderController.getItemPriceAfterDiscounts(item.getPrice()))));


			} else {
				priceLabel.setText(item.getPrice() + "₪");
			}

			// Prepare the gridpanes for the items in the cart
			GridPane newItemInCart = createGridPane("ItemInViewCartBoundary");
			ImageView newItemInCartImage = (ImageView) newItemInCart.getChildren().get(0);
			Label itemInCartNameLabel = (Label) newItemInCart.getChildren().get(1);
			Label itemInCartAmountLabel = (Label) newItemInCart.getChildren().get(3);
			Button itemInCartMinusBtn = (Button) newItemInCart.getChildren().get(2);
			Button itemInCartPlusBtn = (Button) newItemInCart.getChildren().get(4);
			Button deleteItemBtn = (Button) newItemInCart.getChildren().get(5);
			itemInCartNameLabel.setText(item.getName());

			itemInCartNameLabel.setWrapText(true);
			// Handle delete button
			deleteItemBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					cartPopupAmountLabel.setText(Integer.parseInt(cartPopupAmountLabel.getText())
							- OrderController.getItemAmount(item) + "");
					if (!OrderController.changeItemQuantity(item, 0))
						System.out.println("Couldn't change the item's amount");
					// Update total amount and price
					updateCartTotalLabels();
					addToCartBtn.setOpacity(1);
					addToCartBtn.setMouseTransparent(false);
					cartViewGridpane.getChildren().remove(cartViewGridpane.getChildren().indexOf(newItemInCart));
					reorderCart(cartViewGridpane);
					if (Integer.parseInt(cartPopupAmountLabel.getText()) == 0)
						cartGroup.setVisible(false);
				}
			});
			newItemInCartImage.setImage(item.getItemImage());
			image.setImage(item.getItemImage());

			// Add to cart button
			addToCartBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					// Check if it's possible to add the item
					if (item.getCurrentAmount() > 0) {
						addToCartBtn.setOpacity(0);
						addToCartBtn.setMouseTransparent(true);
						amountLabel.setText("1");
						int amount = Integer.parseInt(amountLabel.getText());
						itemInCartAmountLabel.setText(amountLabel.getText());
						cartViewGridpane.add(newItemInCart, 0, cartViewGridpane.getChildren().size());
						if (item.getCurrentAmount() == 1) {
							plusBtn.setDisable(true);
							itemInCartPlusBtn.setDisable(true);
						}
						if (!OrderController.addItemToCart(item, amount)) // Add item to cart
							System.out.println("Couldn't add the item to the cart\n");
					}
					cartPopupAmountLabel.setText(Integer.parseInt(cartPopupAmountLabel.getText()) + 1 + "");
					cartGroup.setVisible(true);
					viewCartPane.setVisible(false);
					viewCartPane.setMouseTransparent(false);

					updateCartTotalLabels();
				}
			});

			plusBtn.setOnMouseClicked(getPlusEvent(amountLabel, plusBtn, itemInCartPlusBtn, addToCartBtn, newItemInCart,
					item, itemInCartAmountLabel, true));
			minusBtn.setOnMouseClicked(getMinusEvent(amountLabel, plusBtn, itemInCartPlusBtn, addToCartBtn,
					newItemInCart, item, itemInCartAmountLabel, true));
			itemInCartPlusBtn.setOnMouseClicked(getPlusEvent(amountLabel, plusBtn, itemInCartPlusBtn, addToCartBtn,
					newItemInCart, item, itemInCartAmountLabel, false));
			itemInCartMinusBtn.setOnMouseClicked(getMinusEvent(amountLabel, plusBtn, itemInCartPlusBtn, addToCartBtn,
					newItemInCart, item, itemInCartAmountLabel, false));
			if (item.getCurrentAmount() == 0) {
				newItem.setDisable(true);
				image.setOpacity(0.5);
				btnBar.setVisible(false);
				addToCartBtn.setText("Not Available");
			}
			catalogViewGridpane.add(newItem, i, j);
			return newItem;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return newItem;
	}

	// Handle removing an item from the cart
	private EventHandler<MouseEvent> getMinusEvent(Label amountLabel, Button plusBtn, Button itemInCartPlusBtn,
			Button addToCartBtn, GridPane newItemInCart, ItemInMachineEntity item, Label itemInCartAmountLabel,
			boolean flag) {
		EventHandler<MouseEvent> minusEvent = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				int amount = Integer.parseInt(amountLabel.getText());
				amountLabel.setText(String.valueOf(amount - 1));
				amount = Integer.parseInt(amountLabel.getText());
				if (plusBtn.isDisabled()) {
					plusBtn.setDisable(false);
					itemInCartPlusBtn.setDisable(false);
				}
				if (amount == 0) {
					addToCartBtn.setOpacity(1);
					addToCartBtn.setMouseTransparent(false);
					if (!OrderController.changeItemQuantity(item, 0))
						System.out.println("Couldn't change the item's amount");

					cartViewGridpane.getChildren().remove(cartViewGridpane.getChildren().indexOf(newItemInCart));
					reorderCart(cartViewGridpane);
				} else {
					itemInCartAmountLabel.setText(amountLabel.getText());
					if (!OrderController.changeItemQuantity(item, amount))
						System.out.println("Couldn't change the item's amount");
				}
				if (flag) {
					viewCartPane.setVisible(false);
					viewCartPane.setMouseTransparent(false);
				}
				cartPopupAmountLabel.setText(Integer.parseInt(cartPopupAmountLabel.getText()) - 1 + "");
				if (Integer.parseInt(cartPopupAmountLabel.getText()) == 0)
					cartGroup.setVisible(false);
				updateCartTotalLabels();
			}
		};
		return minusEvent;
	}

	// Handle adding an item to the cart
	private EventHandler<MouseEvent> getPlusEvent(Label amountLabel, Button plusBtn, Button itemInCartPlusBtn,
			Button addToCartBtn, GridPane newItemInCart, ItemInMachineEntity item, Label itemInCartAmountLabel,
			boolean flag) {
		EventHandler<MouseEvent> plusEvent = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				int amount = Integer.parseInt(amountLabel.getText());
				amountLabel.setText(String.valueOf(amount + 1));
				amount = Integer.parseInt(amountLabel.getText());
				if (amount == item.getCurrentAmount()) {
					plusBtn.setDisable(true);
					itemInCartPlusBtn.setDisable(true);
				}
				if (!OrderController.changeItemQuantity(item, amount))
					System.out.println("Couldn't change the item's amount");

				itemInCartAmountLabel.setText(amountLabel.getText());
				if (flag) {
					viewCartPane.setVisible(false);
					viewCartPane.setMouseTransparent(false);
				}
				cartPopupAmountLabel.setText(Integer.parseInt(cartPopupAmountLabel.getText()) + 1 + "");
				updateCartTotalLabels();
			}
		};
		return plusEvent;
	}

	private void updateCartTotalLabels() {
		int totalPrice = OrderController.getTotalPrice();
		if (OrderController.getCartSize() == 0) {
			cartSizeLabel.setText("Cart is Empty");
			totalPriceLabel.setVisible(false);
			totalMoneyImage.setVisible(false);
			discountTotalLabel.setVisible(false);
		} else {
			totalMoneyImage.setVisible(true);
			totalPriceLabel.setVisible(true);
			cartSizeLabel.setText(OrderController.getCartSize() + " Items");//+ (OrderController.getTotalPrice() -  OrderController.getTotalDiscounts()) + "₪"
			

			if (currentSupplyMethod.equals("Delivery") ||  !OrderController.isActiveSale())
				totalPriceLabel.setText("Total: " + totalPrice + "₪");
			else {
				discountTotalLabel.setVisible(true);
				discountTotalLabel.setText("Discount: " + totalPrice*OrderController.getDiscountsPercentage() + "₪");
				totalPriceLabel.setText("Total: " + ( totalPrice - totalPrice*OrderController.getDiscountsPercentage()) + "₪");
			}
		}
	}

	private void reorderCart(GridPane cartViewGridpane) {
		ObservableList<Node> tempItems = FXCollections.observableArrayList(cartViewGridpane.getChildren());
		cartViewGridpane.getChildren().clear();
		int i = 0;
		for (Node n : tempItems) {
			cartViewGridpane.add(n, 0, i);
			i++;
		}

	}

	private void reorderCatalog(String newValue) {
		catalogViewGridpane.getChildren().clear();
		int i = 0, j = 0;
		if (newValue.equals("")) {
			renewCatalog();
			return;
		}
		for (Node item : allCatalogItems) {
			GridPane itemAsGrid = (GridPane) item;
			Label productNameLabel = (Label) itemAsGrid.getChildren().get(4);
			if (productNameLabel.getText().toLowerCase().contains(newValue.toLowerCase())) {
				catalogViewGridpane.add(item, (i++) % 4, i % 4 == 0 ? j++ : j);
			}
		}
	}

	private void renewCatalog() {
		int i = 0, j = 0;
		for (Node item : allCatalogItems) {
			catalogViewGridpane.add(item, (i++) % 4, i % 4 == 0 ? j++ : j);

		}
	}

}
