package mx.com.desoft.hidrogas.scm.servidor;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import mx.com.desoft.hidrogas.scm.dto.Data;

public class MainServidor {

	/**
	 * Puerto
	 */
	private final static int PORT = 5000;
	private static ArrayList<Data> pedidosMensajes;
	private static String numeroCelular = "";
	private static String mensaje = "";
	private static String nombreArchivo = "";

    private static DataInputStream bufferDeEntrada = null;
    private static ObjectOutputStream bufferDeSalida = null;
    final static String COMANDO_TERMINACION = "--T";
    final static String COMANDO_INCIAR = "--I";
    private static Socket clientSocket;

	/**
	 * @param args
	 *            the command line arguments
	 */
	@SuppressWarnings({ "resource" })
	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			System.out.println("Servidor> Servidor iniciado");
			System.out.println("Servidor> En espera de cliente...");
			while (true) {
				clientSocket = serverSocket.accept();
				System.out.println("Servidor> Conexi√≥n exitosa");
				flujos();
				recibirDatos();
				clientSocket.close();
				for (Data list : pedidosMensajes) {
					System.out.println(list.getNumeroCelular().concat(" - " ).concat(list.getMensaje()));
				}
			}
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Metodo usado para abrir los buffers de comunicacion
	 *
	 */
	public static void flujos() {
        try {
        	bufferDeEntrada = new DataInputStream(clientSocket.getInputStream());
            bufferDeSalida = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
        	System.out.println("Error en la apertura de flujos");
        }
    }

	/**
	 * Metodo usado para recibir y enviar datos
	 *
	 */
	public static void recibirDatos() {
        String nombreArchivo = "";
        try {
            while(true) {
            	nombreArchivo = (String) bufferDeEntrada.readUTF();
                if(nombreArchivo.equals(COMANDO_INCIAR)) {
                	LeerDirectorio();
                	bufferDeSalida.flush();
                	bufferDeSalida.writeObject(pedidosMensajes);
                } else if (nombreArchivo.equals(COMANDO_TERMINACION)) {
                	break;
                } else {
                	EliminarArchivo(nombreArchivo);
                }
            }
        } catch (IOException e) {
        	System.out.println("Error en recibir datos");
        }
    }

	/**
	 * Metodo usado para acceder al directorio
	 * donde se encuentran los achivos
	 *
	 */
	public static void LeerDirectorio() {
		File directorio = new File("/Users/ErickMV/Documents/pruebas");
		if (directorio.exists()) {
			File[] archivos = directorio.listFiles();
			pedidosMensajes = new ArrayList<>();
			for (int i = 0; i < archivos.length; i++) {
				if (getExtension(archivos[i].getName()).equals("txt")) {
					LeerArchivos(archivos[i]);
					if(!numeroCelular.equals("") && !mensaje.equals("")) {
						pedidosMensajes.add(new Data(numeroCelular, mensaje, nombreArchivo));
					}
				}
			}
			archivos = null;
		} else {
			System.out.println("Error al leer el archivo");
		}
	}

	/**
	 * Metodo usado para leer los archivos
	 *
	 * @param archivoSMS
	 */
	private static void LeerArchivos(File archivoSMS) {
		String linea = "";
		mensaje = "";
		numeroCelular = "";
		nombreArchivo = "";
		try {
			nombreArchivo = archivoSMS.getName();
			FileInputStream leerContenido = new FileInputStream(archivoSMS);
			BufferedReader reader = new BufferedReader(new InputStreamReader(leerContenido));
			while ((linea = reader.readLine()) != null) {
				if (numeroCelular == "1") {
					numeroCelular = linea;
				} else {
					mensaje = linea;
					numeroCelular = "1";
				}
			}
			leerContenido.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo usado para eliminar los archivos
	 *
	 * @param nombreArchivo
	 */
	private static void EliminarArchivo(String nombreArchivo) {
		File archivo = new File("/Users/ErickMV/Documents/pruebas/"+nombreArchivo);
		System.out.println("pathAbso: " + archivo.getAbsolutePath());
		if(archivo.exists()) {
			System.out.println("existe" );
			System.out.println("archivo eliminado: " + nombreArchivo);
			archivo.delete();
		}
	}

	/**
	 * Metodo usado para obtener la extensiÛn de los archivos
	 *
	 * @param nombreArchivo
	 */
	private static String getExtension(String fileName) {
		String extension = "";
		int index = fileName.lastIndexOf('.');
		if (index >= 0) {
			extension = fileName.substring(index + 1);
		}
		return extension;
	}
}
