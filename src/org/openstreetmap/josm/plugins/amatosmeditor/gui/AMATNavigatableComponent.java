package org.openstreetmap.josm.plugins.amatosmeditor.gui;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class AMATNavigatableComponent extends NavigatableComponent {
	private static final long serialVersionUID = 1L;
	
	private DataSet dataSet;
	
	/**
	 * 
	 */
	public AMATNavigatableComponent() {
		super();
	}

	/**
	 * @param dataSet
	 */
	public AMATNavigatableComponent(DataSet dataSet) {
		super();
		setDataSet(dataSet);
	}

	/**
	 * @return the dataSet
	 */
	public DataSet getDataSet() {
		return dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getCurrentDataSet()
	 */
	@Override
	protected DataSet getCurrentDataSet() {
		return dataSet;
	}
	
	
}
