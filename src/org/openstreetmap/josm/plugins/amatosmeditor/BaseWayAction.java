package org.openstreetmap.josm.plugins.amatosmeditor;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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

public abstract class BaseWayAction extends JosmAction
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	protected DataSet srcDataSet;
	protected DataSet dstDataSet;
	protected String loc_refText;

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
			boolean installAdapters,String loc_refText) {
		super(name, iconName, tooltip, shortcut, registerInToolbar, toolbarId,
				installAdapters);
		
		this.loc_refText = loc_refText;
				
		updateEnabledState();
	}
	
	/**
	 * @return the loc_refText
	 */
	public String getLoc_refText() {
		return loc_refText;
	}

	/**
	 * @param loc_refText the loc_refText to set
	 */
	public void setLoc_refText(String loc_refText) {
		this.loc_refText = loc_refText;
		updateEnabledState();				//see if we're enabled	
	}

	/**
	 * Search for Ways that are referrers of any Node near the specified one.
	 * Try with a bigger buffer around the specified Node first, and try with smaller ones 
	 * only if more than one Node found nearby.
	 * Give up as soon as no Node is found inside the buffer.
	 * Note that only first and last Nodes of Ways are considered for matching.
	 * @param wayNode
	 * @return A Set of all the Ways found to be referrers of a Node near the specified one.
	 */
	protected Set<Way> findWays(Node wayNode) {		
		double BUFFER_SIZE = 0.00005;
		while(BUFFER_SIZE >= 0.000008) {
			BBox bbox = wayNode.getBBox();
			bbox.addPrimitive(wayNode, BUFFER_SIZE);
			List<Node> nodes = srcDataSet.searchNodes(bbox);
			if(nodes.isEmpty())			//if no Node found
				return null;				//no point trying a smaller buffer

			Set<Node> foundNodes = new LinkedHashSet<Node>();
			Set<Way> foundWays = new LinkedHashSet<Way>();
			for (Node node : nodes)			
				for (Way way : OsmPrimitive.getFilteredSet(node.getReferrers(), Way.class))
					if(way.isFirstLastNode(node)) {
						foundNodes.add(node);			//add Node to set of first/last Nodes
						foundWays.add(way);				//add Ways to set of Way found by first/last Node
					}
			
			if(foundNodes.size() == 1)			//if exactly one first/last Node found
				return foundWays;					//return found Ways
			
			BUFFER_SIZE *= 0.8;					//try with a smaller box size
		}		
		
		return null;
	}

	/**
	 * Check if the two specified Ways have the same geometric direction.
	 * The test is made by turning the heading of the second Way by the same amount that is needed to have
	 * the first heading point towards 90° (PI / 2). 
	 * If the second heading is between 0° and 180° (0 and PI), it means the difference between the two headings 
	 * is no more than 90°, so they have the same direction. 
	 * @param way1 The first Way to check
	 * @param way2 The second Way to check
	 * @return true if the two specified Ways have the same geometric direction
	 */
	protected boolean haveSameDirection(Way way1,Way way2) {
		LatLon first1 = way1.firstNode().getCoor();
		LatLon last1 = way1.lastNode().getCoor();
		LatLon first2 = way2.firstNode().getCoor();
		LatLon last2 = way2.lastNode().getCoor();
		double h1 = first1.bearing(last1);
		double h2 = first2.bearing(last2);
		h2 = (h2 + PI / 2 - h1) % (2 * PI);
		if(h2 < 0)
			h2 = (2 * PI + h2)  % (2 * PI);
		
		return h2 >= 0 && h2 <= PI;
	}

	/**
	 * Check if the two specified Ways are "too far away" from one another.
	 * The test is made by comparing the distances of the first and last Node of each Way.
	 * If first Nodes or last Nodes are too distant, the two Ways are considered to be too far away.   
	 * @param way1 The first Way to check
	 * @param way2 The second Way to check
	 * @return true if the two specified Ways have the same geometric direction
	 */
	protected boolean tooFarAway(Way way1,Way way2,double maxDistance) {
		if(way1.firstNode().getCoor().greatCircleDistance(way2.firstNode().getCoor()) > maxDistance)
			return true;
		
		if(way1.lastNode().getCoor().greatCircleDistance(way2.lastNode().getCoor()) > maxDistance)
			return true;
		
		return false;
	}

	protected Way findOSMWay() {
		if(!isEnabled())
			return null;

		Collection<Way> selectedWays = dstDataSet.getSelectedWays();
		return selectedWays.size() == 1 ? selectedWays.iterator().next() : null;
	}
	
	protected Way findAMATWay() {
		if(!isEnabled())
			return null;

		Way osmWay = findOSMWay();
		if( osmWay == null)
			return null;
		
		Way amatWay = null;					//by default no single AMAT Way found 
		
		//If a single AMAT Way is selected, use that, otherwise search for a Way by near Nodes
		Collection<Way> selectedWays = srcDataSet.getSelectedWays();
		if(selectedWays.size() == 1)
			amatWay = selectedWays.iterator().next();
		else {
			Set<Way> ways = findWays(osmWay.firstNode());
			if(ways == null) {
				JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for origin node"));
				return null;
			}
			
			Set<Way> ways2 = findWays(osmWay.lastNode());
			if(ways2 == null) {
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

			amatWay = ways.iterator().next(); 
		}
		
		if(!haveSameDirection(osmWay, amatWay)) {
			JOptionPane.showMessageDialog(Main.parent, tr("OSM and AMAT ways have opposite geometric direction"));
			return null;			
		}
		
		if(tooFarAway(osmWay, amatWay, 100)) {
			if(JOptionPane.showConfirmDialog(
					Main.parent, 
					new String[] {tr("OSM and AMAT ways are very far away"),tr("Continue?")},
					tr("Warning"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION)
				return null;			
		}
		
		return amatWay;
	}
		
	@Override
	protected void updateEnabledState() {
		super.updateEnabledState();
		setEnabled(srcDataSet != null && dstDataSet != null);
	}

	public DataSet getDataSetFromLayer(Layer layer) {
		try {
			Field field = layer.getClass().getField("data");
			
			return (DataSet)field.get(layer);
		} catch (Exception e) {
		}
		
		return null;
	}
	
	public void setSrcLayer(Layer layer) {
		srcDataSet = getDataSetFromLayer(layer);
		updateEnabledState();
	}
	
	public void setDstLayer(Layer layer) {
		dstDataSet = getDataSetFromLayer(layer);
		updateEnabledState();
	}	
}
