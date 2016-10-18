## Spring Cloud Launcher

### Building

`./mvnw clean install` from parent directory

### Installing

Install Spring CLI first: https://github.com/spring-cloud/spring-cloud-cli/blob/master/docs/src/main/asciidoc/install.adoc

### Running

```
$ spring cloud
```

Currently starts configserver, eureka, h2 (db server), hystrixdashboard and kafka. [Here](https://github.com/spring-cloud/spring-cloud-cli/blob/devtools/spring-cloud-launcher/spring-cloud-launcher-deployer/src/main/resources/cloud.yml) is the full configuration.

### Configuring

Spring Cloud Launcher use normal Spring Boot configuration mechanisms. The config name is `cloud`, so configuration can go in `cloud.yml` or `cloud.properties`.

For example, to run configserver and eureka, create a `cloud.yml` that looks like:
```
spring:
  cloud:
    launcher:
      deployables:
        - name: configserver
          coordinates: maven://org.springframework.cloud.launcher:spring-cloud-launcher-configserver:1.2.1.RELEASE
          port: 8888
          waitUntilStarted: true
          order: -10
        - name: eureka
          coordinates: maven://org.springframework.cloud.launcher:spring-cloud-launcher-eureka:1.2.1.RELEASE
          port: 8761
```

The `name` attribute is required. If `waitUntilStarted` is true, Launcher will block until the application has reached the `deployed` state. Before commands are deployed, the list is sorted using Spring's `OrderComparator`. In the above case, `configserver` is deployed before any other app is deployed. Currently only `maven:` coordinates and standard Spring Resources (`file:`, etc...) are supported. 

You can also select from the [predefined deployables](https://github.com/spring-cloud/spring-cloud-cli/blob/devtools/spring-cloud-launcher/spring-cloud-launcher-deployer/src/main/resources/cloud.yml). For example to run Spring Cloud Data Flow execute:
```
spring cloud eureka,configserver
```

### Config Server git uri

To run configserver with a git repo set the following in `./configserver.yml`:
```
spring:
  profiles.active: git
  cloud.config.server.git.uri: http://mygitserver/myrepo.git
```

### Stopping

`Ctrl-C` in the same terminal `spring cloud` was run.

### TODO

- [X] Eureka
- [X] Configserver
- [X] Hystrix Dashboard
- [X] Kafka Broker
- [X] Kafka Bus
- [X] Easy inclusion of default deployables
- [X] H2 Database
- [X] Spring Cloud Dataflow server
- [X] Launcher landing page (Eureka Dashboard works for now)
- [X] Sleuth/Zipkin
- [ ] Support external rabbit
- [ ] Speedup startup (parallel start?, retry for config server, db and kafka?)
- [ ] Cassandra Database
- [ ] Client Side Library
- [ ] Spring Boot Admin (Not compatible with Brixton)
