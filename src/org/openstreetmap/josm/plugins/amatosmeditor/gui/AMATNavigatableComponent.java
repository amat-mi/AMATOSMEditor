package org.openstreetmap.josm.plugins.amatosmeditor.gui;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Utils;

public class AMATNavigatableComponent extends NavigatableComponent {
	private static final long serialVersionUID = 1L;
	
	private DataSet dataSet;

	private NavigatableComponent nc;
	
	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getScale()
	 */
	public double getScale() {
		return nc.getScale();
	}

	/**
	 * @param newCenter
	 * @param newScale
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomTo(org.openstreetmap.josm.data.coor.EastNorth, double)
	 */
	public void zoomTo(EastNorth newCenter, double newScale) {
		nc.zoomTo(newCenter, newScale);
	}

	/**
	 * @param newCenter
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#smoothScrollTo(org.openstreetmap.josm.data.coor.EastNorth)
	 */
	public void smoothScrollTo(EastNorth newCenter) {
		nc.smoothScrollTo(newCenter);
	}

	/**
	 * @param x
	 * @param y
	 * @param factor
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomToFactor(double, double, double)
	 */
	public void zoomToFactor(double x, double y, double factor) {
		nc.zoomToFactor(x, y, factor);
	}

	/**
	 * @param factor
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomToFactor(double)
	 */
	public void zoomToFactor(double factor) {
		nc.zoomToFactor(factor);
	}

	/**
	 * 
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomPrevious()
	 */
	public void zoomPrevious() {
		nc.zoomPrevious();
	}

	/**
	 * 
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#zoomNext()
	 */
	public void zoomNext() {
		nc.zoomNext();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getViewID()
	 */
	public int getViewID() {
		return nc.getViewID();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getCenter()
	 */
	public EastNorth getCenter() {
		return nc.getCenter();
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getEastNorth(int, int)
	 */
	public EastNorth getEastNorth(int x, int y) {
		return nc.getEastNorth(x, y);
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getProjectionBounds()
	 */
	public ProjectionBounds getProjectionBounds() {
		return nc.getProjectionBounds();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getRealBounds()
	 */
	public Bounds getRealBounds() {
		return nc.getRealBounds();
	}

	/**
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getAffineTransform()
	 */
	public AffineTransform getAffineTransform() {
		return nc.getAffineTransform();
	}

	/**
	 * @param p
	 * @return
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getPoint2D(org.openstreetmap.josm.data.coor.EastNorth)
	 */
	public Point2D getPoint2D(EastNorth p) {
		return nc.getPoint2D(p);
	}

	/**
	 * 
	 */
	public AMATNavigatableComponent(NavigatableComponent nc) {
		super();
		this.nc = nc;
	}

	/**
	 * @param dataSet
	 */
	public AMATNavigatableComponent(DataSet dataSet) {
		super();
		setDataSet(dataSet);
	}

	/**
	 * @return the dataSet
	 */
	public DataSet getDataSet() {
		return dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.NavigatableComponent#getCurrentDataSet()
	 * NOT OVERRIDE ANYMORE!!!
	 */
	protected DataSet getCurrentDataSet() {
		return dataSet;
	}
	
	//////////////////////////////////
	/// Copied from NavigatableComponent and modified to use local dataset
	//////////////////////////////////
	
    /**
     * The *result* does not depend on the current map selection state, neither does the result *order*.
     * It solely depends on the distance to point p.
     * @param p point
     * @param predicate predicate to match
     *
     * @return a sorted map with the keys representing the distance of their associated nodes to point p.
     */
    private Map<Double, List<Node>> getNearestNodesImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();
        DataSet ds = this.dataSet;

        if (ds != null) {
            double dist, snapDistanceSq = PROP_SNAP_DISTANCE.get();
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, PROP_SNAP_DISTANCE.get()))) {
                if (predicate.test(n)
                        && (dist = getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    List<Node> nlist;
                    if (nearestMap.containsKey(dist)) {
                        nlist = nearestMap.get(dist);
                    } else {
                        nlist = new LinkedList<>();
                        nearestMap.put(dist, nlist);
                    }
                    nlist.add(n);
                }
            }
        }

        return nearestMap;
    }

    private BBox getBBox(Point p, int snapDistance) {
        return new BBox(getLatLon(p.x - snapDistance, p.y - snapDistance),
                getLatLon(p.x + snapDistance, p.y + snapDistance));
    }
    
    /**
     * The *result* does not depend on the current map selection state, neither does the result *order*.
     * It solely depends on the distance to point p.
     * @param p the screen point
     * @param predicate this parameter imposes a condition on the returned object, e.g.
     *        give the nearest node that is tagged.
     *
     * @return a sorted map with the keys representing the perpendicular
     *      distance of their associated way segments to point p.
     */
    private Map<Double, List<WaySegment>> getNearestWaySegmentsImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<WaySegment>> nearestMap = new TreeMap<>();
        DataSet ds = this.dataSet;

        if (ds != null) {
            double snapDistanceSq = Config.getPref().getInt("mappaint.segment.snap-distance", 10);
            snapDistanceSq *= snapDistanceSq;

            for (Way w : ds.searchWays(getBBox(p, Config.getPref().getInt("mappaint.segment.snap-distance", 10)))) {
                if (!predicate.test(w)) {
                    continue;
                }
                Node lastN = null;
                int i = -2;
                for (Node n : w.getNodes()) {
                    i++;
                    if (n.isDeleted() || n.isIncomplete()) { //FIXME: This shouldn't happen, raise exception?
                        continue;
                    }
                    if (lastN == null) {
                        lastN = n;
                        continue;
                    }

                    Point2D pA = getPoint2D(lastN);
                    Point2D pB = getPoint2D(n);
                    double c = pA.distanceSq(pB);
                    double a = p.distanceSq(pB);
                    double b = p.distanceSq(pA);

                    /* perpendicular distance squared
                     * loose some precision to account for possible deviations in the calculation above
                     * e.g. if identical (A and B) come about reversed in another way, values may differ
                     * -- zero out least significant 32 dual digits of mantissa..
                     */
                    double perDistSq = Double.longBitsToDouble(
                            Double.doubleToLongBits(a - (a - b + c) * (a - b + c) / 4 / c)
                            >> 32 << 32); // resolution in numbers with large exponent not needed here..

                    if (perDistSq < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq) {
                        List<WaySegment> wslist;
                        if (nearestMap.containsKey(perDistSq)) {
                            wslist = nearestMap.get(perDistSq);
                        } else {
                            wslist = new LinkedList<>();
                            nearestMap.put(perDistSq, wslist);
                        }
                        wslist.add(new WaySegment(w, i));
                    }

                    lastN = n;
                }
            }
        }

        return nearestMap;
    }

    /**
     * This is used as a helper routine to {@link #getNearestNodeOrWay(Point, Predicate, boolean)}
     * It decides, whether to yield the node to be tested or look for further (way) candidates.
     *
     * @param osm node to check
     * @param p point clicked
     * @param useSelected whether to prefer selected nodes
     * @return true, if the node fulfills the properties of the function body
     */
    private boolean isPrecedenceNode(Node osm, Point p, boolean useSelected) {
        if (osm != null) {
            if (p.distanceSq(getPoint2D(osm)) <= (4*4)) return true;
            if (osm.isTagged()) return true;
            if (useSelected && osm.isSelected()) return true;
        }
        return false;
    }
    
    /**
     * The *result* depends on the current map selection state IF use_selected is true
     *
     * If more than one node within node.snap-distance pixels is found,
     * the nearest node selected is returned IF use_selected is true.
     *
     * If there are no selected nodes near that point, the node that is related to some of the preferredRefs
     *
     * Else the nearest new/id=0 node within about the same distance
     * as the true nearest node is returned.
     *
     * If no such node is found either, the true nearest node to p is returned.
     *
     * Finally, if a node is not found at all, null is returned.
     *
     * @param p the screen point
     * @param predicate this parameter imposes a condition on the returned object, e.g.
     *        give the nearest node that is tagged.
     * @param useSelected make search depend on selection
     * @param preferredRefs primitives, whose nodes we prefer
     *
     * @return A node within snap-distance to point p, that is chosen by the algorithm described.
     * @since 6065
     * 
     * WARN!!! Changed name because original method is declared final!!!
     */
    public final Node getAMATNearestNode(Point p, Predicate<OsmPrimitive> predicate,
            boolean useSelected, Collection<OsmPrimitive> preferredRefs) {

        Map<Double, List<Node>> nlists = getNearestNodesImpl(p, predicate);
        if (nlists.isEmpty()) return null;

        if (preferredRefs != null && preferredRefs.isEmpty()) preferredRefs = null;
        Node ntsel = null, ntnew = null, ntref = null;
        boolean useNtsel = useSelected;
        double minDistSq = nlists.keySet().iterator().next();

        for (Entry<Double, List<Node>> entry : nlists.entrySet()) {
            Double distSq = entry.getKey();
            for (Node nd : entry.getValue()) {
                // find the nearest selected node
                if (ntsel == null && nd.isSelected()) {
                    ntsel = nd;
                    // if there are multiple nearest nodes, prefer the one
                    // that is selected. This is required in order to drag
                    // the selected node if multiple nodes have the same
                    // coordinates (e.g. after unglue)
                    useNtsel |= Utils.equalsEpsilon(distSq, minDistSq);
                }
                if (ntref == null && preferredRefs != null && Utils.equalsEpsilon(distSq, minDistSq)) {
                    List<OsmPrimitive> ndRefs = nd.getReferrers();
                    for (OsmPrimitive ref: preferredRefs) {
                        if (ndRefs.contains(ref)) {
                            ntref = nd;
                            break;
                        }
                    }
                }
                // find the nearest newest node that is within about the same
                // distance as the true nearest node
                if (ntnew == null && nd.isNew() && (distSq-minDistSq < 1)) {
                    ntnew = nd;
                }
            }
        }

        // take nearest selected, nearest new or true nearest node to p, in that order
        if (ntsel != null && useNtsel)
            return ntsel;
        if (ntref != null)
            return ntref;
        if (ntnew != null)
            return ntnew;
        return nlists.values().iterator().next().get(0);
    }
    
    /**
     * The *result* depends on the current map selection state IF use_selected is true.
     *
     * @param p the point for which to search the nearest segment.
     * @param predicate the returned object has to fulfill certain properties.
     * @param useSelected whether selected way segments should be preferred.
     * @param preferredRefs - prefer segments related to these primitives, may be null
     *
     * @return The nearest way segment to point p,
     *      and, depending on use_selected, prefers a selected way segment, if found.
     * Also prefers segments of ways that are related to one of preferredRefs primitives
     *
     * @see #getNearestWaySegments(Point, Collection, Predicate)
     * @since 6065
     * 
     * WARN!!! Changed name because original method is declared final!!!
     */
    public final WaySegment getAMATNearestWaySegment(Point p, Predicate<OsmPrimitive> predicate,
            boolean useSelected, Collection<OsmPrimitive> preferredRefs) {
        WaySegment wayseg = null;
        WaySegment ntsel = null;
        WaySegment ntref = null;
        if (preferredRefs != null && preferredRefs.isEmpty())
            preferredRefs = null;

        searchLoop: for (List<WaySegment> wslist : getNearestWaySegmentsImpl(p, predicate).values()) {
            for (WaySegment ws : wslist) {
                if (wayseg == null) {
                    wayseg = ws;
                }
                if (ntsel == null && ws.way.isSelected()) {
                    ntsel = ws;
                    break searchLoop;
                }
                if (ntref == null && preferredRefs != null) {
                    // prefer ways containing given nodes
                    for (Node nd: ws.way.getNodes()) {
                        if (preferredRefs.contains(nd)) {
                            ntref = ws;
                            break searchLoop;
                        }
                    }
                    Collection<OsmPrimitive> wayRefs = ws.way.getReferrers();
                    // prefer member of the given relations
                    for (OsmPrimitive ref: preferredRefs) {
                        if (ref instanceof Relation && wayRefs.contains(ref)) {
                            ntref = ws;
                            break searchLoop;
                        }
                    }
                }
            }
        }
        if (ntsel != null && useSelected)
            return ntsel;
        if (ntref != null)
            return ntref;
        return wayseg;
    }
    
    /**
     * The *result* depends on the current map selection state IF use_selected is true.
     *
     * @param p the point for which to search the nearest segment.
     * @param predicate the returned object has to fulfill certain properties.
     * @param useSelected whether selected way segments should be preferred.
     *
     * @return The nearest way segment to point p,
     *      and, depending on use_selected, prefers a selected way segment, if found.
     * @see #getNearestWaySegments(Point, Collection, Predicate)
     * 
     * WARN!!! Changed name because original method is declared final!!!
     */
    public final WaySegment getAMATNearestWaySegment(Point p, Predicate<OsmPrimitive> predicate, boolean useSelected) {
        WaySegment wayseg = null;
        WaySegment ntsel = null;

        for (List<WaySegment> wslist : getNearestWaySegmentsImpl(p, predicate).values()) {
            if (wayseg != null && ntsel != null) {
                break;
            }
            for (WaySegment ws : wslist) {
                if (wayseg == null) {
                    wayseg = ws;
                }
                if (ntsel == null && ws.way.isSelected()) {
                    ntsel = ws;
                }
            }
        }

        return (ntsel != null && useSelected) ? ntsel : wayseg;
    }
    
    /**
     * The *result* depends on the current map selection state IF use_selected is true.
     *
     * IF use_selected is true, use {@link #getNearestNode(Point, Predicate)} to find
     * the nearest, selected node.  If not found, try {@link #getNearestWaySegment(Point, Predicate)}
     * to find the nearest selected way.
     *
     * IF use_selected is false, or if no selected primitive was found, do the following.
     *
     * If the nearest node found is within 4px of p, simply take it.
     * Else, find the nearest way segment. Then, if p is closer to its
     * middle than to the node, take the way segment, else take the node.
     *
     * Finally, if no nearest primitive is found at all, return null.
     *
     * @param p The point on screen.
     * @param predicate the returned object has to fulfill certain properties.
     * @param useSelected whether to prefer primitives that are currently selected or referred by selected primitives
     *
     * @return A primitive within snap-distance to point p,
     *      that is chosen by the algorithm described.
     * @see #getNearestNode(Point, Predicate)
     * @see #getNearestWay(Point, Predicate)
     * 
     * WARN!!! Changed name because original method is declared final!!!
     */
    public final OsmPrimitive getAMATNearestNodeOrWay(Point p, Predicate<OsmPrimitive> predicate, boolean useSelected) {
        Collection<OsmPrimitive> sel;
        DataSet ds = this.dataSet;
        if (useSelected && ds != null) {
            sel = ds.getSelected();
        } else {
            sel = null;
        }
        OsmPrimitive osm = getAMATNearestNode(p, predicate, useSelected, sel);

        if (isPrecedenceNode((Node) osm, p, useSelected)) return osm;
        WaySegment ws;
        if (useSelected) {
            ws = getAMATNearestWaySegment(p, predicate, useSelected, sel);
        } else {
            ws = getAMATNearestWaySegment(p, predicate, useSelected);
        }
        if (ws == null) return osm;

        if ((ws.way.isSelected() && useSelected) || osm == null) {
            // either (no _selected_ nearest node found, if desired) or no nearest node was found
            osm = ws.way;
        } else {
            int maxWaySegLenSq = 3*PROP_SNAP_DISTANCE.get();
            maxWaySegLenSq *= maxWaySegLenSq;

            Point2D wp1 = getPoint2D(ws.way.getNode(ws.lowerIndex));
            Point2D wp2 = getPoint2D(ws.way.getNode(ws.lowerIndex+1));

            // is wayseg shorter than maxWaySegLenSq and
            // is p closer to the middle of wayseg  than  to the nearest node?
            if (wp1.distanceSq(wp2) < maxWaySegLenSq &&
                    p.distanceSq(project(0.5, wp1, wp2)) < p.distanceSq(getPoint2D((Node) osm))) {
                osm = ws.way;
            }
        }
        return osm;
    }
}
