package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Shortcut;

public class CopyWayAction extends BaseWayAction
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	public CopyWayAction()
	{
		super("AMAT Copy Way", 
				"amat-logo-32", 
				"Copy AMAT Way data into selected OSM Way", 
				Shortcut.registerShortcut("amatosmeditor:copyway", 
						"AMAT Copy Way", KeyEvent.VK_A, Shortcut.CTRL_SHIFT),
				true,
				null,
				true);
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

        return offset <= 0 || offset >= 1 ? null :
        	node.getEastNorth().sub(new EastNorth(p1.getX() + ldx * offset, p1.getY() + ldy * offset));
    }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;
		
		Way amatWay = findAMATWay();
		if( amatWay == null)
			return;

		//Copy over tags from AMAT to OSM Way, excluding keys like "AMAT%" and keys that must not be exported
		//Ignore tag already present in OSM Way with the same value as in AMAT Way
		//Set a flag if any tag was already present in OSM Way with a different value		
		Set<String> tagsDoNotExport = new HashSet<String>();
		tagsDoNotExport.add("highway");		
		AbstractMap<String, String> tagsToSet = new HashMap<String,String>();
		boolean needConfirmation = false;
		for (String key : amatWay.keySet()) {
			if(!key.startsWith("AMAT"))
				if(!tagsDoNotExport.contains(key)) {
					String amatValue = amatWay.get(key);
					if(osmWay.hasKey(key)) {
						String osmValue = osmWay.get(key);
						if((osmValue == null && amatValue != null) || (osmValue != null && amatValue == null)) {
							tagsToSet.put(key, amatValue);
							needConfirmation = true;
						} else if(osmValue != null && amatValue != null) {
							if(osmValue.compareTo(amatValue) != 0) {
								tagsToSet.put(key, amatValue);		
								needConfirmation = true;
							}
						}
					} else
						tagsToSet.put(key, amatValue);
				}
		}

		//Remove from OSM Way, tags with keys that must not be present if not present in AMAT Way
		//Do it only if they're actually present in OSM Way
		//Set a flag if any tag removed		
		Set<String> tagsRemoveIfAbsent = new HashSet<String>();
		tagsRemoveIfAbsent.add("oneway");
		for (String key : tagsRemoveIfAbsent) {
			if(!amatWay.hasKey(key) && osmWay.hasKey(key)) {
				tagsToSet.put(key, null);
				needConfirmation = true;
			}
		}

		//By default should copy geometry, loc_ref and other tags from AMAT Way to OSM Way
		boolean geomConfirmed = true;
		boolean locrefConfirmed = true;
		boolean tagsConfirmed = true;
		
		//If any tag to change or to delete in OSM Way, show comparison dialog and check for confirmation
		if(needConfirmation) {
			AMATComparePrimitiveDialog dialog = new AMATComparePrimitiveDialog(osmWay,amatWay, null, true);
			dialog.showDialog();
			if(dialog.isCancelled() )
				return;
			
			geomConfirmed = dialog.isGeomConfirmed();
			locrefConfirmed = dialog.isLocRefConfirmed();
			tagsConfirmed = dialog.isTagsConfirmed();
		}

		//Create a list of commands to submit as a sequence to the undo/redo system
		List<Command> commands = new ArrayList<Command>();
		
		//if any change or deletion of tags
		if(!tagsToSet.isEmpty()) {
			//If change of tags confirmed add a command for tags changes
			if(tagsConfirmed)
				commands.add(new ChangePropertyCommand(Collections.singletonList(osmWay), tagsToSet));
			 
			//If change of tag loc_ref confirmed add a command for tag change
			if(locrefConfirmed) {
				if(tagsToSet.containsKey("loc_ref"))
					commands.add(new ChangePropertyCommand(Collections.singletonList(osmWay), 
							"loc_ref",tagsToSet.get("loc_ref")));
			}			
		}
		 
		//If creation of geometry confirmed (or needed anyway) 
		if(geomConfirmed) {
			List<Node> nodesToSet = new ArrayList<Node>();			//Nodes to set into OSM Way
			List<Node> nodesToRemove = new ArrayList<Node>();		//Nodes to remove from OSM Way
			List<Node> nodesToNotRemove = new ArrayList<Node>();	//Nodes to not remove from OSM Way
			List<Node> nodesToAdd = new ArrayList<Node>();			//Nodes to add to OSM Way
	
			//Add a command to move OSM Way first and last Node to AMAT Way first and last Node position
			commands.add(new MoveCommand(osmWay.firstNode(), amatWay.firstNode().getCoor()));
			commands.add(new MoveCommand(osmWay.lastNode(), amatWay.lastNode().getCoor()));
			
			//Add first Node from OSM Way as first node to set in the end
			nodesToSet.add(osmWay.firstNode());
	
			//Remove from OSM Way any intermediate Node (not first nor last) that has no Tags 
			//and that is not a connection with other Ways
			//Store references to nodes not removed
			if(osmWay.getNodesCount() > 2) {
				for (int i = 1; i < osmWay.getNodesCount() - 1; i++) {
					Node node = osmWay.getNode(i);
//					if(!node.hasKeys() && !node.isConnectionNode()) {       //which one is better???
					if(!node.isTagged() && !node.isConnectionNode()) {		//which one is better???
						nodesToRemove.add(node);
					} else {
						nodesToNotRemove.add(node);
					}
				}
			}		
	
			//For each pair of Nodes in AMAT Way, look if there is a not removed Node in OSM Way
			//that can be projected on the segment delimited by the pair itself.
			//If there's, add it to the set of Nodes to be kept in OSM Way and move it on the projection.
			//Then add to OSM Way each intermediate Node in AMAT Way (not first nor last Node)
			//But only if AMAT Way has more than two Nodes or there are not removed Node in OSM Way
			if(amatWay.getNodesCount() > 2 || !nodesToNotRemove.isEmpty()) {
				for (int i = 0; i < amatWay.getNodesCount() - 1; i++) {
					Node node1 = amatWay.getNode(i);
					Node node2 = amatWay.getNode(i + 1);
					for (Node node : nodesToNotRemove) {
						EastNorth en = closestPointTo(node1, node2, node);
						if(en != null) {
							commands.add(new MoveCommand(Collections.singleton((OsmPrimitive)node),en));						
							nodesToSet.add(node);
						}
					}
					
					if(!amatWay.isFirstLastNode(node2)) {
						Node newnode = new Node(node2.getCoor());
						nodesToSet.add(newnode);
						nodesToAdd.add(newnode);
					}
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
		}
		
//		osmWay.getDataSet().beginUpdate();
//		osmWay.getDataSet().endUpdate();
		//If there actually is some command to execute, add a sequence to the undo/redo system 
		if(!commands.isEmpty())
			Main.main.undoRedo.add(new SequenceCommand(tr("AMATEditor"),commands));
	}
}
