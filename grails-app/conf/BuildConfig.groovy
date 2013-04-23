grails.project.work.dir = 'target'
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	plugins {
		build ':release:2.2.1',
				':rest-client-builder:1.0.3', {
			export = false
		}

		compile ':routing:1.2.0',
				':multi-tenant-single-db:0.8.2',
				":hibernate:$grailsVersion", {
			export = false
		}
	}
}

