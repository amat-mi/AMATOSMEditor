/**
 * 
 */
package org.openstreetmap.josm.plugins.amatosmeditor;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;

/**
 * @author Paolo
 *
 */
public class AMATDownloadOsmTask extends DownloadOsmTask {
	
	protected class AMATDownloadTask extends DownloadOsmTask.DownloadTask {

		/**
		 * @param newLayer
		 * @param reader
		 * @param progressMonitor
		 */
		public AMATDownloadTask(boolean newLayer, OsmServerReader reader,
				ProgressMonitor progressMonitor) {
			super(newLayer, reader, progressMonitor);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask.DownloadTask#createNewLayer(java.lang.String)
		 */
		@Override
		protected OsmDataLayer createNewLayer(String layerName) {
			//must always use a fixed name
			return super.createNewLayer("AMATOSM");
		}				
	}
}
