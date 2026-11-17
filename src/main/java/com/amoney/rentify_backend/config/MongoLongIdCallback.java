package com.amoney.rentify_backend.config;

import com.amoney.rentify_backend.model.MongoIdentifiable;
import org.bson.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoLongIdCallback implements BeforeConvertCallback<MongoIdentifiable> {

    private static final String SEQUENCE_COLLECTION = "database_sequences";

    private final ObjectProvider<MongoTemplate> mongoTemplateProvider;

    public MongoLongIdCallback(ObjectProvider<MongoTemplate> mongoTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
    }

    @Override
    public MongoIdentifiable onBeforeConvert(MongoIdentifiable entity, String collection) {
        if (entity.getId() == null) {
            Document sequence = mongoTemplateProvider.getObject().findAndModify(Query.query(Criteria.where("_id").is(collection)),
                    new Update().inc("value", 1),
                    FindAndModifyOptions.options().upsert(true).returnNew(true),
                    Document.class,
                    SEQUENCE_COLLECTION);

            if (sequence == null) {
                throw new IllegalStateException("Could not allocate an ID for MongoDB collection: " + collection);
            }

            entity.setId(((Number) sequence.get("value")).longValue());
        }
        return entity;
    }
}
