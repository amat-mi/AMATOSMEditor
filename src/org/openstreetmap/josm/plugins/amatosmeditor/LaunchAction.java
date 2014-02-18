package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
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
import org.openstreetmap.josm.plugins.amatosmeditor.gui.AMATNavigatableComponent;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Shortcut;

public class LaunchAction extends JosmAction implements SelectionChangedListener
{
	private static final long serialVersionUID = -3508864293222033185L;
	private Way osmWay = null;

	private String pluginDir;
	private DataSet dataSet;

	public LaunchAction(String pluginDir)
	{
		super("AMATOSMEditor", 
				"amat-logo-32", 
				"Downloads from OSM and AMAT", 
				Shortcut.registerShortcut("edit:amatosmeditor", "AMATOSMEditor", KeyEvent.VK_A, Shortcut.CTRL_SHIFT),
				true);

		this.pluginDir = pluginDir;
		DataSet.addSelectionListener(this);
		setEnabled(false);

	}

	/**
	 * launch the editor
	 */
	protected void launchEditor()
	{
		if (!isEnabled())
		{
			return;
		}

		TagDialog dialog = new TagDialog(pluginDir, osmWay);
		dialog.showDialog();

	}

	private Set<Way> findWays(Node wayNode) {
		double BUFFER_SIZE = 0.00005;

		BBox bbox = wayNode.getBBox();		
		bbox.addPrimitive(wayNode, BUFFER_SIZE);
		
		Set<Way> ways = new LinkedHashSet<Way>();
		for (Node node : dataSet.searchNodes(bbox)) {
			ways.addAll(OsmPrimitive.getFilteredSet(node.getReferrers(), Way.class));
		}
		
		return ways;
	}

	/**
	 * @see Geometry
	 * @param p1
	 * @param p2
	 * @param point
	 * @return
	 */
    private static EastNorth closestPointTo(Node node1, Node node2, Node node) {    	
        CheckParameterUtil.ensureParameterNotNull(node1, "node1");
        CheckParameterUtil.ensureParameterNotNull(node2, "node2");
        CheckParameterUtil.ensureParameterNotNull(node, "node");

        EastNorth p1 = node1.getEastNorth();
        EastNorth p2 = node2.getEastNorth();
        EastNorth point = node.getEastNorth();
        
        double ldx = p2.getX() - p1.getX();
        double ldy = p2.getY() - p1.getY();

        if (ldx == 0 && ldy == 0) //segment zero length
            return null;

        double pdx = point.getX() - p1.getX();
        double pdy = point.getY() - p1.getY();

        double offset = (pdx * ldx + pdy * ldy) / (ldx * ldx + ldy * ldy);

        if (offset <= 0 || offset >= 1)
            return null;
        else
            return new EastNorth(p1.getX() + ldx * offset, p1.getY() + ldy * offset);
    }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;

		Set<Way> ways = findWays(osmWay.firstNode());
		if(ways.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for origin node"));
			return;
		}
		
		Set<Way> ways2 = findWays(osmWay.lastNode());
		if(ways2.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for destination node"));
			return;
		}
		
		ways.retainAll(ways2);
		if(ways.isEmpty()) {
			JOptionPane.showMessageDialog(Main.parent, tr("No AMAT way found for origin and destination nodes"));
			return;
		}

		if(ways.size() != 1) {
			JOptionPane.showMessageDialog(Main.parent, tr("Multiple AMAT ways found for origin and destination nodes"));
			return;
		}
		
		Way amatWay = ways.iterator().next(); 
		LatLon amatFirst = amatWay.firstNode().getCoor();
		LatLon amatLast = amatWay.lastNode().getCoor();
		LatLon osmFirst = osmWay.firstNode().getCoor();
		if(osmFirst.distance(amatFirst) > osmFirst.distance(amatLast)) {
			JOptionPane.showMessageDialog(Main.parent, tr("OSM and AMAT ways have opposite geometry directions"));
			return;
		}
				
		AMATComparePrimitiveDialog dialog = new AMATComparePrimitiveDialog(osmWay,amatWay, null);
		dialog.showDialog();
		if(dialog.getValue() != 1)
			return;
		
		List<OsmPrimitive> osmPrimitives = new ArrayList<OsmPrimitive>(1);
		osmPrimitives.add(osmWay);
		List<Command> commands = new ArrayList<Command>();
		osmWay.getDataSet().beginUpdate();

		//Copy over tags from AMAT to OSM Way, excluding keys like "AMAT%" and keys that must not be exported
		Set<String> tagsDoNotExport = new HashSet<String>();
		tagsDoNotExport.add("highway");		
		AbstractMap<String, String> tagsToAdd = new HashMap<String,String>();
		for (String key : amatWay.keySet()) {
			if(!key.startsWith("AMAT"))
				if(!tagsDoNotExport.contains(key))
					tagsToAdd.put(key, amatWay.get(key));
		}
		if(!tagsToAdd.isEmpty())
			commands.add(new ChangePropertyCommand(osmPrimitives, tagsToAdd));

		//Remove from OSM Way, tags with keys that must not be present if not present in AMAT Way
		Set<String> tagsRemoveIfAbsent = new HashSet<String>();
		tagsRemoveIfAbsent.add("oneway");
		AbstractMap<String, String> tagsToRemove = new HashMap<String,String>();
		for (String key : tagsRemoveIfAbsent) {
			if(!amatWay.hasKey(key))
				tagsToRemove.put(key, null);
		}
		if(!tagsToRemove.isEmpty())
			commands.add(new ChangePropertyCommand(osmPrimitives, tagsToRemove));
		 
		List<Node> nodesToSet = new ArrayList<Node>();			//Nodes to set into OSM Way
		List<Node> nodesToRemove = new ArrayList<Node>();		//Nodes to remove from OSM Way
		List<Node> nodesToAdd = new ArrayList<Node>();			//Nodes to add to OSM Way

		//Add a command to move OSM Way first and last Node to AMAT Way first and last Node position 
		commands.add(new MoveCommand(osmWay.firstNode(), amatFirst));
		commands.add(new MoveCommand(osmWay.lastNode(), amatLast));
		
		//Add first Node from OSM Way as first node to set in the end
		nodesToSet.add(osmWay.firstNode());

		//Remove from OSM Way any intermediate Node (not first nor last) that has no Tags
		//and move it upon the AMAT Way if it has
		if(osmWay.getNodesCount() > 2) {
			for (int i = 1; i < osmWay.getNodesCount() - 1; i++) {
				Node node = osmWay.getNode(i);
				if(!node.hasKeys() && !node.isConnectionNode()) {
//				if(!node.isTagged() && !node.isConnectionNode()) {
					nodesToRemove.add(node);
				} else {
					nodesToSet.add(node);
					commands.add(new MoveCommand(node, amatFirst));
				}
			}
		}		
		
		//If OSM Way has only two nodes (it's straight) while AMAT Way has more than two,
		//add to OSM Way a new Node for each intermediate Node in AMAT Way (not first nor last Node) 
//		if(osmWay.getNodesCount() == 2 && amatWay.getNodesCount() > 2) {
		if(nodesToSet.size() == 1 && amatWay.getNodesCount() > 2) {
			for (int i = 1; i < amatWay.getNodesCount() - 1; i++) {
				Node node = new Node(amatWay.getNode(i).getCoor()); 
				nodesToSet.add(node);
				nodesToAdd.add(node);
			}
		}
		
		//Add last Node from OSM Way as last node to set in the end
		nodesToSet.add(osmWay.lastNode());

		//Add a command for each Node to be added to DataSet 
		if(!nodesToAdd.isEmpty()) {
			for(Node node : nodesToAdd)
				commands.add(new AddCommand(node));
		}
				
		//Add a command to set the new Node list into the OSM Way
		if(!nodesToSet.isEmpty()) {
			commands.add(new ChangeNodesCommand(osmWay,nodesToSet));
		}

		//Add a command to delete Node from DataSet
		if(!nodesToRemove.isEmpty()) {
			commands.add(new DeleteCommand(nodesToRemove));
		}
		
		osmWay.getDataSet().endUpdate();
		Main.main.undoRedo.add(new SequenceCommand(tr("AMATEditor"),commands));
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
