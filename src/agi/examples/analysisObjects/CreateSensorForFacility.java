package agi.examples.analysisObjects;


import java.awt.Color;
import agi.foundation.IServiceProvider;
import agi.foundation.Trig;
import agi.foundation.access.LinkRole;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.FieldOfViewGraphicsExtension;
import agi.foundation.cesium.GridMaterialGraphics;
import agi.foundation.cesium.SensorFieldOfViewGraphics;
import agi.foundation.cesium.SolidColorMaterialGraphics;
import agi.foundation.cesium.advanced.IMaterialGraphics;
import agi.foundation.coordinates.Cartesian;
import agi.foundation.geometry.AxesTargetingLink;
import agi.foundation.geometry.VectorFixed;
import agi.foundation.geometry.shapes.RectangularPyramid;
import agi.foundation.platforms.FieldOfViewExtension;
import agi.foundation.platforms.Platform;

public class CreateSensorForFacility{

    /**
     * Creates a sensor and attaches it to a platform.
     * @param facility the facility end of the link. the sensor platform will be attached to this platform
     * @param vehicle the vehicle end of the link.
     * @param link the link used for the sensor axes
     * @return the sensor platform
     */
    public static Platform CreateSensor(Platform facility, Platform vehicle, IServiceProvider link)
    {
        // Create a sensor, attached to the facility, and oriented to target the ISS.
        Platform sensor = new Platform();
        sensor.setLocationPoint(facility.getLocationPoint());

        VectorFixed referenceVector = new VectorFixed(vehicle.getOrientationAxes(), new Cartesian(0, 0, 1));
        sensor.setOrientationAxes(new AxesTargetingLink(link, LinkRole.TRANSMITTER, referenceVector));

        // Define the sensor geometry.
        RectangularPyramid rectangularPyramid = new RectangularPyramid();
        rectangularPyramid.setXHalfAngle(Trig.degreesToRadians(8.0));
        rectangularPyramid.setYHalfAngle(Trig.degreesToRadians(4.5));
        rectangularPyramid.setRadius(500000.0);
        sensor.getExtensions().add(new FieldOfViewExtension(rectangularPyramid));

        // Define the sensor graphically.
        SensorFieldOfViewGraphics fieldOfViewGraphics = new SensorFieldOfViewGraphics();
        GridMaterialGraphics gridMaterial = new GridMaterialGraphics();
        gridMaterial.setColor(new ConstantCesiumProperty<>(Color.WHITE));
        gridMaterial.setCellAlpha(new ConstantCesiumProperty<>(0.0));
        fieldOfViewGraphics.setDomeSurfaceMaterial(new ConstantCesiumProperty<IMaterialGraphics>(gridMaterial));

        Color transparentGreen = new Color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), 128);
        SolidColorMaterialGraphics lateralSurfaceMaterial = new SolidColorMaterialGraphics(transparentGreen);
        fieldOfViewGraphics.setLateralSurfaceMaterial(new ConstantCesiumProperty<IMaterialGraphics>(lateralSurfaceMaterial));

        sensor.getExtensions().add(new FieldOfViewGraphicsExtension(fieldOfViewGraphics));
        return sensor;        
    }
}