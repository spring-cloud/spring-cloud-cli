Spring Boot command line features for
[Spring Cloud](https://github.com/spring-cloud).  To install, make
sure you have
[Spring Boot CLI](https://github.com/spring-projects/spring-boot)
(1.1.x with x>=5):

    $ spring version
    Spring CLI v1.1.5.RELEASE

Then build the jar file in this project and copy it to the lib
directory in your CLI. E.g. for GVM users

    $ mvn install
    $ cp spring-cloud-cli/target/*.jar ~/.gvm/springboot/1.1.5.RELEASE/lib
