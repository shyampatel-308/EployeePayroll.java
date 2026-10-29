import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

class Employee {

    private int id;
    private String name;
    private double basicSalary;
    private String department;

    //Default constructor
    public Employee() {
        id = 0;
        name = "Unknown";
        basicSalary = 0;
        department = "HR";
    }

    //Parameterized constructor (validates using the setters)
    public Employee(int id, String name, double basicSalary, String department) {
        setId(id);
        setName(name);
        setBasicSalary(basicSalary);
        setDepartment(department);
    }

    //Copy constructor
    public Employee(Employee other) {
        this.id = other.id;
        this.name = other.name;
        this.basicSalary = other.basicSalary;
        this.department = other.department;
    }

    //Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBasicSalary() { return basicSalary; }
    public String getDepartment() { return department; }

    //Setters with validation
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be a positive number.");
        }
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty.");
        }
        if (name.contains("|")) {
            throw new IllegalArgumentException("Employee name cannot contain the | character.");
        }
        this.name = name.trim();
    }

    public void setBasicSalary(double basicSalary) {
        if (!(basicSalary > 0) || Double.isInfinite(basicSalary)) {
            throw new IllegalArgumentException("Basic salary must be greater than 0.");
        }
        this.basicSalary = basicSalary;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a department.");
        }
        this.department = department;
    }

    //Salary calculation
    // Allowances = 20% of basic salary
    public double getAllowances() {
        return basicSalary * 0.20;
    }

    // Deductions = 10% of basic salary
    public double getDeductions() {
        return basicSalary * 0.10;
    }

    // Net Salary = Basic + Allowances - Deductions
    public double getNetSalary() {
        return basicSalary + getAllowances() - getDeductions();
    }

    //File format: id|name|basic|department
    public String toFileString() {
        return id + "|" + name + "|" + basicSalary + "|" + department;
    }

    // Build an Employee from one line of the file.
    // Throws IllegalArgumentException if the line is invalid.
    public static Employee fromFileString(String line) {
        String[] p = line.split("\\|");
        if (p.length != 4) {
            throw new IllegalArgumentException("Invalid record: " + line);
        }
        return new Employee(
                Integer.parseInt(p[0].trim()),
                p[1],
                Double.parseDouble(p[2].trim()),
                p[3]);
    }
}

//GUI + main
public class EployeePayroll extends JFrame {

    private static final String FILE_NAME = "employees.txt";

    // Collection that stores all employees
    private ArrayList<Employee> list = new ArrayList<>();

    // Input fields
    private JTextField idField = new JTextField(15);
    private JTextField nameField = new JTextField(15);
    private JTextField salaryField = new JTextField(15);
    private JComboBox<String> deptBox = new JComboBox<>(
            new String[]{"HR", "IT", "Finance", "Sales", "Admin"});

    // Buttons
    private JButton addBtn = new JButton("Add Employee");
    private JButton saveBtn = new JButton("Save");
    private JButton loadBtn = new JButton("Load");
    private JButton viewBtn = new JButton("View All");

    // Table
    private String[] columns = {"ID", "Name", "Department", "Basic Salary",
                                "Allowances", "Deductions", "Net Salary"};
    private DefaultTableModel model = new DefaultTableModel(columns, 0);
    private JTable table = new JTable(model);

    public EployeePayroll() {
        setTitle("Employee Payroll System");
        setLayout(new BorderLayout(10, 10));

        //Form panel
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        form.add(new JLabel("Employee ID:"));        form.add(idField);
        form.add(new JLabel("Employee Name:"));      form.add(nameField);
        form.add(new JLabel("Basic Salary:"));       form.add(salaryField);
        form.add(new JLabel("Department:"));         form.add(deptBox);

        //Button panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.add(addBtn);
        buttons.add(saveBtn);
        buttons.add(loadBtn);
        buttons.add(viewBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        //Table
        table.setDefaultEditor(Object.class, null);       // cells cannot be edited
        table.setRowHeight(25);
        table.getTableHeader().setVisible(false);         // header hidden until View All
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(800, 220));

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        center.add(scroll, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        //Button actions
        addBtn.addActionListener(e -> addEmployee());
        saveBtn.addActionListener(e -> saveToFile());
        loadBtn.addActionListener(e -> loadFromFile(true));
        viewBtn.addActionListener(e -> viewAll());

        loadFromFile(false);   // load saved records silently at startup

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    //Add Employee
    private void addEmployee() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            double basic = Double.parseDouble(salaryField.getText().trim());
            String dept = (String) deptBox.getSelectedItem();

            if (idExists(list, id)) {
                showMessage("Employee ID " + id + " already exists.");
                return;
            }

            // Parameterized constructor
            Employee emp = new Employee(id, name, basic, dept);
            list.add(emp);

            showMessage("Employee added successfully!");

          

        } catch (NumberFormatException ex) {
            showMessage("ID and Salary must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage());   // validation message from Employee class
        }
    }

    //Save
    private void saveToFile() {
        try (FileWriter fw = new FileWriter(FILE_NAME)) {
            for (Employee e : list) {
                fw.write(e.toFileString() + "\n");
            }
            showMessage("Saved " + list.size() + " employee(s) to " + FILE_NAME);
        } catch (IOException ex) {
            showMessage("Error while saving file: " + ex.getMessage());
        }
    }

    //Load
    private void loadFromFile(boolean showMessages) {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            if (showMessages) {
                showMessage("No saved file found yet. Add employees and click Save.");
            }
            return;
        }

        ArrayList<Employee> loaded = new ArrayList<>();
        int skipped = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Employee emp = Employee.fromFileString(line);
                    if (idExists(loaded, emp.getId())) {
                        skipped++;            // duplicate ID in file
                    } else {
                        loaded.add(emp);
                    }
                } catch (IllegalArgumentException ex) {
                    skipped++;                // invalid line in file
                }
            }
        } catch (IOException ex) {
            showMessage("Error while reading file: " + ex.getMessage());
            return;
        }

        // Replace the old list, so repeated loading never creates duplicates
        list.clear();
        list.addAll(loaded);

        if (showMessages) {
            String msg = "Loaded " + list.size() + " employee(s).";
            if (skipped > 0) {
                msg += "\nSkipped " + skipped + " invalid or duplicate line(s).";
            }
            showMessage(msg);
        }
    }

    //View All
    private void viewAll() {
        model.setRowCount(0);   // clear old rows

        if (list.isEmpty()) {
            showMessage("No employee records to show.");
            return;
        }

        table.getTableHeader().setVisible(true);
        for (Employee e : list) {
            model.addRow(new Object[]{
                e.getId(),
                e.getName(),
                e.getDepartment(),
                String.format("%.2f", e.getBasicSalary()),
                String.format("%.2f", e.getAllowances()),
                String.format("%.2f", e.getDeductions()),
                String.format("%.2f", e.getNetSalary())
            });
        }
    }

    //Helper methods
    private boolean idExists(ArrayList<Employee> employees, int id) {
        for (Employee e : employees) {
            if (e.getId() == id) {
                return true;
            }
        }
        return false;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    //Program starts here
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EployeePayroll().setVisible(true));
    }
}