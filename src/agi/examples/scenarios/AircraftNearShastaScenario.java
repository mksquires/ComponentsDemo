package agi.examples.scenarios;

import agi.examples.analysisObjects.CoverageResultsToCesium;
import agi.examples.analysisObjects.CreateAircraft;
import agi.examples.analysisObjects.CreateCoverageGridNearPoint;
import agi.foundation.Trig;
import agi.foundation.access.AccessQuery;
import agi.foundation.access.LinkInstantaneous;
import agi.foundation.access.LinkRole;
import agi.foundation.access.constraints.CentralBodyObstructionConstraint;
import agi.foundation.access.constraints.SensorVolumeConstraint;
import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.coverage.AssetDefinition;
import agi.foundation.coverage.CoverageResults;
import agi.foundation.coverage.ParameterizedSpatiallyPartitionedCoverageDefinition;
import agi.foundation.geometry.discrete.ICoverageGrid;
import agi.foundation.platforms.Platform;
import agi.foundation.time.TimeInterval;

public class AircraftNearShastaScenario{

    /**
     * Creates an aircraft near Mt. Shasta in California.
     * @param centralBody the central body on which this scenario is created
     * @param analysisInterval the time interval in which to analyze the scenario
     * @param document the CZML document into which the results of the scenario should be written
     * @return the name of the scenario
     */
    public static String CreateScenario(CentralBody centralBody, TimeInterval analysisInterval, CzmlDocument document){
            
            Cartographic mtShastaLocation = new Cartographic(Trig.degreesToRadians(-122.3106), Trig.degreesToRadians(41.3099), 0.0);
            
            // aircraft and sensor
            Platform aircraft = CreateAircraft.CreateAircraftAroundPoint(analysisInterval.getStart(), mtShastaLocation, centralBody);
            Platform sensor = CreateAircraft.AddSensorToAircraft(aircraft, centralBody);

            CreateAircraft.AddAircraftGraphics(aircraft, "https://assets02.agi.com/models/f-22a_raptor.gltf", null, null);
            CreateAircraft.AddSensorGraphics(sensor);

            // grid
            ICoverageGrid grid = CreateCoverageGridNearPoint.CreateCoverageGridWithTerrain(mtShastaLocation, centralBody, 0.001); // 0.001 in picture

            // coverage definition
            ParameterizedSpatiallyPartitionedCoverageDefinition coverage = new ParameterizedSpatiallyPartitionedCoverageDefinition(grid, true);

            // constraints

            // cb obstruction constraint
            LinkInstantaneous link = new LinkInstantaneous(coverage.getGridPointPlaceholder(), sensor);
            CentralBodyObstructionConstraint constraint = new CentralBodyObstructionConstraint(link, centralBody);

            SensorVolumeConstraint sensorVolumeConstraint = new SensorVolumeConstraint(link, LinkRole.RECEIVER);

            coverage.addAsset(new AssetDefinition(link, AccessQuery.and(constraint, sensorVolumeConstraint)));

            // Run the coverage problem
            CoverageResults results = coverage.computeCoverageOverTheGrid(analysisInterval.getStart(), analysisInterval.getStop());

            // add objects to the czml
            CoverageResultsToCesium.ResultsToCesiumPathOnlyWithExtrudedPolygons(document, results, centralBody);
            document.getObjectsToWrite().add(aircraft);
            document.getObjectsToWrite().add(sensor);

            // return the scenario name
            return "aircraftNearShasta";
    }
}