import grails.plugin.multitenantsingledbcamelintegration.MultiTenantSingleDbCamelIntegrationGrailsPluginUtils as Utils

class MultiTenantSingleDbCamelIntegrationGrailsPlugin {
	def version = '0.3-SNAPSHOT'
	def grailsVersion = "2.0 > *"
	def pluginExcludes = ['grails-app/views/error.gsp']
	def title = 'Multi Tenant Single Db Apache Camel Integration Plugin'
	def author = 'Alex Anderson'
	def authorEmail = ''
	def description = 'Integration between Apache Camel and Multi-Tenant-Single-Db plugins to allow jobs to be run in the context of a Tenant.'
	def documentation = 'http://grails.org/plugin/multi-tenant-single-db-camel-integration'
	def license = 'APACHE'
	def loadAfter = ['routing', 'multi-tenant-single-db']
	def organization = [name:'FrontlineSMS', url:'http://www.frontlinesms.com']
	def issueManagement = [system:'github', url:'https://github.com/frontlinesms/grails-multi-tenant-single-db-camel-integration-plugin/issues']
	def scm = [url:'git@github.com:frontlinesms/grails-multi-tenant-single-db-camel-integration-plugin.git']

	def doWithSpring = Utils.doWithSpring

	def doWithDynamicMethods = Utils.doWithDynamicMethods

	def watchedResources = Utils.watchedResources

	def onChange = Utils.onChange
}

