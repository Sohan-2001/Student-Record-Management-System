
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class Main extends JFrame {
    int selectedRow;
    private final JTextField nameTextField;
    private final JTextField regNumTextField;
    private final JTextField courseTextField;
    private final JTextField timestampTextField;
    private final JFormattedTextField dobTextField;
    private static final int regNumCounter = 10000000;

    // Constructor to setup GUI components and event handlers
    public Main() {
        setTitle("Registration Form");
        setBounds(300, 90, 900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Components of the Form
        Container c = getContentPane();
        c.setLayout(null);

        // Name Label and Text Field
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setBounds(100, 100, 100, 30);
        c.add(nameLabel);
        nameTextField = new JTextField();
        nameTextField.setBounds(260, 100, 150, 30);
        c.add(nameTextField);

        // Registration Number Label and Text Field (Non-editable)
        JLabel regNumLabel = new JLabel("Registration Number");
        regNumLabel.setBounds(100, 150, 150, 30);
        c.add(regNumLabel);
        regNumTextField = new JTextField(String.valueOf(regNumCounter));
        regNumTextField.setBounds(260, 150, 150, 30);
        regNumTextField.setEditable(false);
        c.add(regNumTextField);

        // Course Label and Text Field
        JLabel courseLabel = new JLabel("Course");
        courseLabel.setBounds(100, 200, 100, 30);
        c.add(courseLabel);
        courseTextField = new JTextField();
        courseTextField.setBounds(260, 200, 150, 30);
        c.add(courseTextField);

        // Date of Birth
        JLabel dobLabel = new JLabel("Date Of Birth");
        dobLabel.setBounds(100, 250, 100, 30);
        c.add(dobLabel);
        dobTextField = new JFormattedTextField(new SimpleDateFormat("dd-MM-yyyy"));
        dobTextField.setBounds(260, 250, 150, 30);
        c.add(dobTextField);

        // Timestamp Label and Text Field (Non-editable)
        JLabel timestampLabel = new JLabel("Timestamp");
        timestampLabel.setBounds(100, 300, 100, 30);
        c.add(timestampLabel);
        timestampTextField = new JTextField();
        timestampTextField.setBounds(260, 300, 150, 30);
        timestampTextField.setEditable(false);
        c.add(timestampTextField);

        // Write Button
        JButton writeButton = new JButton("Write");
        writeButton.setBounds(100, 350, 100, 30);
        c.add(writeButton);

        // Edit Button
        JButton editButton = new JButton("Edit");
        editButton.setBounds(210, 350, 100, 30);
        c.add(editButton);

        // Read Button
        JButton readButton = new JButton("Read");
        readButton.setBounds(320, 350, 100, 30);
        c.add(readButton);

        // Search Field for Registration Number
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(430, 20, 150, 30);
        c.add(searchTextField);
        JLabel regLabel = new JLabel("Registration Number Based Search");
        regLabel.setBounds(600, 20, 200, 30);
        c.add(regLabel);


        // Table to display data
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"Name", "Registration Number", "Course", "D.O.B", "Timestamp"});
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(430, 60, 440, 320);
        c.add(scrollPane);

        writeButton.addActionListener(e -> {

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            timestampTextField.setText(timestamp);

            String name = nameTextField.getText();
            String course = courseTextField.getText();
            String dob = dobTextField.getText();

            // Increment Registration Number
            String getLastRegNumSql = "SELECT RegistrationNumber FROM data ORDER BY RegistrationNumber DESC LIMIT 1";
            Connection con;
            try {
                con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/srm", "root", "");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            Statement stmt;
            try {
                con.createStatement();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            try {
                stmt = con.createStatement();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            ResultSet rs;
            try {
                rs = stmt.executeQuery(getLastRegNumSql);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            // Set the RegistrationNumber to the last number in the database and increment it
            int regNum = regNumCounter; // Default to regNumCounter if no result is found
            try {
                if (rs.next()) {
                    regNum = Integer.parseInt(rs.getString("RegistrationNumber")) + 1;
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            regNumTextField.setText(String.valueOf(regNum));

            timestampTextField.setText(timestamp);

            try {
                Class.forName("com.mysql.jdbc.Driver");

                String sql = "INSERT INTO data (Name, RegistrationNumber, Course, DateOfBirth, Timestamp) VALUES (?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(sql);

                pstmt.setString(1, name);
                pstmt.setString(2, String.valueOf(regNum));
                pstmt.setString(3, course);
                pstmt.setString(4, dob);
                pstmt.setString(5, timestamp);

                int rowsAffected = pstmt.executeUpdate();
                System.out.println(rowsAffected + " rows affected.");

                con.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
            model.addRow(new Object[]{name, String.valueOf(regNum), course, dob, timestamp});

            setVisible(true);

        });

        editButton.addActionListener(e -> {
            selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                // Get the data from the table's model
                String name = model.getValueAt(selectedRow, 1).toString(); // Assuming name is at column index 1
                String course = model.getValueAt(selectedRow, 2).toString(); // Assuming course is at column index 2
                String dob = model.getValueAt(selectedRow, 3).toString(); // Assuming dob is at column index 3
                String timestamp = model.getValueAt(selectedRow, 4).toString(); // Assuming timestamp is at column index 4
                int regNum = Integer.parseInt(model.getValueAt(selectedRow, 0).toString()); // Assuming regNum is at column index 0

                if (!name.isEmpty() && !course.isEmpty() && !dob.isEmpty() && !timestamp.isEmpty()) {

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Connection con = DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/srm", "root", "");

                        String sql = "UPDATE data SET Name = ?, Course = ?, DateOfBirth = ?, Timestamp = ? WHERE RegistrationNumber = ?";

                        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                            pstmt.setString(1, String.valueOf(regNum));
                            pstmt.setString(2, course);
                            pstmt.setString(3, dob);
                            pstmt.setString(4, timestamp);
                            pstmt.setString(5, String.valueOf(name));

                            // Debugging: Print the PreparedStatement to check the formed SQL query
                            System.out.println("Executing SQL: " + pstmt.toString());

                            int rowsAffected = pstmt.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println(rowsAffected + " rows updated.");
                            } else {
                                System.out.println("No rows affected. Please check if the RegistrationNumber exists and the data is correct.");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        con.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields before updating.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a row to edit.");
            }
        });

        readButton.addActionListener(e -> {
            String regNumSearch = searchTextField.getText();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/srm", "root", "");

                // SQL SELECT statement to get data by Registration Number
                String sql = "SELECT * FROM data WHERE RegistrationNumber = ?";
                PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(regNumSearch));

                ResultSet rs = pstmt.executeQuery();

                // Process the result set and populate the table
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();


                Object Name_ = null;
                Object Reg = null;
                Object Course = null;
                Object DOB = null;
                Object Time_ = null;
                while (rs.next()) {
                    Vector<Object> vector = new Vector<>();
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                        vector.add(rs.getObject(columnIndex));
                    }
                    Name_ = vector.get(0);
                    Reg = vector.get(1);
                    Course = vector.get(2);
                    DOB = vector.get(3);
                    Time_ = vector.get(4);

                }
                model.addRow(new Object[]{Name_, Reg, Course, DOB, Time_});

                rs.close();
                pstmt.close();
                con.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }
}
