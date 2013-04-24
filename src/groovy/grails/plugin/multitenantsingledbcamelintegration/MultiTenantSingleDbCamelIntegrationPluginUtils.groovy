package grails.plugin.multitenantsingledbcamelintegration

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor

class MultiTenantSingleDbCamelIntegrationGrailsPluginUtils {
	static doWithSpring = {
		myInterceptor(MultitenantInterceptor) {
			currentTenant = ref('currentTenant')
		}
	}

	static doWithDynamicMethods = { ctx ->
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
	static watchedResources = [
		"file:./grails-app/controllers/**/*Controller.groovy",
		"file:./grails-app/services/**/*Service.groovy"
	]

	/** Copied from RoutingGrailsPlugin.  Keep in sync. */
	static onChange = { event ->
		def artifactName = "${event.source.name}"

		if (artifactName.endsWith('Controller') || artifactName.endsWith('Service')) {
			def artifactType = (artifactName.endsWith('Controller')) ? 'controller' : 'service'
			def grailsClass = application."${artifactType}Classes".find { it.fullName == artifactName }
			addDynamicMethods([ grailsClass ], event.ctx.getBean('producerTemplate'))
		}
	}

	private static addDynamicMethods(artifacts, template, currentTenant) {
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

	private static createBodyAndHeadersAndPropertyProcessor(final endpoint,
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

	private static isQuartzPluginInstalled(application) {
		// this is a nasty implementation... maybe there's something better?
		try {
			def tasks = application.taskClasses
			return true
		} catch (e) {
			return false
		}
	}
}

