package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Utils;

public class AMATPreferenceSetting extends DefaultTabPreferenceSetting {
    private JLabel lblApiUrl;
    private JosmTextField tfOsmServerUrl;
	
    public AMATPreferenceSetting() {
    	super("connection", tr("AMAT Connection Settings"), tr("Connection Settings for the AMATOSM server."));
    }
	
	@Override
	public void addGui(PreferenceTabbedPane gui) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
		
        JPanel amatosmPanel = new JPanel(new GridBagLayout());
        amatosmPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        // the input field for the URL
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,0,3);
        amatosmPanel.add(lblApiUrl = new JLabel(tr("AMAT OSM Server URL:")), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        amatosmPanel.add(tfOsmServerUrl = new JosmTextField(), gc);
        
        gui.createPreferenceTab(this).add(amatosmPanel, gc);
		        
        tfOsmServerUrl.setText(Main.pref.get("amatosm-server.url", ""));
	}

	@Override
	public boolean ok() {
        Main.pref.put("amatosm-server.url", Utils.strip(tfOsmServerUrl.getText()));
        
		return false;
	}

}
