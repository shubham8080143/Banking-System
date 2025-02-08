import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Account {
	private Connection connection;
	private Scanner scanner;

	// Constructor
	public Account(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}

	/**
	 * openaccount() method: - Generates a unique account number. - Inserts a new
	 * record into the "accounts" table with an initial balance of 0.0.
	 */
	public void openaccount() {
		// Generate a random account number.
		long accountNumber = generate_accountnumber();

		// Ensure the generated account number is unique.
		while (account_exist(accountNumber)) {
			accountNumber = generate_accountnumber();
		}

		// Insert the new account into the database.
		String insertQuery = "INSERT INTO accounts (account_id, balance) VALUES (?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
			stmt.setLong(1, accountNumber);
			stmt.setDouble(2, 0.0); // Initial balance is 0.0
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("✅ Account opened successfully with account number: " + accountNumber);
			} else {
				System.out.println("❌ Failed to open account.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * account_exist(long accountNumber) method: - Checks if an account with the
	 * provided account number exists in the "accounts" table.
	 *
	 * @param accountNumber the account number to check
	 * @return true if the account exists; false otherwise
	 */
	public boolean account_exist(long accountNumber) {
		String query = "SELECT * FROM accounts WHERE account_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setLong(1, accountNumber);
			ResultSet rs = stmt.executeQuery();
			return rs.next(); // returns true if at least one record is found
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * getAccountNumber() method: - Prompts the user to enter an account number and
	 * returns it.
	 *
	 * @return the account number entered by the user.
	 */
	public long getAccountNumber() {
		System.out.println("Enter your account number:");
		return scanner.nextLong();
	}

	/**
	 * generate_accountnumber() method: - Generates a random 10-digit account
	 * number.
	 *
	 * @return a randomly generated account number.
	 */
	public long generate_accountnumber() {
		long min = 1000000000L; // smallest 10-digit number
		long max = 9999999999L; // largest 10-digit number
		return min + (long) (Math.random() * (max - min + 1));
	}
}
