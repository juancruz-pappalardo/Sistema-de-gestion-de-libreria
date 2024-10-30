import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class InterfazUsuario {
    private JFrame frame;
    private JTextArea textAreaLibros;
    private JTextArea textAreaUsuarios;
    private Connection connection;

    public InterfazUsuario() {
        frame = new JFrame("Registro de Biblioteca");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(1, 2));

        textAreaLibros = new JTextArea();
        JScrollPane scrollPaneLibros = new JScrollPane(textAreaLibros);
        JButton cargarLibroButton = new JButton("Cargar Libro");

        cargarLibroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cargarLibro();
            }
        });

        JButton devolverLibroButton = new JButton("Devolver Libro");

        devolverLibroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                devolverLibro();
            }
        });

        JPanel librosPanel = new JPanel(new BorderLayout());
        JPanel librosButtonPanel = new JPanel(new GridLayout(1, 2));
        librosButtonPanel.add(cargarLibroButton);
        librosButtonPanel.add(devolverLibroButton);
        librosPanel.add(librosButtonPanel, BorderLayout.NORTH);
        librosPanel.add(scrollPaneLibros, BorderLayout.CENTER);
        librosPanel.setBorder(BorderFactory.createTitledBorder("Libros"));

        textAreaUsuarios = new JTextArea();
        JScrollPane scrollPaneUsuarios = new JScrollPane(textAreaUsuarios);
        JButton cargarUsuarioButton = new JButton("Cargar Usuario");

        cargarUsuarioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cargarUsuario();
            }
        });

        JButton prestarLibroButton = new JButton("Prestar Libro");

        prestarLibroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prestarLibro();
            }
        });

        JPanel usuariosPanel = new JPanel(new BorderLayout());
        JPanel usuariosButtonPanel = new JPanel(new GridLayout(1, 2));
        usuariosButtonPanel.add(cargarUsuarioButton);
        usuariosButtonPanel.add(prestarLibroButton);
        usuariosPanel.add(usuariosButtonPanel, BorderLayout.NORTH);
        usuariosPanel.add(scrollPaneUsuarios, BorderLayout.CENTER);
        usuariosPanel.setBorder(BorderFactory.createTitledBorder("Usuarios"));

        panel.add(librosPanel);
        panel.add(usuariosPanel);

        frame.add(panel);
        frame.setVisible(true);

        // Conectar a la base de datos SQLite
        connection = ConexionSQLite.conectar();
        if (connection == null) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos.");
            System.exit(1);
        }
        System.out.println("Conexión a la base de datos establecida.");

        // Mostrar libros y usuarios al iniciar la aplicación
        mostrarLibros();
        mostrarUsuarios();
    }

    public void mostrarLibros() {
        textAreaLibros.setText("");
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM libros");
            while (resultSet.next()) {
                String titulo = resultSet.getString("titulo");
                String autor = resultSet.getString("autor");
                boolean prestado = resultSet.getBoolean("prestado");
                String estadoPrestamo = "";
                if (prestado) {
                    int personaId = resultSet.getInt("persona_id");
                    String nombrePersona = obtenerNombrePersona(personaId);
                    estadoPrestamo = "Prestado a: " + nombrePersona;
                } else {
                    estadoPrestamo = "Disponible";
                }
                textAreaLibros.append("Título: " + titulo + ", Autor: " + autor + ", Estado: " + estadoPrestamo + "\n");
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar libros: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    private String obtenerNombrePersona(int personaId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT nombre FROM personas WHERE id = ?");
        statement.setInt(1, personaId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("nombre");
        } else {
            return "Desconocido";
        }
    }

    public void cargarLibro() {
        String titulo = JOptionPane.showInputDialog("Ingrese el título del libro:");
        String autor = JOptionPane.showInputDialog("Ingrese el autor del libro:");
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO libros (titulo, autor, prestado) VALUES (?, ?, 0)");
            statement.setString(1, titulo);
            statement.setString(2, autor);
            statement.executeUpdate();
            System.out.println("Libro insertado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al insertar libro: " + e.getMessage());
        }
        mostrarLibros();
    }

    public void cargarUsuario() {
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del usuario:");
        String apellido = JOptionPane.showInputDialog("Ingrese el apellido del usuario:");
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO personas (nombre, apellido) VALUES (?, ?)");
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.executeUpdate();
            System.out.println("Usuario insertado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al insertar usuario: " + e.getMessage());
        }
        mostrarUsuarios();
    }


    public void prestarLibro() {
        String tituloLibro = JOptionPane.showInputDialog("Ingrese el título del libro a prestar:");
        String nombreUsuario = JOptionPane.showInputDialog("Ingrese el nombre del usuario que presta el libro:");
        try {
            // Obtener el ID del libro
            PreparedStatement statementLibro = connection.prepareStatement("SELECT id FROM libros WHERE titulo = ?");
            statementLibro.setString(1, tituloLibro);
            ResultSet resultSetLibro = statementLibro.executeQuery();
            int idLibro = resultSetLibro.getInt("id");

            // Obtener el ID del usuario
            PreparedStatement statementUsuario = connection.prepareStatement("SELECT id FROM personas WHERE nombre = ?");
            statementUsuario.setString(1, nombreUsuario);
            ResultSet resultSetUsuario = statementUsuario.executeQuery();
            int idUsuario = resultSetUsuario.getInt("id");

            // Actualizar el estado de préstamo del libro y registrar la persona que lo prestó
            PreparedStatement statementPrestamo = connection.prepareStatement("UPDATE libros SET prestado = 1, persona_id = ? WHERE id = ?");
            statementPrestamo.setInt(1, idUsuario);
            statementPrestamo.setInt(2, idLibro);
            statementPrestamo.executeUpdate();

            System.out.println("Libro prestado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al prestar libro: " + e.getMessage());
        }
        mostrarLibros();
    }

    public void devolverLibro() {
        String tituloLibro = JOptionPane.showInputDialog("Ingrese el título del libro a devolver:");
        try {
            // Actualizar el estado de préstamo del libro y eliminar la referencia al usuario que lo prestó
            PreparedStatement statementPrestamo = connection.prepareStatement("UPDATE libros SET prestado = 0, persona_id = NULL WHERE titulo = ?");
            statementPrestamo.setString(1, tituloLibro);
            int rowsAffected = statementPrestamo.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Libro devuelto correctamente.");
            } else {
                System.out.println("No se encontró el libro prestado con el título proporcionado.");
            }
        } catch (SQLException e) {
            System.out.println("Error al devolver libro: " + e.getMessage());
        }
        mostrarLibros();
    }

    public void mostrarUsuarios() {
        textAreaUsuarios.setText("");
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM personas");
            while (resultSet.next()) {
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");
                textAreaUsuarios.append("Nombre: " + nombre + ", Apellido: " + apellido + "\n");
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar usuarios: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }


    static class ConexionSQLite {
        public static Connection conectar() {
            Connection connection = null;
            try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:sistema.db";
                connection = DriverManager.getConnection(url);
                System.out.println("Conexión a la base de datos establecida.");
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Error al conectar a la base de datos: " + e.getMessage());
            }
            return connection;
        }
    }

    public static void main(String[] args) {
        new InterfazUsuario();
    }
}

