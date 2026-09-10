package com.primera_app_spring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Debe obtener una conexión válida del pool HikariCP")
    void testConexionBaseDatos() throws SQLException {
        // Verifica que Spring ha inyectado el DataSource correctamente
        assertNotNull(dataSource, "El DataSource no debería ser nulo");

        // Solicita una conexión al pool HikariCP
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "La conexión a la BD no debería ser nula");
            assertTrue(connection.isValid(2), "La conexión a MySQL debería responder en menos de 2 segundos");
            assertEquals("MySQL", connection.getMetaData().getDatabaseProductName(), "El motor debe ser MySQL");
        }
    }
}