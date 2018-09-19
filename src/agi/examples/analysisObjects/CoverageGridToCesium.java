package agi.examples.analysisObjects;

import java.awt.Color;
import java.util.List;

import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.cesium.PointGraphics;
import agi.foundation.cesium.PointGraphicsExtension;
import agi.foundation.geometry.discrete.CoverageGridPoint;
import agi.foundation.geometry.discrete.ICoverageGrid;
import agi.foundation.platforms.Platform;

public class CoverageGridToCesium {
    /**
     * Visualizes a ICoverageGrid as points in the specified color.
     * @param document the CZML document to add the visualizations to
     * @param grid the coverage grid to visualize
     * @param color the color of the grid points
     */
    public static void GenerateGridCZML(CzmlDocument document, ICoverageGrid grid, Color color)
    {
        List<CoverageGridPoint> gridPoints = grid.generateGridPoints();
        
        for(CoverageGridPoint point : gridPoints){
            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(point.getPosition());
            PointGraphics pointGraphics = new PointGraphics();
            pointGraphics.setColor(new ConstantCesiumProperty<>(color));
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(10.0));
            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            
            document.getObjectsToWrite().add(gridPointPlatform);
        }
    }
}
