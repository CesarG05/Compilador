package Compilador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizar {
	
	 private String Codigo_Optimizado;
	 private List<String[]> instrucciones;
	 
	// Constructor que inicializa las listas para triplos y lexemas
	    public Optimizar() {
	        this.instrucciones = new ArrayList<>();
	    }
	
	// Método principal 
    public String separar_Lineas(String Codigo) {
    	
    	// Eliminar todos los espacios en blanco
    	String cidigoSinEspacios = Codigo.replace(" ", "");
        Codigo = cidigoSinEspacios;
   
        String[] lineas_codigo = Codigo.split("\n"); // Dividir el código en líneas

        for (int i = 0; i < lineas_codigo.length; i++) { // Iterar sobre cada línea de código, procesándolas una por una
            String instruccion = lineas_codigo[i];
            if (instruccion.contains("=")){
            	obtener_Expresion(instruccion);
            }
        }
        
        Codigo_Optimizado = Codigo;
        analizar_Expresiones();
        StringBuilder codigo_Optimizado = new StringBuilder(Codigo_Optimizado);
        Codigo_Optimizado = eliminar_Linea_Vacias(codigo_Optimizado).toString();
        
        return Codigo_Optimizado;
    }
    
    // Metódo para separar partes de las instrucciones 
    public void obtener_Expresion(String instruccion) {
    	
    	String[] partes = instruccion.split("=");
    	String variable = partes[0].replace(" ", ""); 
    	String expresion = partes[1].replace(" ", ""); 
    	instrucciones.add(new String[]{variable, expresion});
    }
    
    // Metódo para encontar coincidencia en dos o mas instruciones 
    public void analizar_Expresiones() {
    	
    	// Recorremos la lista para comparar el segundo elemento de cada String[] con los demás
    	for (int i = 0; i < instrucciones.size(); i++) {
    	    String[] array_Actual = instrucciones.get(i);
    	    String expresion_Actual = array_Actual[1]; // Tomamos el segundo elemento de la instrucción actual

    	    for (int j = i + 1; j < instrucciones.size(); j++) {
    	        String[] array_Siguiente = instrucciones.get(j);
    	        String expresion_Siguiente = array_Siguiente[1]; // Tomamos el segundo elemento de la siguiente instrucción

    	        // Comparamos los segundos elementos
    	        if (expresion_Actual.equals(expresion_Siguiente)) {	
    	        	
    	        	//System.out.println("Coincidencia encontrada entre: "
        	        //                + "Linea " + (i + 1) + " y Linea " + (j + 1) + " -> " + expresion_Actual);
    	        	
    	        	String exprReg_Id = "#([a-zA-Z0-9]{1,5})#";
    	            Pattern pattern = Pattern.compile(exprReg_Id);
    	            Matcher matcher = pattern.matcher(expresion_Siguiente);
    	            int bandera = 0;
    	            // Comprobar si hay coincidencia
    	            while (matcher.find()) {
    	            	if (matcher.group(1) != null) {
        	                //System.out.println("La expresion contiene variable.");
        	                String variable_EnExpresion = matcher.group(1);
        	                
        	                for (int n = j - 1; n > i; n--) {
        	                	String[] array_instruccion = instrucciones.get(n);
        	        	        String variable = array_instruccion[0];
        	        	         if (variable.contains(variable_EnExpresion)) {
        	        	        	 //System.out.println("La bandera cambio a 1");
        	        	        	 bandera = 1;
        	        	         }
        	                }
        	            }      
    	            }
    	            
    	            if (bandera == 0) {
    	                cambiar_Variables(array_Actual, array_Siguiente);
    	            }
    	        }
    	    }
    	}
    }
    
    // Metódo para hacer el cambio de variable en el código
    public void cambiar_Variables(String[] linea_1, String[] linea_2){
    	
    	// Eliminar todos los espacios en blanco
    	String cidigoSinEspacios = Codigo_Optimizado.replace(" ", "");
        Codigo_Optimizado = cidigoSinEspacios;
        
        StringBuilder codigo_Optimizado = new StringBuilder(Codigo_Optimizado);
        String instruccion_2 = linea_2[0] + "=" + linea_2[1];
        codigo_Optimizado = eliminar_Instruccion(codigo_Optimizado, instruccion_2);
        
        //Reemplazar ocurrencias de la variable a reemplazar
        if (linea_1.length > 0 && linea_2.length > 0) {
            String variable = linea_2[0];
            String nuevoValor = linea_1[0];
            int index = codigo_Optimizado.indexOf(variable);
            while (index != -1) {
                codigo_Optimizado.replace(index, index + variable.length(), nuevoValor);
                index = codigo_Optimizado.indexOf(variable, index + nuevoValor.length());
            }
        }
        
        codigo_Optimizado = eliminar_Linea_Vacias(codigo_Optimizado);
        Codigo_Optimizado = codigo_Optimizado.toString();
    }
    
   // Metódo para elimianr las lineas vacías (en blanco)
    public StringBuilder eliminar_Linea_Vacias(StringBuilder codigo) {
	   String nuevo_Codigo = codigo.toString();
	   
	   // Dividir el texto en líneas y eliminar las vacías
       String[] lineas = nuevo_Codigo.split("\n");
       StringBuilder resultado = new StringBuilder();
       for (String linea : lineas) {
           if (!linea.trim().isEmpty()) {  // Solo agregar líneas que no están vacías
               resultado.append(linea).append("\n");
           }
       }
       return resultado;
    }
    
    // Metódo para eliminar instrucciones que se repiten 
    public StringBuilder eliminar_Instruccion(StringBuilder codigo_Optimizado, String instruccion) {    
        int index = codigo_Optimizado.indexOf(instruccion);
        // Eliminamos todas las ocurrencias de instruccion
        while (index != -1) {
            codigo_Optimizado.delete(index, index + instruccion.length());
            index = codigo_Optimizado.indexOf(instruccion, index); // Buscar la siguiente ocurrencia
        }
        return codigo_Optimizado;
    }
    
    // Metódo para guardar el código optimizado 
    public void exportarCodigoOptimizado(String nombreArchivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write(Codigo_Optimizado);
            System.out.println("El código optimizado se ha guardado en: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }
/*   
    // Metódo Main 
    public static void main(String args[]) {
    	String codigo = "#val1# = 3 + #num1#\r\n"
	    			+ "#val2# = 5 * #cont#\r\n"
	    			+ "#val3# = 3 + #num1#\r\n"
	    			+ "\r\n"
	    			+ "if ( #val2# < 10) {\r\n"
	    			+ "     #respA# = #val1# + 2\r\n"
	    			+ "} else {\r\n"
	    			+ "    #respA# = #val3# * 2\r\n"
	    			+ "}\r\n"
	    			+ "\r\n"
	    			+ "#num2# = 5 * #cont#\r\n"
	    			+ "#respZ# = #val3# + #num2#\r\n";
    	
       separar_Lineas(codigo);
       // Mostrar el código optimizado
       System.out.println("Código Optimizado:\n" + Codigo_Optimizado);
       exportarCodigoOptimizado("codigo_optimizado.txt");
    }*/
}
