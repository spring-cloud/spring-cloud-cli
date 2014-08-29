Integration between [Cloudfoundry](https://github.com/cloudfoundry)
and the [Spring Platform](https://github.com/spring-platform).

Add this project to a Spring Boot REST service and deploy to
Cloudfoundry (and use the Actuator for maximum flexibility). It will
expose service-broker endpoints automatically (look in /mappings for
/v2/*) and you can register it with the Cloud Controller as described
here:
[http://docs.cloudfoundry.org/services/managing-service-brokers.html](http://docs.cloudfoundry.org/services/managing-service-brokers.html).

Example script to deploy and register a broker:

```
DOMAIN=mydomain.net
cf push app -p target/*.jar --no-start
cf env app | grep SPRING_PROFILES_ACTIVE || cf set-env app SPRING_PROFILES_ACTIVE cloud
cf env app | grep APPLICATION_DOMAIN || cf set-env app APPLICATION_DOMAIN ${DOMAIN}

cf services | grep configserver && cf bind app configserver
    
cf restart app
cf create-service-broker app user secure http://app.${DOMAIN}

for f in `cf curl /v2/service_plans | grep '\"guid' | sed -e 's/.*: "//' -e 's/".*//'`; do 
    cf curl v2/service_plans/$f -X PUT -d '{"public":true}'
done

cf create-service app free appi
```

At which point you have a service called "app" and a service instance called "appi":

```
$ cf marketplace
OK

service        plans   description   
app            free    Singleton service app
$ cf services
Getting services in org default / space development as admin...
OK

name           service        plan   bound apps   
appi           app            free   
```

Your application can define a configuration property
`application.domain` (defaults to "cfapps.io") which will be used to
construct the credentials for any app that binds to your service. Or
it can define the URI directly using
`cloudfoundry.service.definition.metadata.uri`.

You can change some other basic metadata by setting config properties:

* `cloudfoundry.service.definition.*` is bound to a
  `ServiceDefinition` (defined in spring-boot-cf-service-broker) which
  has optional setters for plans and metadata.
  
* `cloudfoundry.service.broker.*` is bound to an internal bean. It has
  optional setters for "name" (the service name), "description" (user
  friendly description) and "prefix" (used to create a unique id from
  the name).
  
An app which binds to your service will get credentials that contain a
"uri" property linking to your service. A Spring Boot app can bind to
that through the `vcap.services.[service].credentials.uri` environment
property.

If your service also has a
[Eureka core](https://github.com/Netflix/eureka) dependency, and you
can expose it as a Eureka service, then any service which registers
with Eureka will also become a Cloudfoundry service. Example app with
Eureka server (include jersey 1.13 to get the JAX-RS dependencies):

```
@Configuration
@EnableAutoConfiguration
public class Application extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public FilterRegistrationBean jersey() {
		FilterRegistrationBean bean = new FilterRegistrationBean();
		bean.setFilter(new ServletContainer());
		bean.addInitParameter("com.sun.jersey.config.property.WebPageContentRegex",
				"(/|/(flex/|images/|js/|css/|jsp/|admin/|v2/catalog|v2/service_instances).*)");
		bean.addInitParameter("com.sun.jersey.config.property.packages",
				"com.sun.jersey;com.netflix");
		return bean;
	}

}

```
