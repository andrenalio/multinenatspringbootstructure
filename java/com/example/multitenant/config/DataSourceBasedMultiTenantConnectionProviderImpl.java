package com.example.multitenant.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.hibernate.engine.jdbc.connections.internal.MultiTenantConnectionProviderInitiator;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator.MultiTenantConnectionProviderJdbcConnectionAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl  implements  MultiTenantConnectionProvider<String>   {

	private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @Override
    public Connection getAnyConnection() throws SQLException {
        // Retorna conexão do tenant "default"
        return selectDataSource("default").getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        DataSource dataSource = selectDataSource(tenantIdentifier);
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    public DataSource selectDataSource(String tenantIdentifier) {
        return dataSources.computeIfAbsent(tenantIdentifier, this::createDataSourceForTenant);
    }

    private DataSource createDataSourceForTenant(String tenantIdentifier) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://database-1.cxoyu2sswdh5.sa-east-1.rds.amazonaws.com:3306/" + tenantIdentifier + "?useSSL=false&serverTimezone=UTC");
        ds.setUsername("admin");
        ds.setPassword("praxium123");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }
    

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    // Métodos default exigidos pelo Hibernate 6
    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

}
