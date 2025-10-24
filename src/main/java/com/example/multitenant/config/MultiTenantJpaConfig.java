package com.example.multitenant.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class MultiTenantJpaConfig {

	private static final String BASE_PATH = "com.example.multitenant.tenantmodel";

	@Autowired
	private DataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider;

	@Autowired
	private CurrentTenantIdentifierResolverImpl tenantIdentifierResolver;

	@Autowired
	private JpaProperties jpaProperties;

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setPackagesToScan(BASE_PATH);
		emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Map<String, Object> props = new HashMap<>(jpaProperties.getProperties());
		props.put("hibernate.multiTenancy", "SCHEMA");
		props.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
		props.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
		props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.put("hibernate.show_sql", true);
		props.put("hibernate.format_sql", true);

		emf.setJpaPropertyMap(props);
		return emf;
	}
}
