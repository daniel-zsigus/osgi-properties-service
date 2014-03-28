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
package org.everit.osgi.props.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.api.CacheFactory;
import org.everit.osgi.cache.api.CacheHolder;
import org.everit.osgi.props.PropertyService;
import org.everit.osgi.props.PropertyServiceDataSourceConnectionException;
import org.everit.osgi.props.PropertyServiceProps;
import org.everit.osgi.props.schema.qdsl.QProperties;
import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.BundleContext;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

@Component(name = PropertyServiceProps.PROPERTYSERVICE_COMPONENT_NAME, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = PropertyServiceProps.DATASOURCE_TARGET),
        @Property(name = PropertyServiceProps.TRANSACTION_HELPER_TARGET),
        @Property(name = PropertyServiceProps.SQLTEMPLATES_TARGET),
        @Property(name = PropertyServiceProps.CACHECONFIGURATION_TARGET),
        @Property(name = PropertyServiceProps.CACHEFACTORY_TARGET)
})
@Service
public class PropertyComponent implements PropertyService {

    @Reference
    private DataSource dataSource;

    @Reference
    private TransactionHelper th;

    @Reference
    private SQLTemplates sqlTemplates;

    @Reference
    private CacheConfiguration<String, String> cacheConfiguration;

    @Reference
    private CacheFactory cacheFactory;

    private CacheHolder<String, String> cacheHolder;
    private ConcurrentMap<String, String> cache;

    @Activate
    public void activate(final BundleContext bundleContext) {
        cacheHolder = cacheFactory.createCache(
                cacheConfiguration, this.getClass().getClassLoader());
        cache = cacheHolder.getCache();
    }

    @Override
    public void addProperty(final String key, final String value) {
        th.required(new Callback<Object>() {
            @Override
            public Object execute() {
                try (Connection connection = dataSource.getConnection()) {
                    QProperties prop = QProperties.propProperties;

                    cache.put(key, value);
                    new SQLInsertClause(connection, sqlTemplates, prop)
                            .set(prop.key, key)
                            .set(prop.value, value)
                            .execute();
                } catch (SQLException e) {
                    throw new PropertyServiceDataSourceConnectionException("Cannot connect to DataSource.", e);
                }
                return null;
            }
        });
    }

    @Deactivate
    public void deactivate(final BundleContext bundleContext) {
        cacheHolder.close();
    }

    @Override
    public String getProperty(final String key) {

        String cacheValue = cache.get(key);
        if (cacheValue != null) {
            return cacheValue;
        } else {
            try (Connection connection = dataSource.getConnection()) {
                SQLQuery sqlQuery = new SQLQuery(connection, sqlTemplates);
                QProperties prop = QProperties.propProperties;
                List<String> values = sqlQuery.from(prop).where(prop.key.eq(key)).list(prop.value);

                if (values.isEmpty()) {
                    return null;
                } else {
                    return values.get(0);
                }
            } catch (SQLException e) {
                throw new PropertyServiceDataSourceConnectionException("Cannot connect to DataSource.", e);
            }
        }
    }

    @Override
    public String removeProperty(final String key) {
        String previousValue = getProperty(key);

        if (previousValue == null) {
            return previousValue;
        } else {
            th.required(new Callback<Object>() {
                @Override
                public Object execute() {
                    try (Connection connection = dataSource.getConnection()) {
                        QProperties prop = QProperties.propProperties;

                        cache.remove(key);
                        new SQLDeleteClause(connection, sqlTemplates, prop)
                                .where(prop.key.eq(key))
                                .execute();
                    } catch (SQLException e) {
                        throw new PropertyServiceDataSourceConnectionException("Cannot connect to DataSource.", e);
                    }
                    return null;
                }
            });
            return previousValue;
        }
    }

    @Override
    public String setProperty(final String key, final String newValue) {
        String previousValue = getProperty(key);

        if (previousValue == null) {
            return previousValue;
        } else {
            th.required(new Callback<Object>() {
                @Override
                public Object execute() {
                    try (Connection connection = dataSource.getConnection()) {
                        QProperties prop = QProperties.propProperties;

                        cache.replace(key, newValue);
                        new SQLUpdateClause(connection, sqlTemplates, prop)
                                .where(prop.key.eq(key))
                                .set(prop.value, newValue)
                                .execute();

                    } catch (SQLException e) {
                        throw new PropertyServiceDataSourceConnectionException("Cannot connect to DataSource.", e);
                    }
                    return null;
                }
            });
            return previousValue;
        }
    }
}
