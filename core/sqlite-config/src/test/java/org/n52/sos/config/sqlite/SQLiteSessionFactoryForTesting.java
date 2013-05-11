package org.n52.sos.config.sqlite;

import java.io.File;
import java.io.IOException;

import org.n52.sos.config.sqlite.SQLiteSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When testing, make the sqlite config database a temp file and delete on cleanup.
 * 
 * @author Shane StClair
 *
 */
public class SQLiteSessionFactoryForTesting extends SQLiteSessionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SQLiteSessionFactoryForTesting.class); 
    protected static final String TESTING_DATABASE_NAME = "testing-configuration";
    protected static final String TESTING_CONNECTION_URL_TEMPLATE = "jdbc:sqlite:%s";    
    
    private File dbFile;
    
    protected String getFilename() {
    	if (dbFile == null) {
            try {
            	dbFile = File.createTempFile(TESTING_DATABASE_NAME, ".db");
            } catch (IOException ioe) {
            	LOG.warn("Couldn't create testing sqlite config database in temp directory.");
            }        
            if (dbFile == null) {
            	dbFile = new File("target/" + TESTING_DATABASE_NAME + ".db");
                LOG.warn("Creating testing sqlite config database in target directory.");
            }    		
    	}
        return String.format(TESTING_CONNECTION_URL_TEMPLATE, dbFile.getAbsolutePath());
    }
    
    @Override
    public void cleanup() {
    	super.cleanup();
    	if (dbFile != null && dbFile.exists() && dbFile.canWrite()){
    		dbFile.delete();
    	}
    }    
}
