
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
    public static boolean isDuplicate=false;

    public void communicate()
    {
        scan = new Scanner(System.in);
        do {
            System.out.println("Enter your name: ");
            String name = scan.nextLine();

            //Send data over socket
            sendToServer(name);

            //Receive text from server
            receive();

            if(isDuplicate)
            {

            }

        }while(!isDuplicate);


        while(true) {
            //Request User choose menu option and send to server
            pullUpMenu();
            receive();
            if(temp == 3)
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

        try
        {
            String line = in.readLine();
            System.out.println("Text received: ");
            System.out.println(line);
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