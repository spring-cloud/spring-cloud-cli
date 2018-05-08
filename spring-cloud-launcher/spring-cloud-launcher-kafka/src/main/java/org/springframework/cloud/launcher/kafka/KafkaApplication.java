/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.launcher.kafka;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.common.utils.Utils;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.server.NotRunning;
import kafka.utils.CoreUtils;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;

/**
 * @author Spencer Gibb
 *
 * see https://github.com/spring-projects/spring-kafka/blob/2.0.x/spring-kafka-test/src/main/java/org/springframework/kafka/test/rule/KafkaEmbedded.java
 */
@SpringBootApplication
public class KafkaApplication {

	private static final Log log = LogFactory.getLog(KafkaApplication.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(KafkaApplication.class).run(args);
	}

	@Service
	static class KafkaDevServer implements SmartLifecycle {
		private AtomicBoolean running = new AtomicBoolean(false);
		private ZkClient zkClient;

		private EmbeddedZookeeper zookeeper;

		private KafkaServer kafkaServer;

		@Value("${kafka.port:${KAFKA_PORT:9092}}")
		private int port;

		@Value("${zk.port:${ZK_PORT:2181}}")
		private int zkPort;

		@Override
		public boolean isAutoStartup() {
			return true;
		}

		@Override
		public void stop(Runnable callback) {
			stop();
			callback.run();
		}

		@Override
		public void start() {
			if (this.running.compareAndSet(false, true)) {
				try {
					log.info("Starting Zookeeper");
					this.zookeeper = new EmbeddedZookeeper(this.zkPort);
					String zkConnectString = "127.0.0.1:" + this.zookeeper.getPort();
					log.info("Started Zookeeper at " + zkConnectString);
					try {
						int zkConnectionTimeout = 10000;
						int zkSessionTimeout = 10000;
						zkClient = new ZkClient(zkConnectString, zkSessionTimeout,
								zkConnectionTimeout, ZKStringSerializer$.MODULE$);
					}
					catch (Exception e) {
						zookeeper.shutdown();
						throw e;
					}
					try {
						log.info("Creating Kafka server");
						// TODO: move to properties?
						int nodeId = 0;
						boolean enableControlledShutdown = true;
						Properties brokerConfigProperties = TestUtils.createBrokerConfig(
								nodeId, zkConnectString, enableControlledShutdown, true,
								port, scala.Option.apply(null),
								scala.Option.apply(null),
								scala.Option.apply(null), true, false, 0,
								false, 0, false, 0, scala.Option.apply(null), 1);
						brokerConfigProperties.setProperty("replica.socket.timeout.ms",
								"1000");
						brokerConfigProperties.setProperty("controller.socket.timeout.ms",
								"1000");
						brokerConfigProperties
								.setProperty("offsets.topic.replication.factor", "1");
						brokerConfigProperties.put("zookeeper.connect", zkConnectString);
						kafkaServer = TestUtils.createServer(
								new KafkaConfig(brokerConfigProperties),
								Time.SYSTEM);
						log.info("Created Kafka server at "
								+ kafkaServer.config().hostName() + ":"
								+ kafkaServer.config().port());
					}
					catch (Exception e) {
						zookeeper.shutdown();
						zkClient.close();
						throw e;
					}
				}
				catch (Exception e) {
					ReflectionUtils.rethrowRuntimeException(e);
				}
			}
		}

		@Override
		public void stop() {
			if (this.running.compareAndSet(true, false)) {
				log.info("Stopping Kafka");
				try {
					if (kafkaServer.brokerState()
							.currentState() != (NotRunning.state())) {
						kafkaServer.shutdown();
						kafkaServer.awaitShutdown();
					}
				}
				catch (Exception e) {
					// do nothing
				}
				try {
					CoreUtils.delete(kafkaServer.config().logDirs());
				}
				catch (Exception e) {
					// do nothing
				}
				log.info("Stopping Zookeeper");
				try {
					this.zkClient.close();
				}
				catch (ZkInterruptedException e) {
					// do nothing
				}
				try {
					this.zookeeper.shutdown();
				}
				catch (Exception e) {
					// do nothing
				}
			}
		}

		@Override
		public boolean isRunning() {
			return this.running.get();
		}

		@Override
		public int getPhase() {
			return 0;
		}
	}

	static class EmbeddedZookeeper {
		private File snapshotDir = TestUtils.tempDir();
		private File logDir = TestUtils.tempDir();
		private int tickTime = 500;
		private NIOServerCnxnFactory factory = new NIOServerCnxnFactory();
		private ZooKeeperServer zookeeper;
		private InetSocketAddress addr;
		private int port;

		public EmbeddedZookeeper(int port) throws Exception {
			this.port = port;
			zookeeper = new ZooKeeperServer(snapshotDir, logDir, tickTime);
			addr = new InetSocketAddress("127.0.0.1", port);
			factory.configure(addr, 0);
			factory.startup(zookeeper);
		}

		public int getPort() {
			return port;
		}

		void shutdown() throws Exception {
			zookeeper.shutdown();
			factory.shutdown();
			Utils.delete(logDir);
			Utils.delete(snapshotDir);
		}
	}
}
