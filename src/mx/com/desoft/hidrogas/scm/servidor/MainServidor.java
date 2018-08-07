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


	private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream bufferDeEntrada = null;
    private ObjectOutputStream bufferDeSalida = null;
    final String COMANDO_TERMINACION = "--T";
    final String COMANDO_INCIAR = "--I";

	/**
	 * @param args
	 *            the command line arguments
	 */
	@SuppressWarnings({ "resource" })
	public static void main(String[] args) {
		/*try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			System.out.println("Servidor> Servidor iniciado");
			System.out.println("Servidor> En espera de cliente...");
			Socket clientSocket;
			while (true) {
				clientSocket = serverSocket.accept();
				System.out.println("Servidor> ConexiÃ³n exitosa");
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
				String request = input.readLine();
                System.out.println("Cliente> peticiÃ³n [" + request +  "]");
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
		}*/
		MainServidor server = new MainServidor();
		server.ejecutarConexion(PORT);
	}

	public void ejecutarConexion(int puerto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        levantarConexion(puerto);
                        flujos();
                        recibirDatos();
                    } finally {
                        cerrarConexion();
                    }
                }
            }
        });
        hilo.start();
    }

	public void levantarConexion(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Esperando conexión entrante en el puerto " + String.valueOf(puerto) + "...");
            socket = serverSocket.accept();
            System.out.println("Conexión establecida con: " + socket.getInetAddress().getHostName() + "\n\n\n");
        } catch (Exception e) {
        	System.out.println("Error en levantarConexion(): " + e.getMessage());
            System.exit(0);
        }
    }

	public void flujos() {
        try {
        	bufferDeEntrada = new DataInputStream(socket.getInputStream());

            bufferDeSalida = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
        	System.out.println("Error en la apertura de flujos");
        }
    }

	public void recibirDatos() {
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
                	EliminarArchivo(nombreArchivo);
                }
            } while (!nombreArchivo.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
            cerrarConexion();
        }
    }

	public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
        } catch (IOException e) {
        	System.out.println("Excepción en cerrarConexion(): " + e.getMessage());
        } finally {
        	System.out.println("Conversación finalizada....");
            System.exit(0);

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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void EliminarArchivo(String nombre) {
		File archivo = new File("/Users/ErickMV/Documents/pruebas/"+nombre);
		System.out.println("/Users/ErickMV/Documents/pruebas/"+nombre);
		System.out.println("path: " + archivo.getPath());
		System.out.println("pathAbso: " + archivo.getAbsolutePath());
		if(archivo.exists()) {
			System.out.println("existe" );
			archivo.delete();
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
