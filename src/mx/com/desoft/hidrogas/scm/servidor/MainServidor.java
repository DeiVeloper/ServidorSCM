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


//	private static Socket socket;
//    private static ServerSocket serverSocket;
    private static DataInputStream bufferDeEntrada = null;
    private static ObjectOutputStream bufferDeSalida = null;
    final static String COMANDO_TERMINACION = "--T";
    final static String COMANDO_INCIAR = "--I";
    private static ArrayList<String> archivosEliminar;
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
//			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				System.out.println("Servidor> Conexión exitosa");
				/*BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
				String request = input.readLine();
                System.out.println("Cliente> petición [" + request +  "]");
				LeerDirectorio();
				output.flush();
				output.writeObject(pedidosMensajes);*/
				archivosEliminar = new ArrayList<String>();
				flujos();
				recibirDatos();
				clientSocket.close();
				EliminarArchivo(archivosEliminar);
				for (Data list : pedidosMensajes) {
					System.out.println(list.getNumeroCelular().concat(" - " ).concat(list.getMensaje()));
				}
			}
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
//		MainServidor server = new MainServidor();
//		server.ejecutarConexion(PORT);
	}

	public static void flujos() {
        try {
        	bufferDeEntrada = new DataInputStream(clientSocket.getInputStream());
            bufferDeSalida = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
        	System.out.println("Error en la apertura de flujos");
        }
    }

	public static void recibirDatos() {
        String nombreArchivo = "";
        try {
            do {
            	nombreArchivo = (String) bufferDeEntrada.readUTF();
                if(nombreArchivo.equals(COMANDO_INCIAR)) {
                	System.out.println("entra");
                	System.out.println(nombreArchivo);
                	LeerDirectorio();
                	bufferDeSalida.flush();
                	bufferDeSalida.writeObject(pedidosMensajes);
                } else {
                	System.out.println(nombreArchivo);
                	archivosEliminar.add(nombreArchivo);
//                	EliminarArchivo(nombreArchivo);
                }
            } while (!nombreArchivo.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
//            cerrarConexion();
        	System.out.println("Error en recibir datos");
        }
    }

	public static void LeerDirectorio() {
		File directorio = new File("/Users/ErickMV/Documents/pruebas");
		if (directorio.exists()) {
			File[] archivos = directorio.listFiles();
			pedidosMensajes = new ArrayList<>();
			for (int i = 0; i < archivos.length; i++) {
				LeerArchivos(archivos[i]);
				if(!numeroCelular.equals("") && !mensaje.equals("")) {
					pedidosMensajes.add(new Data(numeroCelular, mensaje, nombreArchivo));
				}
			}
			archivos = null;
		} else {
			System.out.println("Error al leer el archivo");
		}
	}

	@SuppressWarnings("resource")
	private static void LeerArchivos(File archivoSMS) {
		String linea = "";
		mensaje = "";
		numeroCelular = "";
		nombreArchivo = "";
		try {
			if (getExtension(archivoSMS.getName()).equals("txt")) {
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
			}
			archivoSMS = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void EliminarArchivo(ArrayList<String> archivosEliminar) {
		File archivo;
		for(String nombreArchivo : archivosEliminar) {
			archivo = new File("/Users/ErickMV/Documents/pruebas/"+nombreArchivo);
			System.out.println("pathAbso: " + archivo.getAbsolutePath());
			if(archivo.exists()) {
				System.out.println("existe" );
				System.out.println("archivo eliminado: " + nombreArchivo);
				archivo.delete();
			}
		}
	}

	private static String getExtension(String fileName) {
		String extension = "";
		int index = fileName.lastIndexOf('.');
		if (index >= 0) {
			extension = fileName.substring(index + 1);
		}
		return extension;
	}
}
