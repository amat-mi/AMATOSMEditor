package org.openstreetmap.josm.plugins.amatosmeditor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.tools.Shortcut;

public class CompareWayAction extends AMATWayAction
{
	private static final long serialVersionUID = -3508864293222033185L;

	public CompareWayAction()
	{
		super("AMATOSMEditor-CompareWayAction", 
				"amat-logo-32", 
				"Compare selected OSM Way with AMAT Way", 
				Shortcut.registerShortcut("amatosmeditor:compareway", 
						"AMAT Compare Way", KeyEvent.VK_Q, Shortcut.CTRL_SHIFT),
				true,
				null,
				true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Way amatWay = findAMATWay();

		if( amatWay != null) {
			AMATComparePrimitiveDialog dialog = new AMATComparePrimitiveDialog(osmWay,amatWay, null);
			dialog.showDialog();
		}
	}
}
