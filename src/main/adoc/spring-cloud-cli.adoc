Spring Boot command line features for
[Spring Cloud](https://github.com/spring-cloud).  To install, make
sure you have
[Spring Boot CLI](https://github.com/spring-projects/spring-boot)
(1.1.x with x>=5):

    $ spring version
    Spring CLI v1.1.5.RELEASE

E.g. for GVM users

```
$ gvm install springboot 1.1.5.RELEASE
$ gvm use springboot 1.1.5.RELEASE
```

then get the install command plugin (backported from Boot 1.2.0):

```
$ wget http://dl.bintray.com/dsyer/generic/install-0.0.1.jar
```

install it in the Spring Boot CLI, e.g. with GVM (MacOS users that rely on brew might have to find the `/lib` directory by scanning `brew info springboot`):

```
$ cp install-0.0.1.jar ~/.gvm/springboot/1.1.5.RELEASE/lib
```

and finally install the Spring Cloud plugin:

```
$ mvn install
$ spring install org.springframework.cloud:spring-cloud-cli:1.0.0.BUILD-SNAPSHOT
```
