package Compilador;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Triplo {

    private int tempCont; // Contador para temporales
    private ArrayList<String> lexemas; // Almacena los lexemas por cada linea 
    private List<String[]> triplos; // Almacena los triplos generados
    
 // Constructor que inicializa las listas para triplos y lexemas
    public Triplo() {
        this.triplos = new ArrayList<>();
        this.lexemas = new ArrayList<>();
    }

 // Método principal para iniciar la generación de triplos a partir del código fuente
    public void generador_Triplo(String Codigo) {

        String[] lineas_codigo = Codigo.split("\n"); // Dividir el código en líneas

        for (int i = 0; i < lineas_codigo.length; i++) { // Iterar sobre cada línea de código, procesándolas una por una
            String instruccion = lineas_codigo[i]; 
            procesar_linea(instruccion); // Llama al método que procesa cada línea según el tipo de instrucción

         // Agrega un triplo de cierre si es la última línea y contiene "}"
            if (i == lineas_codigo.length - 1) {
            	if (lineas_codigo[i].contains("}")) {
            		 triplos.add(new String[]{String.valueOf(triplos.size() + 1), "...", "...", "..."});
            	}
            }
        }
    }
    
 // Método que determina el tipo de instrucción y realizar el procesamiento adecuado
    private void procesar_linea(String instruccion) {
    	if (instruccion.contains("if")) { // Condición
    		analizar_Condicion(instruccion); // Llama al método que maneja la generación de triplos para condiciones "if"
    	} else if (instruccion.contains("}")) { // Cierre de bloque
    	    colocar_Salto(instruccion);	 // Llama al método que coloca un salto para finalizar el bloque
    	} else if (instruccion.contains("=")) { // Asignación
    	    obtener_Triplo_Aritmetico(instruccion);	// Llama al método que genera triplos para operaciones aritméticas
    	}
    }
    
 // Analiza condiciones y las divide en partes si contienen operadores lógicos
    private void analizar_Condicion(String condicion){
    	
   	   lexemas.clear();// Limpia la lista de lexemas para evitar que contenga lexemas de lineas anteriores  
       
       if (condicion.contains("&&") || condicion.contains("||")) { // Verifica si la condición contiene operadores lógicos "&&" (AND) o "||" (OR)
    	   String[] condicionales = condicion.split("(&&|\\|\\|)");
    	   lexemas_expresion(condicionales[0]); // Obtiene los lexemas la primera parte de la condición
    	   if (condicion.contains("&&")){ // Agrega el operador lógico segun sea
    		   lexemas.add("&&");
           } else if (condicion.contains("||")) {
        	   lexemas.add("||");
           }
           lexemas_expresion(condicionales[1]); // Obtiene los lexemas la segunda parte de la condición
       } else { 
       	   lexemas_expresion(condicion); // Si la condición no contiene operadores lógicos, obtiene los lexemas de la expresión completa
       }
       obtener_Triplo_Condicion(); // Genera el triplo correspondiente a la condición
    }
    
 // Genera los triplos correspondientes a una condición lógica
    private void obtener_Triplo_Condicion() {
    	
        tempCont = 1; // Inicializa el contador de temporales 
        String dato_Fuente;
        String tempId = "T" + tempCont; // Establece el primer temporal
    	
    	for (int i = 1; i < lexemas.size(); i += 2) { // Recorre la lista de lexemas, saltando de dos en dos para procesar cada par condicional
    		
    		dato_Fuente = lexemas.get(i - 1); // Toma el dato de la primera parte de la condición
            triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, "="}); // Genera el triplo T1 = dato_Fuente
            triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, lexemas.get(i + 1), lexemas.get(i)}); // Genera el triplo de la condicion
            
            if (i + 2 < lexemas.size()-1) {  // Comprueba si hay otro operador lógico después de la comparación actual
                if (lexemas.get(i + 2).contains("&&")) { // Genera triplos para manejar la lógica del operador "&&" (AND)
                	triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "True", String.valueOf(triplos.size() + 3)});
                    triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "False", null});
                } else if (lexemas.get(i + 2).contains("||")) {  // Genera triplos para manejar la lógica del operador "||" (OR)
                	triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "True", String.valueOf(triplos.size() + 7)});
                    triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "False", String.valueOf(triplos.size() + 2)});
                }
                i += 2;
            } else { // Si es la última condición, genera los triplos para el final de la evaluación
            	triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "True", String.valueOf(triplos.size() + 3)});
                triplos.add(new String[]{String.valueOf(triplos.size() + 1), "TR1", "False", null});
            }       
    	}	
    }
    
 // Coloca un salto según el tipo de instrucción
    private void colocar_Salto(String instruccion) {
    	
    	if (instruccion.contains("else")) { // Verifica si la instrucción es un bloque "else"
    		triplos.add(new String[]{String.valueOf(triplos.size() + 1), null, null, "JMP"});  // Agrega un triplo de salto incondicional (JMP) para saltar al final del bloque "else"
        	
       	    for (int i = triplos.size() - 2; i >= 0; i--) { // Recorre la lista de triplos hacia atrás para encontrar el salto "False" sin asignar
                String[] arreglo = triplos.get(i);
                
                 if (arreglo[2].contains("False") && arreglo[3] == null) { // Si encuentra un triplo con "False" sin destino, le asigna el indice destino del salto
               	  arreglo[3]=String.valueOf(triplos.size() + 1);
                 }
            }
    	} else { // Para bloques de cierre generales, recorre los triplos hacia atrás para colocar el salto adecuado
    		for (int i = triplos.size() - 1; i >= 0; i--) {
                String[] arreglo = triplos.get(i);
                
                 if (arreglo[1] == null && arreglo[2] == null) {
                	 arreglo[2]=String.valueOf(triplos.size() + 1);
                	 break; // Sale del bucle una vez asignado el salto
                 } else if (arreglo[2].contains("False") && arreglo[3] == null) { // Si encuentra un "False" sin destino, asigna el indice del siguiente triplo como destino
                	 arreglo[3]=String.valueOf(triplos.size() + 1);
                 }
    		}
    	}
    }

 // Genera triplos para una operación aritmética en una asignación
    private void obtener_Triplo_Aritmetico(String instruccion) {
    	
    	String[] parts = instruccion.split("="); // Divide la instrucción en variable y expresión usando el signo '='
	    String variable = parts[0].trim(); // Extrae la variable a la izquierda del '='
	    String expresion = parts[1].trim(); // Extrae la expresión a la derecha del '='

    	lexemas.clear(); // Limpia los lexemas antes de procesar una nueva expresión
    	lexemas_expresion(expresion); // Divide la expresión en lexemas

        tempCont = 1; // Inicializa el contador temporal
        String dato_Fuente; // Variable que contendra los dato fuente
        String tempId = ""; // Identificador temporal para almacenar resultados intermedios
        ArrayList<String> conversion = new ArrayList<>();  // Lista para almacenar operadores y operandos de la expresión

        for (int i = 1; i < lexemas.size(); i += 2) { // Itera sobre los lexemas de la expresión de dos en dos, ya que alternan operandos y operadores

            if (i == 1) { // Si estamos en el primer operador de la expresión

                if (lexemas.get(i).equals("+") || lexemas.get(i).equals("-")) { // Procesa operadores de baja precedencia (+, -)
                    dato_Fuente = lexemas.get(i - 1); // Primer operando
                    tempId = "T" + tempCont++; 
                    triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, "="}); // Crea un triplo que asigna el primer operando al temporal
                    conversion.add(tempId); // Añade el temporal a la lista de conversion
                    conversion.add(lexemas.get(i)); // Añade el operador a la lista de conversion
                    if (i + 2 > lexemas.size()-1) { // Si el segundo operando es el último, lo añade directamente
                        conversion.add(lexemas.get(i + 1));
                    }
                } else if (lexemas.get(i).equals("*") || lexemas.get(i).equals("/") || lexemas.get(i).equals("%")) { // Procesa operadores de alta precedencia (*, /, %)
                    dato_Fuente = lexemas.get(i - 1); // Primer operando
                    tempId = "T" + tempCont++;
                    triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, "="}); // Crea un triplo que asigna el primer operando al temporal
                    dato_Fuente = lexemas.get(i + 1); // Segundo operando
                    triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, lexemas.get(i)}); // Crea un triplo para la operación
                    conversion.add(tempId); // Añade el resultado al temporal a la lista de conversion
                }
            } else {  // Procesa operadores de baja precedencia (+, -) si ya se han procesado otros operadores
                if (lexemas.get(i).equals("+") || lexemas.get(i).equals("-")) { // Verifica si el operador actual es de baja precedencia
                    if (lexemas.get(i - 2).equals("*") || lexemas.get(i - 2).equals("/") || lexemas.get(i - 2).equals("%")) { // Si el operador anterior es de alta precedencia
                        conversion.add(lexemas.get(i)); // Añade el operador actual a la lista de conversión para continuar la secuencia de operaciones
                        if (i + 2 > lexemas.size()-1) { // Si el próximo elemento es el último operando en la expresión
                            conversion.add(lexemas.get(i + 1)); // Añade el último operando a la lista de conversión
                        }
                    } else {
                        conversion.add(lexemas.get(i - 1)); // Añade el operando anterior a la lista de conversión
                        conversion.add(lexemas.get(i)); // Añade el operador actual a la lista de conversión
                        if (i + 2 > lexemas.size()-1) { // Si el próximo elemento es el último operando
                            conversion.add(lexemas.get(i + 1)); // Añade el último operando a la lista de conversión
                        }
                    }
                } else if (lexemas.get(i).equals("*") || lexemas.get(i).equals("/") || lexemas.get(i).equals("%")) { // Procesa operadores de alta precedencia (*, /, %)
                    if (lexemas.get(i - 2).equals("+") || lexemas.get(i - 2).equals("-")) { // Si el operador anterior es de baja precedencia
                        dato_Fuente = lexemas.get(i - 1);
                        tempId = "T" + tempCont++;
                        triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, "="});
                        dato_Fuente = lexemas.get(i + 1);
                        triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, lexemas.get(i)});
                        conversion.add(tempId);
                    } else {
                        triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, lexemas.get(i + 1), lexemas.get(i)});
                    }
                }
            }
        }
        
        if (conversion.size() == 0) { // Si no hay operadores en la expresión
        	dato_Fuente = lexemas.get(0); // Usa el primer lexema como dato fuente
            tempId = "T" + tempCont++; // Genera el temporal
            triplos.add(new String[]{String.valueOf(triplos.size() + 1), tempId, dato_Fuente, "="}); // Asigna el operando al temporal
            conversion.add(tempId); // Añade el temporal a la lista de conversión
        } 
        
        if (conversion.size() >= 3) { // Si hay al menos un operador y dos operandos
            for (int i = 1; i < conversion.size(); i = i + 2) {
                triplos.add(new String[]{String.valueOf(triplos.size() + 1), conversion.get(0), conversion.get(i + 1), conversion.get(i)}); // Crea un triplo para la operación en la lista de conversión
            }
        } 
        // Asignar el resultado final a la variable
        triplos.add(new String[]{String.valueOf(triplos.size() + 1), variable, conversion.get(0), "="});
    }
    
 // Método para visualizar los triplos generados en un JTable
    public void mostrar_Triplo(JTable tablaTriplo) {
        // Obtener el modelo de la tabla
        DefaultTableModel modelTriplos = (DefaultTableModel) tablaTriplo.getModel();
        modelTriplos.setRowCount(0); // Limpiar la tabla 

        // Añadir cada elemento del ArrayList Triplos a la tabla
        for (Object[] triplo : triplos) {
            modelTriplos.addRow(triplo); // Añadir la fila al modelo de la tabla
        }
    }
    
 // Método para extraer y clasificar los diferentes lexemas en el código fuente
    private void lexemas_expresion(String codigo) {

        // Expresiones regulares para reconocer diferentes tipos de lexemas
        String ID_REGEX = "#[a-zA-Z0-9]{1,5}#"; // Identificadores
        String NUM_INT = "\\d+"; // Números enteros
        String NUM_REAL_REGEX = "\\d+\\.\\d+"; // Números reales
        String CADENA_REGEX = "\"[^\"]*\""; // Cadenas entre comillas
        String OPERADOR_REGEX = "[+\\-*/%]"; // Operadores aritméticos
        String OPERADOR_RELACIONAL_REGEX = "(==|!=|<=|>=|<|>)"; // Operadores lógicos

        // Combinar todas las expresiones regulares para capturar diferentes lexemas
        String combinedRegex = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)",
                ID_REGEX,
                NUM_REAL_REGEX,
                NUM_INT,
                CADENA_REGEX,
                OPERADOR_REGEX,
                OPERADOR_RELACIONAL_REGEX);
        Pattern pattern = Pattern.compile(combinedRegex);
        Matcher matcher = pattern.matcher(codigo);

        // Identificar y clasificar los lexemas encontrados
        while (matcher.find()) {
            if (matcher.group(1) != null) { // Identificadores
                lexemas.add(matcher.group(1));
            } else if (matcher.group(2) != null) { // Números reales
                lexemas.add(matcher.group(2));
            } else if (matcher.group(3) != null) { // Números enteros
                lexemas.add(matcher.group(3));
            } else if (matcher.group(4) != null) { // Cadenas
                lexemas.add(matcher.group(4));
            } else if (matcher.group(5) != null) { // Operadores aritméticos
                lexemas.add(matcher.group(5));
            } else if (matcher.group(6) != null) { // Operadores relacionales
                lexemas.add(matcher.group(6));
            } 
        }
    }
}