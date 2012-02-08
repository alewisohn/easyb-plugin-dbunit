/**
 * 
 */
package org.easyb.plugin.dbunit

import groovy.lang.Closure;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager
import java.sql.SQLException;

import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.operation.DatabaseOperation;
import org.easyb.plugin.BasePlugin;

/**
 * 
 *
 * @author Andrew Lewisohn
 */
class DBUnitPlugin extends BasePlugin {

	/**
	 * The default database operation to be performed.
	 */
	private static final DatabaseOperation DEFAULT_DATABASE_OPERATION = DatabaseOperation.CLEAN_INSERT
	
	def void database_model(final Connection connection, final Closure dataSet) {
		doDatabaseOperation(new DatabaseConnection(connection), dataSet, DEFAULT_DATABASE_OPERATION)
	}
	
	def void database_model(final Connection connection, final DatabaseOperation operation, final Closure dataSet) {
		doDatabaseOperation(new DatabaseConnection(connection), dataSet, operation)
	}
	
	def void database_model(final Connection connection, final DatabaseConfig config, final Closure dataSet) {
		final IDatabaseConnection conn = new DatabaseConnection(connection)
		doDatabaseConfiguration(conn, config)
		doDatabaseOperation(conn, dataSet, DEFAULT_DATABASE_OPERATION)
	}
	
	def void database_model(final Connection connection, final DatabaseOperation operation, final DatabaseConfig config, final Closure dataSet) {
		final IDatabaseConnection conn = new DatabaseConnection(connection)
		doDatabaseConfiguration(conn, config)
		doDatabaseOperation(new DatabaseConnection(connection), dataSet, operation)
	}

	def void database_model(final String driver, final String url, final String user, final String password, final Closure dataSet) throws Throwable {
		final IDatabaseConnection conn = getConnection(driver, url, user, password)
		doDatabaseOperation(conn, dataSet, DEFAULT_DATABASE_OPERATION)
	}

	def void database_model(final String driver, final String url, final String user, final String password, final DatabaseOperation operation, final Closure dataSet) throws Throwable {
		final IDatabaseConnection conn = getConnection(driver, url, user, password)
		doDatabaseOperation(conn, dataSet, operation)
	}
	
	def void database_model(final String driver, final String url, final String user, final String password, final DatabaseConfig config, final Closure dataSet) throws Throwable {
		final IDatabaseConnection conn = getConnection(driver, url, user, password)
		doDatabaseConfiguration(conn, config)
		doDatabaseOperation(conn, dataSet, DEFAULT_DATABASE_OPERATION)
	}
	
	def void database_model(final String driver, final String url, final String user, final String password, final DatabaseOperation operation, final DatabaseConfig config, final Closure dataSet) throws Throwable {
		final IDatabaseConnection conn = getConnection(driver, url, user, password)
		doDatabaseConfiguration(conn, config)
		doDatabaseOperation(conn, dataSet, operation)
	}
	
	private void doDatabaseConfiguration(final IDatabaseConnection conn, final DatabaseConfig config) {
		config._propertyMap.each { key, value ->
			conn.config.setProperty(key, value)
		}
	}
	
	private void doDatabaseOperation(final IDatabaseConnection conn, final Closure dataSet, final DatabaseOperation operation) {
		try {
			operation.execute(conn, getDataSet(dataSet))
		} catch(e) {
			println e.message
		} finally {
			conn.close()
		}
	}

	def IDatabaseConnection getConnection(final String driver, final String url, final String user, final String pssword) throws ClassNotFoundException, SQLException {
		Class.forName(driver)
		return new DatabaseConnection(DriverManager.getConnection(url, user, pssword))
	}

	private IDataSet getDataSet(final Closure dataSet) throws IOException, DataSetException {
		def result = dataSet.call()
		if (result instanceof IDataSet) {
			return result
		} else {
			final String datastr = (String) result
			return new FlatXmlDataSet(new ByteArrayInputStream(datastr.getBytes()))
		} 
	}

	def String getName() {
		return "dbunit"
	}
}
