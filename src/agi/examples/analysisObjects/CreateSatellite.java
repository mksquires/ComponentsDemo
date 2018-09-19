package agi.examples.analysisObjects;

import java.awt.Color;

import agi.foundation.celestial.CentralBody;
import agi.foundation.celestial.WorldGeodeticSystem1984;
import agi.foundation.cesium.CesiumResource;
import agi.foundation.cesium.CesiumResourceBehavior;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.LabelGraphics;
import agi.foundation.cesium.LabelGraphicsExtension;
import agi.foundation.cesium.ModelGraphics;
import agi.foundation.cesium.ModelGraphicsExtension;
import agi.foundation.cesium.PathGraphics;
import agi.foundation.cesium.PathGraphicsExtension;
import agi.foundation.cesium.PolylineOutlineMaterialGraphics;
import agi.foundation.cesium.TimeIntervalCesiumProperty;
import agi.foundation.cesium.advanced.IPolylineMaterialGraphics;
import agi.foundation.coordinates.KeplerianElements;
import agi.foundation.geometry.AxesVehicleVelocityLocalHorizontal;
import agi.foundation.geometry.Point;
import agi.foundation.platforms.Platform;
import agi.foundation.propagators.TwoBodyPropagator;
import agi.foundation.time.JulianDate;
import agi.foundation.time.TimeInterval1;
import agi.foundation.time.TimeIntervalCollection1;

public class CreateSatellite{
    /**
     * Always produces a label. Uses the TwoBody propagator and WGS84.
     * @param name - the name of the satellite
     * @param modelURL - the URI/URL of the model used for the satellite. If null then no model will be used
     * @param centralBody - the central body about which the satellite orbits
     * @param epoch - the orbital epoch of the satellite
     * @param inclination - the orbital inclination
     * @param arg - the argument of perigee
     * @param eccentricity- the eccentricity
     * @param sma - the semimajor axis
     * @param pathColor - the color used to draw the path of the satellite. If null then no path graphics will be produced
     * @return A configured satellite with graphics.
     */
    public static Platform Create(String name, String modelURL, CentralBody centralBody, JulianDate epoch, double inclination, double arg, double eccentricity, double sma, Color pathColor){
        KeplerianElements orbitalElements = new KeplerianElements(sma, eccentricity, inclination, arg, 0.0, 0.0, WorldGeodeticSystem1984.GravitationalParameter);
        
        // use a simple two body propagator
        TwoBodyPropagator twoBodySatellitePropagator = new TwoBodyPropagator(epoch, centralBody.getInertialFrame(), orbitalElements);
        Point satellitePoint = (Point) twoBodySatellitePropagator.createPoint();
        
        Platform sat = new Platform();
        sat.setName(name);
        sat.setLocationPoint(satellitePoint);
        sat.setOrientationAxes(new AxesVehicleVelocityLocalHorizontal(centralBody.getFixedFrame(), satellitePoint));

        // Configure label for the sat.
        LabelGraphics issLabel = new LabelGraphics();
        issLabel.setText(new ConstantCesiumProperty<>(sat.getName()));
        TimeIntervalCollection1<Color> intervals = new TimeIntervalCollection1<>();
        intervals.add(new TimeInterval1<>(JulianDate.getMinValue(), epoch.addDays(0.5), Color.GREEN, true, false));
        intervals.add(new TimeInterval1<>(epoch.addDays(0.5), JulianDate.getMaxValue(), Color.RED, false, true));
        issLabel.setFillColor(new TimeIntervalCesiumProperty<>(intervals));
        sat.getExtensions().add(new LabelGraphicsExtension(issLabel));

        if( modelURL != null) {
            // sat Model Graphics
            ModelGraphics issModel = new ModelGraphics();
            ConstantCesiumProperty<CesiumResource> resource = new ConstantCesiumProperty<CesiumResource>();
            resource.setValue(new CesiumResource(modelURL, CesiumResourceBehavior.LINK_TO));
            issModel.setModel(resource);
            issModel.setRunAnimations(new ConstantCesiumProperty<>(false));
            sat.getExtensions().add(new ModelGraphicsExtension(issModel));
        }

        if(pathColor != null){
            // Configure graphical display of the orbital path of the sat.
            PathGraphics satPath = new PathGraphics();
            PolylineOutlineMaterialGraphics satPathMaterial = new PolylineOutlineMaterialGraphics();
            satPathMaterial.setColor(new ConstantCesiumProperty<>(pathColor));
            satPathMaterial.setOutlineWidth(new ConstantCesiumProperty<>(1.0));
            satPathMaterial.setOutlineColor(new ConstantCesiumProperty<>(Color.BLACK));
            satPath.setMaterial(new ConstantCesiumProperty<IPolylineMaterialGraphics>(satPathMaterial));
            satPath.setWidth(new ConstantCesiumProperty<>(2.0));
            satPath.setLeadTime(new ConstantCesiumProperty<>(60.0 * 44.0));// 44 minutes
            satPath.setTrailTime(new ConstantCesiumProperty<>(60.0 * 44.0));
            sat.getExtensions().add(new PathGraphicsExtension(satPath));
        }
        
        return sat;
    }
}