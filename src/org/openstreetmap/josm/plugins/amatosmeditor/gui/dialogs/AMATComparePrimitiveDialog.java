// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.amatosmeditor.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.widgets.JosmTextArea;
import org.openstreetmap.josm.tools.DateUtils;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * Panel to inspect one or more OsmPrimitives.
 *
 * Gives an unfiltered view of the object's internal state.
 * Might be useful for power users to give more detailed bug reports and
 * to better understand the JOSM data representation.
 */
public class AMATComparePrimitiveDialog extends ExtendedDialog {

	private OsmPrimitive primitive1;	//the left primitive to compare and confirm
	private OsmPrimitive primitive2;	//the right primitive to compare and confirm
	private Layer layer;
    private boolean askConfirmation;	//wherever to show confirmation buttons, or cancel only
	private Set<String> keysToAdd;		//the keys of tags that will be added to the left primitive
	private Set<String> keysToChange;	//the keys of tags that will be changed in the left primitive
	private Set<String> keysToRemove;	//the keys of tags that will be removed from the left primitive
    
    public static final int VALUE_ALL = 1;
    public static final int VALUE_GEOM_LOCREF = 2;
    public static final int VALUE_GEOM = 3;
    public static final int VALUE_LOCREF = 4;
    public static final int VALUE_TAGS = 5;
    public static final int VALUE_CANCELLED = VALUE_TAGS + 1; 

    public AMATComparePrimitiveDialog(OsmPrimitive primitive1, OsmPrimitive primitive2,
    		Layer layer,boolean askConfirmation) {
    	this(primitive1,primitive2,null,null,null,layer,askConfirmation);
    }
    
    public AMATComparePrimitiveDialog(OsmPrimitive primitive1, OsmPrimitive primitive2,
    		AbstractMap<String, String> tagsToAdd,
			AbstractMap<String, String> tagsToChange,
			AbstractMap<String, String> tagsToRemove,
    		Layer layer,boolean askConfirmation) {
        super(Main.parent, 
        		askConfirmation ?
        				tr("Compare OSM and AMAT ways and confirm updating") :
        				tr("Compare OSM and AMAT ways"),
        		askConfirmation ? 
        				new String[] { 
        					tr("All"), 
        					tr("Geom+LocRef"), 
        					tr("Geom"), 
        					tr("LocRef"), 
        					tr("Tags"), 
        					tr("Cancel") } :
        				new String[] { tr("Cancel") },
        		true);
        
        this.primitive1 = primitive1;
        this.primitive2 = primitive2;
        this.layer = layer;
        this.askConfirmation = askConfirmation;
		this.keysToAdd = tagsToAdd != null ? tagsToAdd.keySet() : null;
		this.keysToChange = tagsToChange != null ? tagsToChange.keySet() : null;
		this.keysToRemove = tagsToRemove != null ? tagsToRemove.keySet() : null;
        
        setRememberWindowGeometry(getClass().getName() + ".geometry",
                WindowGeometry.centerInWindow(Main.parent, new Dimension(750, 550)));

        setContent(buildDataPanel(), false);
        
        if(askConfirmation)
        	setButtonIcons(new String[] { "ok.png","ok.png","ok.png","ok.png","ok.png","cancel.png" });
        else
        	setButtonIcons(new String[] { "cancel.png" });
        
        setDefaultButton(1);
        setupDialog();
        getRootPane().setDefaultButton(defaultButton);        
    }

    public boolean isCancelled() {
    	int value = getValue();
    	if(value == ExtendedDialog.DialogClosedOtherwise)
    		return true;
    	return askConfirmation ? value >= VALUE_CANCELLED : true; 
    }
    
    public boolean isGeomConfirmed() {
    	int value = getValue();
    	return isCancelled() ? false : value == VALUE_ALL || value == VALUE_GEOM || value == VALUE_GEOM_LOCREF;
    }
       
    public boolean isLocRefConfirmed() {
    	int value = getValue();
    	return isCancelled() ? false : isTagsConfirmed() ? true : 
    		value == VALUE_ALL || value == VALUE_LOCREF || value == VALUE_GEOM_LOCREF;
    }
       
    public boolean isTagsConfirmed() {
    	int value = getValue();
    	return isCancelled() ? false : value == VALUE_ALL || value == VALUE_TAGS;  
    }
    
    protected Component buildDataPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel("OSM Way"), GBC.std(0,0).weight(0.1,1.0).fill(GBC.NONE));
        p.add(new JLabel("AMAT Way"), GBC.std(1,0).weight(0,1.0).fill(GBC.NONE));
        p.add(buildDataTextArea(primitive1), GBC.std(0,1).weight(0.1,1.0).fill(GBC.BOTH));
        p.add(buildDataTextArea(primitive2), GBC.std(1,1).weight(0,1.0).fill(GBC.BOTH));
        JScrollPane scroll = new JScrollPane(p);
        p = new JPanel(new GridBagLayout());
        p.add(scroll, GBC.std().fill(GBC.BOTH));
        
        return p;        
    }

    protected JosmTextArea buildDataTextArea(OsmPrimitive primitive) {
        JosmTextArea txtData = new JosmTextArea();

        DataText dt = new DataText(txtData);
        dt.addPrimitive(primitive);
        //highlight
        dt.highlite(keysToAdd, Color.GREEN);
        dt.highlite(keysToChange, Color.YELLOW);
        dt.highlite(keysToRemove, Color.RED);
        
        txtData.setLineWrap(true);
        txtData.setFont(new Font("Monospaced", txtData.getFont().getStyle(), txtData.getFont().getSize()));
        txtData.setEditable(false);
        txtData.setSelectionStart(0);
        txtData.setSelectionEnd(0);

        return txtData;
    }

    class DataText {
    	private JTextArea textComp;
    	private Map<String,int[]> tagsPos = new HashMap<String,int[]>();
    	
        /**
		 * @param textComp
		 */
		public DataText(JTextArea textComp) {
			super();
			this.textComp = textComp;
		}

		static final String INDENT = "  ";
        static final String NL = "\n";

        private JTextArea append(String str) {
        	textComp.append(str);
        	
        	return textComp;
        }
        
        private DataText add(String title, String... values) {
            append(INDENT).append(title);
            for (String v : values) {
                append(v);
            }
            append(NL);
            return this;
        }

        private String getNameAndId(String name, long id) {
            if (name != null) {
                return name + tr(" ({0})", /* sic to avoid thousand seperators */ Long.toString(id));
            } else {
                return Long.toString(id);
            }
        }

        public void highlite(Set<String> keys,Color color) {
        	if(keys != null && !keys.isEmpty()) {                
            	Highlighter hilite = textComp.getHighlighter();
            	try {
            		for (String key : keys) {
						if(tagsPos.containsKey(key)) {
	            			int [] pos = tagsPos.get(key);
	        				hilite.addHighlight(pos[0], pos[1], new DefaultHighlightPainter(color));												
						}
					}
    			} catch (BadLocationException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}        		
        	}
        }
        
        public void addPrimitive(OsmPrimitive o) {

            addHeadline(o);

            if (!(o.getDataSet() != null && o.getDataSet().getPrimitiveById(o) != null)) {
                append(NL).append(INDENT);
                append(tr("not in data set")).append(NL);
                return;
            }
            if (o.isIncomplete()) {
                append(NL).append(INDENT);
                append(tr("incomplete")).append(NL);
                return;
            }
            append(NL);

            addState(o);
            addCommon(o);
            addAttributes(o);
            addSpecial(o);
            addReferrers(o);
            addConflicts(o);
            append(NL);
        }

        void addHeadline(OsmPrimitive o) {
            addType(o);
            addNameAndId(o);
        }

        void addType(OsmPrimitive o) {
            if (o instanceof Node) {
                append(tr("Node: "));
            } else if (o instanceof Way) {
                append(tr("Way: "));
            } else if (o instanceof Relation) {
                append(tr("Relation: "));
            }
        }

        void addNameAndId(OsmPrimitive o) {
            String name = o.get("name");
            if (name == null) {
                append(String.valueOf(o.getUniqueId()));
            } else {
                append(getNameAndId(name, o.getUniqueId()));
            }
        }

        void addState(OsmPrimitive o) {
            StringBuilder sb = new StringBuilder(INDENT);
            /* selected state is left out: not interesting as it is always selected */
            if (o.isDeleted()) {
                sb.append(tr("deleted")).append(INDENT);
            }
            if (!o.isVisible()) {
                sb.append(tr("deleted-on-server")).append(INDENT);
            }
            if (o.isModified()) {
                sb.append(tr("modified")).append(INDENT);
            }
            if (o.isDisabledAndHidden()) {
                sb.append(tr("filtered/hidden")).append(INDENT);
            }
            if (o.isDisabled()) {
                sb.append(tr("filtered/disabled")).append(INDENT);
            }
            if (o.hasDirectionKeys()) {
                if (o.reversedDirection()) {
                    sb.append(tr("has direction keys (reversed)")).append(INDENT);
                } else {
                    sb.append(tr("has direction keys")).append(INDENT);
                }
            }
            String state = sb.toString().trim();
            if (!state.isEmpty()) {
                add(tr("State: "), sb.toString().trim());
            }
        }

        void addCommon(OsmPrimitive o) {
            add(tr("Data Set: "), Integer.toHexString(o.getDataSet().hashCode()));
            add(tr("Edited at: "), o.isTimestampEmpty() ? tr("<new object>")
                    : DateUtils.fromDate(o.getTimestamp()));
            add(tr("Edited by: "), o.getUser() == null ? tr("<new object>")
                    : getNameAndId(o.getUser().getName(), o.getUser().getId()));
            add(tr("Version: "), Integer.toString(o.getVersion()));
            add(tr("In changeset: "), Integer.toString(o.getChangesetId()));
        }

        void addAttributes(OsmPrimitive o) {        	
            if (o.hasKeys()) {
                add(tr("Tags: "));
                List<String> keys = new ArrayList<String>(o.keySet());
                Collections.sort(keys);
                for (String key : keys) {
                    append(INDENT).append(INDENT);
                	int startPos = textComp.getCaretPosition();
                    append(String.format("\"%s\"=\"%s\"%n", key, o.get(key)));
                	int endPos = textComp.getCaretPosition();
                	tagsPos.put(key, new int[]{startPos,endPos});
                }                
            }
        }

        void addSpecial(OsmPrimitive o) {
            if (o instanceof Node) {
                addCoordinates((Node) o);
            } else if (o instanceof Way) {
                addBbox(o);
                add(tr("Centroid: "), Main.getProjection().eastNorth2latlon(
                        Geometry.getCentroid(((Way) o).getNodes())).toStringCSV(", "));
                addWayNodes((Way) o);
            } else if (o instanceof Relation) {
                addBbox(o);
                addRelationMembers((Relation) o);
            }
        }

        void addRelationMembers(Relation r) {
            add(trn("{0} Member: ", "{0} Members: ", r.getMembersCount(), r.getMembersCount()));
            for (RelationMember m : r.getMembers()) {
                append(INDENT).append(INDENT);
                addHeadline(m.getMember());
                append(tr(" as \"{0}\"", m.getRole()));
                append(NL);
            }
        }

        void addWayNodes(Way w) {
            add(tr("{0} Nodes: ", w.getNodesCount()));
            for (Node n : w.getNodes()) {
                append(INDENT).append(INDENT);
                addNameAndId(n);
                append(NL);
            }
        }

        void addBbox(OsmPrimitive o) {
            BBox bbox = o.getBBox();
            if (bbox != null) {
                add(tr("Bounding box: "), bbox.toStringCSV(", "));
                EastNorth bottomRigth = Main.getProjection().latlon2eastNorth(bbox.getBottomRight());
                EastNorth topLeft = Main.getProjection().latlon2eastNorth(bbox.getTopLeft());
                add(tr("Bounding box (projected): "),
                        Double.toString(topLeft.east()), ", ",
                        Double.toString(bottomRigth.north()), ", ",
                        Double.toString(bottomRigth.east()), ", ",
                        Double.toString(topLeft.north()));
                add(tr("Center of bounding box: "), bbox.getCenter().toStringCSV(", "));
            }
        }

        void addCoordinates(Node n) {
            if (n.getCoor() != null) {
                add(tr("Coordinates: "),
                        Double.toString(n.getCoor().lat()), ", ",
                        Double.toString(n.getCoor().lon()));
                add(tr("Coordinates (projected): "),
                        Double.toString(n.getEastNorth().east()), ", ",
                        Double.toString(n.getEastNorth().north()));
            }
        }

        void addReferrers(OsmPrimitive o) {
            List<OsmPrimitive> refs = o.getReferrers();
            if (!refs.isEmpty()) {
                add(tr("Part of: "));
                for (OsmPrimitive p : refs) {
                    append(INDENT).append(INDENT);
                    addHeadline(p);
                    append(NL);
                }
            }
        }

        void addConflicts(OsmPrimitive o) {
        	try {
        		Method method = layer.getClass().getMethod("getConflicts");
                Conflict<?> c = ((ConflictCollection)method.invoke(layer)).getConflictForMy(o);
                if (c != null) {
                    add(tr("In conflict with: "));
                    addNameAndId(c.getTheir());
                }
        	} catch(Exception e) {        		
        	}        	
        }

        @Override
        public String toString() {
            return textComp.getText();
        }
    }
}
