//The client will:
// 1) Accept a machine name and port number to connect to as command line arguments.
// 2) Connect to the server.
// 3) Prompt for and send the userâ€™s name.
// 4) Present the following menu of choices to the user:
//    a. Display the names of all known users.
//    b. Display the names of all currently connected users.
//    c. Send a text message to a particular user. //messages can only be up to 80 chars long
//    d. Send a text message to all currently connected users. //messages can only be up to 80 chars long
//    e. Send a text message to all known users.
//    f. Get my messages.  //remove messages from server
//    g. Exit.
// 5) Interact with the server to support the menu choices.
// 6) Ask the user for the next choice or exit.

import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.net.*;

public class SocketClient
{
    Socket socket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    int temp;
    Scanner scan;

    public void communicate()
    {
        scan = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String name = scan.nextLine();


        //Send data over socket
        sendToServer(name);

        //Receive text from server
        receive();

        while(true) {
            //Request User choose menu option and send to server
            pullUpMenu();
            sendToServer(temp);
            //Retrieve information based on what information is requested
            switch(temp){
                case 1:
                    //receive mega-message of all known users
                    receive();
                    break;
                case 2:
                    receive();
                    break;
                case 3:
                    //have user type message BLAH BLAH BLAH
                    //send message
                    //print line indicating if message sent or not sent
                    receive();
                    break;
                case 4:
                    //sendMessageToAllConnectedUsers();
                    break;
                case 5:
                    //sendMessageToAllKnownUsers();
                    break;
                case 6:
                    //getMyMessages();
                    break;
                case 7:
                    //closeSocket();
                    break;
                default:
                    //System.out.println(" Invalid Menu");
                    break;
            }//end switch()
        }

    }//end communicate()

    public void pullUpMenu()
    {
        try {
            System.out.println("Menu: ");
            System.out.println("1) Display the names of all known users");
            System.out.println("2) Display the names of all currently connected users");
            System.out.println("3) Send a text message to a particular user");
            System.out.println("4) Send a text message to all currently connected users");
            System.out.println("5) Send a text message to all known users ");
            System.out.println("6) Get my messages");
            System.out.println("7) Exit");
            System.out.print("Enter option number indicating your choice: ");

            temp = scan.nextInt();
        }catch(InputMismatchException exception)
        {
            System.out.println("Invalid input");
            System.exit(1);
        }
    }//end pullUpMenu()

    public void sendToServer(int n) //for int/menu entries
    {
        out.println(n);
    }

    public void sendToServer(String str)// for string entries
    {
        out.println(str);
    }

    public void receive()
    {

        try
        {
            String line = in.readLine();
            System.out.println("Text received: " + line);
        }
        catch (IOException e)
        {
            System.out.println("Read failed");
            System.exit(1);
        }
    }



    public void listenSocket(String host, int port)
    {
        //Create socket connection
        try
        {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.out.println("Unknown host");
            System.exit(1);
        }
        catch (IOException e)
        {
            System.out.println("No I/O");
            System.exit(1);
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage:  client hostname port");
            System.exit(1);
        }

        SocketClient client = new SocketClient();

        String host = args[0];
        int port = Integer.valueOf(args[1]);
        client.listenSocket(host, port);
        client.communicate();
    }

}