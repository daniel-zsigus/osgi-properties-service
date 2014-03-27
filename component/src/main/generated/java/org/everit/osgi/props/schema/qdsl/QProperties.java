/**
 * This file is part of Everit - Property Service Component.
 *
 * Everit - Property Service Component is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Property Service Component is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Property Service Component.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.props.schema.qdsl;

import javax.annotation.Generated;

import com.mysema.query.sql.ColumnMetadata;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.path.StringPath;

/**
 * QProperties is a Querydsl query type for QProperties
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QProperties extends com.mysema.query.sql.RelationalPathBase<QProperties> {

    private static final long serialVersionUID = -2037609374;

    public static final QProperties propProperties = new QProperties("prop_properties");

    public final StringPath key = createString("key");

    public final StringPath value = createString("value");

    public final com.mysema.query.sql.PrimaryKey<QProperties> propPropertiesPk = createPrimaryKey(key);

    public QProperties(final Path<? extends QProperties> path) {
        super(path.getType(), path.getMetadata(), null, "prop_properties");
        addMetadata();
    }

    public QProperties(final PathMetadata<?> metadata) {
        super(QProperties.class, metadata, null, "prop_properties");
        addMetadata();
    }

    public QProperties(final String variable) {
        super(QProperties.class, PathMetadataFactory.forVariable(variable), null, "prop_properties");
        addMetadata();
    }

    public QProperties(final String variable, final String schema, final String table) {
        super(QProperties.class, PathMetadataFactory.forVariable(variable), schema, table);
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(key, ColumnMetadata.named("key").ofType(12).withSize(2147483647).notNull());
        addMetadata(value, ColumnMetadata.named("value").ofType(2005).withSize(2147483647).notNull());
    }

}
