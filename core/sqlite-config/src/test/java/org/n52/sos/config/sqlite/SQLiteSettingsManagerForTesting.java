package org.n52.sos.config.sqlite;

import org.n52.sos.config.sqlite.SQLiteSettingsManager;

public class SQLiteSettingsManagerForTesting extends SQLiteSettingsManager {
	@Override
    protected void createDefaultConnectionProvider() {
        lock.lock();
        try {
            if (this.connectionProvider == null) {
                this.connectionProvider = new SQLiteSessionFactoryForTesting();
            }
        } finally {
            lock.unlock();
        }
    }
}
