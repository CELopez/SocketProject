//The client will:
// 1) Accept a machine name and port number to connect to as command line arguments.
// 2) Connect to the server.
// 3) Prompt for and send the users name.
// 4) Present the following menu of choices to the user:
//    a. Display the names of all known users.
//    b. Display the names of all currently connected users.
//    c. Send a text message to a particular user. messages can only be up to 80 chars long
//    d. Send a text message to all currently connected users. messages can only be up to 80 chars long
//    e. Send a text message to all known users.
//    f. Get my messages. remove messages from server
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
    private int systemInstruction;
    private String line;
    /*System Instructions:
    0 - Repeated User Name indicator
    1 - Next value is required by Socket
    2 - message full indicator
    */
    public static boolean isDuplicate, isFull = false;

    public void communicate()
    {
            scan = new Scanner(System.in);
            System.out.println("Enter your name: ");
            String name = scan.nextLine();

            //Send data over socket
            sendToServer(name);

            //Receive text from server
            receive();

            //Condition if there is a duplicate of this name on server
            receive();
            if(isDuplicate)
            {
                for(int i=0; i<3; i++)
                {
                    try{
                        do{

                        }while(!in.ready());
                        System.out.println(in.readLine());
                    }catch(IOException ex)
                    {
                        System.out.println("Client Side IO Exception at duplicate instruction read");
                    }

                }
                try{
                    do{
                        System.out.println("caught waiting");
                    }while(!in.ready());
                    System.out.print(in.readLine()+" ");
                }catch(IOException ex)
                {
                    System.out.println("Client Side IO Exception at duplicate instruction read");
                }

                do {
                        temp = scan.nextInt();
                        System.out.println("temp input:"+temp);
                        sendToServer(temp);
                        System.out.println("temp was sent");
                        receive();

                }while(isDuplicate);
            }

        scan = new Scanner(System.in);

        while(true) {
            //Request User choose menu option and send to server
            pullUpMenu();
            receive();

/*            if(temp == 3)
            {
                try {
                    temp = scan.nextInt();
                }catch(InputMismatchException exception)
                {
                    System.out.println("Invalid input");
                    System.exit(1);
                }
                sendToServer(temp);

                //if Other is chosen, asks for name then sends name to server
                if(temp == SocketThrdServer.clients.size()-1)
                {
                    receive(); //server request for name of user
                    String str = scan.nextLine();
                    sendToServer(str);
                    receive(); //server sends confirmation that message was sent

                }
            }
            else if (temp == 4 || temp == 5)
            {
                receive();//server requests message
                String str = scan.nextLine();
                sendToServer(str);
                receive();//server sends confirmation that message was sent
            }
*/
        }


    }

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
            System.out.println("pullupMenu input was :"+temp);
        }catch(InputMismatchException exception)
        {
            System.out.println("Invalid input");
            System.exit(1);
        }
        sendToServer(temp);
    }
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
        System.out.println("Receive was called");
        try {
            do {

            }while(!in.ready());
        }catch(IOException in_wrong)
        {
            System.out.println("Error: couldn't receive, SocketClient_BufferedReader not ready");
        }
                try {
                    line = in.readLine();
                    if (line.charAt(0) == '~' && line.charAt(1) == '!') {
                        //set systemInstruction equal to what comes after flag ~!
                        systemInstruction = Integer.parseInt(line.substring(2).trim());
                        switch (systemInstruction) {
                            //toggle isDuplicate
                            case 0:
                                if(isDuplicate == false)
                                    isDuplicate = true;
                                else if(isDuplicate == true)
                                    isDuplicate = false;
                                break;
                            //toggle isFull
                            case 1:
                                if(isFull == false)
                                    isFull = true;
                                else if(isFull == true)
                                    isFull = false;
                            case 9:
                                closeClientSession();
                            default:
                                break;
                        }

                    } else {
                        System.out.println("\nServer Message: ");
                        System.out.println(line+"\n");
                    }
                } catch (IOException e) {
                    System.out.println("Read failed");
                    System.exit(1);
                }
    }

    public static void closeClientSession()
    {
        if(isDuplicate)
        {
            System.out.println("This user is already connected. Goodbye");
        }
        System.exit(0);
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