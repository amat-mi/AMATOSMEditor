package org.openstreetmap.josm.plugins.amatosmeditor.gui;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.NavigatableComponent;

public class AMATNavigatableComponent extends NavigatableComponent {
	private static final long serialVersionUID = 1L;
	
	private DataSet dataSet;

	private NavigatableComponent nc;
	
	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getScale()
	 */
	public double getScale() {
		return nc.getScale();
	}

	/**
	 * @param newCenter
	 * @param newScale
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomTo(org.openstreetmap.josm.data.coor.EastNorth, double)
	 */
	public void zoomTo(EastNorth newCenter, double newScale) {
		nc.zoomTo(newCenter, newScale);
	}

	/**
	 * @param newCenter
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#smoothScrollTo(org.openstreetmap.josm.data.coor.EastNorth)
	 */
	public void smoothScrollTo(EastNorth newCenter) {
		nc.smoothScrollTo(newCenter);
	}

	/**
	 * @param x
	 * @param y
	 * @param factor
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomToFactor(double, double, double)
	 */
	public void zoomToFactor(double x, double y, double factor) {
		nc.zoomToFactor(x, y, factor);
	}

	/**
	 * @param factor
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomToFactor(double)
	 */
	public void zoomToFactor(double factor) {
		nc.zoomToFactor(factor);
	}

	/**
	 * 
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomPrevious()
	 */
	public void zoomPrevious() {
		nc.zoomPrevious();
	}

	/**
	 * 
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomNext()
	 */
	public void zoomNext() {
		nc.zoomNext();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getViewID()
	 */
	public int getViewID() {
		return nc.getViewID();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getCenter()
	 */
	public EastNorth getCenter() {
		return nc.getCenter();
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getEastNorth(int, int)
	 */
	public EastNorth getEastNorth(int x, int y) {
		return nc.getEastNorth(x, y);
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getProjectionBounds()
	 */
	public ProjectionBounds getProjectionBounds() {
		return nc.getProjectionBounds();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getRealBounds()
	 */
	public Bounds getRealBounds() {
		return nc.getRealBounds();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getAffineTransform()
	 */
	public AffineTransform getAffineTransform() {
		return nc.getAffineTransform();
	}

	/**
	 * @param p
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getPoint2D(org.openstreetmap.josm.data.coor.EastNorth)
	 */
	public Point2D getPoint2D(EastNorth p) {
		return nc.getPoint2D(p);
	}

	/**
	 * 
	 */
	public AMATNavigatableComponent(NavigatableComponent nc) {
		super();
		this.nc = nc;
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
	 * NOT OVERRIDE ANYMORE!!!
	 */
	protected DataSet getCurrentDataSet() {
		return dataSet;
	}
}
