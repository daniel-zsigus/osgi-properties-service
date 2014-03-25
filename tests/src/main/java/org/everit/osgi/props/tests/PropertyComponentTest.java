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

@Component(name = "PropertyComponentTest", immediate = true)
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

        Assert.assertNull(propertyService.getProperty(generateString()));

        String key1 = generateString();
        String value1 = generateString();

        String retValue = propertyService.setProperty(key1, value1);

        System.out.println(retValue);
        Assert.assertNull(retValue);
        Assert.assertEquals(value1, propertyService.getProperty(key1));

        String value2 = generateString();
        retValue = propertyService.setProperty(key1, value2);

        Assert.assertTrue(value1.equals(retValue));
        Assert.assertEquals(value2, propertyService.getProperty(key1));

        propertyService.setProperty(key1, null);
        Assert.assertNull(propertyService.getProperty(key1));

    }

    protected void unbindPropertyService(final PropertyService propertyService) {
        this.propertyService = null;
    }
}
