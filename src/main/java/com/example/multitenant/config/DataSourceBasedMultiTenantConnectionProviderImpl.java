package com.example.multitenant.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {


	private static final String DEFAULT_SCHEMA = "default";

	private static final long serialVersionUID = 1L;

	    @Value("${spring.datasource.url}")
	    private String baseUrl;

	    @Value("${spring.datasource.username}")
	    private String username;

	    @Value("${spring.datasource.password}")
	    private String password;

	    @Value("${spring.datasource.driver-class-name}")
	    private String driverClassName;
	    
	    @Value("${spring.datasource.defaultschema}")
	    private String defaultSchema;

	// 🧠 Cache de DataSources já criados
	private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

	/**
	 * Retorna um DataSource genérico (core ou default)
	 */
	public DataSource selectAnyDataSource() {
		return getOrCreateDataSource(defaultSchema); // pode ser o schema padrão
	}

	/**
	 * Retorna o DataSource específico do tenant
	 */
	public DataSource selectDataSource(String tenantIdentifier) {
		return getOrCreateDataSource(tenantIdentifier);
	}

	/**
	 * Obtém o DataSource existente no cache ou cria um novo.
	 */
	private DataSource getOrCreateDataSource(String tenantIdentifier) {
		return dataSources.computeIfAbsent(tenantIdentifier, this::createDataSourceForTenant);
	}

	/**
	 * Cria dinamicamente um DataSource (Hikari) para o schema do tenant.
	 */
	private DataSource createDataSourceForTenant(String tenant) {
		try {
			// Clona as configurações do datasource base
			HikariConfig config = new HikariConfig();

			config.setDriverClassName(driverClassName);

			// Substitui apenas o schema (último segmento da URL)
			String tenantUrl = replaceSchemaInUrl(baseUrl, tenant);
			config.setJdbcUrl(tenantUrl);

			config.setUsername(username);
			config.setPassword(password);

			// 🔧 Parâmetros de performance recomendados
			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);
			config.setPoolName("HikariPool-" + tenant);
			config.setIdleTimeout(30000);
			config.setConnectionTimeout(20000);
			config.setLeakDetectionThreshold(60000);

			HikariDataSource dataSource = new HikariDataSource(config);
			System.out.println("✅ DataSource criado para tenant: " + tenant);
			return dataSource;

		} catch (Exception e) {
			throw new RuntimeException("❌ Erro ao criar DataSource para tenant: " + tenant, e);
		}
	}

	/**
	 * Utilitário para trocar o schema na URL base.
	 */
	private String replaceSchemaInUrl(String baseUrl, String schema) {
		// Exemplo: jdbc:mysql://localhost:3306/core_db →
		// jdbc:mysql://localhost:3306/{schema}
		return baseUrl.replaceAll("/[^/]+\\?(.*)", "/" + schema + "?$1").replaceAll("/[^/]+$", "/" + schema);
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return selectAnyDataSource().getConnection();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		return selectDataSource(tenantIdentifier).getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return MultiTenantConnectionProvider.class.equals(unwrapType)
				|| DataSourceBasedMultiTenantConnectionProviderImpl.class.isAssignableFrom(unwrapType);
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		if (isUnwrappableAs(unwrapType)) {
			return (T) this;
		} else {
			throw new IllegalArgumentException("Não é possível desembrulhar para: " + unwrapType);
		}
	}

}
