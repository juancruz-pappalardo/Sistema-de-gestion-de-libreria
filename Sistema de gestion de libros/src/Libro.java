

import java.sql.*;

public class Libro {
    private int id;
    private String titulo;
    private String autor;
    private boolean prestado;
    private int personaPrestamoId;

    public Libro(String titulo, String autor) {
        this.titulo = titulo;
        this.autor = autor;
        this.prestado = false;
        this.personaPrestamoId = -1; // Por defecto, ningún usuario tiene prestado este libro
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public boolean estaPrestado() {
        return prestado;
    }

    public void setPrestado(boolean prestado) {
        this.prestado = prestado;
    }

    public int getPersonaPrestamoId() {
        return personaPrestamoId;
    }

    public void setPersonaPrestamoId(int personaPrestamoId) {
        this.personaPrestamoId = personaPrestamoId;
    }

    public void insertarEnBaseDeDatos(Connection connection) throws SQLException {
        String query = "INSERT INTO libros (titulo, autor, prestado, persona_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, titulo);
            statement.setString(2, autor);
            statement.setBoolean(3, prestado);
            statement.setInt(4, personaPrestamoId);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            } else {
                throw new SQLException("No se pudieron obtener las claves generadas para el libro.");
            }
        }
    }
}
