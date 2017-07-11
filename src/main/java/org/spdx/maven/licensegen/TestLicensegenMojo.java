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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.spdx.compare.CompareTemplateOutputHandler;
import org.spdx.compare.LicenseCompareHelper;
import org.spdx.licenseTemplate.LicenseTemplateRuleException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;

/**
 * Test goal for licensegen which compares the generated licenses to expected license text
 * @author Gary O'Neall
 *
 */
@Mojo( name = "test", defaultPhase = LifecyclePhase.TEST )
public class TestLicensegenMojo extends AbstractMojo {
	
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
		if (textCompare == null || !textCompare.exists()) {
			this.getLog().warn("No textCompare directory specified in configuration for test phase");
			return;
		}
		if (!textCompare.isDirectory()) {
			throw new MojoExecutionException("Test directory "+textCompare.getName()+" is not a directory.");
		}
		File output = outputDirectory;
		if (outputDirectory == null) {
			throw new MojoExecutionException("No output directory was provided in the configuration.  Add a configuration paramater 'outputDir' to the plugin configuration with a value of the directory path for the license XML files.");
		}
		if (!output.exists() || !output.isDirectory()) {
			throw new MojoExecutionException("Output directory "+output.getName()+" does not exist.");
		}
		int numErrors = 0;
		Path templateDirPath = output.toPath().resolve("template");
		Path genTextDirPath = output.toPath().resolve("text");
		File[] textFiles = textCompare.listFiles();
		if (textFiles != null) {
			for (File textFile:textFiles) {
				Path textFilePath = textFile.toPath();
				String text = null;
				try {
					text = readFileToString(textFilePath);
				} catch (IOException e) {
					throw new MojoExecutionException("IO Error reading test text file "+textFile.getName(),e);
				}
				String fileName = textFilePath.getFileName().toString();
				if (!fileName.endsWith(".txt")) {
					fileName = fileName + ".txt";
				}
				String templateFileName = fileName.substring(0, fileName.length()-".txt".length()) + ".template.txt";
				Path templatePath = templateDirPath.resolve(templateFileName);
				File templateFile = templatePath.toFile();
				Path genTextPath = genTextDirPath.resolve(fileName);
				File genTextFile = genTextPath.toFile();
				if (templateFile.exists() && templateFile.isFile()) {
					if (!textMatchesTemplate(templatePath, text)) {
						this.getLog().error("License template does not allow for the match of the test license text "+textFile.getName());;
						numErrors++;
					}				
				} else {
					this.getLog().error("No template file was found for test file "+fileName);
					numErrors++;
				}
				if (genTextFile.exists() && genTextFile.isFile()) {
					String genText = null;
					try {
						genText = readFileToString(genTextPath);
					} catch (IOException e) {
						throw new MojoExecutionException("IO Error reading generated text file "+genTextPath.toString(),e);
					}
					if (!LicenseCompareHelper.isLicenseTextEquivalent(text, genText)) {
						this.getLog().error("Generated license text does not match test license text for "+textFile.getName());;
						numErrors++;
					}
				}
			}
		}		
		if (numErrors > 0) {
			throw new MojoFailureException("Errors found testing generated licenses files against expected license text");
		}
	}

	/**
	 * @param templatePath
	 * @param text
	 * @return true if the text is matched by the template stored in the file templatePath
	 * @throws MojoExecutionException
	 */
	private boolean textMatchesTemplate(Path templatePath, String text) throws MojoExecutionException {
		String template = null;
		try {
			template = readFileToString(templatePath);
		} catch (IOException e) {
			throw new MojoExecutionException("IO Error reading template file "+templatePath.toString(),e);
		}
		
		CompareTemplateOutputHandler compareTemplateOutputHandler = new CompareTemplateOutputHandler(text);
		try {
			SpdxLicenseTemplateHelper.parseTemplate(template, compareTemplateOutputHandler);
		} catch (LicenseTemplateRuleException e) {
			throw(new MojoExecutionException("Invalid template rule found during compare: "+e.getMessage(),e));
		}
		return compareTemplateOutputHandler.matches();
	}

	private String readFileToString(Path path) throws IOException {
		List<String> lines = Files.readAllLines(path);
		if (lines.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(lines.get(0));
		for (int i = 1; i < lines.size(); i++) {
			sb.append("\n");
			sb.append(lines.get(i));
		}
		return sb.toString();
	}

}
