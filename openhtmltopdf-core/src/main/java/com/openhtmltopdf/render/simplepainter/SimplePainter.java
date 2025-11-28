/*
 * {{{ header & license
 * Copyright (c) 2025 mgm technology partners GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.render.simplepainter;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

import com.openhtmltopdf.extend.StructureType;
import com.openhtmltopdf.layout.CollapsedBorderSide;
import com.openhtmltopdf.layout.InlinePaintable;
import com.openhtmltopdf.layout.Layer;
import com.openhtmltopdf.newtable.TableCellBox;
import com.openhtmltopdf.render.BlockBox;
import com.openhtmltopdf.render.Box;
import com.openhtmltopdf.render.DisplayListItem;
import com.openhtmltopdf.render.OperatorClip;
import com.openhtmltopdf.render.OperatorSetClip;
import com.openhtmltopdf.render.RenderingContext;
import com.openhtmltopdf.render.displaylist.DisplayListCollector;
import com.openhtmltopdf.render.displaylist.TransformCreator;

public class SimplePainter {
    private final int xTranslate;
    private final int yTranslate;
    
    public SimplePainter(int xtrans, int ytrans) {
        this.xTranslate = xtrans;
        this.yTranslate = ytrans;
    }
    
    private void debugOnly(String msg, Object arg) {
        //System.out.println(msg + " : " + arg);
    }
    
    public void paintLayer(RenderingContext c, Layer layer) {
        Box master = layer.getMaster();
        Rectangle parentClip = layer.getMaster().getParentClipBox(c, layer.getParent());
        
        if (parentClip != null) {
            c.getOutputDevice().pushClip(parentClip);
        }

        if (layer.hasLocalTransform()) {
            AffineTransform transform = c.isInPageMargins() ? 
                    TransformCreator.createPageMarginCoordinatesTransform(c, master, c.getPage(), xTranslate, yTranslate) :
                    TransformCreator.createPageCoordinatesTranform(c, master, c.getPage(), c.getShadowPageNumber());
                    
            c.getOutputDevice().pushTransformLayer(transform);
        }
        
        if (((BlockBox) master).isReplaced()) {
            paintLayerBackgroundAndBorder(c, master);
            paintReplacedElement(c, (BlockBox) master);
        } else {
            SimpleBoxCollector boxCollector = new SimpleBoxCollector();
            boxCollector.collect(c, layer);
            
            if (master instanceof BlockBox) {
                paintLayerBackgroundAndBorder(c, master);
            }

            if (layer.isRootLayer() || layer.isStackingContext() || master.getStyle().isFixed()) {
                paintLayers(c, layer.getSortedLayers(Layer.NEGATIVE));
            }
            
            Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders = boxCollector.tcells().isEmpty() ? null
                    : DisplayListCollector.collectCollapsedTableBorders(c, boxCollector.tcells());
            
            paintBackgroundsAndBorders(c, boxCollector.blocks(), collapsedTableBorders);
            paintFloats(c, layer.getFloats());
            paintListMarkers(c, boxCollector.listItems());
            paintInlineContent(c, boxCollector.inlines());
            paintReplacedElements(c, boxCollector.replaceds());
            
            if (layer.isRootLayer() || layer.isStackingContext() || master.getStyle().isFixed()) {
                paintLayers(c, layer.collectLayers(Layer.AUTO));
                // TODO z-index: 0 layers should be painted atomically
                paintLayers(c, layer.getSortedLayers(Layer.ZERO));
                paintLayers(c, layer.getSortedLayers(Layer.POSITIVE));
            }
        }

        if (layer.hasLocalTransform()) {
            c.getOutputDevice().popTransformLayer();
        }
        
        if (parentClip != null) {
            c.getOutputDevice().popClip();
        }
    }
    
    private void paintLayerBackgroundAndBorder(RenderingContext c, Box master) {
        master.paintBackground(c);
        master.paintBorder(c);
    }
    
    private void clip(RenderingContext c, OperatorClip clip) {
        debugOnly("clipping", clip.getClip());
        c.getOutputDevice().pushClip(clip.getClip());
    }
    
    private void setClip(RenderingContext c, OperatorSetClip setclip) {
        debugOnly("popping clip", null);
        c.getOutputDevice().popClip();
    }

    private void paintBackgroundsAndBorders(RenderingContext c, List<DisplayListItem> blocks, Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders) {
        for (DisplayListItem dli : blocks) {
            if (dli instanceof OperatorClip) {
                OperatorClip clip = (OperatorClip) dli;
                clip(c, clip);
            } else if (dli instanceof OperatorSetClip) {
                OperatorSetClip setClip = (OperatorSetClip) dli;
                setClip(c, setClip);
            } else {
                BlockBox box = (BlockBox) dli;

                // add header and footer tagging
                Object outerToken = c.getOutputDevice().startStructure(StructureType.BLOCK, box);
                Object innerToken = c.getOutputDevice().startStructure(StructureType.BACKGROUND, box);
                // change end
                
                debugOnly("painting bg", box);
                box.paintBackground(c);
                box.paintBorder(c);

                if (collapsedTableBorders != null && box instanceof TableCellBox) {
                    TableCellBox cell = (TableCellBox) box;

                    if (cell.hasCollapsedPaintingBorder()) {
                        List<CollapsedBorderSide> borders = collapsedTableBorders.get(cell);

                        if (borders != null) {
                            for (CollapsedBorderSide border : borders) {
                                border.getCell().paintCollapsedBorder(c, border.getSide());
                            }
                        }
                    }
                }

                // add header and footer tagging
                c.getOutputDevice().endStructure(innerToken);
                c.getOutputDevice().endStructure(outerToken);
                // change end
            }
        }
        
    }

    private void paintListMarkers(RenderingContext c, List<DisplayListItem> listItems) {
        for (DisplayListItem dli : listItems) {
            if (dli instanceof OperatorClip) {
                OperatorClip clip = (OperatorClip) dli;
                clip(c, clip);
            } else if (dli instanceof OperatorSetClip) {
                OperatorSetClip setClip = (OperatorSetClip) dli;
                setClip(c, setClip);
            } else {
                debugOnly("Painting list item", dli);
                ((BlockBox) dli).paintListMarker(c);
            }
        }
    }

    private void paintInlineContent(RenderingContext c, List<DisplayListItem> inlines) {
        for (DisplayListItem dli : inlines) {
            if (dli instanceof OperatorClip) {
                OperatorClip clip = (OperatorClip) dli;
                clip(c, clip);
            } else if (dli instanceof OperatorSetClip) {
                OperatorSetClip setClip = (OperatorSetClip) dli;
                setClip(c, setClip);
            } else if (dli instanceof BlockBox) {
                // Inline blocks need to be painted as a layer.
                BlockBox bb = (BlockBox) dli;
                paintAsLayer(c, bb);
            } else {
                InlinePaintable paintable = (InlinePaintable) dli;
                debugOnly("Painting Inline", paintable);

                // add header and footer tagging
                Object token = c.getOutputDevice().startStructure(StructureType.INLINE, (Box) dli);
                // change end

                paintable.paintInline(c);

                // add header and footer tagging
                c.getOutputDevice().endStructure(token);
                // change end
            }
        }
        
    }
    
    private void paintReplacedElement(RenderingContext c, BlockBox replaced) {
        
        Rectangle contentBounds = replaced.getContentAreaEdge(
                replaced.getAbsX(), replaced.getAbsY(), c);
        
        // Minor hack:  It's inconvenient to adjust for margins, border, padding during
        // layout so just do it here.
        Point loc = replaced.getReplacedElement().getLocation();
        if (contentBounds.x != loc.x || contentBounds.y != loc.y) {
            replaced.getReplacedElement().setLocation(contentBounds.x, contentBounds.y);
        }

        // add header and footer tagging
        Object outerToken = c.getOutputDevice().startStructure(StructureType.REPLACED, replaced);
        c.getOutputDevice().paintReplacedElement(c, replaced);
        c.getOutputDevice().endStructure(outerToken);
        // change end
    }

    private void paintReplacedElements(RenderingContext c, List<DisplayListItem> replaceds) {
        for (DisplayListItem dli : replaceds) {
            if (dli instanceof OperatorClip) {
                OperatorClip clip = (OperatorClip) dli;
                clip(c, clip);
            } else if (dli instanceof OperatorSetClip) {
                OperatorSetClip setClip = (OperatorSetClip) dli;
                setClip(c, setClip);
            } else {
                BlockBox box = (BlockBox) dli;
                paintReplacedElement(c, box);
            }
        }
    }

    private void paintFloats(RenderingContext c, List<BlockBox> floaters) {
        for (int i = floaters.size() - 1; i >= 0; i--) {
            paintAsLayer(c, floaters.get(i));
        }
    }
    
    private void paintLayers(RenderingContext c, List<Layer> layers) {
        for (Layer layer : layers) {
            paintLayer(c, layer);
        }
    }
    
    public void paintAsLayer(RenderingContext c, BlockBox startingPoint) {
        if (startingPoint.getStyle().requiresLayer()) {
            return;
        }
        
        SimpleBoxCollector collector = new SimpleBoxCollector();
        collector.collect(c, startingPoint.getContainingLayer(), startingPoint);

        Map<TableCellBox, List<CollapsedBorderSide>>  collapsedTableBorders = collector.tcells().isEmpty() ? null : DisplayListCollector.collectCollapsedTableBorders(c, collector.tcells());

        paintBackgroundsAndBorders(c, collector.blocks(), collapsedTableBorders);
        paintListMarkers(c, collector.listItems());
        paintInlineContent(c, collector.inlines());
        paintReplacedElements(c, collector.replaceds());
    }
}
