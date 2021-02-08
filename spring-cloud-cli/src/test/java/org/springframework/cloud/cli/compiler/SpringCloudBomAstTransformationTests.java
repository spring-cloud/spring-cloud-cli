package org.springframework.cloud.cli.compiler;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringCloudBomAstTransformationTests {

	@Test
	public void defaultVersionReadFromFile() {
		String version = new SpringCloudBomAstTransformation().getBomVersion();
		// starts with one or more word chars then a .
		// TODO: change to \\d (digits) when merged to master
		assertThat(version).isNotBlank().doesNotContainAnyWhitespaces().containsPattern("^\\w+\\..*");
	}
}
