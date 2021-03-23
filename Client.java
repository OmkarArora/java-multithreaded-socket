import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private static int port = 9999;

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {

		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Scanner sc = new Scanner(System.in);

		socket = new Socket(host.getHostName(), port);
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
		while (true) {
			String menu = dis.readUTF();
			System.out.println(menu);
			String input = sc.nextLine();
			dos.writeUTF(input);

			// If client sends exit,close this connection
			// and then break from the while loop
			if (input.equalsIgnoreCase("4")) {
				System.out.println("Closing this connection : " + socket);
				socket.close();
				System.out.println("Connection closed");
				break;
			}

			// For withdraw and deposit, user will be prompted to enter amount
			if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("2")) {
				String reply = dis.readUTF();
				System.out.println(reply);
				input = sc.nextLine();
				dos.writeUTF(input);
				reply = dis.readUTF();
				System.out.println(reply);
			}

			// handling check balance reply
			if (input.equalsIgnoreCase("3")) {
				String reply = dis.readUTF();
				System.out.println(reply);
			}

		}

		dis.close();
		dos.close();
		sc.close();
	}
}
