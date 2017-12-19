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

package org.springframework.cloud.launcher.zipkin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotatedTypeMetadata;
import zipkin.collector.CollectorMetrics;
import zipkin.collector.CollectorSampler;
import zipkin.internal.V2StorageComponent;
import zipkin.server.ZipkinQueryApiV1;
import zipkin.server.ZipkinServerConfiguration;
import zipkin.server.brave.BraveConfiguration;
import zipkin.storage.StorageComponent;
import zipkin2.storage.InMemoryStorage;

/**
 * @author Spencer Gibb
 */
// @EnableZipkinServer
@Import({ZipkinServerConfiguration.class, BraveConfiguration.class, ZipkinQueryApiV1.class})
@EnableDiscoveryClient
@SpringBootApplication
public class ZipkinServerApplication {


	@Bean
	@ConditionalOnMissingBean({CollectorSampler.class})
	CollectorSampler traceIdSampler(@Value("${zipkin.collector.sample-rate:1.0}") float rate) {
		return CollectorSampler.create(rate);
	}

	@Bean
	CollectorMetrics metrics() {
		return CollectorMetrics.NOOP_METRICS;
	}

	static final class StorageTypeMemAbsentOrEmpty implements Condition {
		StorageTypeMemAbsentOrEmpty() {
		}

		public boolean matches(ConditionContext condition, AnnotatedTypeMetadata ignored) {
			String storageType = condition.getEnvironment().getProperty("zipkin.storage.type");
			if (storageType == null) {
				return true;
			} else {
				storageType = storageType.trim();
				return storageType.isEmpty() ? true : storageType.equals("mem");
			}
		}
	}

	@Configuration
	@Conditional({ZipkinServerApplication.StorageTypeMemAbsentOrEmpty.class})
	@ConditionalOnMissingBean({StorageComponent.class})
	static class InMemoryConfiguration {
		InMemoryConfiguration() {
		}

		@Bean
		StorageComponent storage(@Value("${zipkin.storage.strict-trace-id:true}") boolean strictTraceId, @Value("${zipkin.storage.mem.max-spans:500000}") int maxSpans) {
			return V2StorageComponent.create(InMemoryStorage.newBuilder().strictTraceId(strictTraceId).maxSpanCount(maxSpans).build());
		}

		@Bean
		InMemoryStorage v2Storage(V2StorageComponent component) {
			return (InMemoryStorage)component.delegate();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ZipkinServerApplication.class, args);
	}
}
