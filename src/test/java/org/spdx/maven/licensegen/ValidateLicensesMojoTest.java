/**
 * Copyright (c) 2017 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.spdx.maven.licensegen;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gary O'Neall
 *
 */
public class ValidateLicensesMojoTest extends AbstractMojoTestCase {

	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.testing.AbstractMojoTestCase#setUp()
	 */
	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see org.codehaus.plexus.PlexusTestCase#tearDown()
	 */
	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testValidXml() throws Exception {
        File pom = getTestFile( "src/test/resources/unit/valid-licenses-project/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        ValidateLicensesMojo myMojo = (ValidateLicensesMojo) lookupMojo( "validate", pom );
        assertNotNull( myMojo );
        myMojo.execute();
	}
	
	@Test
	public void testInvalidXml() throws Exception {
        File pom = getTestFile( "src/test/resources/unit/invalid-licenses-project/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        ValidateLicensesMojo myMojo = (ValidateLicensesMojo) lookupMojo( "validate", pom );
        assertNotNull( myMojo );
        try {
        	myMojo.execute();
        	fail("Invalid XML was not detected");
        } catch (MojoFailureException ex) {
        	//Expected
        }
        
	}

}
