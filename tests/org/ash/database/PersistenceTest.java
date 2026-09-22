package org.ash.database;

import com.sleepycat.je.*;
import com.sleepycat.persist.*;
import org.ash.datamodel.AshIdTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PersistenceTest {
    @TempDir Path directory;
    @Test void reopensHistoryAndPreservesPrimaryAndSecondaryIndexes() throws Exception {
        EnvironmentConfig config = new EnvironmentConfig();
        config.setAllowCreate(true);
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        for (int pass = 0; pass < 2; pass++) {
            Environment environment = new Environment(directory.toFile(), config);
            EntityStore store = new EntityStore(environment, "ash.db", storeConfig);
            try {
                PrimaryIndex<Long, AshIdTime> samples = store.getPrimaryIndex(Long.class, AshIdTime.class);
                if (pass == 0) samples.put(new AshIdTime(42, 123456789));
                assertEquals(123456789, samples.get(42L).getsampleTime());
                assertEquals(42, store.getSecondaryIndex(samples, Double.class, "sampleTime").get(123456789.0).getsampleId());
            } finally {
                store.close();
                environment.close();
            }
        }
    }
}
