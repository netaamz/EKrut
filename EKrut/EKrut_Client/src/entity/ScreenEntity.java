package entity;

import common.CommonFunctions;
import common.ScreensNames;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class ScreenEntity {
	private ScreensNames sc;
	private Scene scene;
	private Label headline, path;
	
	public ScreenEntity(ScreensNames sc, Scene scene) {
		super();
		this.sc = sc;
		this.scene = scene;

	}
	
	@Override
	public String toString() {
		return CommonFunctions.splitByUpperCase(sc.toString());
	}
	
	public ScreensNames getSc() {
		return sc;
	}
	public void setSc(ScreensNames sc) {
		this.sc = sc;
	}
	public Scene getScene() {
		return scene;
	}
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	public Label getHeadline() {
		return headline;
	}
	public void setHeadline(Label headline) {
		this.headline = headline;
	}
	public Label getPath() {
		return path;
	}
	public void setPath(Label path) {
		this.path = path;
	}
	
	
}
