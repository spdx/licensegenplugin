# Archive Status
This repository has been moved to archive status as of 9 January 2022.

# licensegenplugin
Maven plugin for generating the license data from the license list XML repository.  The plugin supports various validation functions and the creation of license data from the license-xml source.

## Under Development
The plugin is currently under development and is not stable.

## Goals Overview
The licensegenplugin supports 3 goals:
* licensegen:validate - Validates a directory containing license XML files against a license XML schema.  Used in the validate phase.
* licensegen:generate - Generates license data (HTML, website, JSON, RDFa, template and text formats) from the license XML files.  Used in the compile phase.
* licensegen:test - Test the generated license data against known licenses.  Identifies any duplicate licenses and any license text that doesn't match the known text.

## Usage
Configure the licensegenplugin by adding the plugin to the plugins in your project's pom.xml file:

	<plugins>
		<plugin>
			<groupid>org.spdx.manven</groupid>
			<artifactid>licensegen</artifactid>
			<version>1.0.0</version>
			<configuration>
				<schemaFile>[path to the schema file]</schemaFile>         				<sourceDirectory>[path to the dir containing license XML files]</sourceDirectory>
				<outputDirectory>[path to the output directory]</outputDirectory>
				<licenseListVersion>[version of the license list]</licenseListVersion>
				<textCompare>[Directory containing the text only version of the licenses (used for testing)]</textCompare>
			</configuration>
		</plugin>
	</plugins>

To validate the license xml files execute:

	mvn licensegen:validate

To generate the data:

	mvn licensegen:generate
