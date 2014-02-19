package org.openstreetmap.josm.plugins.amatosmeditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;

public class AMATOSMEditorPlugin extends Plugin implements LayerChangeListener {
	AMATDownloadAction downloadAction;
	CopyWayAction copyAction;
	CompareWayAction compareAction;

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
			//NOOO!!! Do not use this right now, it is not complete, nor working!!!			
			//			this.mapFrame.addMapMode(new IconToggleButton(new AMATSelectAction(this.mapFrame)));

			MapView.addLayerChangeListener(this);
			MapView.addLayerChangeListener(downloadAction);
			downloadAction.setMapView(this.mapFrame.mapView);
		}
	}

	/////////// LayerChangeListener
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#activeLayerChange(org.openstreetmap.josm.gui.layer.Layer, org.openstreetmap.josm.gui.layer.Layer)
	 */
	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		// TODO Auto-generated method stub

	}

	/**
	 * If the added layer is the AMAT one, pass it to our actions and set it below all other
	 */
	@Override
	public void layerAdded(Layer newLayer) {
		if(newLayer instanceof AMATOsmDataLayer) {
			copyAction.setLayer(newLayer);
			compareAction.setLayer(newLayer);
			mapFrame.mapView.moveLayer(newLayer, 99999);
		}
	}

	/**
	 * If the removed layer is the AMAT one, unset it from our actions
	 */
	@Override
	public void layerRemoved(Layer oldLayer) {
		if(oldLayer instanceof AMATOsmDataLayer) {
			copyAction.setLayer(null);
			compareAction.setLayer(null);
		}
	}

	///////////
}
