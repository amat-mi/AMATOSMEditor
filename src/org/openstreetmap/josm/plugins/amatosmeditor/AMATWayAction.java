package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Shortcut;

public abstract class AMATWayAction extends JosmAction implements SelectionChangedListener
{
	private static final long serialVersionUID = -3508864293222033185L;
	protected Way osmWay = null;

	private DataSet dataSet;
	
	/**
	 * @param name
	 * @param iconName
	 * @param tooltip
	 * @param shortcut
	 * @param registerInToolbar
	 * @param toolbarId
	 * @param installAdapters
	 */
	public AMATWayAction(String name, String iconName, String tooltip,
			Shortcut shortcut, boolean registerInToolbar, String toolbarId,
			boolean installAdapters) {
		super(name, iconName, tooltip, shortcut, registerInToolbar, toolbarId,
				installAdapters);
		
		DataSet.addSelectionListener(this);
		setEnabled(false);
	}

	protected Set<Way> findWays(Node wayNode) {
		double BUFFER_SIZE = 0.00005;

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
	
	private void updateEnabled() {
		setEnabled(osmWay != null && dataSet != null);
	}

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

		updateEnabled();
	}

	public void setLayer(Layer newLayer) {
		dataSet = null;
		try {
			Field field = newLayer.getClass().getField("data");
			dataSet = (DataSet)field.get(newLayer);
		} catch (Exception e) {
		}
		updateEnabled();
	}    
}
