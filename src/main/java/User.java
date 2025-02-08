import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {
	private Connection connection;
	private Scanner scanner;

	// Constructor
	public User(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}

	// 🔹 REGISTER USER METHOD
	public void register() {
		scanner.nextLine(); // Consume newline
		System.out.println("Enter Name:");
		String name = scanner.nextLine();
		System.out.println("Enter Email:");
		String email = scanner.nextLine();
		System.out.println("Enter Password:");
		String security_pin = scanner.nextLine();

		// Check if user already exists
		if (user_exist(email)) {
			System.out.println("⚠️ User already exists with this email: " + email);
			return;
		}

		// Register user
		String register_query = "INSERT INTO users (name, email, security_pin) VALUES (?, ?, ?)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(register_query)) {
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, security_pin);
			int affectedRows = preparedStatement.executeUpdate();
			if (affectedRows > 0) {
				System.out.println("✅ Registration successful!");
			} else {
				System.out.println("❌ Registration failed.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 🔹 CHECK IF USER ALREADY EXISTS
	public boolean user_exist(String email) {
		String check_query = "SELECT * FROM users WHERE email = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(check_query)) {
			preparedStatement.setString(1, email);
			ResultSet resultSet = preparedStatement.executeQuery(); // Use executeQuery() for SELECT
			return resultSet.next(); // If result exists, user exists
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// 🔹 LOGIN METHOD
	public void login() {
		scanner.nextLine();
		System.out.println("Enter Email:");
		String email = scanner.nextLine();
		System.out.println("Enter Security Key:");
		String password = scanner.nextLine();

		String login_query = "SELECT * FROM users WHERE email = ? AND security_pin = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(login_query)) {
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				System.out.println("✅ Login successful! Welcome, " + resultSet.getString("name"));
			} else {
				System.out.println("❌ Invalid email or password.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 🔹 LOGOUT METHOD
	public void logout() {
		System.out.println("✅ User logged out successfully.");
	}
}
