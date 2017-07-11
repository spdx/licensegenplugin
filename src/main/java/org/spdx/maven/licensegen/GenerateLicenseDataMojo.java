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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.spdx.tools.LicenseGeneratorException;
import org.spdx.tools.LicenseRDFAGenerator;

/**
 * Mojo to generate license data
 * @author Gary O'Neall
 *
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.COMPILE )
public class GenerateLicenseDataMojo extends AbstractMojo {
	
    /**
     * Directory containing the license list XML files
     */	
    @Parameter( defaultValue = "${project.build.sourceDirectory}"+"schema/ListedLicense.xsd", required = true )
    private File schemaFile;
    
    /**
     * Directory containing the license list XML files
     */
    @Parameter( defaultValue = "${project.build.sourceDirectory}", required = true )
    private File sourceDirectory;
    
    /**
     * Output directory
     */
    @Parameter( defaultValue = "${project.build.outputDirectory}", required = true )
    private File outputDirectory;
    
    @Parameter(defaultValue = "${project.version}", required = false)
    private String licenseListVersion;
    
    @Parameter(required = false)
    private File textCompare;

	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		String version;
		if (licenseListVersion == null || licenseListVersion.trim().isEmpty()) {
			version = "UNKNOWN";
		} else {
			version = licenseListVersion;
		}
		File src = sourceDirectory;
		if (src == null) {
			throw new MojoExecutionException("No source directory was provided in the configuration.  Add a configuration paramater 'sourceDir' to the plugin configuration with a value of the directory path for the license XML files.");
		}
		if (!src.exists() || !src.isDirectory()) {
			throw new MojoExecutionException("Source directory "+src.getName()+" does not exist.");
		}
		File output = outputDirectory;
		if (outputDirectory == null) {
			throw new MojoExecutionException("No output directory was provided in the configuration.  Add a configuration paramater 'outputDir' to the plugin configuration with a value of the directory path for the license XML files.");
		}
		if (!output.exists() || !output.isDirectory()) {
			throw new MojoExecutionException("Output directory "+output.getName()+" does not exist.");
		}
		String releaseDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		try {
			List<String> warnings = LicenseRDFAGenerator.generateLicenseData(src, output, version, releaseDate);
			if (warnings.size() > 0) {
				for (String warning:warnings) {
					this.getLog().warn(warning);
				}
			}
		} catch (LicenseGeneratorException e) {
			throw new MojoFailureException("Failure generating license data: "+e.getMessage(),e);
		}
	}

}
