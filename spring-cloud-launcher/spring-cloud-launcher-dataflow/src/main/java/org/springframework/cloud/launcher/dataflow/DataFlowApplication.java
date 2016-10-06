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

package org.springframework.cloud.launcher.dataflow;

import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchDatabaseInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;
import org.springframework.cloud.dataflow.server.config.features.FeaturesProperties;
import org.springframework.cloud.dataflow.server.repository.RdbmsTaskDefinitionRepository;
import org.springframework.cloud.dataflow.server.repository.TaskDefinitionRepository;
import org.springframework.cloud.dataflow.server.repository.support.DataflowRdbmsInitializer;
import org.springframework.cloud.task.repository.support.TaskRepositoryInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author Spencer Gibb
 */
@EnableDataFlowServer
@EnableDiscoveryClient
@SpringBootApplication
public class DataFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataFlowApplication.class, args);
	}

	/*
	 * Special case for local H2 with tcp, but not in memory. Create a load of beans that
	 * dataflow doesn't in this case, but probably should (see
	 * https://github.com/spring-cloud/spring-cloud-dataflow/issues/926).
	 */
	@Configuration
	@ConditionalOnExpression("#{'${spring.datasource.url:}'.startsWith('jdbc:h2:tcp://localhost:') && !'${spring.datasource.url:}'.contains('/mem:')}")
	public static class H2ServerConfiguration {

		@Bean
		public JobRepositoryFactoryBean jobRepositoryFactoryBean(DataSource dataSource,
				DataSourceTransactionManager dataSourceTransactionManager) {
			JobRepositoryFactoryBean repositoryFactoryBean = new JobRepositoryFactoryBean();
			repositoryFactoryBean.setDataSource(dataSource);
			repositoryFactoryBean.setTransactionManager(dataSourceTransactionManager);
			return repositoryFactoryBean;
		}

		@Bean
		public BatchDatabaseInitializer batchRepositoryInitializer(
				DataSource dataSource) {
			return new BatchDatabaseInitializer();
		}

		@Bean
		public TaskRepositoryInitializer taskRepositoryInitializer(
				DataSource dataSource) {
			TaskRepositoryInitializer taskRepositoryInitializer = new TaskRepositoryInitializer();
			taskRepositoryInitializer.setDataSource(dataSource);
			return taskRepositoryInitializer;
		}

		@Bean
		@ConditionalOnMissingBean
		public TaskDefinitionRepository taskDefinitionRepository(DataSource dataSource)
				throws Exception {
			return new RdbmsTaskDefinitionRepository(dataSource);
		}

		@Bean
		public DataSourceTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		public DataflowRdbmsInitializer dataflowRdbmsInitializer(DataSource dataSource,
				FeaturesProperties featuresProperties) {
			DataflowRdbmsInitializer dataflowRdbmsInitializer = new DataflowRdbmsInitializer(
					featuresProperties);
			dataflowRdbmsInitializer.setDataSource(dataSource);
			return dataflowRdbmsInitializer;
		}
	}

}
