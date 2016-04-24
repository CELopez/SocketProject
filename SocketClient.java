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
    int temp, serverInfo=0;
    Scanner scan;
    private int systemInstruction;
    private String line;
    /*System Instructions:
    0 - isduplicate toggle indicator
    1 - message full toggle indicator
    2 - isLooping flag toggle indicator
    3 - Next value is required by Socket
    */
    public static boolean isDuplicate, isFull, isLooping = false;

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
                    }while(!in.ready());
                    System.out.print(in.readLine()+" ");
                }catch(IOException ex)
                {
                    System.out.println("Client Side IO Exception at duplicate instruction read");
                }

                do {
                        temp = scan.nextInt();
                        sendToServer(temp);
                        receive();

                }while(isDuplicate);
            }

        scan = new Scanner(System.in);

        while(true) {
            //Request User choose menu option and send to server
            pullUpMenu();
            receive();
            if(temp == 1 || temp == 2 || temp == 3|| temp == 6)
            {
                receive();
                do
                {
                    //receive list until no more lines are provided
                    receiveMultiLines();
                }while(isLooping);
            }

            if(temp == 3)
            {
                try {
                    temp = scan.nextInt();
                }catch(InputMismatchException exception)
                {
                    System.out.println("Invalid input");
                    System.exit(1);
                }
                //send user's choice of who to send msg to, to the server
                sendToServer(temp);

                String str = "";
                receive();

                //if Other is chosen, asks for name then sends name to server
                if(temp == serverInfo)
                {
                    scan.nextLine();
                    receive(); //server request for name of user
                    str = scan.nextLine();
                    sendToServer(str);
                }
                    //Condition if there is a duplicate of this name on server OR full inbox
                    receive();      //if no duplicate nor full inbox, this is a prompt to enter msg
                    if(isDuplicate)
                    {
                        receive(); //recieve message "Exiting to main menu"
                    }
                    else if(!isFull)
                    {
                        //recieve msg from user and send to server
                        scan.nextLine();
                        str = scan.nextLine();
                        sendToServer(str);
                    }//else there is NOT a duplicate but messages ARE full
                    else
                    {
                        //receive message of inbox being full
                        receive();
                    }



                //toggle of either isDuplicate or isFull OR confirmation of msg being sent
                receive();
            }
            else if (temp == 4 || temp == 5)
            {   
                //server already requested message, user composes and sends message
                scan.nextLine();
                String str = scan.nextLine();
                sendToServer(str);
                receive();  //receive isLooping flag
                do
                {
                    receive();//server sends confirmation that message was sent/toggle for isLooping
                }while(isLooping);
            }

        }


    }

    //prints out menu, takes user menu choice, and sends choice to server
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
    public void receiveMultiLines()
    {
        try {
            do {

            }while(!in.ready());
        }catch(IOException in_wrong)
        {
            System.out.println("Error: couldn't receive, SocketClient_BufferedReader not ready");
        }
        try
        {
            line = in.readLine();
            while(line.length()==0)
            {
                try {
                    do {

                    }while(!in.ready());
                    line = in.readLine();
                }catch(IOException in_wrong)
                {
                    System.out.println("Error: couldn't receive, SocketClient_BufferedReader not ready");
                }
            }

            if (line.charAt(0) == '~' && line.charAt(1) == '!')
            {
                //set systemInstruction equal to what comes after flag ~!
                systemInstruction = Integer.parseInt(""+line.charAt(2));
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
                        //toggle isLooping
                    case 2:
                        if(isLooping == false)
                            isLooping = true;
                        else if(isLooping == true)
                            isLooping = false;
                        break;
                    case 3:
                            serverInfo = Integer.parseInt(line.substring(3));
                        break;
                    case 9:
                        closeClientSession();
                    default:
                        break;
                }

            } else {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Read failed");
            System.exit(1);
        }

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

        try 
        {
            line = in.readLine();
            while(line.length()==0 || line == null || line =="")
            {
                try {
                    do {
                    }while(!in.ready());
                    line = in.readLine();
                }catch(IOException in_wrong)
                {
                    System.out.println("Error: couldn't receive, SocketClient_BufferedReader not ready");
                }
            }

        if (line.charAt(0) == '~' && line.charAt(1) == '!') 
        {
            //set systemInstruction equal to what comes after flag ~!
            systemInstruction = Integer.parseInt(""+line.charAt(2));
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
                //toggle isLooping
                case 2:
                    if(isLooping == false)
                        isLooping = true;
                    else if(isLooping == true)
                        isLooping = false;
                    break;
                case 3:
                    serverInfo = Integer.parseInt(line.substring(3));
                    break;
                case 9:
                    closeClientSession();
                default:
                    break;
            }

        } else {
            System.out.println("\nServer Message: ");
            System.out.println(line);
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