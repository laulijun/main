package udo.util.ui;

import java.util.ArrayList;

import javax.swing.JPanel;

import udo.util.shared.Command;
import udo.util.shared.ItemData;
import udo.util.shared.OutputData;

public class View extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public View() {
		setOpaque(false);
	}
	
	public void populateView(ArrayList<ItemData> data) {
		removeAll();
		for (int i = 0; i < data.size(); i++) {
			ItemData item = data.get(i);
			addEntry(item);
		}
	}
	
	protected void addEntry(ItemData item) {
		Entry entry = new Entry(item, item.getItemType());
		add(entry);
	}
	
	protected void init(OutputData output, Command type) {
		//TODO also a stub
	}

}