package org.openstreetmap.josm.plugins.amatosmeditor;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.LayerPositionStrategy;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.amatosmeditor.actions.mapmode.AMATSelectAction;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.spi.preferences.PreferenceChangedListener;

public class AMATOSMEditorPlugin extends Plugin implements LayerChangeListener, ActiveLayerChangeListener {
	AMATDownloadAction downloadAction;
	CopyPrimitiveAction copyAction;
	ComparePrimitiveAction compareAction;
	AMATSelectAction selectAction;
	
	private MapFrame mapFrame;

	/**
	 * constructor
	 */
	public AMATOSMEditorPlugin(PluginInformation info) {
		super(info);
		downloadAction = new AMATDownloadAction(Main.pref.get("amatosm-server.url", ""));
		MainMenu mainMenu = MainApplication.getMenu();
		MainMenu.add(mainMenu.dataMenu, downloadAction, false,0);      
		copyAction = new CopyPrimitiveAction(Main.pref.get("loc_ref.text", ""));
		MainMenu.add(mainMenu.dataMenu, copyAction, false,0);      
		compareAction = new ComparePrimitiveAction(Main.pref.get("loc_ref.text", ""));
		MainMenu.add(mainMenu.dataMenu, compareAction, false,0);
		
		Config.getPref().addPreferenceChangeListener(new PreferenceChangedListener() {			
			@Override
			public void preferenceChanged(PreferenceChangeEvent e) {
				if("amatosm-server.url".equals(e.getKey()))
					downloadAction.setAmatosmUrl(Main.pref.get("amatosm-server.url", ""));
				
				if("loc_ref.text".equals(e.getKey())) {
					copyAction.setLoc_refText(Main.pref.get("loc_ref.text", ""));
					compareAction.setLoc_refText(Main.pref.get("loc_ref.text", ""));
				}
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
			MainApplication.getLayerManager().removeLayerChangeListener(this);
			MainApplication.getLayerManager().removeLayerChangeListener(downloadAction);
			MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
			downloadAction.setMapView(null);
		}

		this.mapFrame = newFrame;

		if(this.mapFrame != null) {
			MainApplication.getLayerManager().addLayerChangeListener(this);
			MainApplication.getLayerManager().addLayerChangeListener(downloadAction);
			MainApplication.getLayerManager().addActiveLayerChangeListener(this);
			downloadAction.setMapView(this.mapFrame.mapView);
			selectAction = new AMATSelectAction(this.mapFrame);
			this.mapFrame.addMapMode(new IconToggleButton(selectAction));
		}
	}
		
	/////////// LayerChangeListener
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener#layerAdded(org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent)
	 */
	/**
	 * If the added layer is the AMAT one, pass it to our actions and set it below all other
	 */
	@Override
	public void layerAdded(LayerAddEvent e) {
		Layer newLayer = e.getAddedLayer();
		if(newLayer instanceof AMATOsmDataLayer) {
			copyAction.setSrcLayer(newLayer);
			compareAction.setSrcLayer(newLayer);
			selectAction.setLayer(newLayer);
//			//mapFrame.mapView.moveLayer(newLayer, 99999);
//			//int position = LayerPositionStrategy.BEFORE_FIRST_BACKGROUND_LAYER.getPosition(MainApplication.getLayerManager());
//			int position = LayerPositionStrategy.AFTER_LAST_DATA_LAYER.getPosition(MainApplication.getLayerManager());
//			MainApplication.getLayerManager().moveLayer(newLayer, position);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener#layerRemoving(org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent)
	 */
	/**
	 * If the removed layer is the AMAT one, unset it from our actions
	 */
	@Override
	public void layerRemoving(LayerRemoveEvent e) {		
		if(e.getRemovedLayer() instanceof AMATOsmDataLayer) {
			copyAction.setSrcLayer(null);
			compareAction.setSrcLayer(null);
			selectAction.setLayer(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener#layerOrderChanged(org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent)
	 */
	@Override
	public void layerOrderChanged(LayerOrderChangeEvent e) {
	}
	///////////

	/////////// ActiveLayerChangeListener
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener#activeOrEditLayerChanged(org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent)
	 */
	@Override
	public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
		Layer newLayer = e.getSource().getActiveLayer();
		if(newLayer instanceof OsmDataLayer) {
			copyAction.setDstLayer(newLayer);
			compareAction.setDstLayer(newLayer);
		}
		
	}
	///////////
}
