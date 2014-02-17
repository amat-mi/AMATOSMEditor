// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.amatosmeditor.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;

public class AMATToggleUploadDiscouragedLayerAction extends JosmAction {

    private AMATOsmDataLayer layer;
    
    public AMATToggleUploadDiscouragedLayerAction(AMATOsmDataLayer layer) {
        super(tr("Encourage/discourage upload"), null, null, null, false);
        this.layer = layer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        layer.setUploadDiscouraged(!layer.isUploadDiscouraged());
        LayerListDialog.getInstance().repaint();
    }
}
