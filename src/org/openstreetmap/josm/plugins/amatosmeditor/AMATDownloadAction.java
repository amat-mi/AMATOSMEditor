package org.openstreetmap.josm.plugins.amatosmeditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.plugins.amatosmeditor.downloadtasks.AMATDownloadOsmTask;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs.AMATComparePrimitiveDialog;
import org.openstreetmap.josm.plugins.amatosmeditor.gui.layer.AMATOsmDataLayer;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Geometry;
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
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void layerAdded(Layer newLayer) {
		updateEnabledState();				//see if we're enabled	
	}

	@Override
	public void layerRemoved(Layer oldLayer) {
		updateEnabledState();				//see if we're enabled	
	}

	///////////
	
	public void openUrl(final String url,boolean deletePreviousData) {
		PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Download Data"));
		DownloadTask task = new AMATDownloadOsmTask(deletePreviousData);
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

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!isEnabled())
			return;
		
		boolean deletePreviousData = true;
		for (OsmDataLayer layer : mapView.getLayersOfType(OsmDataLayer.class)) {
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
		setEnabled(amatosmUrl != null && !amatosmUrl.isEmpty() && mapView != null && getEditLayer() != null);
	}
}
