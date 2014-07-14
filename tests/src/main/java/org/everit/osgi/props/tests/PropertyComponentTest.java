/**
 * This file is part of Everit - Property Manager Tests.
 *
 * Everit - Property Manager Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Property Manager Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Property Manager Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.props.tests;

import java.util.Random;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.props.PropertyManager;
import org.junit.Assert;
import org.junit.Test;

@Component(name = "org.everit.osgi.props.ri.tests.PropertyManagerTest", immediate = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Service(value = PropertyComponentTest.class)
@Properties({ @Property(name = "propertyManager.target"), @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "PropertyComponentTest") })
public class PropertyComponentTest {

    /**
     * The maximum length of the key and value.
     */
    private static final int MAX_LENGTH = 100;

    @Reference(bind = "setPropertyManager")
    private PropertyManager propertyManager;

    /**
     * Generating String to the first and second name.
     *
     * @return the random length string.
     */
    private String generateString() {
        Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = random.nextInt(MAX_LENGTH);
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    public void setPropertyManager(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
    }

    @Test
    @TestDuringDevelopment
    public void testNullParameter() {
        try {
            propertyManager.addProperty("test", null);
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null values are not supported!", e.getMessage());
        }

        try {
            propertyManager.updateProperty("test", null);
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null values are not supported!", e.getMessage());
        }

        try {
            propertyManager.addProperty(null, "test");
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null key is not supported!", e.getMessage());
        }

        try {
            propertyManager.updateProperty(null, "test");
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null key is not supported!", e.getMessage());
        }

        try {
            propertyManager.removeProperty(null);
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null key is not supported!", e.getMessage());
        }

        try {
            propertyManager.getProperty(null);
            Assert.fail("NPE should have been thrown");
        } catch (NullPointerException e) {
            Assert.assertEquals("Null key is not supported!", e.getMessage());
        }
    }

    @Test
    @TestDuringDevelopment
    public void testPropertyService() {

        String key1 = generateString();
        String value1 = generateString();

        Assert.assertNull(propertyManager.getProperty(key1));

        propertyManager.addProperty(key1, value1);
        Assert.assertNotNull(propertyManager.getProperty(key1));
        Assert.assertEquals(value1, propertyManager.getProperty(key1));

        String value2 = generateString();
        String retString = propertyManager.updateProperty(key1, value2);
        Assert.assertTrue(value1.equals(retString));
        Assert.assertEquals(value2, propertyManager.getProperty(key1));

        String key2 = generateString();
        propertyManager.addProperty(key2, value2);
        Assert.assertEquals(value2, propertyManager.getProperty(key2));

        retString = propertyManager.removeProperty(key2);
        Assert.assertEquals(value2, retString);
        Assert.assertNull(propertyManager.getProperty(key2));

        retString = propertyManager.removeProperty("notexist");
        Assert.assertNull(retString);

        retString = propertyManager.updateProperty("notexist", "dummy");
        Assert.assertNull(retString);
        Assert.assertNull(propertyManager.getProperty("notexist"));
    }
}
