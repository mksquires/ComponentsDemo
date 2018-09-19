package agi.examples.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import agi.examples.scenarios.AircraftNearShastaScenario;
import agi.examples.scenarios.ISSToGridNearAGIScenario;
import agi.foundation.UnsupportedCaseException;
import agi.foundation.celestial.CentralBodiesFacet;
import agi.foundation.celestial.EarthCentralBody;
import agi.foundation.cesium.Clock;
import agi.foundation.cesium.CzmlDocument;
import agi.foundation.time.Duration;
import agi.foundation.time.GregorianDate;
import agi.foundation.time.JulianDate;
import agi.foundation.time.TimeInterval;

public class Main {
    public static void main(final String[] args) throws IOException {
        
        // some rudimentary command line parsing to select the scenario
        // 1 - aircraft near shasta
        // 2 - cuboid grid near AGI HQ

        int scenarioInt = 1;
        if(args.length > 0 && args.length < 2){
            try {
                scenarioInt = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer specifying the scenario.");
                System.exit(1);
            }
        }

        EarthCentralBody earth = CentralBodiesFacet.getFromContext().getEarth();

        // analysis time interval
        JulianDate epoch = new JulianDate(new GregorianDate(2018, 8, 20));
        TimeInterval analysisInterval = new TimeInterval(epoch.addMinutes(1.0), epoch.addDays(1.0));

        // Configure the interval over which to generate data in CZML
        TimeInterval dataInterval = new TimeInterval(epoch.add(Duration.fromSeconds(60.0)), epoch.addDays(1));

        // Create and configure the CZML document.
        CzmlDocument czmlDocument = new CzmlDocument();
        czmlDocument.setName("SimpleExample");
        czmlDocument.setDescription("A simple example");
        czmlDocument.setPrettyFormatting(true);
        czmlDocument.setRequestedInterval(dataInterval);
        Clock clock = new Clock();
        clock.setInterval(dataInterval);
        clock.setCurrentTime(dataInterval.getStart());
        czmlDocument.setClock(clock);

        // Create our  scenario
        String scenarioName;
        switch(scenarioInt){
            case 1:
                scenarioName = AircraftNearShastaScenario.CreateScenario(earth, analysisInterval, czmlDocument);
                break;
            case 2:
                scenarioName = ISSToGridNearAGIScenario.CreateScenario(earth, analysisInterval, czmlDocument);
                break;
            default:
                throw new UnsupportedCaseException("Invalid scenario selected " + scenarioInt);
        }       

        // Write the CZML document to a file.
        System.out.println(scenarioName + " scenario selected.");
        File output = new File( scenarioName + ".czml");
        System.out.println("Writing CZML to output file " + output.getCanonicalPath());
        FileWriter streamWriter = new FileWriter(output);
        czmlDocument.writeDocument(streamWriter);
        streamWriter.close();
    }
}