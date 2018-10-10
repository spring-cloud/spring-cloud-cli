#!/usr/bin/env bash

desired_spring_boot_cli_version="${1}"
desired_spring_cloud_cli_version="${2}"

spring_installed="false"
spring --version && echo "Spring CLI installed" && spring_installed="true" || echo "No Spring Installed"

source ~/.sdkman/bin/sdkman-init.sh
if [[ "${spring_installed}" == "false" ]]; then
	sdk install springboot "${desired_spring_boot_cli_version}"
fi

current_spring_cli_version="$(spring --version | cut -d' ' -f3 | cut -d'v' -f 2)"
current_spring_cloud_version="$(spring cloud --version | cut -d' ' -f4 | cut -d'v' -f 2)"

mkdir -p target
echo "${current_spring_cli_version}" > target/boot_cli_version
echo "${current_spring_cloud_version}" > target/cloud_cli_version

case "${desired_spring_boot_cli_version}" in
  *RELEASE*)
    LOCATION="https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/${desired_spring_boot_cli_version}/spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
    ;;
  *SR*)
    LOCATION="https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/${desired_spring_boot_cli_version}/spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
    ;;
  *M*)
    LOCATION="https://repo.spring.io/milestone/org/springframework/boot/spring-boot-cli/${desired_spring_boot_cli_version}/spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
    ;;
  *BUILD-SNAPSHOT*)
    LOCATION="https://repo.spring.io/snapshot/org/springframework/boot/spring-boot-cli/${desired_spring_boot_cli_version}/spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
    ;;
  *)
    LOCATION="https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/${desired_spring_boot_cli_version}/spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
    ;;
esac

echo "Downloading [${desired_spring_boot_cli_version}] version of Spring Boot CLI"
wget -O target/"spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip" "${LOCATION}"
pushd target
	unzip "spring-boot-cli-${desired_spring_boot_cli_version}-bin.zip"
popd
echo "Removing current Spring Boot CLI sc-cli-dev installation"
rm -rf ~/.sdkman/candidates/springboot/sc-cli-dev
echo "Installing [${desired_spring_boot_cli_version}] version of Spring Boot CLI"
yes | sdk install springboot sc-cli-dev "target/spring-${desired_spring_boot_cli_version}/"
echo "Use Boot CLI in version ${desired_spring_boot_cli_version}"
yes | sdk use springboot sc-cli-dev
echo "Use Cloud CLI in version ${desired_spring_cloud_cli_version}"
spring install org.springframework.cloud:spring-cloud-cli:"${desired_spring_cloud_cli_version}"