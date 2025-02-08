import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Banking_App {

	public static void main(String[] args) {
		// Database connection details
		String url = "jdbc:mysql://localhost:3306/banking_system"; // Change DB name if needed
		String dbUser = "root"; // Change if necessary
		String dbPassword = "root"; // Change if necessary

		try {
			// Load MySQL JDBC Driver (Optional for newer versions)
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Connect to the database
			Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
			System.out.println("✅ Connected to MySQL successfully!");

			// Create a Scanner for user input
			Scanner scanner = new Scanner(System.in);

			// Create objects for handling user, account, and account management operations
			User userObj = new User(conn, scanner);
			Account accountObj = new Account(conn, scanner);
			AccountManager accountManager = new AccountManager(conn, scanner);

			boolean exit = false;
			boolean loggedIn = false;

			while (!exit) {
				System.out.println("\n===== Banking System Menu =====");
				System.out.println("1. Register");
				System.out.println("2. Login");
				System.out.println("3. Open Account");
				System.out.println("4. Check Balance");
				System.out.println("5. Deposit");
				System.out.println("6. Withdraw");
				System.out.println("7. Transfer");
				System.out.println("8. Print Transaction History");
				System.out.println("9. Logout");
				System.out.println("10. Exit");
				System.out.print("Select an option: ");

				int option = scanner.nextInt();
				switch (option) {
				case 1:
					// Register a new user
					userObj.register();
					break;
				case 2:
					// Login user
					userObj.login();
					// Here, we assume the login is successful.
					loggedIn = true;
					break;
				case 3:
					// Open a new account (user must be logged in)
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						accountObj.openaccount();
						// The openaccount() method prints the new account number.
					}
					break;
				case 4:
					// Check account balance
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						System.out.print("Enter your account number: ");
						long accNum = scanner.nextLong();
						double balance = accountManager.checkBalance(accNum);
						if (balance != -1) {
							System.out.println("Your balance is: " + balance);
						}
					}
					break;
				case 5:
					// Deposit funds (credit account)
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						System.out.print("Enter your account number: ");
						long accNum = scanner.nextLong();
						System.out.print("Enter deposit amount: ");
						double depositAmount = scanner.nextDouble();
						accountManager.creditAccount(accNum, depositAmount);
					}
					break;
				case 6:
					// Withdraw funds (debit account)
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						System.out.print("Enter your account number: ");
						long accNum = scanner.nextLong();
						System.out.print("Enter withdrawal amount: ");
						double withdrawAmount = scanner.nextDouble();
						accountManager.withdraw(accNum, withdrawAmount);
					}
					break;
				case 7:
					// Transfer funds between accounts
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						System.out.print("Enter your account number (sender): ");
						long senderAcc = scanner.nextLong();
						System.out.print("Enter receiver's account number: ");
						long receiverAcc = scanner.nextLong();
						System.out.print("Enter transfer amount: ");
						double transferAmount = scanner.nextDouble();
						accountManager.transfer(senderAcc, receiverAcc, transferAmount);
					}
					break;
				case 8:
					// Print transaction history for an account
					if (!loggedIn) {
						System.out.println("Please login first.");
					} else {
						System.out.print("Enter your account number: ");
						long accNum = scanner.nextLong();
						accountManager.printTransactionHistory(accNum);
					}
					break;
				case 9:
					// Logout user
					if (!loggedIn) {
						System.out.println("No user is currently logged in.");
					} else {
						userObj.logout();
						loggedIn = false;
					}
					break;
				case 10:
					// Exit the application
					exit = true;
					System.out.println("Exiting the application. Goodbye!");
					break;
				default:
					System.out.println("Invalid option. Please try again.");
				}
			}

			// Clean up resources
			scanner.close();
			conn.close();
		} catch (ClassNotFoundException e) {
			System.out.println("❌ MySQL JDBC Driver not found.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("❌ Connection error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
