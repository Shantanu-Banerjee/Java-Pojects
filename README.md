E-Voting System (Java + Swing + JDBC)
This is a Java-based E-Voting System project that simulates a simple electronic voting platform with login, registration, secure authentication, vote casting, and admin result management. Built with Java Swing for GUI and MySQL for data persistence, it showcases concepts like authentication, GUI design, JDBC, and secure password hashing.

📌 Features
✅ User Registration & Login (with SHA-256 password hashing)

✅ Role-based Panels: Admin & Voter

✅ Vote Casting (one vote per user restriction)

✅ Live Result Viewing (Admin only)

✅ Logout / Session Handling

✅ Secure Vote Update with Transactions

✅ Simple and intuitive UI with Swing

🛠️ Technologies Used

Component	Technology
Language	Java
GUI	Java Swing
Database	MySQL
DB Connector	JDBC
Password Hash	SHA-256 (Java Security)
📂 Database Schema
Make sure you have a MySQL database named evoting with the following tables:

1. users
sql
Copy
Edit
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    role ENUM('admin', 'voter') NOT NULL DEFAULT 'voter',
    has_voted BOOLEAN DEFAULT FALSE
);
2. candidates
sql
Copy
Edit
CREATE TABLE candidates (
    id INT PRIMARY KEY AUTO_INCREMENT,
    candidate_name VARCHAR(100) UNIQUE NOT NULL,
    votes INT DEFAULT 0
);
🚀 Getting Started
Prerequisites:
Java JDK (8 or above)

MySQL Server

JDBC Driver (mysql-connector-java-x.x.x.jar)

An IDE (like IntelliJ, Eclipse, or NetBeans)

🧑‍💻 Setup Instructions
Clone the Repository

bash
Copy
Edit
git clone https://github.com/yourusername/Java-projects.git
cd Java-projects
Set Up MySQL Database

Create the evoting database and both tables as shown above.

Insert sample candidates into the candidates table:

sql
Copy
Edit
INSERT INTO candidates (candidate_name) VALUES ('Alice'), ('Bob'), ('Charlie');
Update DB Credentials

In the initialize() method, modify:

java
Copy
Edit
String user = "your_mysql_username";
String password = "your_mysql_password";
Compile and Run the Project

Add JDBC driver to your classpath.

Run the EVotingSystem.java file.

👤 User Roles
Admin: Can view live results.

Add an admin manually in the DB:

sql
Copy
Edit
INSERT INTO users (username, password, role) 
VALUES ('admin', SHA2('adminpass', 256), 'admin');
Voter: Can register, login, and vote once.

🔐 Security
Passwords are hashed using SHA-256 before storage.

Transactions ensure atomic vote + status update.

Basic session management to prevent back navigation.

📸 Screenshots (Optional)
Add screenshots of login, voting, and admin panels to visually represent the project.

🤝 Contribution
Pull requests and suggestions are welcome! If you find a bug or want to add a feature, feel free to fork and contribute.
