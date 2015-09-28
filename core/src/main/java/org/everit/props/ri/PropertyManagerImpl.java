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
package org.everit.props.ri;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import org.everit.persistence.querydsl.support.QuerydslSupport;
import org.everit.props.PropertyManager;
import org.everit.props.ri.schema.qdsl.QProperty;
import org.everit.transaction.propagator.TransactionPropagator;

import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.dml.SQLDeleteClause;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.dml.SQLUpdateClause;

/**
 * Implementation of {@link PropertyManager}.
 */
public class PropertyManagerImpl implements PropertyManager {

  private ConcurrentMap<String, String> cache;

  private QuerydslSupport querydslSupport;

  private TransactionPropagator transactionPropagator;

  /**
   * Constructor.
   */
  public PropertyManagerImpl(final ConcurrentMap<String, String> cache,
      final QuerydslSupport querydslSupport,
      final TransactionPropagator transactionPropagator) {
    this.cache = Objects.requireNonNull(cache, "chache cannot be null");
    this.querydslSupport = Objects.requireNonNull(querydslSupport, "chache cannot be null");
    this.transactionPropagator =
        Objects.requireNonNull(transactionPropagator, "chache cannot be null");
  }

  @Override
  public void addProperty(final String key, final String value) {
    Objects.requireNonNull(key, "Null key is not supported!");
    Objects.requireNonNull(value, "Null values are not supported!");
    transactionPropagator.required(() -> querydslSupport.execute((connection, configuration) -> {
      QProperty prop = QProperty.property;

      return new SQLInsertClause(connection, configuration, prop)
          .set(prop.key, key)
          .set(prop.value, value)
          .execute();
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
      QProperty prop = QProperty.property;
      return new SQLQuery(connection, configuration)
          .from(prop)
          .where(prop.key.eq(key))
          .uniqueResult(prop.value);
    });
    cache.put(key, value);
    return value;
  }

  @Override
  public String removeProperty(final String key) {
    Objects.requireNonNull(key, "Null key is not supported!");

    String previousValue = getProperty(key);

    boolean deleted = transactionPropagator.required(() -> {
      return querydslSupport.execute((connection, configuration) -> {
        QProperty prop = QProperty.property;

        long deletedRowNum = new SQLDeleteClause(connection, configuration, prop)
            .where(prop.key.eq(key))
            .execute();

        cache.remove(key);

        return deletedRowNum > 0;
      });
    });

    if (deleted) {
      return previousValue;
    } else {
      return null;
    }
  }

  @Override
  public String updateProperty(final String key, final String newValue) {
    Objects.requireNonNull(key, "Null key is not supported!");
    Objects.requireNonNull(newValue, "Null values are not supported!");
    String previousValue = getProperty(key);

    boolean updated = transactionPropagator.required(() -> {
      return querydslSupport.execute((connection, configuration) -> {
        QProperty prop = QProperty.property;

        long updatedRowNum = new SQLUpdateClause(connection, configuration, prop)
            .where(prop.key.eq(key))
            .set(prop.value, newValue)
            .execute();

        cache.replace(key, newValue);
        return updatedRowNum > 0;
      });
    });

    if (updated) {
      return previousValue;
    } else {
      return null;
    }
  }
}
