/**
 * This file is part of Everit - Property Manager RI.
 *
 * Everit - Property Manager RI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Property Manager RI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Property Manager RI.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.props.ri.internal;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.props.PropertyManager;
import org.everit.osgi.props.ri.PropertyManagerRIConstants;
import org.everit.osgi.props.ri.schema.qdsl.QProperty;
import org.everit.osgi.querydsl.support.QuerydslSupport;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.framework.Constants;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

@Component(name = PropertyManagerRIConstants.SERVICE_FACTORY_PID_PROPERTY_MANAGER, metatype = true,
        configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false,
                value = PropertyManagerRIConstants.DEFAULT_SERVICE_DESCRIPTION),
        @Property(name = PropertyManagerRIConstants.PROP_QUERYDSL_SUPPORT_TARGET),
        @Property(name = PropertyManagerRIConstants.PROP_CACHE_TARGET, value = "(cache.driver.name=noop)"),
        @Property(name = PropertyManagerRIConstants.PROP_TRANSACTION_HELPER_TARGET)
})
@Service
public class PropertyComponent implements PropertyManager {

    @Reference(bind = "setCache")
    private ConcurrentMap<String, String> cache;

    @Reference(bind = "setQuerydslSupport")
    private QuerydslSupport querydslSupport;

    @Reference(name = "transactionHelper", bind = "setTransactionHelper")
    private TransactionHelper th;

    @Override
    public void addProperty(final String key, final String value) {
        Objects.requireNonNull(key, "Null key is not supported!");
        Objects.requireNonNull(value, "Null values are not supported!");
        th.required(() -> querydslSupport.execute((connection, configuration) -> {
            QProperty prop = new QProperty("p");

            new SQLInsertClause(connection, configuration, prop)
                    .set(prop.key, key)
                    .set(prop.value, value)
                    .execute();
            return null;
        }));

    }

    @Override
    public String getProperty(final String key) {
        Objects.requireNonNull(key, "Null key is not supported!");

        String cachedValue = cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        String value = querydslSupport.execute((connection, configuration) -> {
            SQLQuery sqlQuery = new SQLQuery(connection, configuration);
            QProperty prop = new QProperty("p");
            return sqlQuery.from(prop).where(prop.key.eq(key)).uniqueResult(prop.value);
        });
        cache.put(key, value);
        return value;
    }

    @Override
    public String removeProperty(final String key) {
        Objects.requireNonNull(key, "Null key is not supported!");

        String previousValue = getProperty(key);

        boolean deleted = th.required(() -> querydslSupport.execute((connection, configuration) -> {
            QProperty prop = new QProperty("p");

            long deletedRowNum = new SQLDeleteClause(connection, configuration, prop)
                    .where(prop.key.eq(key))
                    .execute();

            cache.remove(key);

            return deletedRowNum > 0;

        }));

        if (deleted) {
            return previousValue;
        } else {
            return null;
        }
    }

    public void setCache(final ConcurrentMap<String, String> cache) {
        this.cache = cache;
    }

    public void setQuerydslSupport(final QuerydslSupport querydslSupport) {
        this.querydslSupport = querydslSupport;
    }

    public void setTransactionHelper(final TransactionHelper th) {
        this.th = th;
    }

    @Override
    public String updateProperty(final String key, final String newValue) {
        Objects.requireNonNull(key, "Null key is not supported!");
        Objects.requireNonNull(newValue, "Null values are not supported!");
        String previousValue = getProperty(key);

        boolean updated = th.required(() -> querydslSupport.execute((connection, configuration) -> {
            QProperty prop = new QProperty("p");

            long updatedRowNum = new SQLUpdateClause(connection, configuration, prop)
                    .where(prop.key.eq(key))
                    .set(prop.value, newValue)
                    .execute();

            cache.replace(key, newValue);
            return updatedRowNum > 0;
        }));

        if (updated) {
            return previousValue;
        } else {
            return null;
        }

    }
}
