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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gary
 *
 */
public class TestLicensgenMojoTest extends AbstractMojoTestCase {

	static final String UNIT_TEST_TARGET_VALID = "src/test/resources/unit/valid-licenses-project/target";
	private static final String UNIT_TEST_POM_FILE_VALID = "src/test/resources/unit/valid-licenses-project/pom.xml";

	static final String UNIT_TEST_TARGET_FAILTEST = "src/test/resources/unit/failtest-licenses-project/target";
	private static final String UNIT_TEST_POM_FILE_FAILTEST = "src/test/resources/unit/failtest-licenses-project/pom.xml";
	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.testing.AbstractMojoTestCase#setUp()
	 */
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		deleteDirectoryContents(new File(UNIT_TEST_TARGET_VALID));
	}

	/* (non-Javadoc)
	 * @see org.codehaus.plexus.PlexusTestCase#tearDown()
	 */
	@After
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteDirectoryContents(new File(UNIT_TEST_TARGET_VALID));
	}
	
	/**
	 * Delete the contents of a directory - leaving the top level directory intack
	 * @param dir
	 */
	private void deleteDirectoryContents(File dir) throws IOException {
		if (!dir.exists()) {
			throw new FileNotFoundException(dir.getName()+"does not exist");
		}
		File[] content = dir.listFiles();
		for (File child:content) {
			if (child.isDirectory()) {
				deleteDirectoryContents(child);
			}
			if (!child.delete()) {
				throw(new IOException("Unable to delete "+child.getName()));
			}
		}
	}

	@Test
	public void testNoErrors() throws Exception {
		
		File pom = getTestFile( UNIT_TEST_POM_FILE_VALID );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        GenerateLicenseDataMojo myMojo = (GenerateLicenseDataMojo) lookupMojo( "generate", pom );
        assertNotNull( myMojo );
        myMojo.execute();
        
        TestLicensegenMojo testMojo = (TestLicensegenMojo) lookupMojo( "test", pom );
        assertNotNull( testMojo );
        testMojo.execute();
	}
	
	@Test
	public void testExpectedErrors() throws Exception {
		
		File pom = getTestFile( UNIT_TEST_POM_FILE_FAILTEST );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        GenerateLicenseDataMojo myMojo = (GenerateLicenseDataMojo) lookupMojo( "generate", pom );
        assertNotNull( myMojo );
        myMojo.execute();
        
        TestLicensegenMojo testMojo = (TestLicensegenMojo) lookupMojo( "test", pom );
        assertNotNull( testMojo );
        try {
        	testMojo.execute();
        	fail("The test mojo didn't fail when it was supposed to!");
        } catch(MojoFailureException ex) {
        	// Expected
        }
	}

}
