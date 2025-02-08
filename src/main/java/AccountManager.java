import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountManager {
	private Connection connection;
	private Scanner scanner;

	public AccountManager(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}

	/**
	 * Checks and returns the current balance of the account.
	 *
	 * @param accountNumber The account number to check.
	 * @return The current balance, or -1 if the account is not found.
	 */
	public double checkBalance(long accountNumber) {
		double balance = -1;
		String query = "SELECT balance FROM accounts WHERE account_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setLong(1, accountNumber);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				balance = rs.getDouble("balance");
			} else {
				System.out.println("Account not found.");
			}
		} catch (SQLException e) {
			System.out.println("Error checking balance: " + e.getMessage());
			e.printStackTrace();
		}
		return balance;
	}

	/**
	 * Credits (deposits) the specified amount into the account.
	 *
	 * @param accountNumber The account to credit.
	 * @param amount        The amount to deposit.
	 * @return true if the operation was successful; false otherwise.
	 */
	public boolean creditAccount(long accountNumber, double amount) {
		if (amount < 0) {
			System.out.println("Amount cannot be negative.");
			return false;
		}
		String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
			stmt.setDouble(1, amount);
			stmt.setLong(2, accountNumber);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows > 0) {
				System.out.println("Account credited successfully.");
				// Log the credit transaction
				logTransaction(accountNumber, "CREDIT", amount, "Credited " + amount);
				return true;
			} else {
				System.out.println("Account not found or credit failed.");
				return false;
			}
		} catch (SQLException e) {
			System.out.println("Error crediting account: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Debits (withdraws) the specified amount from the account.
	 *
	 * @param accountNumber The account to debit.
	 * @param amount        The amount to withdraw.
	 * @return true if the operation was successful; false otherwise.
	 */
	public boolean debitAccount(long accountNumber, double amount) {
		if (amount < 0) {
			System.out.println("Amount cannot be negative.");
			return false;
		}

		// Check if there is sufficient balance
		double currentBalance = checkBalance(accountNumber);
		if (currentBalance < amount) {
			System.out.println("Insufficient balance.");
			return false;
		}

		String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
			stmt.setDouble(1, amount);
			stmt.setLong(2, accountNumber);
			int affectedRows = stmt.executeUpdate();
			if (affectedRows > 0) {
				System.out.println("Account debited successfully.");
				// Log the debit transaction
				logTransaction(accountNumber, "DEBIT", amount, "Debited " + amount);
				return true;
			} else {
				System.out.println("Account not found or debit failed.");
				return false;
			}
		} catch (SQLException e) {
			System.out.println("Error debiting account: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Withdraws the specified amount from the account.
	 *
	 * @param accountNumber The account number to withdraw from.
	 * @param amount        The amount to withdraw.
	 * @return true if the withdrawal was successful; false otherwise.
	 */
	public boolean withdraw(long accountNumber, double amount) {
		System.out.println("Withdrawing " + amount + " from account " + accountNumber);
		return debitAccount(accountNumber, amount);
	}

	/**
	 * Transfers funds from one account to another. This method uses a transaction
	 * to ensure both operations succeed.
	 *
	 * @param fromAccountNumber The sender's account number.
	 * @param toAccountNumber   The receiver's account number.
	 * @param amount            The amount to transfer.
	 * @return true if the transfer was successful; false otherwise.
	 */
	public boolean transfer(long fromAccountNumber, long toAccountNumber, double amount) {
		try {
			// Begin transaction
			connection.setAutoCommit(false);

			// Check if sender has sufficient funds
			double senderBalance = checkBalance(fromAccountNumber);
			if (senderBalance < amount) {
				System.out.println("Insufficient balance in the sender's account.");
				connection.rollback();
				return false;
			}

			// Debit sender's account
			boolean debitSuccess = debitAccount(fromAccountNumber, amount);
			if (!debitSuccess) {
				connection.rollback();
				return false;
			}

			// Credit receiver's account
			boolean creditSuccess = creditAccount(toAccountNumber, amount);
			if (!creditSuccess) {
				connection.rollback();
				return false;
			}

			// Log the transfer transactions for both accounts
			logTransaction(fromAccountNumber, "TRANSFER_DEBIT", amount,
					"Transferred " + amount + " to account " + toAccountNumber);
			logTransaction(toAccountNumber, "TRANSFER_CREDIT", amount,
					"Received " + amount + " from account " + fromAccountNumber);

			// Commit transaction
			connection.commit();
			System.out.println("Transfer successful.");
			return true;
		} catch (SQLException e) {
			System.out.println("Error during transfer: " + e.getMessage());
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				System.out.println("Error during rollback: " + ex.getMessage());
				ex.printStackTrace();
			}
			return false;
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				System.out.println("Error resetting auto-commit: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Logs a transaction into the transactions table.
	 *
	 * @param accountNumber   The account involved in the transaction.
	 * @param transactionType The type of transaction (e.g., CREDIT, DEBIT,
	 *                        TRANSFER_DEBIT, TRANSFER_CREDIT).
	 * @param amount          The amount involved in the transaction.
	 * @param description     A description of the transaction.
	 */
	private void logTransaction(long accountNumber, String transactionType, double amount, String description) {
		String insertTransactionQuery = "INSERT INTO transactions (account_number, transaction_type, amount, description) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(insertTransactionQuery)) {
			stmt.setLong(1, accountNumber);
			stmt.setString(2, transactionType);
			stmt.setDouble(3, amount);
			stmt.setString(4, description);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error logging transaction: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Prints the transaction history for a given account.
	 *
	 * @param accountNumber The account number whose history will be printed.
	 */
	public void printTransactionHistory(long accountNumber) {
		String query = "SELECT transaction_id, transaction_type, amount, transaction_time, description "
				+ "FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setLong(1, accountNumber);
			ResultSet rs = stmt.executeQuery();
			System.out.println("Transaction History for Account: " + accountNumber);
			System.out.println("-------------------------------------------------------");
			while (rs.next()) {
				int transactionId = rs.getInt("transaction_id");
				String transactionType = rs.getString("transaction_type");
				double amount = rs.getDouble("amount");
				String transactionTime = rs.getString("transaction_time");
				String description = rs.getString("description");
				System.out.printf("ID: %d | Type: %s | Amount: %.2f | Time: %s | Description: %s%n", transactionId,
						transactionType, amount, transactionTime, description);
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving transaction history: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
