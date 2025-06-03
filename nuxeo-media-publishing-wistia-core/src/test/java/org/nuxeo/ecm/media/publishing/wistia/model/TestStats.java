/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Test Suite Author
 */

package org.nuxeo.ecm.media.publishing.wistia.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.media.publishing.wistia.model.Stats;

public class TestStats {

    private Stats stats;

    @Before
    public void setUp() {
        stats = new Stats();
    }

    @Test
    public void testStatsProperties() {
        // Test all stats properties
        stats.setVisitors(100);
        assertEquals(100, stats.getVisitors());

        stats.setPlays(250);
        assertEquals(250, stats.getPlays());

        stats.setAveragePercentWatched(75);
        assertEquals(75, stats.getAveragePercentWatched());

        stats.setPageLoads(150);
        assertEquals(150, stats.getPageLoads());

        stats.setPercentOfVisitorsClickingPlay(60);
        assertEquals(60, stats.getPercentOfVisitorsClickingPlay());
    }

    @Test
    public void testDefaultValues() {
        Stats newStats = new Stats();
        
        assertEquals(0, newStats.getVisitors());
        assertEquals(0, newStats.getPlays());
        assertEquals(0, newStats.getAveragePercentWatched());
        assertEquals(0, newStats.getPageLoads());
        assertEquals(0, newStats.getPercentOfVisitorsClickingPlay());
    }

    @Test
    public void testToString() {
        stats.setVisitors(100);
        stats.setPlays(250);
        stats.setAveragePercentWatched(75);

        String result = stats.toString();
        assertNotNull("toString should not return null", result);
        assertTrue("toString should contain stats info", result.contains("Stats info"));
        assertTrue("toString should contain visitors", result.contains("100"));
        assertTrue("toString should contain plays", result.contains("250"));
        assertTrue("toString should contain percent watched", result.contains("75"));
    }

    @Test
    public void testBoundaryValues() {
        // Test with boundary values
        stats.setVisitors(0);
        assertEquals(0, stats.getVisitors());

        stats.setPlays(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, stats.getPlays());

        stats.setAveragePercentWatched(100);
        assertEquals(100, stats.getAveragePercentWatched());

        stats.setPercentOfVisitorsClickingPlay(0);
        assertEquals(0, stats.getPercentOfVisitorsClickingPlay());
    }
}