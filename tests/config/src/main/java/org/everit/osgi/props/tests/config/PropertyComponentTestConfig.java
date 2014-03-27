package org.everit.osgi.props.tests.config;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service(value = PropertyComponentTestConfig.class)
@Properties({ @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "PropertyComponentTest"),
        @Property(name = "dataSource.target") })
public class PropertyComponentTestConfig {

}
