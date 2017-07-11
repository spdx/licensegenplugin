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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Goal to validate the XML files against a schema
 *
 */
@Mojo( name = "validate", defaultPhase = LifecyclePhase.VALIDATE )
public class ValidateLicensesMojo
    extends AbstractMojo
{
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
    
    @Parameter(required = false)
    private File textCompare;
    
	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		File sf = schemaFile;
		if (sf == null) {
			throw new MojoExecutionException("No Schema file was provided in the configuration.  Add a configuration paramater 'schema' to the plugin configuration with a value of the license XML schema file path.");
		}
		if (!sf.exists()) {
			throw new MojoExecutionException("Schema file "+sf.getName()+" does not exist.");
		}
		if (!sf.canRead()) {
			throw new MojoExecutionException("Can not read schema file "+sf.getName());
		}
		File src = sourceDirectory;
		if (src == null) {
			throw new MojoExecutionException("No source directory was provided in the configuration.  Add a configuration paramater 'sourceDir' to the plugin configuration with a value of the directory path for the license XML files.");
		}
		if (!src.exists() || !src.isDirectory()) {
			throw new MojoExecutionException("Source directory "+src.getName()+" does not exist.");
		}
		InputStream schemaIs = null;
		try {
			schemaIs = new FileInputStream(sf);
			
			Source schemaSource = new StreamSource(schemaIs);
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaSource);
			Validator validator = schema.newValidator();
			if (!validateDirectory(src, validator)) {
				throw new MojoFailureException("Source directory contains one or more invalid license XML files");
			}
		} catch (IOException e) {
			this.getLog().error("IO error reading schema file "+sf.getName(),e);
			throw(new MojoExecutionException("IO error reading schema file "+sf.getName(),e));
		} catch (SAXException e) {
			this.getLog().error("Error parsing schema file "+sf.getName(),e);
			throw(new MojoExecutionException("Error parsing reading schema file "+sf.getName(),e));
		} finally {
			if (schemaIs != null) {
				try {
					schemaIs.close();
				} catch (IOException e) {
					this.getLog().warn("IO Exception closing schema file: "+e.getMessage());
				}
			}
		}
	}
	/**
	 * Validate all files in the directory and subdirectories that end in ".xml"
	 * @param dir
	 * @param validator
	 * @return true if all files are valid
	 * @throws MojoExecutionException 
	 */
	private boolean validateDirectory(File dir, Validator validator) throws MojoExecutionException {
		boolean retval = true;
		if (!dir.isDirectory()) {
			throw new MojoExecutionException(dir.getName()+" is not a directory");
		}
		File[] children = dir.listFiles();
		if (children != null) {
			for (File child:children) {
				if (child.isFile() && child.getName().toLowerCase().endsWith(".xml")) {
					retval = validateFile(child,validator) & retval;
				} else if (child.isDirectory()) {
					retval = validateDirectory(child, validator) & retval;
				}
			}
		}
		return retval;
	}
	
	/**
	 * Validate a license XML file against the validator
	 * @param file
	 * @param validator
	 * @return true if valid
	 */
	private boolean validateFile(File file, Validator validator) {
		Source xmlSource = new StreamSource(file);
		try {
			validator.validate(xmlSource);
		} catch (SAXParseException e) {
			this.getLog().error("Parsing error in XML file "+file.getName()+ " at line "+e.getLineNumber()+", column "+e.getColumnNumber()+":"+e.getMessage());
			return false;
		} catch (SAXException e) {
			this.getLog().error("File "+file.getName()+" contains the following XML parsing error: "+e.getMessage(),e);
			return false;
		} catch (IOException e) {
			this.getLog().error("IO Error validating "+file.getName()+": "+e.getMessage(),e);
			return false;
		}
		return true;
	}
}
