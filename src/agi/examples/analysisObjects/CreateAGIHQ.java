package agi.examples.analysisObjects;

import java.awt.Color;

import agi.foundation.Trig;
import agi.foundation.celestial.CentralBody;
import agi.foundation.cesium.CesiumResource;
import agi.foundation.cesium.CesiumResourceBehavior;
import agi.foundation.cesium.ConstantCesiumProperty;
import agi.foundation.cesium.LabelGraphics;
import agi.foundation.cesium.LabelGraphicsExtension;
import agi.foundation.cesium.ModelGraphics;
import agi.foundation.cesium.ModelGraphicsExtension;
import agi.foundation.coordinates.Cartographic;
import agi.foundation.geometry.AxesEastNorthUp;
import agi.foundation.geometry.PointCartographic;
import agi.foundation.platforms.Platform;

public class CreateAGIHQ
{
    /**
     * Creates the AGI HQ as a platform.
     * @param earth the central body on which the AGI HQ is located
     * @return the platform representing AGI HQ, decorated with CZML visualization properties
     */
    public static Platform CreateHQ(CentralBody earth)
    {
                // Create Platform at the location of AGI's headquarters.
                Cartographic facilityCartographic = new Cartographic(Trig.degreesToRadians(-75.59567709999999), Trig.degreesToRadians(40.0392551), 150.0);
                PointCartographic facilityLocationPoint = new PointCartographic(earth, facilityCartographic);
                Platform facility = new Platform("AGI HQ");
                facility.setLocationPoint(facilityLocationPoint);
                facility.setOrientationAxes(new AxesEastNorthUp(earth, facilityLocationPoint));

                // Configure model graphics
                ModelGraphics facilityModel = new ModelGraphics();
                ConstantCesiumProperty<CesiumResource> resourceFacility = new ConstantCesiumProperty<CesiumResource>();
                resourceFacility.setValue(new CesiumResource("http://assets02.agi.com/models/facility.gltf", CesiumResourceBehavior.EMBED));
                facilityModel.setModel(resourceFacility);

                // Configure label for AGI HQ.
                LabelGraphics facilityLabel = new LabelGraphics();
                facilityLabel.setText(new ConstantCesiumProperty<>(facility.getName()));
                facilityLabel.setFillColor(new ConstantCesiumProperty<>(Color.WHITE));
                
                // Add the graphical extensions
                facility.getExtensions().add(new LabelGraphicsExtension(facilityLabel));
                facility.getExtensions().add(new ModelGraphicsExtension(facilityModel));

                return facility;
    }
}