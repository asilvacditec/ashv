/*
 * @(#)BasicDrawingState.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.model.drawing.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.egantt.model.drawing.DrawingPart;

/**
  * The simplest implementation of a DrawingState notifies the container
  * that a repaint has occured
  */
public class BasicDrawingState extends AbstractDrawingState
{
	protected List<DrawingPart> parts = new ArrayList<DrawingPart>();

	//___________________________________________________________________________

	/**
	  * Intent: To add a @link com.egantt.model.drawing.DrawingPart
	  * for it to be rendered
	  *
	  * Note: not in the DrawingState interface as a Component never needs
	  * to access this method
	  */
	public synchronized boolean addDrawingPart(DrawingPart drawingPart)
	{
		return parts.add(drawingPart);
	}

	/**
	  * Intent: To remove a @see com.egantt.model.drawing.DrawingPart
	  * for it to be rendered
	  *
	  * Note: not in the DrawingState interface as a Component never needs
	  * to access this method
	  */
	public synchronized boolean removeDrawingPart(DrawingPart drawingPart)
	{
		return parts.remove(drawingPart);
	}

	// __________________________________________________________________________	
	
	@Override
	public Iterator parts()
	{
		return parts.iterator();
	}
}
