package org.openstreetmap.josm.plugins.amatosmeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.Shortcut;

public class CompareWayAction extends BaseWayAction
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	public CompareWayAction()
	{
		super("AMAT Compare Ways", 
				"amat-logo-32", 
				"Compare selected OSM Way with AMAT Way", 
				Shortcut.registerShortcut("amatosmeditor:compareways", 
						"AMAT Compare Ways", KeyEvent.VK_Q, Shortcut.CTRL_SHIFT),
				true,
				null,
				true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;
		
		Way osmWay = findOSMWay();
		if( osmWay == null)
			return;
		
		Way amatWay = findAMATWay();
		if( amatWay == null)
			return;

		AMATComparePrimitiveDialog dialog = new AMATComparePrimitiveDialog(osmWay,amatWay, null, false);
		dialog.showDialog();
	}
}
