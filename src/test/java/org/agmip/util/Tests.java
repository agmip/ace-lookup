package org.agmip.util.acepathfinder;

import java.util.LinkedHashMap;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tests {
    private static Logger LOG = LoggerFactory.getLogger("org.agmip.util.Tests");
    
    @Test
    public void testPathfinderInit() {
        AcePathfinder.INSTANCE.getPath("blank");
    }

    @Test
    public void blankPathTest() {
        String blankPath = AcePathfinder.INSTANCE.getPath("exname");
        LOG.info("exname: "+blankPath);
        if( blankPath == null ) {
            LOG.info( "exname is NULL" );
        } else if ( blankPath.equals("") ) {
            LOG.info( "exname is empty" );
        }
    }

    @Test
    public void multiPathTest() {
        String basepath = AcePathfinder.INSTANCE.getPath("wst_id");
        if( basepath.contains(",") ) {
            LOG.info("Found a multipath");
            String[] paths = basepath.split(",");
            int l = paths.length;
            for(int i=0; i < l; i++) {
                if(paths[i].equals("")) {
                    LOG.info("Found global path");
                } else {
                    LOG.info("Found path: " + paths[i]);
                }
            }
        }
    }


    @Test
    public void insertionTest() {
        LOG.debug("INSERTION TEST!!!");
        String var = "exname";
        String val = "Test Experiment";
        LinkedHashMap testMap = new LinkedHashMap();
        LinkedHashMap compareMap = new LinkedHashMap();
        compareMap.put(var, val);
        AcePathfinderUtil.insertValue(testMap, var, val);
        assertEquals("Maps match", testMap, compareMap);
    }

    @Test
    public void insertionBucketTest() {
        LOG.debug("BUCKET INSERT TEST!!!!");
        String var = "icdat";
        String val = "19232323";
        LinkedHashMap testMap = new LinkedHashMap();
        LinkedHashMap nestMap = new LinkedHashMap();
        LinkedHashMap compareMap = new LinkedHashMap();

        nestMap.put(var, val);
        compareMap.put("initial_conditions", nestMap);
        AcePathfinderUtil.insertValue(testMap, var, val);
        assertEquals("Map match", testMap, compareMap);
    } 

    @Test
    public void insertionMutliPathTest() {
        String var = "wst_id";
        String val = "123abc";
        LinkedHashMap testMap = new LinkedHashMap();
        LinkedHashMap nestMap = new LinkedHashMap();
        LinkedHashMap compareMap = new LinkedHashMap();

        nestMap.put(var, val);
        compareMap.put(var, val);
        compareMap.put("weather", nestMap);

        AcePathfinderUtil.insertValue(testMap, var, val);
        assertEquals("Multipath Test", testMap, compareMap);
        LOG.info("Value: "+testMap.toString());
    }

    @Test
    public void newRecordTest() throws Exception {
        String var = "pdate";
        String val = "12345678";

        LinkedHashMap testMap = new LinkedHashMap();

        AcePathfinderUtil.newRecord(testMap, AcePathfinder.INSTANCE.getPath(var));
        LOG.info("New record: "+testMap.toString());
    }

    @Test
    public void nestedPathTest() {
        String path = "a:b:c@d!e";

        LinkedHashMap testMap = new LinkedHashMap();
        AcePathfinderUtil.newRecord(testMap, path);
        LOG.info("nestedPath: "+testMap.toString());
    }

    @Test
    public void insertNestedTest() {
        String var = "pdate";
        String val = "12345678";

        LinkedHashMap testMap = new LinkedHashMap();

        AcePathfinderUtil.newRecord(testMap, AcePathfinder.INSTANCE.getPath(var));
        AcePathfinderUtil.insertValue(testMap, var, val);
        LOG.info("insertNested: "+testMap.toString());
    }


    @Test
    public void insertMultipleEvents() {
        String var1 = "pdate";
        String val1 = "123456789";
        String var2 = "hadat";
        String val2 = "987654321";

        LinkedHashMap testMap = new LinkedHashMap();

        AcePathfinderUtil.insertValue(testMap, var1, val1);
        AcePathfinderUtil.newRecord(testMap, AcePathfinder.INSTANCE.getPath(var1));
        AcePathfinderUtil.insertValue(testMap, var1, val2);
        AcePathfinderUtil.insertValue(testMap, var2, val2);
        LOG.info("insertMultipleEvents: "+testMap.toString());
    }

    @Test
    public void insertMultipleValues() {
        String var1 = "pdate";
        String val1 = "12345678";
        String var2 = "wst_id";
        String val2 = "abc123";
        String var3 = "plpop";
        String val3 = "42";
        String var4 = "exname";
        String val4 = "Awesomness";
        String var5 = "hadat";
        String val5 = "87654321";
        String var6 = "invalid";
        String val6 = "invalid";

        LinkedHashMap testMap = new LinkedHashMap();

        AcePathfinderUtil.insertValue(testMap, var4, val4);
        AcePathfinderUtil.insertValue(testMap, var2, val2);
        AcePathfinderUtil.newRecord(testMap, AcePathfinder.INSTANCE.getPath(var1));
        AcePathfinderUtil.insertValue(testMap, var1, val1);
        AcePathfinderUtil.insertValue(testMap, var3, val3);
        AcePathfinderUtil.insertValue(testMap, var5, val5);
        AcePathfinderUtil.insertValue(testMap, var6, val6);

        LOG.info("insertMulti: "+testMap.toString());
    }
}
