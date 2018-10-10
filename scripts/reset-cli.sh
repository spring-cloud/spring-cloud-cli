#!/usr/bin/env bash

desired_spring_boot_cli_version="$(cat target/boot_cli_version)"
desired_spring_cloud_cli_version="$(cat target/cloud_cli_version)"

source ~/.sdkman/bin/sdkman-init.sh

echo "Use Boot CLI in version ${desired_spring_boot_cli_version}"
sdk use springboot "${desired_spring_boot_cli_version}"
echo "Use Cloud CLI in version ${desired_spring_cloud_cli_version}"
spring install org.springframework.cloud:spring-cloud-cli:"${desired_spring_cloud_cli_version}"