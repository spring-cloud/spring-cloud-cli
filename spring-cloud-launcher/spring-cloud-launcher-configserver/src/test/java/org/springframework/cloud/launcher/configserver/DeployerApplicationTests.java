package org.springframework.cloud.launcher.configserver;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest({ "spring.cloud.config.server.git.uri=file:./target",
		"spring.cloud.bus.enabled=false" })
public class DeployerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
