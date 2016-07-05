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

import javax.sql.DataSource;

import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchDatabaseInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;
import org.springframework.cloud.task.repository.support.TaskRepositoryInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

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

	@Bean
	public JobRepositoryFactoryBean jobRepositoryFactoryBeanForServer(
			DataSource dataSource,
			DataSourceTransactionManager dataSourceTransactionManager) {
		JobRepositoryFactoryBean repositoryFactoryBean = new JobRepositoryFactoryBean();
		repositoryFactoryBean.setDataSource(dataSource);
		repositoryFactoryBean.setTransactionManager(dataSourceTransactionManager);
		return repositoryFactoryBean;
	}

	@Bean
	public DataSourceTransactionManager transactionManagerForServer(
			DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public BatchDatabaseInitializer batchRepositoryInitializerForDefaultDBForServer() {
		return new BatchDatabaseInitializer();
	}

	@Bean
	public TaskRepositoryInitializer taskRepositoryInitializerForDefaultDB(
			DataSource dataSource) {
		TaskRepositoryInitializer taskRepositoryInitializer = new TaskRepositoryInitializer();
		taskRepositoryInitializer.setDataSource(dataSource);
		return taskRepositoryInitializer;
	}
}
