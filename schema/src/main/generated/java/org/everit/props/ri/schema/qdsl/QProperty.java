/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.props.ri.schema.qdsl;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;

import com.mysema.query.sql.ColumnMetadata;




/**
 * QProperty is a Querydsl query type for QProperty
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QProperty extends com.mysema.query.sql.RelationalPathBase<QProperty> {

    private static final long serialVersionUID = -138262541;

    public static final QProperty property = new QProperty("prop_property");

    public class PrimaryKeys {

        public final com.mysema.query.sql.PrimaryKey<QProperty> propertyPk = createPrimaryKey(key);

    }

    public final StringPath key = createString("key");

    public final StringPath value = createString("value");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QProperty(String variable) {
        super(QProperty.class, forVariable(variable), "org.everit.props.ri", "prop_property");
        addMetadata();
    }

    public QProperty(String variable, String schema, String table) {
        super(QProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProperty(Path<? extends QProperty> path) {
        super(path.getType(), path.getMetadata(), "org.everit.props.ri", "prop_property");
        addMetadata();
    }

    public QProperty(PathMetadata<?> metadata) {
        super(QProperty.class, metadata, "org.everit.props.ri", "prop_property");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(key, ColumnMetadata.named("key").ofType(12).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("value").ofType(12).withSize(2000).notNull());
    }

}

