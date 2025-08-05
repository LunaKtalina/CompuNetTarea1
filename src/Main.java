import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public void init() throws IOException {
        ServerSocket server = new ServerSocket(8080);

        var isAlive = true;
        while (isAlive) {
            System.out.println("Esperando cliente...");
            var socket = server.accept();
            System.out.println("¡Cliente conectado!");
            dispatchWorker(socket);

        }

    }

    public void dispatchWorker(Socket socket) {
        new Thread(
                () -> {
                    try {
                        handlerRequest(socket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).start();
    }

    public void handlerRequest(Socket socket) throws IOException {

        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String resource = null;

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("GET")) {
                resource = line.split(" ")[1].replace("/", "");
                System.out.println("El cliente está pidiendo: "  + resource);
                break;
            }
        }

        if (resource != null) {
            sendResponse(socket, resource);
        } else {
            socket.close();
        }

    }

    public void sendResponse(Socket socket, String resource) throws IOException {

        var file = new File("resources/" + resource);

        // Obtenemos el stream de salida para responderle al cliente
        OutputStream out = socket.getOutputStream();

        if (file.exists()) {

            String mime = contentType(resource);
            long length = file.length(); // Tamaño archivo bytes

            var fis = new FileInputStream(file);

            /*String line;
            StringBuilder response = new StringBuilder();
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            */
            //Response

            out.write(("HTTP/1.1 200 OK\r\n").getBytes());
            out.write(("Content-Type:" + mime + "\r\n").getBytes());
            out.write(("Content-Length: " + length + "\r\n").getBytes());
            out.write(("Connection: close\r\n").getBytes());
            out.write(("\r\n").getBytes());

            sendBytes(fis, out);

            fis.close();
        } else {
            // Si el archivo NO existe, mostramos mensaje de error 404 en HTML
            System.out.println("Archivo no encontrado: " + resource);

            String errorMessage = "<html><body><h1>404 Not Found</h1><p>El recurso solicitado no existe.</p></body></html>";

            out.write(("HTTP/1.1 404 Not Found\r\n").getBytes());
            out.write(("Content-Type: text/html\r\n").getBytes());
            out.write(("Content-Length: " + errorMessage.length() + "\r\n").getBytes());
            out.write(("Connection: close\r\n").getBytes());
            out.write(("\r\n").getBytes()); // Fin de headers
            out.write(errorMessage.getBytes()); // Enviamos el mensaje HTML
        }

        out.flush(); // Asegura que todo se haya enviado
        out.close(); // Cerramos el stream
        socket.close(); // Cerramos la conexión TCP
    }

    private String contentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream"; // Por defecto: binario genérico
        }
    }

    private void sendBytes (InputStream fis, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024]; // Buffer de 1 KB
        int bytesLeidos;

        // Lee bloques de bytes desde el archivo y los escribe al stream de salida
        while ((bytesLeidos = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesLeidos);
        }
    }

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.init();

        /*

        ServerSocket server = new ServerSocket(8080);

        var isAlive = true;
        while (isAlive) {
            System.out.println("Esperando cliente...");
            var socket = server.accept();
            System.out.println("¡Cliente conectado!");

            var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));



            //Response
            var response = "<html><body><h1>Hola a todos</h1></body></html>";
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + response.length()+"\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.write(response);



            writer.close();
            socket.close();
        }
        server.close();

         */
    }

}