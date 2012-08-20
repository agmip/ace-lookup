package org.agmip.util.acepathfinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public enum AcePathfinder {
    INSTANCE;

    private final LinkedHashMap<String, String> pathfinder = new LinkedHashMap();
    private final Logger LOG = LoggerFactory.getLogger("org.agmip.util.AcePathfinder");

    AcePathfinder() {
        InputStream master = getClass().getClassLoader().getResourceAsStream("pathfinder.csv");
        InputStream observed = getClass().getClassLoader().getResourceAsStream("obs_pathfinder.csv");
        loadFromEmbeddedCSV(master);
        loadFromEmbeddedCSV(observed);
    }

    public String getPath(String lookup) {
        return pathfinder.get(lookup.toLowerCase());
    }

    public void setPath(String lookup, String path) {
        pathfinder.put(lookup.toLowerCase(), path);
    }

    private void loadFromEmbeddedCSV(InputStream res) {
        try {
            if( res != null ) {
                CSVReader reader = new CSVReader(new InputStreamReader(res));
                String[] line;
                reader.readNext(); // Skip the first line
                while(( line = reader.readNext()) != null) {
                    if(! line[23].equals("-2") ) {
                        String path = setGroupMatch(line[15]);
                        if(line[2].toLowerCase().equals("wst_id")) {
                            if( path != null ) path = ",weather";
                        } else if (line[2].toLowerCase().equals("soil_id")) {
                            if( path != null ) path = ",soil";
                        }
                        // if( pathfinder.containsKey(line[4].toLowerCase()) ) LOG.error("Conflict with variable: "+line[0]+" Original Value: "+getPath(line[0])+" New Value: "+path);
                        if( path != null ) {
                            setPath(line[2], path);
                        } 
                    }
                }
                reader.close();
            } else {
                LOG.error("Missing embedded CSV file for configuration. AcePathfinder will be blank");
            }
        } catch(IOException ex) {
            LOG.debug(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private String setGroupMatch(String groupOrder) {
        try {
            int id = new BigInteger(groupOrder).intValue();
            if( ( id >= 1011 && id <= 1081 ) || id == 2011 || id == 2031 || id == 2121 || id == 2071 || id == 2081 ) {
                // Global bucket
                return "";
            } else if ( ( id >= 5011 && id <= 5013 ) || id == 5041 ) {
                // Weather Global bucket
                return "weather";
            } else if ( id == 5052 ) {
                // Weather Daily data
                return "weather@dailyWeather";
            } else if ( ( id >= 4001 && id <= 4031 ) || ( id >= 4041 && id <= 4042 ) || id == 4051 ) {
                // Soil Global
                return "soil";
            } else if ( id == 4052 ) {
                // Soil Layer data
                return "soil@soilLayer";
            } else if ( id == 2051 ) {
                // Initial Conditions
                return "initial_conditions";
            } else if ( id == 2052 ) {
                // Initial Conditions soil layer data
                return "initial_conditions@soilLayer";
            } else if ( id == 2021 || id == 2061 ) {
                // Events - planting
                return "management@events!planting";
            } else if ( id == 2072 ) {
                // Events - irrigation
                return "management@events!irrigation";
            } else if ( id == 2073 ) {
                // Events - auto-irrigation
                return "management@events!auto-irrig";
            } else if ( id == 2082 ) {
                // Events - fertilizer
                return "management@events!fertilizer";
            } else if ( id == 2122 ) {
                // Events - tillage
                return "management@events!tillage";
            } else if ( id == 2141 || id == 2142 ) {
                // Events - harvest
                return "management@events!harvest";
            } else if ( id >= 2502 && id <= 2510 ) {
                // Observed summary data
                return "observed";
            } else if ( id >= 2511 && id <= 2599 ) {
                // Observed time series data
                return "observed@time_series";
            } else {
                // Ignored!
            }
        } catch (NumberFormatException ex) {
            LOG.debug(ex.toString());
            throw new RuntimeException(ex);
        }
        return null;
    }

    LinkedHashMap peekAtPathfinder() {
        return pathfinder;
    }
}
