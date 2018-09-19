package agi.examples.analysisObjects;

import agi.foundation.Bounds;
import agi.foundation.Trig;
import agi.foundation.celestial.CentralBody;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.coordinates.CartographicExtent;
import agi.foundation.geometry.discrete.ExtrudedCentralBodyCoverageGrid;
import agi.foundation.geometry.discrete.ICoverageGrid;
import agi.foundation.geometry.discrete.SpecifiedNumberOfPointsCoverageGriddingTechnique;
import agi.foundation.geometry.discrete.SurfaceRegionsCoverageGrid;
import agi.foundation.geometry.shapes.EllipsoidGeodesic;
import agi.foundation.geometry.shapes.EllipsoidSurfaceRegion;
import agi.foundation.terrain.StkTerrainServer;

public class CreateCoverageGridNearPoint{

    /**
     * Creates an extruded coverage grid with 30km extent about the input point.
     * @param point the point about which the grid is created
     * @param centralBody the central body upon which the grid is defined
     * @return the coverage grid
     */
    public static ICoverageGrid CreateCoverageGrid(Cartographic point, CentralBody centralBody){
        // First cb grid then extrude
        EllipsoidGeodesic geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, 0.0, 15000.0);
        Cartographic north = geodesicFromPoint.getFinalPoint();
        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, Math.PI, 15000.0);
        Cartographic south = geodesicFromPoint.getFinalPoint();

        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, Math.PI/2.0, 15000.0);
        Cartographic east = geodesicFromPoint.getFinalPoint();

        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, 3.0*Math.PI/2.0, 15000.0);
        Cartographic west = geodesicFromPoint.getFinalPoint();

        CartographicExtent extent = new CartographicExtent(west.getLongitude(), south.getLatitude(), east.getLongitude(), north.getLatitude());
        EllipsoidSurfaceRegion region = EllipsoidSurfaceRegion.createRegionUsingExtent(centralBody.getShape(), extent);

        // create 2D grid
        SurfaceRegionsCoverageGrid grid = new SurfaceRegionsCoverageGrid(Trig.degreesToRadians(0.01), centralBody, region);

        // extrude for fanciness
        ExtrudedCentralBodyCoverageGrid extrudedGrid = new ExtrudedCentralBodyCoverageGrid(grid);
        extrudedGrid.setHeightBounds(new Bounds(0.0, 100.0));
        extrudedGrid.setHeightGriddingTechnique(new SpecifiedNumberOfPointsCoverageGriddingTechnique(10));

        return extrudedGrid;
    }

    /**
     * Creates an extruded coverage grid with 30km extent about the input point.
     * NOTE- Uses the AGI Terrain Server- if one uses this code in production this must be switched to the appropriate terrain server.
     * @param point the point about which the grid is created
     * @param centralBody the central body upon which the grid is defined
     * @param resolutionInDegrees the lat/lon spatial resolution of the grid
     * @return the coverage grid
     */
    public static ICoverageGrid CreateCoverageGridWithTerrain(Cartographic point, CentralBody centralBody, double resolutionInDegrees){
        // First cb grid then extrude
        EllipsoidGeodesic geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, 0.0, 15000.0);
        Cartographic north = geodesicFromPoint.getFinalPoint();
        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, Math.PI, 15000.0);
        Cartographic south = geodesicFromPoint.getFinalPoint();

        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, Math.PI/2.0, 15000.0);
        Cartographic east = geodesicFromPoint.getFinalPoint();

        geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, 3.0*Math.PI/2.0, 15000.0);
        Cartographic west = geodesicFromPoint.getFinalPoint();

        CartographicExtent extent = new CartographicExtent(west.getLongitude(), south.getLatitude(), east.getLongitude(), north.getLatitude());
        EllipsoidSurfaceRegion region = EllipsoidSurfaceRegion.createRegionUsingExtent(centralBody.getShape(), extent);

        // terrain

        StkTerrainServer server = new StkTerrainServer();

        // create 2D grid
        SurfaceRegionsCoverageGrid grid = new SurfaceRegionsCoverageGrid(Trig.degreesToRadians(resolutionInDegrees), centralBody, region);
        grid.setReferenceTerrain(server);

        // extrude for fanciness
        ExtrudedCentralBodyCoverageGrid extrudedGrid = new ExtrudedCentralBodyCoverageGrid(grid);
        extrudedGrid.setHeightBounds(new Bounds(0.0, 1000.0));
        extrudedGrid.setHeightGriddingTechnique(new SpecifiedNumberOfPointsCoverageGriddingTechnique(8));

        return extrudedGrid;
    }
}