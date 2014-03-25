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
import org.everit.osgi.props.schema.QProperties;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.BundleContext;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

@Component(name = "org.everit.osgi.props.PropertyComponent", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = "dataSource.target"),
        @Property(name = "th.target"),
        @Property(name = "sqlTemplate.target"),
        @Property(name = "cacheConfiguration.target"),
        @Property(name = "cacheFactory.target")
})
@Service
public class PropertyComponent implements PropertyService {

    @Reference
    private DataSource dataSource;

    @Reference
    private TransactionHelper th;

    @Reference
    private SQLTemplates sqlTemplate;

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

    @Deactivate
    public void deactivate(final BundleContext bundleContext) {
        cacheHolder.close();
    }

    @Override
    public String getProperty(final String key) {

        // test
        String cacheValue = cache.get(key);
        if (cacheValue != null) {
            return cacheValue;
        } else {
            //
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                SQLQuery sqlQuery = new SQLQuery(connection, sqlTemplate);
                QProperties prop = QProperties.propProperties;
                List<String> values = sqlQuery.from(prop).where(prop.key.eq(key)).list(prop.value);

                if (values.isEmpty()) {
                    return null;
                } else {
                    return values.get(0);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public String setProperty(final String key, final String newValue) {
        String previousValue = getProperty(key);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            QProperties prop = QProperties.propProperties;

            if (newValue == null) {
                cache.remove(key);
                new SQLDeleteClause(connection, sqlTemplate, prop)
                        .where(prop.key.eq(key))
                        .execute();

            } else {
                if (previousValue == null) {
                    cache.put(key, newValue);
                    new SQLInsertClause(connection, sqlTemplate, prop)
                            .set(prop.key, key)
                            .set(prop.value, newValue)
                            .execute();
                } else {
                    cache.replace(key, newValue);
                    new SQLUpdateClause(connection, sqlTemplate, prop)
                            .where(prop.key.eq(key))
                            .set(prop.value, newValue)
                            .execute();
                }
            }
            return previousValue;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
