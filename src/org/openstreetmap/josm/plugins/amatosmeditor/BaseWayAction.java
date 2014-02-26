package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Shortcut;

public abstract class BaseWayAction extends JosmAction implements SelectionChangedListener
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	protected DataSet dataSet;
	protected Way osmWay = null;

	/**
	 * @param name
	 * @param iconName
	 * @param tooltip
	 * @param shortcut
	 * @param registerInToolbar
	 * @param toolbarId
	 * @param installAdapters
	 */
	public BaseWayAction(String name, String iconName, String tooltip,
			Shortcut shortcut, boolean registerInToolbar, String toolbarId,
			boolean installAdapters) {
		super(name, iconName, tooltip, shortcut, registerInToolbar, toolbarId,
				installAdapters);
		
		DataSet.addSelectionListener(this);
		setEnabled(false);
	}

	protected Set<Way> findWays(Node wayNode) {
		double BUFFER_SIZE = 0.00002;

		BBox bbox = wayNode.getBBox();		
		bbox.addPrimitive(wayNode, BUFFER_SIZE);
		
		Set<Way> ways = new LinkedHashSet<Way>();
		for (Node node : dataSet.searchNodes(bbox)) {
			ways.addAll(OsmPrimitive.getFilteredSet(node.getReferrers(), Way.class));
		}
		
		return ways;
	}

	protected Way findAMATWay() {
		if(!isEnabled())
			return null;

		Set<Way> ways = findWays(osmWay.firstNode());
		if(ways.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for origin node"));
			return null;
		}
		
		Set<Way> ways2 = findWays(osmWay.lastNode());
		if(ways2.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for destination node"));
			return null;
		}
		
		ways.retainAll(ways2);
		if(ways.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for origin and destination nodes"));
			return null;
		}

		if(ways.size() != 1) {
			JOptionPane.showMessageDialog(Main.parent, tr("Multiple AMAT ways found for origin and destination nodes"));
			return null;
		}
		
		Way amatWay = ways.iterator().next(); 
		LatLon amatFirst = amatWay.firstNode().getCoor();
		LatLon amatLast = amatWay.lastNode().getCoor();
		LatLon osmFirst = osmWay.firstNode().getCoor();
		if(osmFirst.distance(amatFirst) > osmFirst.distance(amatLast)) {
			JOptionPane.showMessageDialog(Main.parent, tr("OSM and AMAT ways have opposite geometry directions"));
			return null;
		}
		
		return amatWay;
	}
		
	@Override
	protected void updateEnabledState() {
		super.updateEnabledState();
		setEnabled(dataSet != null && osmWay != null);
	}

	public void setLayer(Layer newLayer) {
		dataSet = null;
		try {
			Field field = newLayer.getClass().getField("data");
			dataSet = (DataSet)field.get(newLayer);
		} catch (Exception e) {
		}
		updateEnabledState();
	}
	
	/////////// SelectionChangedListener
	
	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection)
	{
		osmWay = null;
		if ((newSelection != null && newSelection.size() == 1))
		{
			OsmPrimitive primitive = newSelection.iterator().next();
			if(primitive instanceof Way)
				osmWay = (Way)primitive;
		}

		updateEnabledState();
	}    

	///////////
}
