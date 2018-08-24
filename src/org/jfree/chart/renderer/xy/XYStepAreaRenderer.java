/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -----------------------
 * XYStepAreaRenderer.java
 * -----------------------
 * (C) Copyright 2003-2008, by Matthias Rose and Contributors.
 *
 * Original Author:  Matthias Rose (based on XYAreaRenderer.java);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes:
 * --------
 * 07-Oct-2003 : Version 1, contributed by Matthias Rose (DG);
 * 10-Feb-2004 : Added some getter and setter methods (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState.  Renamed
 *               XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 06-Jul-2005 : Renamed get/setPlotShapes() --> get/setShapesVisible() (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Jul-2006 : Modified to call dataset methods that return double
 *               primitives only (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 14-Feb-2007 : Added equals() method override (DG);
 * 04-May-2007 : Set processVisibleItemsOnly flag to false (DG);
 * 14-May-2008 : Call addEntity() from within drawItem() (DG);
 *
 */

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 * A step chart renderer that fills the area between the step and the x-axis.
 */
public class XYStepAreaRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7311560779702649635L;

    /** Useful constant for specifying the type of rendering (shapes only). */
    public static final int SHAPES = 1;

    /** Useful constant for specifying the type of rendering (area only). */
    public static final int AREA = 2;

    /**
     * Useful constant for specifying the type of rendering (area and shapes).
     */
    public static final int AREA_AND_SHAPES = 3;

    /** A flag indicating whether or not shapes are drawn at each XY point. */
    private boolean shapesVisible;

    /** A flag that controls whether or not shapes are filled for ALL series. */
    private boolean shapesFilled;

    /** A flag indicating whether or not Area are drawn at each XY point. */
    private boolean plotArea;

    /** A flag that controls whether or not the outline is shown. */
    private boolean showOutline;

    /** Area of the complete series */
    protected transient Polygon pArea = null;

    /**
     * The value on the range axis which defines the 'lower' border of the
     * area.
     */
    private double rangeBase;

    /**
     * Constructs a new renderer.
     */
    public XYStepAreaRenderer() {
        this(AREA);
    }

    /**
     * Constructs a new renderer.
     *
     * @param type  the type of the renderer.
     */
    public XYStepAreaRenderer(int type) {
        this(type, null, null);
    }

    /**
     * Constructs a new renderer.
     * <p>
     * To specify the type of renderer, use one of the constants:
     * AREA, SHAPES or AREA_AND_SHAPES.
     *
     * @param type  the type of renderer.
     * @param toolTipGenerator  the tool tip generator to use
     *                          (<code>null</code> permitted).
     * @param urlGenerator  the URL generator (<code>null</code> permitted).
     */
    public XYStepAreaRenderer(int type,
                              XYToolTipGenerator toolTipGenerator,
                              XYURLGenerator urlGenerator) {

        super();
        setBaseToolTipGenerator(toolTipGenerator);
        setURLGenerator(urlGenerator);

        if (type == AREA) {
            this.plotArea = true;
        }
        else if (type == SHAPES) {
            this.shapesVisible = true;
        }
        else if (type == AREA_AND_SHAPES) {
            this.plotArea = true;
            this.shapesVisible = true;
        }
        this.showOutline = false;
    }

    /**
     * Returns a flag that controls whether or not outlines of the areas are
     * drawn.
     *
     * @return The flag.
     *
     * @see #setOutline(boolean)
     */
    public boolean isOutline() {
        return this.showOutline;
    }

    /**
     * Sets a flag that controls whether or not outlines of the areas are
     * drawn, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param show  the flag.
     *
     * @see #isOutline()
     */
    public void setOutline(boolean show) {
        this.showOutline = show;
        fireChangeEvent();
    }

    /**
     * Returns true if shapes are being plotted by the renderer.
     *
     * @return <code>true</code> if shapes are being plotted by the renderer.
     *
     * @see #setShapesVisible(boolean)
     */
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }

    /**
     * Sets the flag that controls whether or not shapes are displayed for each
     * data item, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param flag  the flag.
     *
     * @see #getShapesVisible()
     */
    public void setShapesVisible(boolean flag) {
        this.shapesVisible = flag;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the shapes are filled.
     *
     * @return A boolean.
     *
     * @see #setShapesFilled(boolean)
     */
    public boolean isShapesFilled() {
        return this.shapesFilled;
    }

    /**
     * Sets the 'shapes filled' for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param filled  the flag.
     *
     * @see #isShapesFilled()
     */
    public void setShapesFilled(boolean filled) {
        this.shapesFilled = filled;
        fireChangeEvent();
    }

    /**
     * Returns true if Area is being plotted by the renderer.
     *
     * @return <code>true</code> if Area is being plotted by the renderer.
     *
     * @see #setPlotArea(boolean)
     */
    public boolean getPlotArea() {
        return this.plotArea;
    }

    /**
     * Sets a flag that controls whether or not areas are drawn for each data
     * item and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param flag  the flag.
     *
     * @see #getPlotArea()
     */
    public void setPlotArea(boolean flag) {
        this.plotArea = flag;
        fireChangeEvent();
    }

    /**
     * Returns the value on the range axis which defines the 'lower' border of
     * the area.
     *
     * @return <code>double</code> the value on the range axis which defines
     *         the 'lower' border of the area.
     *
     * @see #setRangeBase(double)
     */
    public double getRangeBase() {
        return this.rangeBase;
    }

    /**
     * Sets the value on the range axis which defines the default border of the
     * area, and sends a {@link RendererChangeEvent} to all registered
     * listeners.  E.g. setRangeBase(Double.NEGATIVE_INFINITY) lets areas always
     * reach the lower border of the plotArea.
     *
     * @param val  the value on the range axis which defines the default border
     *             of the area.
     *
     * @see #getRangeBase()
     */
    public void setRangeBase(double val) {
        this.rangeBase = val;
        fireChangeEvent();
    }

    /**
     * Initialises the renderer.  Here we calculate the Java2D y-coordinate for
     * zero, since all the bars have their bases fixed at zero.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return The number of passes required by the renderer.
     */
    @Override
	public XYItemRendererState initialise(Graphics2D g2,
                                          Rectangle2D dataArea,
                                          XYPlot plot,
                                          XYDataset data,
                                          PlotRenderingInfo info) {


        XYItemRendererState state = super.initialise(g2, dataArea, plot, data,
                info);
        // disable visible items optimisation - it doesn't work for this
        // renderer...
        state.setProcessVisibleItemsOnly(false);
        return state;

    }


    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    @Override
	public void drawItem(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {

        PlotOrientation orientation = plot.getOrientation();

        // Get the item count for the series, so that we can know which is the
        // end of the series.
        int itemCount = dataset.getItemCount(series);

        Paint paint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(paint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double x = x1;
        double y = Double.isNaN(y1) ? getRangeBase() : y1;
        double transX1 = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());
        double transY1 = rangeAxis.valueToJava2D(y, dataArea,
                plot.getRangeAxisEdge());

        // avoid possible sun.dc.pr.PRException: endPath: bad path
        transY1 = restrictValueToDataArea(transY1, plot, dataArea);

        if (this.pArea == null && !Double.isNaN(y1)) {

            // Create a new Area for the series
            this.pArea = new Polygon();

            // start from Y = rangeBase
            double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY2 = restrictValueToDataArea(transY2, plot, dataArea);

            // The first point is (x, this.baseYValue)
            if (orientation == PlotOrientation.VERTICAL) {
                this.pArea.addPoint((int) transX1, (int) transY2);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                this.pArea.addPoint((int) transY2, (int) transX1);
            }
        }

        double transX0 = 0;
        double transY0 = restrictValueToDataArea(getRangeBase(), plot,
                dataArea);

        double x0;
        double y0;
        if (item > 0) {
            // get the previous data point...
            x0 = dataset.getXValue(series, item - 1);
            y0 = Double.isNaN(y1) ? y1 : dataset.getYValue(series, item - 1);

            x = x0;
            y = Double.isNaN(y0) ? getRangeBase() : y0;
            transX0 = domainAxis.valueToJava2D(x, dataArea,
                    plot.getDomainAxisEdge());
            transY0 = rangeAxis.valueToJava2D(y, dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY0 = restrictValueToDataArea(transY0, plot, dataArea);

            if (Double.isNaN(y1)) {
                // NULL value -> insert point on base line
                // instead of 'step point'
                transX1 = transX0;
                transY0 = transY1;
            }
            if (transY0 != transY1) {
                // not just a horizontal bar but need to perform a 'step'.
                if (orientation == PlotOrientation.VERTICAL) {
                    this.pArea.addPoint((int) transX1, (int) transY0);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    this.pArea.addPoint((int) transY0, (int) transX1);
                }
            }
        }

        Shape shape = null;
        if (!Double.isNaN(y1)) {
            // Add each point to Area (x, y)
            if (orientation == PlotOrientation.VERTICAL) {
                this.pArea.addPoint((int) transX1, (int) transY1);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                this.pArea.addPoint((int) transY1, (int) transX1);
            }

            if (getShapesVisible()) {
                shape = getItemShape(series, item);
                if (orientation == PlotOrientation.VERTICAL) {
                    shape = ShapeUtilities.createTranslatedShape(shape,
                            transX1, transY1);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    shape = ShapeUtilities.createTranslatedShape(shape,
                            transY1, transX1);
                }
                if (isShapesFilled()) {
                    g2.fill(shape);
                }
                else {
                    g2.draw(shape);
                }
            }
            else {
                if (orientation == PlotOrientation.VERTICAL) {
                    shape = new Rectangle2D.Double(transX1 - 2, transY1 - 2,
                            4.0, 4.0);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    shape = new Rectangle2D.Double(transY1 - 2, transX1 - 2,
                            4.0, 4.0);
                }
            }
        }

        // Check if the item is the last item for the series or if it
        // is a NULL value and number of items > 0.  We can't draw an area for
        // a single point.
        if (getPlotArea() && item > 0 && this.pArea != null
                          && (item == (itemCount - 1) || Double.isNaN(y1))) {

            double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY2 = restrictValueToDataArea(transY2, plot, dataArea);

            if (orientation == PlotOrientation.VERTICAL) {
                // Add the last point (x,0)
                this.pArea.addPoint((int) transX1, (int) transY2);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                // Add the last point (x,0)
                this.pArea.addPoint((int) transY2, (int) transX1);
            }

            // fill the polygon
            g2.fill(this.pArea);

            // draw an outline around the Area.
            if (isOutline()) {
                g2.setStroke(plot.getOutlineStroke());
                g2.setPaint(plot.getOutlinePaint());
                g2.draw(this.pArea);
            }

            // start new area when needed (see above)
            this.pArea = null;
        }

        // do we need to update the crosshair values?
        if (!Double.isNaN(y1)) {
            int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
            int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
            updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
                    rangeAxisIndex, transX1, transY1, orientation);
        }

        // collect entity and tool tip information...
        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            addEntity(entities, shape, dataset, series, item, transX1, transY1);
        }
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYStepAreaRenderer)) {
            return false;
        }
        XYStepAreaRenderer that = (XYStepAreaRenderer) obj;
        if (this.showOutline != that.showOutline) {
            return false;
        }
        if (this.shapesVisible != that.shapesVisible) {
            return false;
        }
        if (this.shapesFilled != that.shapesFilled) {
            return false;
        }
        if (this.plotArea != that.plotArea) {
            return false;
        }
        if (this.rangeBase != that.rangeBase) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Helper method which returns a value if it lies
     * inside the visible dataArea and otherwise the corresponding
     * coordinate on the border of the dataArea. The PlotOrientation
     * is taken into account.
     * Useful to avoid possible sun.dc.pr.PRException: endPath: bad path
     * which occurs when trying to draw lines/shapes which in large part
     * lie outside of the visible dataArea.
     *
     * @param value the value which shall be
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @return <code>double</code> value inside the data area.
     */
    protected static double restrictValueToDataArea(double value,
                                                    XYPlot plot,
                                                    Rectangle2D dataArea) {
        double min = 0;
        double max = 0;
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            min = dataArea.getMinY();
            max = dataArea.getMaxY();
        }
        else if (plot.getOrientation() ==  PlotOrientation.HORIZONTAL) {
            min = dataArea.getMinX();
            max = dataArea.getMaxX();
        }
        if (value < min) {
            value = min;
        }
        else if (value > max) {
            value = max;
        }
        return value;
    }

}
