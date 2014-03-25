/**
 * This file is part of Everit - Property Service Component Tests.
 *
 * Everit - Property Service Component Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Property Service Component Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Property Service Component Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.props.tests;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.everit.osgi.cache.infinispan.config.CacheProps;
import org.everit.osgi.querydsl.templates.SQLTemplatesConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.jdbc.DataSourceFactory;

@Component(immediate = true)
@Service(value = ConfigurationInitComponent.class)
public class ConfigurationInitComponent {

    @Reference(bind = "bindConfigAdmin")
    private ConfigurationAdmin configAdmin;

    @Activate
    public void activate(final BundleContext bundleContext) {
        try {
            Dictionary<String, Object> xaDataSourceProps = new Hashtable<String, Object>();
            xaDataSourceProps.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:test");
            String xaDataSourcePid = getOrCreateConfiguration("org.everit.osgi.jdbc.dsf.XADataSourceComponent",
                    xaDataSourceProps);

            Dictionary<String, Object> pooledDataSourceProps = new Hashtable<String, Object>();
            pooledDataSourceProps.put("xaDataSource.target", "(service.pid=" + xaDataSourcePid + ")");
            String pooledDataSourcePid = getOrCreateConfiguration(
                    "org.everit.osgi.jdbc.commons.dbcp.ManagedDataSourceComponent",
                    pooledDataSourceProps);

            Dictionary<String, Object> migratedDataSourceProps = new Hashtable<String, Object>();
            migratedDataSourceProps.put("embeddedDataSource.target", "(service.pid=" + pooledDataSourcePid + ")");
            migratedDataSourceProps.put("schemaExpression", "org.everit.osgi.props.schema");
            String migratedDataSourcePid = getOrCreateConfiguration(
                    "org.everit.osgi.liquibase.datasource.LiquibaseDataSourceComponent",
                    migratedDataSourceProps);

            // sql template config
            Dictionary<String, Object> sqlTemplateProps = new Hashtable<String, Object>();
            sqlTemplateProps.put(SQLTemplatesConstants.PROPERTY_DB_TYPE,
                    SQLTemplatesConstants.DB_TYPE_H2);
            sqlTemplateProps.put(SQLTemplatesConstants.PROPERTY_QUOTE, true);
            String sqlTemplatePid = getOrCreateConfiguration(
                    "org.everit.osgi.querydsl.templates.SQLTemplates",
                    sqlTemplateProps);

            // cache config
            Dictionary<String, Object> cacheFactoryProps = new Hashtable<String, Object>();
            cacheFactoryProps.put(CacheFactoryProps.CLUSTERED, true);
            cacheFactoryProps.put(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, false);
            getOrCreateConfiguration(CacheFactoryProps.CACHE_FACTORY_COMPONENT_NAME, "("
                    + CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED + "=false)",
                    cacheFactoryProps);
            Dictionary<String, Object> simpleCacheConfigProps = new Hashtable<String, Object>();
            simpleCacheConfigProps.put(CacheProps.CACHE_NAME, "simpleCache");
            getOrCreateConfiguration(CacheProps.CACHE_CONFIGURATION_COMPONENT_NAME, "(" + CacheProps.CACHE_NAME
                    + "=simpleCache)", simpleCacheConfigProps);

            Dictionary<String, Object> propertyComponentProps = new Hashtable<String, Object>();
            propertyComponentProps.put("dataSource.target", "(service.pid=" + migratedDataSourcePid + ")");
            propertyComponentProps.put("sqlTemplate.target", "(service.pid=" + sqlTemplatePid + ")");
            getOrCreateConfiguration("org.everit.osgi.props.PropertyComponent",
                    propertyComponentProps);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindConfigAdmin(final ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private String getOrCreateConfiguration(final String factoryPid, final Dictionary<String, Object> props)
            throws IOException,
            InvalidSyntaxException {
        Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
        if ((configurations != null) && (configurations.length > 0)) {
            return configurations[0].getFactoryPid();
        }
        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid, null);
        configuration.update(props);
        return configuration.getPid();
    }

    private String getOrCreateConfiguration(final String factoryPid, final String filterParam,
            final Dictionary<String, Object> props) throws IOException, InvalidSyntaxException {
        String filter = "(service.factoryPid=" + factoryPid + ")";
        if (filterParam != null) {
            filter = "(&" + filter + filterParam + ")";
        }

        Configuration[] configurations = configAdmin.listConfigurations(filter);
        if ((configurations != null) && (configurations.length > 0)) {
            return configurations[0].getFactoryPid();
        }

        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid, null);
        configuration.update(props);
        return configuration.getPid();
    }

}
