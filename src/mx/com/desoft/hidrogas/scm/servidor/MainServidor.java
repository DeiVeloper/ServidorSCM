package mx.com.desoft.hidrogas.scm.servidor;
import java.io.BufferedReader;
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
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				System.out.println("Servidor> Conexión exitosa");
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
				String request = input.readLine();
                System.out.println("Cliente> petición [" + request +  "]");
				LeerDirectorio();
				output.flush();
				output.writeObject(pedidosMensajes);
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

	public static void LeerDirectorio() {
		File directorio = new File("");
		if (directorio.exists()) {
			File[] archivos = directorio.listFiles();
			pedidosMensajes = new ArrayList<>();
			for (int i = 0; i < archivos.length; i++) {
				LeerArchivos(archivos[i]);
				pedidosMensajes.add(new Data(numeroCelular, mensaje));
			}
		} else {
			System.out.println("Error al leer el archivo");
		}
	}

	@SuppressWarnings("resource")
	private static void LeerArchivos(File archivoSMS) {
		String linea = "";
		mensaje = "";
		numeroCelular = "";
		try {
			if (getExtension(archivoSMS.getName()).equals("txt")) {
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
