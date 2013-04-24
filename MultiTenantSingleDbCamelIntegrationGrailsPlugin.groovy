import grails.plugin.multitenantsingledbcamelintegration.*

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor

class MultiTenantSingleDbCamelIntegrationGrailsPlugin {
	def version = "0.1-SNAPSHOT"
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

	def doWithSpring = {
		myInterceptor(grails.plugin.multitenantsingledbcamelintegration.MultitenantInterceptor) {
			currentTenant = ref('currentTenant')
		}
	}

	def doWithDynamicMethods = { ctx ->
		// override dynamic methods from RoutingGrailsPlugin
		// N.B. this code must be kept in sync with RoutingGrailsPlugin.doWithDynamicMethods()
		def template = ctx.getBean('producerTemplate')
		def currentTenant = ctx.getBean('currentTenant')

		addDynamicMethods(application.controllerClasses, template, currentTenant);
		addDynamicMethods(application.serviceClasses, template, currentTenant);

		if (isQuartzPluginInstalled(application))
			addDynamicMethods(application.taskClasses, template, currentTenant);
	}

	/** Copied from RoutingGrailsPlugin.  Keep in sync. */
	def watchedResources = [
		"file:./grails-app/controllers/**/*Controller.groovy",
		"file:./grails-app/services/**/*Service.groovy"
	]

	/** Copied from RoutingGrailsPlugin.  Keep in sync. */
	def onChange = { event ->
		def artifactName = "${event.source.name}"

		if (artifactName.endsWith('Controller') || artifactName.endsWith('Service')) {
			def artifactType = (artifactName.endsWith('Controller')) ? 'controller' : 'service'
			def grailsClass = application."${artifactType}Classes".find { it.fullName == artifactName }
			addDynamicMethods([ grailsClass ], event.ctx.getBean('producerTemplate'))
		}
	}

	private def addDynamicMethods(artifacts, template, currentTenant) {
		artifacts.each { artifact ->
			artifact.metaClass.sendMessage = { endpoint, message ->
				println "MultiTenantSingleDbCamelIntegrationGrailsPlugin.sendMessage() :: ENTRY"
				template.sendBodyAndProperty(endpoint, message, MultitenantInterceptor.PROP_MT_ID, currentTenant.get())
			}
			artifact.metaClass.sendMessageAndHeaders = { endpoint, message, headers ->
				println "MultiTenantSingleDbCamelIntegrationGrailsPlugin.sendMessageAndHeaders() :: ENTRY"
				template.send(endpoint, createBodyAndHeadersAndPropertyProcessor(endpoint,
						message, headers, MultitenantInterceptor.PROP_MT_ID, currentTenant.get()))
			}
			artifact.metaClass.requestMessage = { endpoint, message ->
				artifact.requestBody(endpoint,message)
			}
			artifact.metaClass.requestMessageAndHeaders = { endpoint, message, headers ->
				artifact.requestBodyAndHeaders(endpoint, message, headers)
			}
		}
	}

	private def createBodyAndHeadersAndPropertyProcessor(final endpoint,
			final message, final headers, final propertyName, final propertyValue) {
		println "MultiTenantSingleDbCamelIntegrationGrailsPlugin:w.createBodyAndHeadersAndPropertyProcessor() :: ENTRY"
		return new Processor() {
			void process(Exchange exchange) {
				exchange.setProperty(propertyName, propertyValue)
				Message m = exchange.in
				headers.each { k, v ->
					m.setHeader(k, v)
				}
				m.body = message
			}
		}
	}
}

