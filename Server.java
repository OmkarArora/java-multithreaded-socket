import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket server;

	private static int port = 9999;

	public static void main(String args[]) {
		try {
			server = new ServerSocket(port);
			while (true) {
				System.out.println("Listening for request");

				Socket socket = server.accept();
				System.out.println("A new client is connected : " + socket);
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				Thread t = new ClientHandler(socket, dis, dos);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientHandler extends Thread {
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;

	private static int balance = 1000;

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run() {
		String received;
		int amount = -99;
		String msg = "";
		while (true) {
			try {
				// Ask user what he wants
				dos.writeUTF("1.withdraw\n2.deposit\n3.check balance\n4.exit");

				// receive the answer from client
				received = dis.readUTF();

				// if(received.equalsIgnoreCase("exit"))
				// {
				// System.out.println("Client " + this.s + " sends exit...");
				// System.out.println("Closing this connection.");
				// this.s.close();
				// System.out.println("Connection closed");
				// break;
				// }

				switch (received) {
				case "1":
					dos.writeUTF("Enter amount: ");
					amount = Integer.parseInt(dis.readUTF());
					msg = performAction("withdraw", amount);
					dos.writeUTF(msg);
					break;
				case "2":
					dos.writeUTF("Enter amount: ");
					amount = Integer.parseInt(dis.readUTF());
					msg = performAction("deposit", amount);
					dos.writeUTF(msg);
					break;
				case "3":
					msg = performAction("check balance", -99);
					dos.writeUTF(msg);
					break;
				case "4":
					dos.writeUTF("exiting");
					this.dis.close();
					this.dos.close();
					return;
				default:
					dos.writeUTF("Invalid input");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	synchronized public String performAction(String action, int amount) throws InterruptedException {

		Thread.sleep(2000);
		String returnMsg = "";
		switch (action) {
		case "withdraw":
			if (amount > balance)
				returnMsg = "Insufficient balance";
			else {
				balance = balance - amount;
				returnMsg = "Withdrawn: Rs" + amount;
			}
			break;
		case "deposit":
			balance += amount;
			returnMsg = "Deposited: Rs" + amount;
			break;
		case "check balance":
			returnMsg = "Current Balance: Rs" + balance;
			break;
		default:
			returnMsg = "SERVER ERROR";
		}
		return returnMsg;
	}
}