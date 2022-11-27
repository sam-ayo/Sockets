import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

class BetterServer {
    static ServerSocket serverSocket;
    static Socket clientSocket;
    static BufferedReader in;
    static PrintWriter out;
    static String output = "";
    static String eor = "[EOR]"; // a code for end-of-response

    // establishing a connection
    private static void setup() throws IOException {
        
        serverSocket = new ServerSocket(0);
        toConsole("Server port is " + serverSocket.getLocalPort());
        
        clientSocket = serverSocket.accept();

        // get the input stream and attach to a buffered reader
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        // get the output stream and attach to a printwriter
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        toConsole("Accepted connection from "
                 + clientSocket.getInetAddress() + " at port "
                 + clientSocket.getPort());
            
        sendGreeting();
    }
    
    // the initial message sent from server to client
    private static void sendGreeting()
    {
        appendOutput("Welcome to PigNet!\n");
    }
    
    // what happens while client and server are connected
    private static void talk() throws IOException {
        /* placing echo functionality into a separate private method allows it to be easily swapped for a different behaviour */
        authUser();
        disconnect();
    }
    /*
    * Give user 5 chances to get username and password right. Disconnect if user fails on 5th attempt
    * */
    private static void authUser() throws IOException
    {
        int loginAttempts = 5;
        int i = 0;
        while(i < loginAttempts){
            String [] userDetails = getUserDetails();
            String username = userDetails[0];
            String password = userDetails[1];
            if (checkUser(username,password)){
                return;
            }
            i++;
        }
        disconnect();
    }
    /*
    * Get username and password from client
    * return array of username and password
    * */
    private static String [] getUserDetails() throws IOException{
        appendOutput("Enter your username:");
        sendOutput();
        appendOutput("Enter your password:");
        sendOutput();
        String username = in.readLine();
        String password = in.readLine();
        return new String[]{username, password};
    }

    /*
    * Check username and password input from client
    * @params: username, password
    * return boolean
    * check = true if username and password are correct
    *
    * */
    private static boolean checkUser(String username, String password) throws IOException {
        boolean check = false;
        if (username.equals("Peppa") && password.equals("OINK")) {
            PeppaBank bank = new PeppaBank(username);
            bank.main();
            check = true;
        }
        return check;
    }

    private static void disconnect() throws IOException {
        out.close();
        toConsole("Disconnected.");
        System.exit(0);
    }
    
    // add a line to the next message to be sent to the client
    public static void appendOutput(String line) {
        output += line + "\r";
    }
    
    // send next message to client
    public static void sendOutput() {
        out.println( output + eor);
        out.flush();
        output = "";
    }
    
    // because it makes life easier!
    public static void toConsole(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        try {
            setup();
            talk();
        }
        catch( IOException ioex ) {
            toConsole("Error: " + ioex );
        }
    }
}
class PeppaBank{
    int balance = 0;	// in cents
    ArrayList<Integer> transactions = new ArrayList<Integer>();
    DecimalFormat df = new DecimalFormat("#0.00");
    String username;

    public PeppaBank(String name){
        username = name;
    }

    public void main () throws IOException{
        BetterServer.appendOutput("Welcome "+username+"!\n");
        int selection = 0;
        while (selection != 4)
        {
            selection = menu();
            switch (selection) {
                case 1 -> deposit();
                case 2 -> withdraw();
                case 3 -> viewTransactions();
                case 4 -> exit();
                default -> tryAgain();
            }
        }

    }

    private int menu() throws IOException{
        String menuOptions =
                """
                \n
                Your current balance is:\t"""+inDollars(balance)+"""
                \n
                Please select an option:
                1. Make a deposit
                2. Make a withdrawal
                3. View a list of all transactions
                4. Exit
                Your selection:
                """;

        BetterServer.appendOutput(menuOptions);
        BetterServer.sendOutput();
        return Integer.parseInt(BetterServer.in.readLine());
    }

    private void deposit() throws IOException{
        BetterServer.appendOutput("Enter an amount to deposit in $$.cc:");
        BetterServer.sendOutput();
        double amount = Double.parseDouble(BetterServer.in.readLine());
        try
        {
            int inCents = Math.abs((int) (amount * 100));
            balance += inCents;
            transactions.add(inCents);
        }
        catch ( Exception ex )
        {
            wrongFormat();
        }
    }
    private void withdraw() throws IOException{
        BetterServer.appendOutput("Enter an amount to withdraw in $$.cc:");
        BetterServer.sendOutput();
        double amount = Double.parseDouble(BetterServer.in.readLine());
        try
        {
            int inCents = Math.abs((int) (amount * 100));
            balance -= inCents;
            transactions.add(-inCents);
        }
        catch ( Exception ex )
        {
            wrongFormat();
        }
    }
    private void viewTransactions(){
        for (Integer transaction: transactions){
            if(transaction >= 0){
                BetterServer.appendOutput("deposit: "+inDollars(transaction));
            }else {
                BetterServer.appendOutput("withdrawal: "+inDollars(-transaction));
            }
        }
        BetterServer.sendOutput();
    }
    private void exit(){
        BetterServer.appendOutput("Thank you for using PigNet. Have a nice day!");
        BetterServer.sendOutput();
        System.exit(0);
    }
    private void tryAgain(){
        BetterServer.appendOutput("Please make a valid selection!");
        BetterServer.sendOutput();
    }
    private void wrongFormat(){
        BetterServer.appendOutput("Not a valid amount in $$.cc");
        BetterServer.sendOutput();
    }

    private String inDollars(int inCents){
        return "$" + df.format(inCents/100.00);
    }


}