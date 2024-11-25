package Compilador;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador_Semantico {
    
    // HashMaps para almacenar los lexemas y tipos de datos
    private HashMap<String, String> tabla_ide; // Almacena identificadores
    private HashMap<String, String> tabla_Tipos; // Almacena variables y sus tipos de datos
    private int numErr_Sem; // Contador de errores semánticos
    private int numErr_Lex; 
    private JTable tablaErrores; // Referencia a la tabla de errores en la interfaz gráfica

    // Constructor que inicializa los HashMaps y la tabla de errores
    public Analizador_Semantico(JTable tablaErrores) {
        this.tabla_ide = new HashMap<>();
        this.tabla_Tipos = new HashMap<>();
        this.tablaErrores = tablaErrores;
    }

 // Método para analizar el código ingresado y separar los diferentes lexemas
    private void analizar_Codigo(String codigo) {

        // Expresiones regulares para reconocer diferentes tipos de lexemas
        String ID_REGEX = "#[a-zA-Z0-9]{1,5}#"; // Identificadores válidos
        String NUM_INT = "\\d+"; // Números enteros
        String NUM_REAL_REGEX = "\\d+\\.\\d+"; // Números reales
        String CADENA_REGEX = "\"[^\"]*\""; // Cadenas entre comillas
        //String OPERADOR_RELACIONAL_REGEX = "(==|!=|<=|>=|<|>)"; 
        String ASIGNACION_REGEX = "="; // Asignación con el símbolo "="
        String OPERADOR_REGEX = "[+\\-*/%]"; // Operadores aritméticos 

        // Combinar todas las expresiones regulares para capturar diferentes lexemas
        String combinedRegex = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)", 
                                                ID_REGEX, 
                                                NUM_REAL_REGEX, 
                                                NUM_INT, 
                                                CADENA_REGEX,
                                                OPERADOR_REGEX,
                                                ASIGNACION_REGEX);
        Pattern pattern = Pattern.compile(combinedRegex);
        Matcher matcher = pattern.matcher(codigo);

        // Identificar y clasificar los lexemas encontrados en el código
        while (matcher.find()) {
            if (matcher.group(1) != null) { // Identificadores
                tabla_ide.put(matcher.group(1), "Identificador");
            } else if (matcher.group(2) != null) { // Números reales
                tabla_Tipos.put(matcher.group(2), "Real");
            } else if (matcher.group(3) != null) { // Números enteros
                tabla_Tipos.put(matcher.group(3), "Entero");
            } else if (matcher.group(4) != null) { // Cadenas
                tabla_Tipos.put(matcher.group(4), "Cadena");
            } else if (matcher.group(5) != null) { // Operadores aritméticos
                tabla_Tipos.put(matcher.group(5), null);
            } else if (matcher.group(6) != null) { // Asignación
                tabla_Tipos.put(matcher.group(6), null);
            }
        }
    }

    // Metodo para obtener lo operadores aritmeticos 
    private void analizar_Relacionales(String codigo) {
    	String CADENA_REGEX = "\"[^\"]*\""; // Cadenas entre comillas
        String OPERADOR_RELACIONAL_REGEX = "(==|!=|<=|>=|<|>)"; // Operadores relacionales
        Pattern pattern = Pattern.compile(String.format("(%s)|(%s)", CADENA_REGEX, OPERADOR_RELACIONAL_REGEX));
        Matcher matcher = pattern.matcher(codigo);

        while (matcher.find()) {
            if (matcher.group(2) != null) { 
                tabla_Tipos.put(matcher.group(2), null);
            }
        }
    }
    
    // Método para analizar semánticamente cada línea del código
    public void analizar_Semantica(String codigo) {
        String[] lineas_codigo = codigo.split("\n"); // Dividir el código en líneas
        numErr_Sem = 0; //Reinicia el contador de errores
        numErr_Lex = 0;
        int num_Renglon = 0;
        
        tabla_ide.clear(); // Limpiar HashMaps antes de cada análisis para evitar datos residuales
        tabla_Tipos.clear(); 

        // Analizar cada línea de código
        for (String linea : lineas_codigo) {
        	analizar_Codigo(linea);
        	analizar_Relacionales(linea);
        	num_Renglon += 1;
            analizar_Linea(linea, num_Renglon); // Analizar la línea actual
        }
    }

    // Analizar una sola línea para verificar la validez semántica
    private void analizar_Linea(String linea, int renglon) {
    	if (linea.contains("if") || linea.contains("else")) {
    		return;
    	} else if (linea.contains("=")) { // Verificar si la línea contiene una asignación
            String[] partes_Linea = linea.split("="); // Separar la variable y la expresión
            String variable_actual = partes_Linea[0].trim(); // Variable a la izquierda del "="
            String expresion = partes_Linea[1].trim(); // Expresión a la derecha del "="

            if (tabla_ide.containsKey(variable_actual)) { // Verificar si la variable es valida
                // Determinar el tipo de la expresión
                String tipoExpresion = determinarTipoExpresion(expresion, variable_actual, renglon);
                tabla_Tipos.put(variable_actual, tipoExpresion); // Actualizar el tipo de la variable

            } else { // Error si la variable es valida
            	this.numErr_Lex++;
            	mostar_Errores("ErrLex" + numErr_Lex, variable_actual, renglon, "Error de Sintaxis");
            }
        } 
    }

    // Determinar el tipo de dato de una expresión
    private String determinarTipoExpresion(String expresion, String variable_actual, int renglon) {
        String[] operandos = expresion.split("\\s*([+\\-*/%])\\s*"); // Dividir la expresión en operandos
        String tipo_Dato_Actual = null;
        String tipo_Dato_Anterior = null;
        
        String tipoOperando;

        // Analizar cada operando en la expresión
        for (String operando : operandos) {
        	
            tipo_Dato_Anterior = tipo_Dato_Actual; 
            
            if (tabla_ide.containsKey(operando)) { // Si el operando es una variable
                if (!tabla_Tipos.containsKey(operando)) { // Verificar si la variable tiene un tipo asignado
                    this.numErr_Sem++;
                    mostar_Errores("ErrSem" + numErr_Sem, operando, renglon, "Variable Indefinida"); // Error si la variable no está definida
                    return null; //Retorna nulo
                } else {
                    tipo_Dato_Actual = tabla_Tipos.get(operando); // Obtener el tipo de dato de la variable
                    if (tipo_Dato_Actual == null) { 
                    	this.numErr_Sem++;
                    	mostar_Errores("ErrSem" + numErr_Sem, operando, renglon, "Variable Indefinida");// Error si la variable no tiene tipo debido a un error previo
                    	return null; //Retorna nulo
                    }
                }
            } else { // Si el operando no es una variable, verificar su tipo de dato
                tipoOperando = tabla_Tipos.get(operando);
                if (tipoOperando != null && (tipoOperando.equals("Entero") || tipoOperando.equals("Real") || tipoOperando.equals("Cadena"))) {
                    tipo_Dato_Actual = tipoOperando; // Actualizar el tipo de dato actual
                } else { // Error si el operando no es válido
                	this.numErr_Lex++;
                	mostar_Errores("ErrLex" + numErr_Lex, operando, renglon, "Error de Sintaxis");           
                    return null;
                }
            }
            
            // Verificar compatibilidad de tipos entre los operandos
            if (tipo_Dato_Anterior != null) {         
                if (tipo_Dato_Actual.equals("Entero") && tipo_Dato_Anterior.equals("Entero")) {
                    tipo_Dato_Actual = "Entero";
                } else if ((tipo_Dato_Actual.equals("Entero") && tipo_Dato_Anterior.equals("Real")) || 
                           (tipo_Dato_Actual.equals("Real") && tipo_Dato_Anterior.equals("Entero"))) {
                    tipo_Dato_Actual = "Real";
                } else if (tipo_Dato_Actual.equals("Cadena") && tipo_Dato_Anterior.equals("Cadena")) {
                    tipo_Dato_Actual = "Cadena";
                } else if ((tipo_Dato_Actual.equals("Cadena") && (tipo_Dato_Anterior.equals("Entero") || tipo_Dato_Anterior.equals("Real"))) ||
                           ((tipo_Dato_Actual.equals("Entero") || tipo_Dato_Actual.equals("Real")) && tipo_Dato_Anterior.equals("Cadena"))) {
                    this.numErr_Sem++;
                    mostar_Errores("ErrSem" + numErr_Sem, operando, renglon, "Incompatibilidad de tipos, " + variable_actual);
                    return null;
                }             
            } 
        }
        return tipo_Dato_Actual; // Retornar el tipo final de la expresión
    }

    // Mostrar los símbolos en la tabla de símbolos de la interfaz gráfica
    public void mostrarSimbolosEnTabla(JTable tablaSimbolos) {
        DefaultTableModel modelSimbolos = (DefaultTableModel) tablaSimbolos.getModel();
        modelSimbolos.setRowCount(0); // Limpiar la tabla antes de mostrar los símbolos

        // Añadir cada símbolo a la tabla de símbolos
        for (Map.Entry<String, String> entry : tabla_Tipos.entrySet()) {
            Object[] rowData = { entry.getKey(), entry.getValue() };
            modelSimbolos.addRow(rowData);
        }
    }

    // Mostrar un error semántico en la tabla de errores
    private void mostar_Errores(String TipoError, String lexema, int renglon, String descripcion) {
        DefaultTableModel model = (DefaultTableModel) this.tablaErrores.getModel();
        model.addRow(new Object[]{ TipoError, lexema, renglon, descripcion });
    }
}