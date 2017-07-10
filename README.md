# licensegenplugin
Maven plugin for generating the license data from the license list XML repository.  The plugin supports various validation functions and the creation of license data from the license-xml source.

## Under Development
The plugin is currently under development and is not stable.

## Goals Overview
The licensegenplugin supports 2 goals:
* licensegen:validate - Validates a directory containing license XML files against a license XML schema.
* licensegen:generate - Generates license data (HTML, website, JSON, RDFa, template and text formats) from the license XML files.

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
			</configuration>
		</plugin>
	</plugins>

To validate the license xml files execute:

	mvn licensegen:validate

To generate the data:

	mvn licensegen:generate
