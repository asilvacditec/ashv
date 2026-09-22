/*
 * @(#)CompoundRangeTransform.java
 *
 * Copyright 2002 EGANTT LLP. All rights reserved.
 * PROPRIETARY/QPL. Use is subject to license terms.
 */

package com.egantt.drawing.painter.range.transform;

import com.egantt.drawing.painter.range.RangeTransform;

import com.egantt.model.drawing.DrawingTransform;

public class CompoundRangeTransform implements RangeTransform
{
	protected final int axisKey;
	protected final DrawingTransform transforms [];

	public CompoundRangeTransform(int axisKey, DrawingTransform transforms [])
	{
		this.axisKey = axisKey;
		this.transforms = transforms;
	}
	
	// __________________________________________________________________________
	
	@Override
	public Object inverseTransform(long pixel, long axisSize)
	{
		return transforms[axisKey].inverseTransform(pixel, axisSize);
	}
	
	@Override
	public int transform(Object value, long axisSize)
	{
		return transforms[axisKey].transform(value, axisSize);
	}
}
