package org.springframework.cloud.launcher.kafka;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeployerApplicationTests {

	@BeforeClass
	public static void before() {
		System.setProperty("kafka.port", String.valueOf(SocketUtils.findAvailableTcpPort()));
		System.setProperty("zk.port", String.valueOf(SocketUtils.findAvailableTcpPort()));
	}

	@AfterClass
	public static void after() {
		System.clearProperty("kafka.port");
		System.clearProperty("zk.port");
	}

	@Test
	public void contextLoads() {
	}

}
