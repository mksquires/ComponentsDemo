package agi.examples.analysisObjects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CentralBodySurfaceRegionGraphics;
import agi.foundation.cesium.CentralBodySurfaceRegionGraphicsExtension;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.cesium.PointGraphics;
import agi.foundation.cesium.PointGraphicsExtension;
import agi.foundation.cesium.SolidColorMaterialGraphics;
import agi.foundation.cesium.TimeIntervalCesiumProperty;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.coverage.CoverageGridPointWithResults;
import agi.foundation.coverage.CoverageResults;
import agi.foundation.geometry.discrete.CentralBodyCoverageGrid;
import agi.foundation.geometry.discrete.CoverageGridPoint;
import agi.foundation.geometry.discrete.EllipsoidSurfaceRegionCoverageGridCell;
import agi.foundation.geometry.discrete.ICoverageGridCell;
import agi.foundation.geometry.shapes.EllipsoidSurfaceRegion;
import agi.foundation.platforms.CentralBodySurfaceRegion;
import agi.foundation.platforms.Platform;
import agi.foundation.time.TimeIntervalCollection;
import agi.foundation.time.TimeIntervalCollection1;


public class CoverageOnCentralBodyToCesium {
    /**
     * Adds a black point for the grid point and a colored surface region based upon access
     * No Access- magenta
     * Access- yellow
     * Unknown- blue
     * @param document the CZML document that the visualization is to be added to
     * @param results the CoverageResults to visualize
     */
    public static void CoverageResultsToSurfaceRegionGraphics(CzmlDocument document, CoverageResults results) {
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();

        for (CoverageGridPointWithResults pwr : pointsWithResults) {
            ICoverageGridCell cell = pwr.getCoverageGridPoint().getCoverageGridCell();
            EllipsoidSurfaceRegionCoverageGridCell cell2D = (EllipsoidSurfaceRegionCoverageGridCell) cell; // throws, which is good
            EllipsoidSurfaceRegion regionOfCell  = EllipsoidSurfaceRegion.createRegionUsingDiscreteCurves(cell2D.getGridCellBoundary().getEllipsoid(), cell2D.getGridCellBoundary());
            CentralBodySurfaceRegion cbRegionOfCell = new CentralBodySurfaceRegion(pwr.getCoverageGridPoint().getCentralBody(), regionOfCell);

            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(pwr.getCoverageGridPoint().getPosition());
            PointGraphics pointGraphics = new PointGraphics();

            TimeIntervalCollection accessIntervals = pwr.getAssetCoverage().getSatisfactionIntervals();
            TimeIntervalCollection unkownCalssificationintervals = pwr.getAssetCoverage().getUnknownIntervals(); // probably won't use this
            TimeIntervalCollection consideredIntervals = pwr.getAssetCoverage().getConsideredIntervals();
            TimeIntervalCollection noAccessIntervals = accessIntervals.complement(consideredIntervals.getStart(), consideredIntervals.getStop());

            TimeIntervalCollection1<Color> noAccessWithColor =  noAccessIntervals.addData(Color.MAGENTA);
            TimeIntervalCollection1<Color> accessWithColor = accessIntervals.addData(Color.YELLOW);
            TimeIntervalCollection1<Color> unknownWithColor = unkownCalssificationintervals.addData(Color.BLUE);
            accessWithColor.add(noAccessWithColor); // now access has no access intervals
            accessWithColor.add(unknownWithColor); // now access has unknown intervals

            // Set color based on time intervals
            TimeIntervalCesiumProperty<Color> timeColorProperty = new TimeIntervalCesiumProperty<Color>();
            timeColorProperty.setIntervals(accessWithColor);

            // the material will be colored based upon access
            SolidColorMaterialGraphics cellMaterial = new SolidColorMaterialGraphics();
            cellMaterial.setColor(timeColorProperty);

            CentralBodySurfaceRegionGraphics cellGraphics = new CentralBodySurfaceRegionGraphics();
            cellGraphics.setMaterial(new ConstantCesiumProperty<>(cellMaterial));

            // point graphics for the grid point is just black
            //pointGraphics.setColor(new ConstantCesiumProperty<Color>(Color.BLACK));
            pointGraphics.setColor(timeColorProperty);
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(4.0));
            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            gridPointPlatform.getExtensions().add(cbRegionOfCell);
            gridPointPlatform.getExtensions().add(new CentralBodySurfaceRegionGraphicsExtension(cellGraphics));
            
            document.getObjectsToWrite().add(gridPointPlatform);
        }
    }


        /**
         * Creates a single polygon from a grid with a uniform color. This is intended to work with a 2D CentralBody coverage grid.
         * @param document the CZML document that the visualization is to be added to
         * @param grid the central body coverage grid to visualize
         */
        public static void CoverageGridOnCentralBodyToSinglePolygon(CzmlDocument document, CentralBodyCoverageGrid grid){
                List<CoverageGridPoint> gridPoints = grid.generateGridPoints();
                
                // add all grid cells
                List<EllipsoidSurfaceRegionCoverageGridCell> cells = new ArrayList<EllipsoidSurfaceRegionCoverageGridCell>();
                for (CoverageGridPoint gridPoint : gridPoints) {
                    ICoverageGridCell cell = gridPoint.getCoverageGridCell();
                    EllipsoidSurfaceRegionCoverageGridCell cell2D = (EllipsoidSurfaceRegionCoverageGridCell) cell; // throws, which is good
                    cells.add(cell2D);
                }

                EllipsoidSurfaceRegion region = CellsToRegion(cells, grid.getCentralBody());
 
                CentralBodySurfaceRegion cbRegion = new CentralBodySurfaceRegion(grid.getCentralBody(), region);

                // the material will be colored based upon access
                SolidColorMaterialGraphics gridMaterial = new SolidColorMaterialGraphics();
                gridMaterial.setColor(new ConstantCesiumProperty<Color>(Color.ORANGE));

                CentralBodySurfaceRegionGraphics cellGraphics = new CentralBodySurfaceRegionGraphics();
                cellGraphics.setMaterial(new ConstantCesiumProperty<>(gridMaterial));

                // we attach this region to a platform at the centroid
                Platform gridPointPlatform = new Platform();
                gridPointPlatform.setName("grid");
                gridPointPlatform.setLocationPoint(gridPoints.get(0).getPosition());
                gridPointPlatform.getExtensions().add(cbRegion);
                gridPointPlatform.getExtensions().add(new CentralBodySurfaceRegionGraphicsExtension(cellGraphics));
                
                document.getObjectsToWrite().add(gridPointPlatform);
        }

        /**
         * A helper method that takes a list of EllipsoidSurfaceRegionCoverageGridCells and produces a single EllipsoidSurfaceRegion to represent those cells.
         * @param cells the grid cells to use to compute the encompassing surface region
         * @param centralBody the central body on which the output surface region lies
         * @return the surface region representing the grid cells
         */
        public static EllipsoidSurfaceRegion CellsToRegion(List<EllipsoidSurfaceRegionCoverageGridCell> cells, CentralBody centralBody){
            if(cells.size() < 1)
                return null;
            
            // grand region data
            EllipsoidSurfaceRegion grandRegion = null;
            ArrayList<Cartographic> grandRegionPoints = new ArrayList<Cartographic>();

            for (EllipsoidSurfaceRegionCoverageGridCell cell : cells) {
                // create this cell's region
                EllipsoidSurfaceRegion regionOfCell  = EllipsoidSurfaceRegion.createRegionUsingDiscreteCurves(cell.getGridCellBoundary().getEllipsoid(), cell.getGridCellBoundary());

                // combine all regions into one cell and return it
                if(grandRegion == null){
                    // first region
                    grandRegion = regionOfCell;
                    grandRegionPoints.addAll(cell.getGridCellBoundaryVertices());
                    continue;
                }

                // add all vertices not in the region
                for (Cartographic gridCellVertex : cell.getGridCellBoundaryVertices()) {
                    if(!grandRegion.isPointInsideRegion(gridCellVertex)){
                        grandRegionPoints.add(gridCellVertex);
                    }
                }
                
                // re-create grand region
                grandRegion = EllipsoidSurfaceRegion.createRegionUsingDiscreteRhumbLines(centralBody.getShape(), 0.01, grandRegionPoints);                   
            }

            return grandRegion;
        }

    
}