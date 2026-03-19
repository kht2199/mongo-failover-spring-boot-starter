package io.github.kht2199.mongo.failover.core;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;

public class MongoRouterMongoDatabaseFactory implements MongoDatabaseFactory {

    private final MongoRouter router;

    public MongoRouterMongoDatabaseFactory(MongoRouter router) {
        this.router = router;
    }

    @Override
    public MongoDatabase getMongoDatabase() throws DataAccessException {
        return router.getDatabase();
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
        return router.getActiveClient().getDatabase(dbName);
    }

    @Override
    public ClientSession getSession(ClientSessionOptions options) {
        return router.getActiveClient().startSession(options);
    }

    @Override
    public PersistenceExceptionTranslator getExceptionTranslator() {
        return new MongoExceptionTranslator();
    }

    @Override
    public MongoDatabaseFactory withSession(ClientSession session) {
        throw new UnsupportedOperationException(
            "MongoRouterMongoDatabaseFactory does not support client sessions. " +
            "Transactions and causal consistency are not available with failover routing.");
    }

    @Override
    public boolean isTransactionActive() {
        return false;
    }
}
