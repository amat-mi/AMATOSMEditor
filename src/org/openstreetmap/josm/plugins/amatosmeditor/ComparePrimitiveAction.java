package org.openstreetmap.josm.plugins.amatosmeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.Shortcut;

public class ComparePrimitiveAction extends BasePrimitiveAction
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	public ComparePrimitiveAction(String loc_refText)
	{
		super("AMAT Compare object", 
				"amat-logo-32", 
				"Compare selected OSM Way/Node with AMAT Way/Node", 
				Shortcut.registerShortcut("amatosmeditor:compareobject", 
						"AMAT Compare Way/Node", KeyEvent.VK_Q, Shortcut.CTRL_SHIFT),
				true,
				null,
				true,
				loc_refText);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;
		
		Way osmWay = findOSMWay();		
		if( osmWay != null) {			
			Way amatWay = findAMATWay();
			if( amatWay == null)
				return;

			new AMATComparePrimitiveDialog(osmWay,amatWay, null, false).showDialog();
		} else {
			Node osmNode = findOSMNode();		
			if( osmNode != null) {			
				Node amatNode = findAMATNode();
				if( amatNode == null)
					return;

				new AMATComparePrimitiveDialog(osmNode,amatNode, null, false).showDialog();
			}
		}
	}
}
