import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Socket;

/* client sends messages to server and receives responses
   this version allows the server to send an initial greeting
   and send multiples lines in each response */
class BetterClient {
    private static InetAddress server;
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static BufferedReader user;
    private static String fromServer, userInput;
    private static String eor = "[EOR]";  // a code for end-of-response
    private static String exitCommand = "EXIT";
    
    private static void setup(String [] args) {
        // check arguments are correct
        if (args.length != 2) {
            toConsole("Usage: java LineClient host port");
            System.exit(0);
        }
        int port = 0;
        String host = null;
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch( NumberFormatException nfex ) {
            toConsole("Bad port number");
            System.exit(0);
        }

        try {
            /* determine the address of the server and connect to it */
            server = InetAddress.getByName(host);
            socket = new Socket(server, port);
            toConsole("Connected: " + socket);
            toConsole("\nType " + exitCommand + " to disconnect\n");
                        
            // get the input stream and attach to a buffered reader
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // get the output stream and attach to a printwriter
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // set up a buffered reader for user input
            user = new BufferedReader(new InputStreamReader(System.in));
            
            // get an initial greeting from the server
            while (!(fromServer = in.readLine()).equals(eor)) {
                toConsole(fromServer);
            }
        }
        catch(UnknownHostException uhex) {
            toConsole("bad host name");
            System.exit(0);
        }
        catch(IOException ioex) {
            toConsole("io error:" + ioex);
            System.exit(0);
        }
    }
    
    // repeat a cycle of client requests and server responses
    private static void talk() {
        try {
            while (true) {
                // get a line of user input and send to server
                userInput = user.readLine();
                out.println(userInput);
                out.flush(); // asks for it to be sent
                
                if (userInput.toUpperCase().equals("EXIT")) {
                    disconnect();
                }
                
                // get all current input lines from the server (response)
                while (!(fromServer = in.readLine()).equals(eor)) {
                    toConsole(fromServer);
                }
            }
        }
        catch(IOException ioex) {
            toConsole("io error:" + ioex);
            System.exit(0);
        }
        catch(NullPointerException nex) {
            toConsole("The server has disconnected - goodbye!");
            System.exit(0);
        }
    }
    
    private static void disconnect() {
        try {
            in.close();
            out.close();
            toConsole("\nDisconnected\n");
        }
        catch(IOException ioex) {
            toConsole("io error:" + ioex);
            System.exit(0);
        }
        System.exit(0);
    }
    
    // worth using because it makes so much code shorter!
    private static void toConsole(String message) {
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        setup(args);
        talk();
    }
}
