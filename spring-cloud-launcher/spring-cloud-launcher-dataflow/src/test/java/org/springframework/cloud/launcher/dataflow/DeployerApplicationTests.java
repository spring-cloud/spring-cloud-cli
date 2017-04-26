package org.springframework.cloud.launcher.dataflow;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest({ "spring.cloud.bus.enabled=false", "eureka.client.enabled=false" })
public class DeployerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
