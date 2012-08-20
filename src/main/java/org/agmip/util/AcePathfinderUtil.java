package org.agmip.util.acepathfinder;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A collection of utilities to interact with the AcePathfinder.
 *
 * <pre>AcePathfinderUtil</pre> is a collection of static methods used to interact with
 * the {@link AcePathfinder} singleton.
 *
 * The <i>path</i>, for the purposes of this class, is formated as:
 * <pre>a@b!c,a@b!c,...</pre>
 * <ul>
 * <li>a - nested bucket(s) seperated by <pre>:</pre> [<pre>LinkedHashMap</pre>]</li>
 * <li>b - series data container [<pre>ArrayList</pre>]</li>
 * <li>c - event-based series data record [<pre>&lt;String,String&gt; = &lt;"event",c&gt;</pre></li>
 * </ul>
 */
public class AcePathfinderUtil {
    private static final Logger LOG = LoggerFactory.getLogger("org.agmip.util.AcePathfinderUtil");

    public enum PathType {
        EXPERIMENT,
        WEATHER,
        SOIL
    }

    /**
     * Returns the general section of this variable (weather, soil, experiment).
     * 
     * @param var Variable to lookup
     */
    public static PathType getVariableType(String var) {
        String path = AcePathfinder.INSTANCE.getPath(var);
        LOG.debug("Current var: "+var+", Current Path: "+path);
        if (path.contains("weather")) {
            return PathType.WEATHER;
        } else if (path.contains("soil")) {
            return PathType.SOIL;
        } else {
            return PathType.EXPERIMENT;
        }
    }

    /**
     * Inserts the variable in the appropriate place in a {@link LinkedHashMap},
     * according to the AcePathfinder.
     *
     * @param m the LinkedHashMap to add the variable to.
     * @param var the variable to lookup in the AcePathfinder
     * @param val the value to insert into the LinkedHashMap
     */
    public static void insertValue(LinkedHashMap m, String var, String val) {
        String path = AcePathfinder.INSTANCE.getPath(var);
        if (path == null) return;
        String[] paths = path.split(",");
        int l = paths.length;
        int index;
        for(int i=0; i < l; i++) {
            if( paths[i] != null ) {
                if( paths[i].equals("") ) {
                    m.put(var, val);
                } else {
                    if( paths[i].contains("@") ) {
                        buildPath(m, paths[i]);
                        boolean isEvent = false;
                        // This is a nested value
                        String[] temp = paths[i].split("[!@]"); 
                        if( paths[i].contains("!") ) isEvent = true;
                        LinkedHashMap pointer = traverseToPoint(m, temp[0]);
                        ArrayList a = (ArrayList) pointer.get(temp[1]);
                        if( a.isEmpty() )
                            newRecord(m, paths[i]);
                        LinkedHashMap d = (LinkedHashMap) a.get(a.size()-1);
                        if( isEvent ) {
                            if( d.containsKey("event") ) {
                                if ( ! ((String) d.get("event")).equals(temp[2])) {
                                    // Uh oh, we have a new event without newRecord being called
                                    newRecord(m,  paths[i]);
                                    d = (LinkedHashMap) a.get(a.size()-1);
                                    d.put("event", temp[2]);
                                }
                            } else {
                                // New event
                                d.put("event", temp[2]);
                            }
                        }
                        if( d.containsKey(var) ) {
                            newRecord(m, paths[i]);
                            d = (LinkedHashMap) a.get(a.size()-1);
                            if (isEvent) d.put("event", temp[2]);
                        }
                        d.put(var, val);
                    } else {
                        // This is a bucket-level, non-series value.
                        buildNestedBuckets(m, paths[i]);
                        LinkedHashMap pointer = traverseToPoint(m, paths[i]);
                        pointer.put(var, val);
                    }
                }
            }
        }
    }

    /**
     * Creates a new record in a series, such as daily 
     * weather records, soil layers, etc. If the multi-line dataset space
     * is not already in the <pre>m</pre> parameter, it will be created.
     *
     * @param m The {@link LinkedHashMap} to be modified.
     * @param path The path to lookup and see if a multi-line record is
     *             supported for this field.
     */
    public static void newRecord(LinkedHashMap m, String path) {
        if( path != null ) {
            String[] paths = path.split(",");
            int l = paths.length;
            for(int i=0; i < l; i++) {
                if( paths[i].contains("@") ) {
                    String temp[] = paths[i].split("[@!]");
                    buildPath(m, paths[i]);
                    LinkedHashMap pointer = traverseToPoint(m, temp[0]);
                    ArrayList a = (ArrayList) pointer.get(temp[1]);
                    a.add(new LinkedHashMap());
                } 
            }
        } 
    }

    /**
     * Creates a nested path, if not already present in the map. This does not
     * support multipath definitions. Please split prior to sending the path to
     * this function.
     *
     * @param m The map to create the path inside of.
     * @param p The full path to create.
     */
    private static void buildPath(LinkedHashMap m, String p) {
        boolean isEvent = false;
        if(p.contains("@")) {
            String[] components = p.split("@");
            int cl = components.length;
            buildNestedBuckets(m, components[0]);
            if(cl == 2) {
                if(p.contains("!")) isEvent = true;
                LinkedHashMap pointer = traverseToPoint(m, components[0]);
                String d;
                if(isEvent) {
                    String[] temp = components[1].split("!");
                    d = temp[0];
                } else {
                    d = components[1];
                }
                if( ! pointer.containsKey(d) ) 
                    pointer.put(d, new ArrayList());
            }
        } 
    }

    /**
     * A helper method to created the nested bucket structure.
     * @param m The map in which to build the nested structure.
     * @param p The nested bucket structure to create
     */
    private static void buildNestedBuckets(LinkedHashMap m, String p) {
        String[] components = p.split(":");
        int cl = components.length;
        if(cl == 1) {
            if( ! m.containsKey(components[0]) )
                m.put(components[0], new LinkedHashMap());
        } else {
            LinkedHashMap temp = m;
            for(int i=0; i < cl; i++) {
                if( ! temp.containsKey(components[i]) )
                    temp.put(components[i], new LinkedHashMap());
                temp = (LinkedHashMap) temp.get(components[i]);
            }
        }
    }

    /**
     * A helper method to help traverse to a certain point in the map.
     * @param m Map to traverse
     * @param p Path to traverse to
     * @return A reference to the point in the map.
     */
    private static LinkedHashMap traverseToPoint(LinkedHashMap m, String p) {
        LinkedHashMap pointer = m;
        String[] b = p.split(":");
        int bl = b.length;
        for(int i=0; i < bl; i++)
            pointer = (LinkedHashMap) pointer.get(b[i]);
        return pointer;
    }
}
