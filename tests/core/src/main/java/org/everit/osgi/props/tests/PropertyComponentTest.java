/**
 * This file is part of Everit - Property Service Component Tests Core.
 *
 * Everit - Property Service Component Tests Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Property Service Component Tests Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Property Service Component Tests Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.everit.osgi.props.tests;

import java.util.Random;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.props.PropertyService;
import org.junit.Assert;
import org.junit.Test;

@Component(immediate = true)
@Service(value = PropertyComponentTest.class)
@Properties({ @Property(name = "eosgi.testEngine", value = "junit4"),
        @Property(name = "eosgi.testId", value = "PropertyComponentTest"),
        @Property(name = "dataSource.target") })
public class PropertyComponentTest {

    @Reference(bind = "bindPropertyService", unbind = "unbindPropertyService")
    private PropertyService propertyService;

    /**
     * The maximum length of the key and value.
     */
    private static final int MAX_LENGTH = 100;

    protected void bindPropertyService(final PropertyService propertyService) {
        this.propertyService = propertyService;
    }

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

    @Test
    @TestDuringDevelopment
    public void testPropertyService() {

        String key1 = generateString();
        String value1 = generateString();

        Assert.assertNull(propertyService.getProperty(key1));

        propertyService.addProperty(key1, value1);
        Assert.assertNotNull(propertyService.getProperty(key1));
        Assert.assertEquals(value1, propertyService.getProperty(key1));

        String value2 = generateString();
        String retString = propertyService.setProperty(key1, value2);
        Assert.assertTrue(value1.equals(retString));
        Assert.assertEquals(value2, propertyService.getProperty(key1));

        String key2 = generateString();
        propertyService.addProperty(key2, value2);
        Assert.assertEquals(value2, propertyService.getProperty(key2));

        retString = propertyService.removeProperty(key2);
        Assert.assertEquals(value2, retString);
        Assert.assertNull(propertyService.getProperty(key2));

        retString = propertyService.removeProperty("notexist");
        Assert.assertNull(retString);

        retString = propertyService.setProperty("notexist", "dummy");
        Assert.assertNull(retString);
        Assert.assertNull(propertyService.getProperty("notexist"));

        try {
            propertyService.setProperty(key1, null);
            Assert.fail("setProperty should fail, because null values are not supported in the PropertyService");
        } catch (NullPointerException e) {
            Assert.assertTrue("Null values are not supported!".equals(e.getMessage()));
        }
    }

    protected void unbindPropertyService(final PropertyService propertyService) {
        this.propertyService = null;
    }
}
