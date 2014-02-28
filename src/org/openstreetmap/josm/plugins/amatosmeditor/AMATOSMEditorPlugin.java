package org.openstreetmap.josm.plugins.amatosmeditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.amatosmeditor.actions.mapmode.AMATSelectAction;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;

public class AMATOSMEditorPlugin extends Plugin implements LayerChangeListener {
	AMATDownloadAction downloadAction;
	CopyWayAction copyAction;
	CompareWayAction compareAction;
	AMATSelectAction selectAction;
	
	private MapFrame mapFrame;

	/**
	 * constructor
	 */
	public AMATOSMEditorPlugin(PluginInformation info) {
		super(info);
		downloadAction = new AMATDownloadAction(Main.pref.get("amatosm-server.url", ""));
		MainMenu.add(Main.main.menu.dataMenu, downloadAction, false,0);      
		copyAction = new CopyWayAction();
		MainMenu.add(Main.main.menu.dataMenu, copyAction, false,0);      
		compareAction = new CompareWayAction();
		MainMenu.add(Main.main.menu.dataMenu, compareAction, false,0);
		
		Main.pref.addPreferenceChangeListener(new PreferenceChangedListener() {			
			@Override
			public void preferenceChanged(PreferenceChangeEvent e) {
				if("amatosm-server.url".equals(e.getKey()))
					downloadAction.setAmatosmUrl(Main.pref.get("amatosm-server.url", ""));
			}
		});
	}
	
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new AMATPreferenceSetting();
    }

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		super.mapFrameInitialized(oldFrame, newFrame);
		if(newFrame == null && this.mapFrame != null) {
			MapView.removeLayerChangeListener(this);
			MapView.removeLayerChangeListener(downloadAction);
			downloadAction.setMapView(null);
		}

		this.mapFrame = newFrame;

		if(this.mapFrame != null) {
			MapView.addLayerChangeListener(this);
			MapView.addLayerChangeListener(downloadAction);
			downloadAction.setMapView(this.mapFrame.mapView);
			selectAction = new AMATSelectAction(this.mapFrame);
			this.mapFrame.addMapMode(new IconToggleButton(selectAction));
		}
	}

	/////////// LayerChangeListener
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#activeLayerChange(org.openstreetmap.josm.gui.layer.Layer, org.openstreetmap.josm.gui.layer.Layer)
	 */
	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if(newLayer instanceof OsmDataLayer) {
			copyAction.setDstLayer(newLayer);
			compareAction.setDstLayer(newLayer);
		}
	}

	/**
	 * If the added layer is the AMAT one, pass it to our actions and set it below all other
	 */
	@Override
	public void layerAdded(Layer newLayer) {
		if(newLayer instanceof AMATOsmDataLayer) {
			copyAction.setSrcLayer(newLayer);
			compareAction.setSrcLayer(newLayer);
			selectAction.setLayer(newLayer);
			mapFrame.mapView.moveLayer(newLayer, 99999);
		}
	}

	/**
	 * If the removed layer is the AMAT one, unset it from our actions
	 */
	@Override
	public void layerRemoved(Layer oldLayer) {
		if(oldLayer instanceof AMATOsmDataLayer) {
			copyAction.setSrcLayer(null);
			compareAction.setSrcLayer(null);
			selectAction.setLayer(null);
		}
	}
	///////////
}
