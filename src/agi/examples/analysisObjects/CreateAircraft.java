package agi.examples.analysisObjects;

import java.awt.Color;
import java.util.ArrayList;

import agi.foundation.Trig;
import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CesiumLabelStyle;
import agi.foundation.cesium.CesiumResource;
import agi.foundation.cesium.CesiumResourceBehavior;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.FieldOfViewGraphicsExtension;
import agi.foundation.cesium.GridMaterialGraphics;
import agi.foundation.cesium.LabelGraphics;
import agi.foundation.cesium.LabelGraphicsExtension;
import agi.foundation.cesium.ModelGraphics;
import agi.foundation.cesium.ModelGraphicsExtension;
import agi.foundation.cesium.PathGraphics;
import agi.foundation.cesium.PathGraphicsExtension;
import agi.foundation.cesium.PolylineOutlineMaterialGraphics;
import agi.foundation.cesium.SensorFieldOfViewGraphics;
import agi.foundation.cesium.SolidColorMaterialGraphics;
import agi.foundation.cesium.advanced.IPolylineMaterialGraphics;
import agi.foundation.coordinates.Cartesian;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.geometry.AxesNorthEastDown;
import agi.foundation.geometry.AxesVehicleVelocityLocalHorizontal;
import agi.foundation.geometry.Point;
import agi.foundation.geometry.shapes.EllipsoidGeodesic;
import agi.foundation.geometry.shapes.RectangularPyramid;
import agi.foundation.platforms.FieldOfViewExtension;
import agi.foundation.platforms.Platform;
import agi.foundation.propagators.Waypoint;
import agi.foundation.propagators.WaypointPropagator;
import agi.foundation.time.JulianDate;

public class CreateAircraft{

    /**
     * Creates an aircraft near a specified point at 8km alt.
     * @param date the date at which the aircraft begins flying
     * @param point the point about which aircraft flight occurs
     * @param centralBody the central body upn which the aircraft is defined
     */
    public static Platform CreateAircraftAroundPoint(JulianDate date, Cartographic point, CentralBody centralBody){
        EllipsoidGeodesic geodesicFromPoint = new EllipsoidGeodesic(centralBody.getShape(), point, Math.PI, 8000.0);
        Cartographic finalPoint = geodesicFromPoint.getFinalPoint();
        double altitude = 4500.0;
        finalPoint = new Cartographic(finalPoint.getLongitude(), finalPoint.getLatitude(), altitude);
        double constantVelocity = 300.0; // m/s
        Waypoint waypoint1 = new Waypoint(date, finalPoint, constantVelocity, 0.0  );

        double oneDegreeInRadians = Trig.degreesToRadians(1.0);

        // Create the next two waypoints from a location, the same velocity, and the previous waypoint.
        Cartographic point2 = new Cartographic(finalPoint.getLongitude()+oneDegreeInRadians, finalPoint.getLatitude()+oneDegreeInRadians, altitude);
        Waypoint waypoint2 = new Waypoint(waypoint1, centralBody.getShape(), point2, constantVelocity);

        Cartographic point3 = new Cartographic(finalPoint.getLongitude()+2.0*oneDegreeInRadians, finalPoint.getLatitude()+2.0*oneDegreeInRadians, altitude);
        Waypoint waypoint3 = new Waypoint(waypoint2, centralBody.getShape(), point3, constantVelocity);

        // Construct the waypoint propagator with all of the waypoints
        ArrayList<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(waypoint1);
        waypoints.add(waypoint2);
        waypoints.add(waypoint3);
        WaypointPropagator propagator = new WaypointPropagator(centralBody, waypoints);

        Point propagatedPoint = propagator.createPoint();

        Platform aircraft = new Platform("F-22", propagatedPoint, new AxesVehicleVelocityLocalHorizontal(centralBody.getFixedFrame(), propagatedPoint));
        
        return aircraft;
    }

    /**
     * Adds graphics to the aircraft platform. 
     * @param aircraft the aircraft to which the graphics are added
     * @param gltfString the URL/URI of the gltf representing the aircraft. If null then no model graphics are added
     * @param labelString the label of the aircraft (white with a black background text). If null then no label is added
     * @param pathColor the color of the path of the aircraft. If null then no path is added
     */
    public static void AddAircraftGraphics(Platform aircraft, String gltfString, String labelString, Color pathColor ){

                // Configure label
                if(labelString != null){
                    LabelGraphics label = new LabelGraphics();
                    label.setText(new ConstantCesiumProperty<>(labelString));               
                    label.setFillColor(new ConstantCesiumProperty<Color>(Color.WHITE));
                    label.setOutlineColor(new ConstantCesiumProperty<Color>(Color.BLACK));
                    label.setOutlineWidth(new ConstantCesiumProperty<Double>(4.0));
                    label.setEyeOffset(new ConstantCesiumProperty<Cartesian>(new Cartesian(0,0,-100.0)));
                    label.setStyle(new ConstantCesiumProperty<CesiumLabelStyle>(CesiumLabelStyle.FILL_AND_OUTLINE));
                    aircraft.getExtensions().add(new LabelGraphicsExtension(label));
                }
        
                // Configure path
                if(pathColor != null){
                    PathGraphics path = new PathGraphics();
                    PolylineOutlineMaterialGraphics issPathMaterial = new PolylineOutlineMaterialGraphics();
                    issPathMaterial.setColor(new ConstantCesiumProperty<>(Color.YELLOW));
                    issPathMaterial.setOutlineWidth(new ConstantCesiumProperty<>(1.0));
                    issPathMaterial.setOutlineColor(new ConstantCesiumProperty<>(Color.BLACK));
                    path.setMaterial(new ConstantCesiumProperty<IPolylineMaterialGraphics>());
                    path.setWidth(new ConstantCesiumProperty<>(2.0));
                    path.setLeadTime(new ConstantCesiumProperty<>(60.0 * 44.0));// 44 minutes
                    path.setTrailTime(new ConstantCesiumProperty<>(60.0 * 44.0));
                    aircraft.getExtensions().add(new PathGraphicsExtension(path));
                }
                       
                // Configure model
                if(gltfString != null){
                    ModelGraphics model = new ModelGraphics();
                    ConstantCesiumProperty<CesiumResource> resource = new ConstantCesiumProperty<CesiumResource>();
                    resource.setValue(new CesiumResource(gltfString, CesiumResourceBehavior.LINK_TO));
                    model.setModel(resource);
                    model.setScale(new ConstantCesiumProperty<Double>(16.0)); // a little bigger for visibility
                    model.setRunAnimations(new ConstantCesiumProperty<Boolean>(false)); // very important for converted gltf models
                    
                    // Add graphical extensions
                    aircraft.getExtensions().add(new ModelGraphicsExtension(model));
                }              
    }

    /**
     * Returns a sensor platform located at the aircraft's point. The sensor is a rectangular pyramid.
     * @param aircraft the aircraft platform to which to attach the sensor
     * @param centralBody the central body relative to which the aircraft is defined
     * @return the sensor platform
     */
    public static Platform AddSensorToAircraft(Platform aircraft, CentralBody centralBody){
        
        // create sensor platform
        Platform sensorPlatform = new Platform("sensor");
        sensorPlatform.setLocationPoint(aircraft.getLocationPoint());
        sensorPlatform.setOrientationAxes(new AxesNorthEastDown(centralBody, aircraft.getLocationPoint()));

        // create sensor
        RectangularPyramid rectangularPyramid = new RectangularPyramid();
        rectangularPyramid.setXHalfAngle(Trig.degreesToRadians(16.0));
        rectangularPyramid.setYHalfAngle(Trig.degreesToRadians(9.0));
        rectangularPyramid.setRadius(500000.0);

        FieldOfViewExtension fovExtension = new FieldOfViewExtension(rectangularPyramid);

        sensorPlatform.getExtensions().add(fovExtension);
        
        return sensorPlatform;
    }

    /**
     * Adds graphics to a sensor platform. NOTE- requires SWVL/Cesium ion.
     * The sensor will be transparent blue with a white border.
     * @param sensorPlatform the sensor platform which will be decorated with display graphics
     */
    public static void AddSensorGraphics(Platform sensorPlatform){
        // NOTE- label items commented out
        //LabelGraphics label = new LabelGraphics();
        //label.setText(new ConstantCesiumProperty<>(sensorPlatform.getName()));

        SensorFieldOfViewGraphics sensorGraphics = new SensorFieldOfViewGraphics();
        GridMaterialGraphics gridMaterial = new GridMaterialGraphics();
        gridMaterial.setColor(new ConstantCesiumProperty<>(Color.WHITE));
        gridMaterial.setCellAlpha(new ConstantCesiumProperty<>(0.1));
        sensorGraphics.setDomeSurfaceMaterial(new ConstantCesiumProperty<>(gridMaterial));

        //Color transparentGreen = new Color(0x80008000, true);
        Color transparentGreen = new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 128);
        SolidColorMaterialGraphics lateralSurfaceMaterial = new SolidColorMaterialGraphics(transparentGreen);
        sensorGraphics.setLateralSurfaceMaterial(new ConstantCesiumProperty<>(lateralSurfaceMaterial));

        sensorPlatform.getExtensions().add(new FieldOfViewGraphicsExtension(sensorGraphics));
        //sensorPlatform.getExtensions().add(new LabelGraphicsExtension(label));
    }
}