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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gary O'Neall
 *
 */
public class GenerateLicenseDataMojoTest extends AbstractMojoTestCase {
	
	static final String UNIT_TEST_TARGET_VALID = "src/test/resources/unit/valid-licenses-project/target";
	private static final String UNIT_TEST_POM_FILE_VALID = "src/test/resources/unit/valid-licenses-project/pom.xml";
	private static final String UNIT_TEST_SRC_VALID = "src/test/resources/unit/valid-licenses-project/src";

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
	public void testValid() throws Exception {
		
		File pom = getTestFile( UNIT_TEST_POM_FILE_VALID );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        GenerateLicenseDataMojo myMojo = (GenerateLicenseDataMojo) lookupMojo( "generate", pom );
        assertNotNull( myMojo );
        myMojo.execute();
        
        File htmlTarget = new File(UNIT_TEST_TARGET_VALID + "/html");
        assertTrue(htmlTarget.exists());
        assertTrue(htmlTarget.isDirectory());
        File[] websiteFiles = htmlTarget.listFiles();
        List<String> sourceFileNames = collectSourceXmls();
        assertEquals(sourceFileNames.size(), websiteFiles.length);
        for (File websiteFile:websiteFiles) {
        	String destFileName = websiteFile.toPath().getFileName().toString();
        	String destFileRoot = destFileName.substring(0, destFileName.length()-".html".length());
        	assertTrue(sourceFileNames.contains(destFileRoot));
        }
	}

	private List<String> collectSourceXmls() {
		ArrayList<String> retval = new ArrayList<String>();
		collectSourceXmls(new File(UNIT_TEST_SRC_VALID), retval);
		return retval;
	}

	private void collectSourceXmls(File dir, ArrayList<String> retval) {
		if (!dir.isDirectory()) {
			return;
		}
		File[] children = dir.listFiles();
		for (File child:children) {
			if (child.isDirectory()) {
				collectSourceXmls(child, retval);
			} else if (child.isFile() && child.getName().toLowerCase().endsWith(".xml")) {
				String fileName = child.toPath().getFileName().toString();
				retval.add(fileName.substring(0, fileName.length()-".xml".length()));
			}
		}
	}

}
