package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadParams;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.amatosmeditor.downloadtasks.AMATDownloadOsmTask;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

public class AMATDownloadAction extends JosmAction implements LayerChangeListener
{
	private static final long serialVersionUID = -3508864293222033185L;
	
	private MapView mapView = null;
	private String amatosmUrl;
	
	public AMATDownloadAction(String amatosmUrl) {
		super("AMAT Download", 
				"amat-logo-32", 
				"Download AMAT data from AMATOSM Server", 
				Shortcut.registerShortcut("amatosmeditor:download", 
						"AMAT Download", KeyEvent.VK_D, Shortcut.CTRL_SHIFT),
				true,
				null,
				true);
		
		this.amatosmUrl = amatosmUrl;
	}

	/**
	 * @return the mapView
	 */
	public MapView getMapView() {
		return mapView;
	}

	/**
	 * @param mapView the mapView to set
	 */
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
		updateEnabledState();				//see if we're enabled	
	}
	
	/**
	 * @return the amatosmUrl
	 */
	public String getAmatosmUrl() {
		return amatosmUrl;
	}

	/**
	 * @param amatosmUrl the amatosmUrl to set
	 */
	public void setAmatosmUrl(String amatosmUrl) {
		this.amatosmUrl = amatosmUrl;
		updateEnabledState();				//see if we're enabled	
	}

	/////////// LayerChangeListener
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.MapView.LayerChangeListener#activeLayerChange(org.openstreetmap.josm.gui.layer.Layer, org.openstreetmap.josm.gui.layer.Layer)
	 */	
	@Override
	public void layerAdded(LayerAddEvent e) {
		updateEnabledState();				//see if we're enabled			
	}

	@Override
	public void layerRemoving(LayerRemoveEvent e) {
		updateEnabledState();				//see if we're enabled	
	}

	@Override
	public void layerOrderChanged(LayerOrderChangeEvent e) {
		updateEnabledState();				//see if we're enabled	
	}
	///////////
	
	public void openUrl(final String url,boolean deletePreviousData) {
		PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Download Data"));
		DownloadTask task = new AMATDownloadOsmTask(deletePreviousData);
		Future<?> future = null;
		try {
			future = task.loadUrl(new DownloadParams().withNewLayer(false), url, monitor);
		} catch (IllegalArgumentException e) {
			Logging.error(e);
		}
		if (future != null) {
			MainApplication.worker.submit(new PostDownloadHandler(task, future));
		} else {
			final String details = "Impossibile caricare dati AMATOSM";
			HelpAwareOptionPane.showMessageDialogInEDT(Main.parent, "<html><p>" + tr(
					"Cannot open URL ''{0}''<br>The following download tasks accept the URL patterns shown:<br>{1}",
					url, details) + "</p></html>", tr("Download Location"), JOptionPane.ERROR_MESSAGE, HelpUtil.ht("/Action/OpenLocation"));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;
		
		boolean deletePreviousData = true;
		for (OsmDataLayer layer : getLayerManager().getLayersOfType(OsmDataLayer.class)) {
			for (Bounds bound : layer.data.getDataSourceBounds()) {
				if(!bound.isCollapsed()) { 
					String url = String.format("%s/0.6/map/?bbox=%s",amatosmUrl,bound.toBBox().toStringCSV(","));
					openUrl(url,deletePreviousData);
					deletePreviousData = false;
				}			
			}
		}
	}
	
	/**
	 * 	Enabled only if MapView actually exist and at least an edit layer exists and AMAT OSM Server url is set
	 */
	@Override
	protected void updateEnabledState() {
		setEnabled(amatosmUrl != null && !amatosmUrl.isEmpty() && mapView != null && getLayerManager().getEditLayer() != null);
	}
}
