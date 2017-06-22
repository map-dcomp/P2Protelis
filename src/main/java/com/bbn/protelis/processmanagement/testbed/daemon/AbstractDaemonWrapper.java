package com.bbn.protelis.processmanagement.testbed.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.protelis.vm.ProtelisProgram;
import org.slf4j.Logger;

import com.bbn.protelis.processmanagement.daemon.Daemon;
import com.bbn.protelis.processmanagement.daemon.ProcessStatus;
import com.cedarsoftware.util.io.JsonReader;

//TODO: This file needs checkstyle cleanup
//CHECKSTYLE:OFF

public abstract class AbstractDaemonWrapper implements DaemonWrapper, Daemon.Listener {
    ProcessStatus status = ProcessStatus.init;
    
    public boolean template = false;
    public long uid;        // Must be unique
    public String alias;    // Display name (may not be unique)
    // TODO: determine if "location" is needed
    public int[] location;  // Display location
    public ProtelisProgram program; // Protelis program run by daemon
    public Object[][] environment = new Object[][] {}; // [[String,String/Integer],...]

    public ProcessStatus getDaemonStatus() {
        return status;
    }
    
    public long getUID() {
        return uid;
    }


    public static DaemonWrapper[] configurationFromResource(final Logger logger, final String name) throws IOException {
        try {
        InputStream scenario = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (scenario == null) { // try as absolute instead
            scenario = new FileInputStream(new File(name));
        }
        
        return readConfigurations(logger, scenario);
        } catch (FileNotFoundException e) {
            logger.error("Could not locate configuration resource: " + name);
            throw e;
        }
    }
    
    /**
     * Read configurations from a JSON file.
     * If a node is marked as "template", then it 
     * @param in
     * @return
     * @throws IOException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    private static DaemonWrapper[] readConfigurations(final Logger logger, final InputStream in) throws IOException {
        // Read in the JSON array of configurations
        JsonReader jr = new JsonReader(in);
        try {
            Object[] objects = (Object[])jr.readObject();
            jr.close();
            
            // Copy array and check for presence of template (only first is used)
            AbstractDaemonWrapper template = null;
            AbstractDaemonWrapper[] configs = new AbstractDaemonWrapper[objects.length];
            for (int i = 0; i < objects.length; i++) { 
                configs[i] = (AbstractDaemonWrapper)objects[i];
                if (template == null && configs[i].template) { 
                    template = configs[i]; 
                }
            }
            
            // Shift to final array and fill in from template
            for (AbstractDaemonWrapper d : configs) {
                // Replace any fields that are null with fields from the template
                if (template != null) {
                    Field[] fields = d.getClass().getFields();
                    for (Field f : fields) {
                        if (f.get(d) == null) {
                            f.set(d, f.get(template));
                        }
                    }
                }
            }
            return configs;
        } catch (IllegalAccessException e) {
            logger.error("Configuration specified inaccessible field");
            throw new IOException();
        }
        
    }

    @Override
    public void daemonUpdated(final Daemon d) {
        notifyListeners();
    }
    public interface Listener {
        void daemonUpdated(AbstractDaemonWrapper d);
    }
    ArrayList<Listener> listeners = new ArrayList<>();
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(final Listener listener) {
        listeners.remove(listener);
    }
    private void notifyListeners() {
        for (Listener l : listeners) { 
            l.daemonUpdated(this); 
        }
    }
}
