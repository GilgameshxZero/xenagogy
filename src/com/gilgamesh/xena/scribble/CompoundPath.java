package com.gilgamesh.xena.scribble;

import com.gilgamesh.xena.algorithm.Geometry;

import java.util.ArrayList;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

// Path which has accessible coordinates, and other utilities for drawing and detection.
public class CompoundPath {
	static public final float SHORT_DISTANCE_EPS = 4;
	static public final float BOUNDS_AREA_EPS = 16;

	static public abstract class PointAddedCallback {
		public abstract void onPointAdded(PointF point);
	}

	private PointAddedCallback callback;

	// Ground truth for paths for loading/saving SVGs.
	public ArrayList<PointF> points = new ArrayList<PointF>();
	// Used to draw paths.
	public Path path = new Path();
	// Bounding box of path.
	public RectF bounds = new RectF();

	public CompoundPath(PointF point, PointAddedCallback callback) {
		this.points.add(new PointF(point));
		this.path.moveTo(point.x, point.y);
		this.bounds.set(point.x, point.y, point.x, point.y);
		this.callback = callback;
		this.callback.onPointAdded(point);
	}

	public boolean isIntersectingSegment(PointF start, PointF end) {
		// If this path is small enough, erase it if it is within some distance of
		// the segment ends.
		if (this.bounds.width()
				* this.bounds.height() < CompoundPath.BOUNDS_AREA_EPS
				&& Geometry.distance(start,
						this.points.get(0)) < CompoundPath.SHORT_DISTANCE_EPS
				&& Geometry.distance(end,
						this.points.get(0)) < CompoundPath.SHORT_DISTANCE_EPS) {
			return true;
		}

		// Quick check to see if we even need to iterate through the whole path.
		if (!(this.bounds.contains(start.x, start.y)
				|| this.bounds.contains(end.x, end.y)
				|| Geometry.isSegmentsIntersecting(
						new PointF(this.bounds.left, this.bounds.top),
						new PointF(this.bounds.right, this.bounds.bottom), start, end)
				|| Geometry.isSegmentsIntersecting(
						new PointF(this.bounds.right, this.bounds.top),
						new PointF(this.bounds.left, this.bounds.bottom), start, end))) {
			return false;
		}

		for (int i = 0; i < points.size() - 1; i++) {
			if (Geometry.isSegmentsIntersecting(points.get(i), points.get(i + 1),
					start, end)) {
				return true;
			}
		}
		return false;
	}

	public void addPoint(PointF point) {
		this.points.add(new PointF(point));
		this.path.lineTo(point.x, point.y);
		this.bounds.union(point.x, point.y);
		this.callback.onPointAdded(point);
	}
}