package CustomComponent;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class StatusListCell extends ListCell<String> {
	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item,  empty);
		setGraphic(null);
		setText(null);
		if(item != null) {
			ImageView imageView = new ImageView(new Image(item));
			
			
			//imageView.setFitWidth(43);
			//imageView.setFitHeight(26);
			setGraphic(imageView);
			//setText("a");
		}
	}
}
