package com.openorpg.simpleorpg.client.components;

import com.artemis.Component;

public class Visibility extends Component {
	
	private boolean visible;
	
	public Visibility(boolean visible) {
		this.setVisible(visible);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
