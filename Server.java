import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

class BankAccount {
	int balance = 0;

	public BankAccount(int balance) {
		this.balance = balance;
	}
}

public class Server {
	private static ServerSocket server;
	private static int port = 9999;

	static BankAccount account = new BankAccount(1000);
	static ReentrantLock reentrantLock = new ReentrantLock();
	static int sleepTime = 2000;

	public static void main(String args[]) {
		try {
			server = new ServerSocket(port);
			while (true) {
				System.out.println("Listening for request");

				Socket socket = server.accept();
				System.out.println("A new client is connected : " + socket);
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// Thread t = new ClientHandler(socket, dis, dos, account, sleepTime);
				Thread t = new ClientHandlerUsingLock(socket, dis, dos, account, sleepTime, reentrantLock);

				if (sleepTime > 1000)
					sleepTime -= 1000;
				else
					sleepTime += 1000;

				// for unsync
				// sleepTime = 0;
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
	BankAccount account;
	int sleepTime;

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, BankAccount account, int sleepTime) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.account = account;
		this.sleepTime = sleepTime;
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
		// public String performAction(String action, int amount) throws
		// InterruptedException {
		Thread.sleep(sleepTime);
		String returnMsg = "";
		switch (action) {
		case "withdraw":
			if (amount > account.balance)
				returnMsg = "Insufficient balance";
			else {
				account.balance = account.balance - amount;
				returnMsg = "Withdrawn: Rs" + amount;
			}
			break;
		case "deposit":
			account.balance += amount;
			returnMsg = "Deposited: Rs" + amount;
			break;
		case "check balance":
			returnMsg = "Current Balance: Rs" + account.balance;
			break;
		default:
			returnMsg = "SERVER ERROR";
		}
		return returnMsg;
	}
}

class ClientHandlerUsingLock extends Thread {
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;
	ReentrantLock reentrantLock;
	BankAccount account;
	int sleepTime;

	public ClientHandlerUsingLock(Socket s, DataInputStream dis, DataOutputStream dos, BankAccount account,
			int sleepTime, ReentrantLock reentrantLock) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.account = account;
		this.sleepTime = sleepTime;
		this.reentrantLock = reentrantLock;
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String performAction(String action, int amount) {
		if (reentrantLock.tryLock()) {
			reentrantLock.lock();
			try {
				Thread.sleep(sleepTime);
				String returnMsg = "";
				switch (action) {
				case "withdraw":
					if (amount > account.balance)
						returnMsg = "Insufficient balance";
					else {
						account.balance = account.balance - amount;
						returnMsg = "Withdrawn: Rs" + amount;
					}
					break;
				case "deposit":
					account.balance += amount;
					returnMsg = "Deposited: Rs" + amount;
					break;
				case "check balance":
					returnMsg = "Current Balance: Rs" + account.balance;
					break;
				default:
					returnMsg = "SERVER ERROR";
				}
				System.out.println("Unlocking inner lock");
				reentrantLock.unlock();
				return returnMsg;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println("Unlocking outer lock");
				reentrantLock.unlock();
			}
		}
		return "SERVER BUSY";

	}
}