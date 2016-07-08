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

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QProperty is a Querydsl query type for QProperty
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QProperty extends com.querydsl.sql.RelationalPathBase<QProperty> {

    private static final long serialVersionUID = -138262541;

    public static final QProperty property = new QProperty("PROP_PROPERTY");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QProperty> propertyPk = createPrimaryKey(key);

    }

    public final StringPath key = createString("key");

    public final StringPath value = createString("value");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QProperty(String variable) {
        super(QProperty.class, forVariable(variable), "org.everit.props.ri", "PROP_PROPERTY");
        addMetadata();
    }

    public QProperty(String variable, String schema, String table) {
        super(QProperty.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QProperty(Path<? extends QProperty> path) {
        super(path.getType(), path.getMetadata(), "org.everit.props.ri", "PROP_PROPERTY");
        addMetadata();
    }

    public QProperty(PathMetadata metadata) {
        super(QProperty.class, metadata, "org.everit.props.ri", "PROP_PROPERTY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(key, ColumnMetadata.named("KEY_").withIndex(1).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(value, ColumnMetadata.named("VALUE_").withIndex(2).ofType(Types.VARCHAR).withSize(2000).notNull());
    }

}

