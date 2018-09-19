package agi.examples.analysisObjects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import agi.examples.utilities.ColorGradient;
import agi.foundation.EvaluatorGroup;
import agi.foundation.Trig;
import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CentralBodySurfaceRegionGraphics;
import agi.foundation.cesium.CentralBodySurfaceRegionGraphicsExtension;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.cesium.PointGraphics;
import agi.foundation.cesium.PointGraphicsExtension;
import agi.foundation.cesium.SolidColorMaterialGraphics;
import agi.foundation.cesium.TimeIntervalCesiumProperty;
import agi.foundation.cesium.advanced.IMaterialGraphics;
import agi.foundation.coordinates.Cartesian;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.coverage.CoverageGridPointWithResults;
import agi.foundation.coverage.CoverageResults;
import agi.foundation.platforms.CentralBodySurfaceRegion;
import agi.foundation.platforms.Platform;
import agi.foundation.time.JulianDate;
import agi.foundation.time.TimeInterval;
import agi.foundation.time.TimeIntervalCollection;
import agi.foundation.time.TimeIntervalCollection1;

public class CoverageResultsToCesium{

    /** 
     * Creates points to visualize in SWVL/Cesium that represent the coverage grid results.
     * Colors- magenta = no access, yellow = access, blue = unknown 
     * @param document the CZML document in which the results will be written
     * @param results the results of the coverage calulation to be visualized
    */
    public static void ResultsToCesium(CzmlDocument document, CoverageResults results){
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();

        for(CoverageGridPointWithResults pwr : pointsWithResults){
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

            pointGraphics.setColor(timeColorProperty);
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(10.0));
            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            
            document.getObjectsToWrite().add(gridPointPlatform);
        }

    }

    /** 
     * Creates points to visualize in SWVL/Cesium that represent the coverage grid results.
     * Colors- magenta = no access, yellow = access, blue = unknown
     * @param document the CZML document in which the results will be written
     * @param results the results of the coverage calulation to be visualized  
     * */
    public static void ResultsToCesiumWithPathBehavior(CzmlDocument document, CoverageResults results){
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();

        for(CoverageGridPointWithResults pwr : pointsWithResults){
            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(pwr.getCoverageGridPoint().getPosition());
            PointGraphics pointGraphics = new PointGraphics();

            TimeIntervalCollection accessIntervals = pwr.getAssetCoverage().getSatisfactionIntervals();
            TimeIntervalCollection unkownCalssificationintervals = pwr.getAssetCoverage().getUnknownIntervals();
            TimeIntervalCollection consideredIntervals = pwr.getAssetCoverage().getConsideredIntervals();

            if(accessIntervals.getIsEmpty() == false){
                JulianDate start = accessIntervals.getStart();
                JulianDate stop = consideredIntervals.getStop();
                accessIntervals = new TimeIntervalCollection(new TimeInterval(start, stop));
            }

            TimeIntervalCollection noAccessIntervals = accessIntervals.complement(consideredIntervals.getStart(), consideredIntervals.getStop());

            TimeIntervalCollection1<Color> noAccessWithColor =  noAccessIntervals.addData(Color.MAGENTA);
            TimeIntervalCollection1<Color> accessWithColor = accessIntervals.addData(Color.YELLOW);
            TimeIntervalCollection1<Color> unknownWithColor = unkownCalssificationintervals.addData(Color.BLUE);
            accessWithColor.add(noAccessWithColor); // now access has no access intervals
            accessWithColor.add(unknownWithColor); // now access has unknown intervals

            // Set color based on time intervals
            TimeIntervalCesiumProperty<Color> timeColorProperty = new TimeIntervalCesiumProperty<Color>();
            timeColorProperty.setIntervals(accessWithColor);

            pointGraphics.setColor(timeColorProperty);
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(10.0));
            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            
            document.getObjectsToWrite().add(gridPointPlatform);
        }
    }

    /** 
     * Creates points to visualize in SWVL/Cesium that represent the coverage grid results.
     * Grid points are colored red after access starts. Points are otherwise colored based upon their height relative to WGS84.
     * Colors- red = access, blue = unknown
     * @param document the CZML document in which the results will be written
     * @param results the results of the coverage calulation to be visualized  
     * @param centralBody the central body in which the results are defined
     * */
    public static void ResultsToCesiumWithHeightGradientColoring(CzmlDocument document, CoverageResults results, CentralBody centralBody){
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();
        System.out.println("grid pts " + pointsWithResults.size());
        EvaluatorGroup group = new EvaluatorGroup();
        ColorGradient gradient = new ColorGradient(6);

        for(CoverageGridPointWithResults pwr : pointsWithResults){
            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(pwr.getCoverageGridPoint().getPosition());
            PointGraphics pointGraphics = new PointGraphics();

            TimeIntervalCollection accessIntervals = pwr.getAssetCoverage().getSatisfactionIntervals();
            TimeIntervalCollection unkownCalssificationintervals = pwr.getAssetCoverage().getUnknownIntervals();
            TimeIntervalCollection consideredIntervals = pwr.getAssetCoverage().getConsideredIntervals();

            if(accessIntervals.getIsEmpty() == false){
                JulianDate start = accessIntervals.getStart();
                JulianDate stop = consideredIntervals.getStop();
                accessIntervals = new TimeIntervalCollection(new TimeInterval(start, stop));
            }

            // get height above GEOID
            JulianDate heightDate = consideredIntervals.getStart();
            Cartesian gridPt = pwr.getCoverageGridPoint().getPosition().getEvaluator(group).evaluate(heightDate);
            //CentralBody cb = pwr.getCoverageGridPoint().getCentralBody(); // we know this is not null (but there is a bug in 2018r3 that will be fixed in 2018r4)
            Cartographic gridPtCartographic = centralBody.getShape().cartesianToCartographic(gridPt);
            double height = gridPtCartographic.getHeight();

            Color gridHeightColor = gradient.GetBucketedColor(height);
            

            TimeIntervalCollection noAccessIntervals = accessIntervals.complement(consideredIntervals.getStart(), consideredIntervals.getStop());

            TimeIntervalCollection1<Color> noAccessWithColor =  noAccessIntervals.addData(gridHeightColor);
            TimeIntervalCollection1<Color> accessWithColor = accessIntervals.addData(Color.RED);
            TimeIntervalCollection1<Color> unknownWithColor = unkownCalssificationintervals.addData(gridHeightColor);
            accessWithColor.add(noAccessWithColor); // now access has no access intervals
            accessWithColor.add(unknownWithColor); // now access has unknown intervals

            // Set color based on time intervals
            TimeIntervalCesiumProperty<Color> timeColorProperty = new TimeIntervalCesiumProperty<Color>();
            timeColorProperty.setIntervals(accessWithColor);

            pointGraphics.setColor(timeColorProperty);
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(8.0));
            pointGraphics.setOutlineColor(timeColorProperty);
            pointGraphics.setOutlineWidth(new ConstantCesiumProperty<Double>(0.0));
            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            
            document.getObjectsToWrite().add(gridPointPlatform);
        }

    }

    /** 
     * Creates points to visualize in SWVL/Cesium that represent the coverage grid results.
     * Grid points are colored red after access starts. Points are otherwise colored based upon their height relative to WGS84.
     * Colors- red = access, blue = unknown
     * @param document the CZML document in which the results will be written
     * @param results the results of the coverage calulation to be visualized  
     * @param centralBody the central body in which the results are defined
     * */
    public static void ResultsToCesiumPathOnly(CzmlDocument document, CoverageResults results, CentralBody centralBody){
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();
        System.out.println("grid pts " + pointsWithResults.size());
        EvaluatorGroup group = new EvaluatorGroup();
        ColorGradient gradient = new ColorGradient(6);

        for(CoverageGridPointWithResults pwr : pointsWithResults){
            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(pwr.getCoverageGridPoint().getPosition());
            PointGraphics pointGraphics = new PointGraphics();

            TimeIntervalCollection accessIntervals = pwr.getAssetCoverage().getSatisfactionIntervals();
            TimeIntervalCollection unkownCalssificationintervals = pwr.getAssetCoverage().getUnknownIntervals();
            TimeIntervalCollection consideredIntervals = pwr.getAssetCoverage().getConsideredIntervals();

            if(accessIntervals.getIsEmpty() == false){
                JulianDate start = accessIntervals.getStart();
                JulianDate stop = consideredIntervals.getStop();
                accessIntervals = new TimeIntervalCollection(new TimeInterval(start, stop));
            }
            else{
                continue; // skip over irrelevant points
            }

            // get height above GEOID
            JulianDate heightDate = consideredIntervals.getStart();
            Cartesian gridPt = pwr.getCoverageGridPoint().getPosition().getEvaluator(group).evaluate(heightDate);
            //CentralBody cb = pwr.getCoverageGridPoint().getCentralBody(); // we know this is not null (but there is currently a bug)
            Cartographic gridPtCartographic = centralBody.getShape().cartesianToCartographic(gridPt);
            double height = gridPtCartographic.getHeight();

            Color gridHeightColor = gradient.GetBucketedColor(height);
            

            TimeIntervalCollection noAccessIntervals = accessIntervals.complement(consideredIntervals.getStart(), consideredIntervals.getStop());

            TimeIntervalCollection1<Boolean> noAccessWithColor =  noAccessIntervals.addData(false);
            TimeIntervalCollection1<Boolean> accessWithColor = accessIntervals.addData(true);
            TimeIntervalCollection1<Boolean> unknownWithColor = unkownCalssificationintervals.addData(false);
            accessWithColor.add(noAccessWithColor); // now access has no access intervals
            accessWithColor.add(unknownWithColor); // now access has unknown intervals

            // Set color based on time intervals
            TimeIntervalCesiumProperty<Boolean> timeColorProperty = new TimeIntervalCesiumProperty<Boolean>();
            timeColorProperty.setIntervals(accessWithColor);

            pointGraphics.setShow(timeColorProperty);
            pointGraphics.setColor(new ConstantCesiumProperty<Color>(gridHeightColor));
            pointGraphics.setPixelSize(new ConstantCesiumProperty<>(8.0));
            pointGraphics.setOutlineColor(new ConstantCesiumProperty<Color>(gridHeightColor)); // due to a bug in cesium (1.48 release)
            pointGraphics.setOutlineWidth(new ConstantCesiumProperty<Double>(0.0));

            gridPointPlatform.getExtensions().add(new PointGraphicsExtension(pointGraphics));
            document.getObjectsToWrite().add(gridPointPlatform);            
        }
    }

    /** 
     * Creates extruded polygons to visualize in SWVL/Cesium that represent the coverage grid results.
     * Grid polygons are colored red after access starts. Points are otherwise colored based upon their height relative to WGS84.
     * Colors- red = access, blue = unknown
     * @param document the CZML document in which the results will be written
     * @param results the results of the coverage calulation to be visualized  
     * @param centralBody the central body in which the results are defined
     * */
    public static void ResultsToCesiumPathOnlyWithExtrudedPolygons(CzmlDocument document, CoverageResults results, CentralBody centralBody){
        List<CoverageGridPointWithResults> pointsWithResults = results.getGridPoints();
        System.out.println("grid pts " + pointsWithResults.size());
        EvaluatorGroup group = new EvaluatorGroup();
        ColorGradient gradient = new ColorGradient(6);

        for(CoverageGridPointWithResults pwr : pointsWithResults){
            

            TimeIntervalCollection accessIntervals = pwr.getAssetCoverage().getSatisfactionIntervals();
            TimeIntervalCollection unkownCalssificationintervals = pwr.getAssetCoverage().getUnknownIntervals();
            TimeIntervalCollection consideredIntervals = pwr.getAssetCoverage().getConsideredIntervals();

            if(accessIntervals.getIsEmpty() == false){
                JulianDate start = accessIntervals.getStart();
                JulianDate stop = consideredIntervals.getStop();
                accessIntervals = new TimeIntervalCollection(new TimeInterval(start, stop));
            }
            else{
                continue; // skip over irrelevant points
            }

            Platform gridPointPlatform = new Platform();
            gridPointPlatform.setName("grid");
            gridPointPlatform.setLocationPoint(pwr.getCoverageGridPoint().getPosition());

            // get height above GEOID
            JulianDate heightDate = consideredIntervals.getStart();
            Cartesian gridPt = pwr.getCoverageGridPoint().getPosition().getEvaluator(group).evaluate(heightDate);
            //CentralBody cb = pwr.getCoverageGridPoint().getCentralBody(); // we know this is not null (but there is currently a bug)
            Cartographic gridPtCartographic = centralBody.getShape().cartesianToCartographic(gridPt);
            double height = gridPtCartographic.getHeight();

            Color gridHeightColor = gradient.GetBucketedColor(height);

            double move = Trig.degreesToRadians(1e-3); // ~ 100m
            Cartographic pt1 = new Cartographic(-move+ gridPtCartographic.getLongitude(), move + gridPtCartographic.getLatitude(), gridPtCartographic.getHeight());
            Cartographic pt2 = new Cartographic(move+ gridPtCartographic.getLongitude(), move + gridPtCartographic.getLatitude(), gridPtCartographic.getHeight());
            Cartographic pt3 = new Cartographic(move+ gridPtCartographic.getLongitude(), -move + gridPtCartographic.getLatitude(), gridPtCartographic.getHeight());
            Cartographic pt4 = new Cartographic(-move+ gridPtCartographic.getLongitude(), -move + gridPtCartographic.getLatitude(), gridPtCartographic.getHeight());

            ArrayList<Cartographic> listCarto = new ArrayList<>();
            listCarto.add(pt1);
            listCarto.add(pt2);
            listCarto.add(pt3);
            listCarto.add(pt4);

            TimeIntervalCollection noAccessIntervals = accessIntervals.complement(consideredIntervals.getStart(), consideredIntervals.getStop());

            TimeIntervalCollection1<Boolean> noAccessWithColor =  noAccessIntervals.addData(false);
            TimeIntervalCollection1<Boolean> accessWithColor = accessIntervals.addData(true);
            TimeIntervalCollection1<Boolean> unknownWithColor = unkownCalssificationintervals.addData(false);
            accessWithColor.add(noAccessWithColor); // now access has no access intervals
            accessWithColor.add(unknownWithColor); // now access has unknown intervals

            // Set color based on time intervals
            TimeIntervalCesiumProperty<Boolean> timeColorProperty = new TimeIntervalCesiumProperty<Boolean>();
            timeColorProperty.setIntervals(accessWithColor);

            CentralBodySurfaceRegion region = new CentralBodySurfaceRegion(centralBody, listCarto);
            CentralBodySurfaceRegionGraphics regionGraphics = new CentralBodySurfaceRegionGraphics();
            regionGraphics.setHeight(new ConstantCesiumProperty<Double>(height));
            regionGraphics.setExtrudedHeight(new ConstantCesiumProperty<Double>(height+100.0));
            SolidColorMaterialGraphics solidMaterial = new SolidColorMaterialGraphics(new ConstantCesiumProperty<Color>(gridHeightColor));
            regionGraphics.setMaterial(new ConstantCesiumProperty<IMaterialGraphics>(solidMaterial));
            regionGraphics.setShow(timeColorProperty);

            gridPointPlatform.getExtensions().add(region);
            gridPointPlatform.getExtensions().add(new CentralBodySurfaceRegionGraphicsExtension(regionGraphics));
            document.getObjectsToWrite().add(gridPointPlatform);            
        }
    }
}