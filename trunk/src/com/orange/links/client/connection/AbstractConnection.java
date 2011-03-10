package com.orange.links.client.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.canvas.dom.client.CssColor;
import com.orange.links.client.DiagramController;
import com.orange.links.client.PointShape;
import com.orange.links.client.Shape;
import com.orange.links.client.canvas.DiagramCanvas;
import com.orange.links.client.utils.ConnectionUtils;
import com.orange.links.client.utils.Point;
import com.orange.links.client.utils.Segment;
import com.orange.links.client.utils.SegmentPath;

public abstract class AbstractConnection {

	protected Shape startShape;
	protected Shape endShape;
	protected Set<Segment> segmentSet;
	protected DiagramController controller;
	protected DiagramCanvas canvas;

	protected boolean selectable;
	protected boolean selected;

	public static CssColor defaultConnectionColor = CssColor.make("#000000");
	protected CssColor connectionColor = defaultConnectionColor;
	public static CssColor selectedConnectionColor = CssColor.make("#FF6600");
	protected CssColor highlightPointColor = CssColor.make("#cccccc 1");

	protected Point highlightPoint;
	protected Segment highlightSegment;
	protected Map<Point,Integer> movablePointsPosition;
	protected List<Point> movablePointsList;
	protected SegmentPath segmentPath;


	public AbstractConnection(DiagramController controller, Shape startShape, Shape endShape){
		this.controller = controller;
		this.startShape = startShape;
		this.endShape = endShape;
		this.movablePointsList = new ArrayList<Point>();
		this.movablePointsPosition = new HashMap<Point, Integer>();
		this.segmentSet = new HashSet<Segment>();
		this.canvas = controller.getDiagramCanvas();

		// Build Path
		Segment s = ConnectionUtils.computeSegment(startShape,endShape);
		this.segmentPath = new SegmentPath(s.getP1(), s.getP2());
	}

	public AbstractConnection(DiagramController controller, Shape startShape, Shape endShape, boolean selectable){
		this(controller,startShape,endShape);
		this.selectable = selectable;
	}

	protected abstract void draw(Point p1, Point p2, boolean lastPoint);

	public void draw(){
		if(highlightPoint != null)
			highlightPoint(highlightPoint);

		// Reset the segments
		segmentSet = new HashSet<Segment>();

		// Draw each segment
		Shape s1 = startShape;
		for(Point p : segmentPath.getPathWithoutExtremities()){
			Shape s2 = new PointShape(p);
			Segment seg = ConnectionUtils.computeSegment(s1,s2);
			draw(seg.getP1(), seg.getP2(), false);
			segmentSet.add(seg);
			s1 = s2;
		}
		// Draw last segment
		Segment seg = ConnectionUtils.computeSegment(s1,endShape);
		segmentSet.add(seg);
		draw(seg.getP1(), seg.getP2(),true);
	}

	public void addMovablePoint(Point p){
		if(highlightSegment != null){
			Point startSegmentPoint = highlightSegment.getP1();
			Point endSegmentPoint = highlightSegment.getP2();
			segmentPath.add(p, startSegmentPoint, endSegmentPoint);
		}
		else{
			
		}
	}

	public Point findHighlightPoint(Point p){
		for(Segment s : segmentSet){
			if(ConnectionUtils.distanceToSegment(s, p) < DiagramController.minDistanceToSegment){
				Point hPoint = ConnectionUtils.projectionOnSegment(s, p);
				highlightSegment = s;
				highlightPoint = hPoint;
				return highlightPoint;
			}
		}
		return null;
	}

	public void highlightPoint(Point p) {
		DiagramCanvas canvas = controller.getDiagramCanvas();
		canvas.beginPath();
		canvas.arc(p.getLeft(), p.getTop(), 5, 0, Math.PI*2, false);
		canvas.setStrokeStyle(highlightPointColor);
		canvas.stroke();
		canvas.setFillStyle(highlightPointColor);
		canvas.fill();
		canvas.closePath();
	}
	
	public void setStraight(){
		segmentPath.straightPath();
	}

	public Shape getStartShape() {
		return startShape;
	}

	public Shape getEndShape() {
		return endShape;
	}

	public void changeSelection() {
		if(selectable){
			if(!isSelected()){
				selected = true;
				connectionColor = selectedConnectionColor;
			}
			else{
				selected = false;
				connectionColor = defaultConnectionColor;
			}
		}
	}
	
	public boolean isPointNearConnection(Point p){
		for(Segment s : segmentSet){
			if(ConnectionUtils.distanceToSegment(s, p) < DiagramController.minDistanceToSegment){
				return true;
			}
		}
		return false;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void unselect() {
		selected = false;
		connectionColor = defaultConnectionColor;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}


	public Point getHighlightPoint() {
		return highlightPoint;
	}

	public void setHighlightPoint(Point highlightPoint) {
		this.highlightPoint = highlightPoint;
	}

}