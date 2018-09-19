package agi.examples.scenarios;

import java.awt.Color;

import agi.examples.analysisObjects.CoverageResultsToCesium;
import agi.examples.analysisObjects.CreateSatellite;
import agi.foundation.Trig;
import agi.foundation.access.LinkInstantaneous;
import agi.foundation.access.constraints.CentralBodyObstructionConstraint;
import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.coverage.AssetDefinition;
import agi.foundation.coverage.CoverageResults;
import agi.foundation.coverage.ParameterizedSpatiallyPartitionedCoverageDefinition;
import agi.foundation.geometry.AxesEastNorthUp;
import agi.foundation.geometry.PointCartographic;
import agi.foundation.geometry.ReferenceFrame;
import agi.foundation.geometry.discrete.CuboidCoverageGrid;
import agi.foundation.platforms.Platform;
import agi.foundation.time.TimeInterval;

public class ISSToGridNearAGIScenario{

    /**
     * Creates the scenario of the ISS to AGI comm link.
     * @param centralBody the central body on which this scenario is created
     * @param analysisInterval the time interval in which to analyze the scenario
     * @param document the CZML document into which the results of the scenario should be written
     * @return the name of the scenario
     */
    public static String CreateScenario(CentralBody centralBody, TimeInterval analysisInterval, CzmlDocument document){
        // Create a grid by AGI
        Cartographic gridCenterCartographic = new Cartographic(Trig.degreesToRadians(-75.59567709999999), Trig.degreesToRadians(40.0392551), 100000.0/2.0);
        PointCartographic gridCenterPoint = new PointCartographic(centralBody, gridCenterCartographic);
        ReferenceFrame frameAtAGI = new ReferenceFrame(gridCenterPoint, new AxesEastNorthUp(centralBody, gridCenterPoint));
        CuboidCoverageGrid cubeGrid = new CuboidCoverageGrid(100000.0, 100000.0, 100000.0, 10, frameAtAGI);

        // Create coverage problem for coloring
        ParameterizedSpatiallyPartitionedCoverageDefinition coverage = new ParameterizedSpatiallyPartitionedCoverageDefinition(cubeGrid, true);
    
        Platform iss = CreateSatellite.Create("iss", "http://assets02.agi.com/models/iss.gltf", centralBody, analysisInterval.getStart(), 0.0, 0.0, 0.1, 1e7, Color.ORANGE);

        // Create link between the coverage grid and the ISS.
        LinkInstantaneous link = new LinkInstantaneous(coverage.getGridPointPlaceholder(), iss);

        // Constrain access for the link so that it only exists when no part of the earth
        // is between the coverage grid and the ISS.
        CentralBodyObstructionConstraint constraint = new CentralBodyObstructionConstraint(link, centralBody);
        coverage.addAsset(new AssetDefinition(link, constraint));

        // Run the coverage problem
        CoverageResults results = coverage.computeCoverageOverTheGrid(analysisInterval.getStart(), analysisInterval.getStop());

        // add objects to the czml
        CoverageResultsToCesium.ResultsToCesiumPathOnlyWithExtrudedPolygons(document, results, centralBody);
        document.getObjectsToWrite().add(iss);

        // return the scenario name
        return "issNearAGI";
    } 
}