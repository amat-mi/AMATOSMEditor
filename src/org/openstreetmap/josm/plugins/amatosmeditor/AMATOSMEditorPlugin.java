package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.amatosmeditor.actions.mapmode.AMATSelectAction;
import org.openstreetmap.josm.plugins.amatosmeditor.downloadtasks.AMATDownloadOsmTask;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;

public class AMATOSMEditorPlugin extends Plugin implements LayerChangeListener, EditLayerChangeListener {
   CopyWayAction copyAction;
   CompareWayAction compareAction;
   
   private MapFrame mapFrame;

   /**
    * constructor
    */
   public AMATOSMEditorPlugin(PluginInformation info) {
      super(info);
      copyAction = new CopyWayAction();
      MainMenu.add(Main.main.menu.dataMenu, copyAction, false,0);      
      compareAction = new CompareWayAction();
      MainMenu.add(Main.main.menu.dataMenu, compareAction, false,0);      
   }

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		super.mapFrameInitialized(oldFrame, newFrame);
		if(newFrame == null && this.mapFrame != null) {
			this.mapFrame.mapView.removeLayerChangeListener(this);
			this.mapFrame.mapView.removeEditLayerChangeListener(this);
		}
		
		this.mapFrame = newFrame;
		
		if(this.mapFrame != null) {
			this.mapFrame.addMapMode(new IconToggleButton(new AMATSelectAction(this.mapFrame)));
			
			this.mapFrame.mapView.addLayerChangeListener(this);
			this.mapFrame.mapView.addEditLayerChangeListener(this, true);
		}
	}

    public void openUrl(final String url) {
        PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Download Data"));
        DownloadTask task = new AMATDownloadOsmTask();
        Future<?> future = null;
        try {
            future = task.loadUrl(true, url, monitor);
        } catch (IllegalArgumentException e) {
            Main.error(e);
        }
        if (future != null) {
            Main.worker.submit(new PostDownloadHandler(task, future));
        } else {
            final String details = "Impossibile caricare dati AMATOSM";
            HelpAwareOptionPane.showMessageDialogInEDT(Main.parent, "<html><p>" + tr(
                    "Cannot open URL ''{0}''<br>The following download tasks accept the URL patterns shown:<br>{1}",
                    url, details) + "</p></html>", tr("Download Location"), JOptionPane.ERROR_MESSAGE, HelpUtil.ht("/Action/OpenLocation"));
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#layerAdded(org.openstreetmap.josm.gui.layer.Layer)
	 */
	@Override
	public void layerAdded(Layer newLayer) {		
		if(newLayer instanceof OsmDataLayer && !"AMATOSM".equalsIgnoreCase(newLayer.getName())) {
			DataSet dataset = ((OsmDataLayer)newLayer).data;
			Bounds allbounds = null;
			for (Bounds bound : dataset.getDataSourceBounds()) {
				if(allbounds == null)
					allbounds = new Bounds(bound);
				else
					allbounds.extend(bound);
			}

			if(allbounds != null && !allbounds.isCollapsed()) { 
				String url = "http://127.0.0.1:8000/osm/api/0.6/map/?bbox=9.100331411844676,45.437485608042365,9.168995962623184,45.46458092725076";
//				String url = String.format("http://127.0.0.1:8000/osm/api/0.6/map/?bbox=%s",allbounds.toBBox().toStringCSV(","));
				openUrl(url);
			}
		}	
		
		if(newLayer instanceof AMATOsmDataLayer) {
			copyAction.setLayer(newLayer);
			compareAction.setLayer(newLayer);
			mapFrame.mapView.moveLayer(newLayer, 99999);
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#layerRemoved(org.openstreetmap.josm.gui.layer.Layer)
	 */
	@Override
	public void layerRemoved(Layer oldLayer) {
		if(oldLayer instanceof AMATOsmDataLayer) {
			copyAction.setLayer(null);
			compareAction.setLayer(null);
		}
	}
	
	///////////

	/////////// EditLayerChangeListener
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.EditLayerChangeListener#editLayerChanged(org.openstreetmap.josm.gui.layer.OsmDataLayer, org.openstreetmap.josm.gui.layer.OsmDataLayer)
	 */
	@Override
	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		// TODO Auto-generated method stub
		
	}   
	
	///////////	
}
