package GlownaPaczka;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.sql.ResultSetMetaData;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.util.regex.Pattern;

public class Baza extends JFrame {

    public Baza() {
        initUI();
    }

    private JLabel sbar;
    private JTextField ip = new JTextField("127.0.0.1");
    private JTextField dbName = new JTextField("Zaliczenie");
    private JTextField username = new JTextField("Admin");
    private JPasswordField pass = new JPasswordField("proba123");
    private JPanel gui = new JPanel();
    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    Statement stmt = null;
    ResultSet rSet = null;
    Connection connection = null;

    final JComponent[] inputs = new JComponent[]{
            new JLabel("IP Address"),
            ip,
            new JLabel("Database name"),
            dbName,
            new JLabel("Username"),
            username,
            new JLabel("Password"),
            pass
    };

    private void initUI() {
        sbar = new JLabel("0");
        sbar.setBorder(BorderFactory.createEtchedBorder());
        createMenuBar();
        setLayout(new BorderLayout());
        setSize(1600, 900);
        gui.setLayout(new BorderLayout());
        gui.setSize(1600, 900);
        ImageIcon icon = new ImageIcon("E:\\JavaProjects\\ProbaBazasql\\src\\GlownaPaczka\\Icon.png");
        setIconImage(icon.getImage());
        setTitle("Baza");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            if(width > 300)
                width=300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException{

        ResultSetMetaData metaData = rs.getMetaData();
        // names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
    private void createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        JMenu databaseMenu = new JMenu("Baza...");
        JMenu impMenu = new JMenu("Import...");
        JMenu fileMenu = new JMenu("File...");

        JMenuItem connectDB = new JMenuItem("Connect to database");
        JMenuItem newDB = new JMenuItem("Create new database");
        JMenuItem newTable = new JMenuItem("Create new table");
        JMenuItem deleteDB = new JMenuItem("Delete database");
        JMenuItem deleteTable = new JMenuItem("Delete table");
        JMenuItem editDatabase = new JMenuItem("Edit database");
        JMenuItem editTable = new JMenuItem("Edit table");
        connectDB.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int result = JOptionPane.showConfirmDialog(getContentPane(), inputs, "Enter options", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    result = connect(ip.getText(), dbName.getText(), username.getText(), pass);
                    switch (result) {
                        case -2: JOptionPane.showMessageDialog(getContentPane(), "Couldn't connect to database"); break;
                        case -1: JOptionPane.showMessageDialog(getContentPane(), "Couldn't register database driver"); break;
                        case 0: JOptionPane.showMessageDialog(getContentPane(), "Error connecting to database"); break;
                        case 1: JOptionPane.showConfirmDialog(getContentPane(), "Connection succesfull"); break;

                    }
                    try {
                        stmt = connection.createStatement();
                        rSet = stmt.executeQuery("SELECT przedmioty.id, przedmioty.przedmiot, wykladowcy.imie, wykladowcy.nazwisko, przedmioty.typ FROM wykladowcy\n" +
                                "  RIGHT JOIN przedmioty ON przedmioty.wykladowca = wykladowcy.id");
                        JTable table = new JTable(buildTableModel(rSet));
                        resizeColumnWidth(table);
                        gui.add(new JScrollPane(table));
                        getContentPane().add(gui);
                        gui.updateUI();
                    } catch (SQLException e) {
                    }
                } else {
                }

            }
        });
        databaseMenu.add(connectDB);
        databaseMenu.addSeparator();
        databaseMenu.add(newDB);
        databaseMenu.add(newTable);
        databaseMenu.addSeparator();
        databaseMenu.add(deleteDB);
        databaseMenu.add(deleteTable);
        databaseMenu.addSeparator();
        databaseMenu.add(editDatabase);
        databaseMenu.add(editTable);

        JMenuItem newsfMi = new JMenuItem("Import newsfeed list");
        JMenuItem bookmMi = new JMenuItem("Import bookmarks");
        JMenuItem mailMi = new JMenuItem("Import mail");

        impMenu.add(newsfMi);
        impMenu.add(bookmMi);
        impMenu.add(mailMi);

        JMenuItem newMi = new JMenuItem("New");
        JMenuItem openMi = new JMenuItem("Open");
        JMenuItem saveMi = new JMenuItem("Save");
        JMenuItem exitMi = new JMenuItem("Exit");
        exitMi.setToolTipText("Exit application");
        exitMi.addActionListener((ActionEvent) -> {
            System.exit(0);
        });
        exitMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        fileMenu.add(newMi);
        fileMenu.add(openMi);
        fileMenu.add(saveMi);
        fileMenu.addSeparator();
        fileMenu.add(impMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitMi);

        menubar.add(fileMenu);
        menubar.add(databaseMenu);

        setJMenuBar(menubar);
    }

    private int connect(String ipAddress, String databaseName, String userName, JPasswordField password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            return -1;
        }
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + ipAddress + "/" + databaseName, userName, password.getText());
        } catch (SQLException e) {
            return -2;
        }
        if (connection != null) {
            return 1;
        } else {
            return 0;
        }
    }
    public void createTable(){
        try {
            rSet = stmt.executeQuery("SELECT * from przedmioty");
            buildTableModel(rSet);
        } catch (SQLException e) {
        }
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(() ->{
            Baza ex = new Baza();
            ex.setVisible(true);

        });
    }
}
