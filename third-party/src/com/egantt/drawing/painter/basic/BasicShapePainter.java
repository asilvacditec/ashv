/*
 * @(#)BasicShapePainter.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.drawing.painter.basic;

import com.egantt.awt.graphics.GraphicsContext;
import com.egantt.drawing.DrawingPainter;
import com.egantt.model.drawing.StateResources;
import com.egantt.model.drawing.painter.PainterResources;
import com.egantt.model.drawing.painter.PainterState;
import com.egantt.swing.graphics.GraphicsResources;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class BasicShapePainter implements DrawingPainter
{
	// __________________________________________________________________________

	@Override
	public Shape paint(Object key, Graphics g, Rectangle bounds,
		PainterState state, GraphicsContext context)
	{
		Graphics2D g2d = (Graphics2D) g;

		Shape shape = (Shape) context.get(key, GraphicsResources.SHAPE);
		if (shape == null)
			shape = (Shape) state.get(StateResources.SHAPE);

		if (shape != null)
			g2d.draw(shape);

		return shape;
	}

	//___________________________________________________________________________

	@Override
	public long width(Object key, Graphics g, Rectangle bounds, GraphicsContext context)
	{
		return g.getClipBounds().width;
	}
}
