package grails.plugin.multitenantsingledbcamelintegration

import org.apache.camel.AsyncCallback
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.processor.DelegateAsyncProcessor
import org.apache.camel.spi.InterceptStrategy

import org.springframework.transaction.support.TransactionSynchronizationManager

import grails.plugin.multitenant.core.CurrentTenant

class MultitenantInterceptor implements InterceptStrategy {
	CurrentTenant currentTenant

	public Processor wrapProcessorInInterceptors(final CamelContext context,
			final ProcessorDefinition<?> definition,
			final Processor target,
			final Processor nextTarget) throws Exception {
		if(!currentTenant) {
			println "][ MultitenantInterceptor.wrapProcessorInInterceptors() :: currentTenant is not set.  Terminating."
			System.exit(66)
		}
		// FIXME change this to a DelegateAsyncProcessor and override process(Exchange, AsyncCallback) as
		// per https://camel.apache.org/maven/current/camel-core/apidocs/org/apache/camel/spi/InterceptStrategy.html
		return new Processor() {
			void process(Exchange exchange) {
				println "][ MultitenantInterceptor.Processor.process() :: target=$target; exchange=$exchange"
				// TODO tenant ID should be got from exchange.properties
				def tenantId = 1

				// Unfortunately there appear to be various obtuse hibernate transactional issues surrounding
				// use of withNewSession and consequently X.withTenantId or multiTenantService.doWithTenantId
				// possibly relating to http://jira.grails.org/browse/GRAILS-7780.
				// Consequently we are left with this potentially troublesome hack:
				println "][ MultitenantInterceptor.Processor.process() :: setting tenant ID: $tenantId"
				currentTenant.set(tenantId)
				target.process(exchange)
			}
		}
	}
}

