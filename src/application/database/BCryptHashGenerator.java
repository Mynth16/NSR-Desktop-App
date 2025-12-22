package application.database;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class to generate BCrypt hashes for passwords.
 * Run this to get hashes for your database.
 */
public class BCryptHashGenerator {
    public static void main(String[] args) {
        // Generate hashes for common test passwords
        String[] passwords = {"admin", "test123", "password"};

        System.out.println("BCrypt Password Hashes:");
        System.out.println("========================\n");

        for (String password : passwords) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println();

            // Verify it works
            boolean verified = BCrypt.checkpw(password, hash);
            System.out.println("Verification: " + (verified ? "✓ SUCCESS" : "✗ FAILED"));
            System.out.println("------------------------\n");
        }

        System.out.println("\nSQL INSERT Examples:");
        System.out.println("===================\n");

        String adminHash = BCrypt.hashpw("admin", BCrypt.gensalt(12));
        System.out.println("INSERT INTO account (username, password, role) VALUES");
        System.out.println("('admin', '" + adminHash + "', 'Admin');");
        System.out.println();

        String staffHash = BCrypt.hashpw("staff123", BCrypt.gensalt(12));
        System.out.println("INSERT INTO account (username, password, role) VALUES");
        System.out.println("('staff', '" + staffHash + "', 'Staff');");
    }
}