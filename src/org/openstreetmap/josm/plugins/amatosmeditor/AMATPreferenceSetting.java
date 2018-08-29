package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane.PreferencePanel;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.Utils;

public class AMATPreferenceSetting extends DefaultTabPreferenceSetting {
    private JLabel lblOsmServerUrl;
    private JosmTextField tfOsmServerUrl;
    private JLabel lblLocRefText;
    private JosmTextField tfLocRefText;
	
    public AMATPreferenceSetting() {
    	super("amat-logo-60", tr("AMATOSMEditor Settings"), tr("Settings for the AMATOSMEditor plugin."));
    }
	
	@Override
	public void addGui(PreferenceTabbedPane gui) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;

        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        // the input field for the URL
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,0,3);
        pnlMain.add(lblOsmServerUrl = new JLabel(tr("AMAT OSM Server URL:")), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        pnlMain.add(tfOsmServerUrl = new JosmTextField(), gc);
        
        // the input field for the "loc_ref" text
        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 1;
        gc.weightx = 0.0;
        gc.insets = new Insets(0,0,0,3);
        pnlMain.add(lblLocRefText = new JLabel(tr("Text for the \"loc_ref\" tag:")), gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        pnlMain.add(tfLocRefText = new JosmTextField(), gc);
        
        gui.createPreferenceTab(this).add(pnlMain, gc);
		        
        tfOsmServerUrl.setText(Main.pref.get("amatosm-server.url", ""));
        tfLocRefText.setText(Main.pref.get("loc_ref.text", "loc_ref"));
	}

	@Override
	public boolean ok() {
        Main.pref.put("amatosm-server.url", Utils.strip(tfOsmServerUrl.getText()));
        String s = Utils.strip(tfLocRefText.getText());
        Main.pref.put("loc_ref.text", s.isEmpty() ? "loc_ref" : s);
        
		return false;
	}

}
