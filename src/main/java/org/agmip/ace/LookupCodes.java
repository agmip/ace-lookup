package org.agmip.ace;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.agmip.core.ModelEnum;

public enum LookupCodes {
    INSTANCE;

    private HashMap<String,String> modelLookup;
    private HashMap<String, HashMap<String,String>> aceLookup;
    private static final Logger LOG = LoggerFactory.getLogger("org.agmip.ace.LookupCodes");

    LookupCodes() {
        modelLookup = new HashMap<String,String>();
        aceLookup = new HashMap<String, HashMap<String, String>>();
    }
}
