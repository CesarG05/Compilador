package Compilador;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Element;

public class Compilador_Main {
    
    private JFrame frameComp; // Ventana principal del compilador
    private JTable tablaErrores; // Tabla para mostrar errores semánticos
    private JTable tablaSimbolos; // Tabla para mostrar los símbolos y sus tipos
    private JTextPane textPane; // Área de texto donde se ingresa el código fuente
    private JTextArea lineNumbers; // Área de texto para mostrar los números de línea
    
    private Analizador_Semantico analizador_Semantico; // Instancia del analizador semántico
    //private Triplo triplo; // Instancia del analizador semántico
    
    private JTable tablaTriplo;

    // Constructor que inicializa la ventana principal y el analizador semántico
    public Compilador_Main() {
        inicializar_Frame(); // Configurar la interfaz gráfica
        analizador_Semantico = new Analizador_Semantico(tablaErrores); // Inicializar el analizador semántico
    }

    // Método para inicializar la ventana principal y sus componentes
    private void inicializar_Frame() {
        frameComp = new JFrame();
        frameComp.setTitle("Compilador EQ12");
        frameComp.setBounds(200, 200, 1052, 505); // Posición y tamaño de la ventana
        frameComp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Panel para contener los botones
        JPanel BotonPanel = new JPanel();
        BotonPanel.setBounds(0, 0, 1250, 46);
        BotonPanel.setBackground(new Color(0, 128, 255)); // Color de fondo del panel
        frameComp.getContentPane().setLayout(null);
        BotonPanel.setLayout(null);
        frameComp.getContentPane().add(BotonPanel);
        
        // Botón para compilar el código
        JButton btnCompilar = new JButton("Compilar");
        btnCompilar.setBounds(47, 11, 89, 23); // Posición y tamaño del botón
        BotonPanel.add(btnCompilar);
        
        // Botón para generar triplo
        JButton btnTriplo = new JButton("Generar Triplo");
        btnTriplo.setBounds(146, 11, 130, 23); // Posición y tamaño del botón
        BotonPanel.add(btnTriplo);
        
        // Botón para optimizar codigo
        JButton btnOptimizar = new JButton("Optimizar");
        btnOptimizar.setBounds(290, 11, 89, 23);
        BotonPanel.add(btnOptimizar);
        
        // Botón para guardar el código en un archivo
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(515, 11, 89, 23);
        BotonPanel.add(btnGuardar);
        
        // Botón para abrir un archivo con código
        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.setBounds(614, 11, 89, 23);
        BotonPanel.add(btnAbrir);
        
        // JTextPane para ingresar y mostrar el código fuente
        textPane = new JTextPane();
        JScrollPane codePane = new JScrollPane(textPane);
        codePane.setBounds(10, 57, 494, 213); // Posición y tamaño del área de código
        frameComp.getContentPane().add(codePane);
        
        // JTextArea para los números de línea
        lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(Color.LIGHT_GRAY); // Fondo gris para los números de línea
        lineNumbers.setEditable(false); // No se puede editar
        codePane.setRowHeaderView(lineNumbers); // Mostrar los números de línea en el área de código
        
        // Detectar cambios en el JTextPane para actualizar los números de línea
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            // Método para obtener el texto con los números de línea
            public String getText() {
                int caretPosition = textPane.getDocument().getLength(); // Posición del cursor
                Element root = textPane.getDocument().getDefaultRootElement();
                StringBuilder text = new StringBuilder("1\n");
                for (int i = 2; i < root.getElementIndex(caretPosition) + 2; i++) {
                    text.append(i).append("\n"); // Añadir números de línea
                }
                return text.toString();
            }
           
            @Override
            public void changedUpdate(DocumentEvent e) {
                lineNumbers.setText(getText()); // Actualizar los números de línea
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                lineNumbers.setText(getText()); // Actualizar los números de línea
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                lineNumbers.setText(getText()); // Actualizar los números de línea
            }
        });
        
        // JScrollPane para la tabla de símbolos
        JScrollPane simbolosPane = new JScrollPane();
        simbolosPane.setBounds(514, 57, 200, 403);
        frameComp.getContentPane().add(simbolosPane);
        
        // JTable para mostrar los símbolos y sus tipos de datos
        tablaSimbolos = new JTable();
        tablaSimbolos.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] { "Lexema", "Tipo de Dato" } // Columnas de la tabla
        ));
        simbolosPane.setViewportView(tablaSimbolos);
        
        // JScrollPane para la tabla de errores
        JScrollPane erroresPane = new JScrollPane();
        erroresPane.setBounds(10, 281, 494, 179);
        frameComp.getContentPane().add(erroresPane);
        
        // JTable para mostrar los errores semánticos
        tablaErrores = new JTable();
        tablaErrores.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] { "Token", "Lexema", "Renglon", "Descripcion" } // Columnas de la tabla
        ));
        tablaErrores.getColumnModel().getColumn(2).setPreferredWidth(40);
        tablaErrores.getColumnModel().getColumn(3).setPreferredWidth(230);
        erroresPane.setViewportView(tablaErrores);
        
        //JScrollPane para la tabla Triplos
        JScrollPane TriploPane = new JScrollPane();
        TriploPane.setBounds(724, 57, 299, 403);
        frameComp.getContentPane().add(TriploPane);
        
        // JTable para el Triplo
        tablaTriplo = new JTable();
        tablaTriplo.setModel(new DefaultTableModel(
        	new Object[][] {},
        	new String[] {" ", "Dato Objeto", "Dato Fuente", "Operador"}
        ));
        tablaTriplo.getColumnModel().getColumn(0).setPreferredWidth(20);
        tablaTriplo.getColumnModel().getColumn(3).setPreferredWidth(40);
        TriploPane.setViewportView(tablaTriplo);
        
        // Funcionalidad del botón Guardar
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showSaveDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(textPane.getText()); // Escribir el contenido del JTextPane en el archivo
                    } catch (IOException ex) {
                        ex.printStackTrace(); // Mostrar el error en la consola
                    }
                }
            }
        });
        
        // Funcionalidad del botón Abrir
        btnAbrir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(null);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                        String line;
                        StringBuilder content = new StringBuilder();
                        while ((line = bufferedReader.readLine()) != null) {
                            content.append(line).append("\n"); // Leer y añadir el contenido del archivo al JTextPane
                        }
                        textPane.setText(content.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace(); // Mostrar el error en la consola
                    }
                }
            }
        });
        
        // Funcionalidad del botón Compilar
        btnCompilar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String codigo = textPane.getText(); // Obtener el código del JTextPane
                
                // Limpiar la tabla de errores antes de compilar
                DefaultTableModel modelErrores = (DefaultTableModel) tablaErrores.getModel();
                modelErrores.setRowCount(0);

                // Limpiar la tabla de símbolos antes de compilar
                DefaultTableModel modelSimbolos = (DefaultTableModel) tablaSimbolos.getModel();
                modelSimbolos.setRowCount(0);

                // Realizar el análisis léxico y semántico
                analizador_Semantico.analizar_Semantica(codigo);
                analizador_Semantico.mostrarSimbolosEnTabla(tablaSimbolos); // Mostrar los resultados en la tabla de símbolos
            }
        });
        
     // Funcionalidad del botón Generar Triplo
        btnTriplo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	Triplo triplo = new Triplo();
            	String codigo = textPane.getText(); 
            	
            	// Limpiar la tabla de Trplos antes de generar los triplos
            	DefaultTableModel modelTriplos = (DefaultTableModel) tablaTriplo.getModel();
                modelTriplos.setRowCount(0); 
                
                triplo.generador_Triplo(codigo);
                triplo.mostrar_Triplo(tablaTriplo);
            }
        });
        
        // Funcionalidad del botón Optimizar
        btnOptimizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                Optimizar optimizar = new Optimizar();
            	String codigo = textPane.getText(); 
            	codigo = optimizar.separar_Lineas(codigo);
            	optimizar.exportarCodigoOptimizado("codigo_optimizado.txt");
            	
            	// Limpiar la tabla de Trplos antes de generar los triplos
            	DefaultTableModel modelTriplos = (DefaultTableModel) tablaTriplo.getModel();
                modelTriplos.setRowCount(0); 
            	Triplo triplo2 = new Triplo(); 
                triplo2.generador_Triplo(codigo);
                triplo2.mostrar_Triplo(tablaTriplo);
            }
        });
    }
    
    // Método main para iniciar la aplicación
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Compilador_Main window = new Compilador_Main();
                    window.frameComp.setVisible(true); // Hacer visible la ventana principal
                } catch (Exception e) {
                    e.printStackTrace(); // Mostrar el error en la consola
                }
            }
        });
    }
}