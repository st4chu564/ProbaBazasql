package GlownaPaczka;

import com.sun.xml.internal.ws.util.StringUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.sql.ResultSetMetaData;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class Baza extends JFrame {

    public Baza() {
        initUI();
    }

    private JLabel sbar;
    private JTable table;
    private JTextField ip = new JTextField("127.0.0.1");
    private JTextField dbName = new JTextField("Zaliczenie");
    private JTextField username = new JTextField("Admin");
    private JPasswordField pass = new JPasswordField("proba123");
    private JTextField queryStart = new JTextField("SELECT");
    private JTextField queryEnd = new JTextField("FROM");
    private JTextField advQueryText = new JTextField("Zapytanie...");
    private JButton button = new JButton("Edytuj");
    private JComboBox<String> combo = null;
    private String statement;
    private JButton addRecord = new JButton("Dodaj rekord");
    private JButton removeRecord = new JButton("Usun");
    private JPanel gui = new JPanel();
            Statement stmt = null;
    ResultSet rSet = null;
    Connection connection = null;
    private Vector<JTextField> fields = new Vector<>();
    private Vector<String> tables = new Vector<String>();
    private Vector<String> columns = new Vector<String>();
    private String selectedTable;
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
    final JComponent[] selectQuery = new JComponent[]{
            new JLabel("Select"),
            queryStart,
            new JLabel("From"),
            queryEnd
    };
    final JComponent[] advancedQuery = new JComponent[]{
            new JLabel("Zapytanie"),
            advQueryText
    };

    private void initUI() {
        sbar = new JLabel("0");
        sbar.setBorder(BorderFactory.createEtchedBorder());
        createMenuBar();
        setLayout(new GridBagLayout());
        setSize(1600, 900);
        gui.setLayout(new GridBagLayout());
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
        JMenu fileMenu = new JMenu("Glowny...");

        JMenuItem connectDB = new JMenuItem("Polacz z baza");
        JMenuItem newQuery = new JMenuItem("Zapytanie select");
        JMenuItem newQueryAdv = new JMenuItem("Zapytanie zaawansowane");
        JMenuItem insertIntoDB = new JMenuItem("Dodaj rekord");
        newQueryAdv.addActionListener(e -> advQuery());
        newQuery.addActionListener(e -> query());
        connectDB.addActionListener(e -> connectToDB());
        insertIntoDB.addActionListener(e -> Insert());
        databaseMenu.add(newQuery);
        databaseMenu.add(newQueryAdv);
        databaseMenu.addSeparator();
        databaseMenu.add(insertIntoDB);


        JMenuItem saveMi = new JMenuItem("Save");
        JMenuItem exitMi = new JMenuItem("Exit");
        connectDB.setToolTipText("Polacz z juz istniejaca baza");
        exitMi.setToolTipText("Exit application");
        exitMi.addActionListener((ActionEvent) -> {
            try{
                connection.close();
            }
            catch (SQLException e){}
            System.exit(0);
        });
        exitMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        fileMenu.add(connectDB);
        fileMenu.addSeparator();
        fileMenu.add(exitMi);

        menubar.add(fileMenu);
        menubar.add(databaseMenu);

        setJMenuBar(menubar);
    }

    private void Insert() {
        if(connection == null) {
            JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
            gui.removeAll();
            DatabaseMetaData md = connection.getMetaData();
            String[] types = {"TABLE"};
            rSet = md.getTables(dbName.getText(), null, "%", types);
            while(rSet.next()){
                tables.add(new String(rSet.getString(3)));
            }
            Object[] tableNames = tables.toArray(new Object[tables.size()]);
            selectedTable = (String) JOptionPane.showInputDialog(getContentPane(), "Wybierz tabele", "Wybor tabeli", JOptionPane.QUESTION_MESSAGE, null, tableNames, tableNames[0]);
            statement = "SELECT * FROM " + selectedTable + " ORDER BY id DESC";
            stmt = connection.createStatement();
            rSet = stmt.executeQuery(statement);
            ResultSetMetaData rsmd = rSet.getMetaData();
            rSet.next();
            int lastID = rSet.getInt("id") + 1;
            for(int i = 1; i <= rsmd.getColumnCount(); i++){
                columns.add(rsmd.getColumnName(i));
            }
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            boolean first = true;
            for(String name : columns){
                JLabel nazwa = new JLabel(name);
                gui.add(nazwa, c);
                c.gridy++;
                JTextField value = new JTextField();
                if(first){
                    value.setEnabled(false);
                    value.setText(String.valueOf(lastID));
                    first = false;
                }
                fields.add(value);
                value.setPreferredSize(new Dimension(150, 25));
                gui.add(value, c);
                c.gridy = 0;
                c.gridx++;
            }
            addRecord.setPreferredSize(new Dimension(fields.size() * 150,25));
            addRecord.addActionListener(e -> insertRecord());
            c.gridy = 2;
            c.gridx = 0;
            c.gridwidth = 4;
            gui.add(addRecord, c);
            getContentPane().add(gui);
            gui.updateUI();
            tables.clear();
            columns.clear();

        }
        catch(SQLException e){}
    }

    private void insertRecord() {
            statement = "INSERT INTO " + selectedTable + " VALUES(";
            for (JTextField pole : fields) {
                if (pole.getText().contains("[0-9]+")) {
                    statement += pole.getText() + ", ";
                } else {
                    statement += "'" + pole.getText() + "', ";
                }
            }
            statement = statement.substring(0, statement.length() - 2);
            statement += ")";
            try {
                stmt = connection.createStatement();
                stmt.execute(statement);
                stmt.close();
                gui.removeAll();
                gui.add(table);
                gui.updateUI();
            } catch (SQLException e) {
            }

            tables.clear();
            columns.clear();
        }

    private void connectToDB() {
        int result = JOptionPane.showConfirmDialog(getContentPane(), inputs, "Enter options", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            result = connect(ip.getText(), dbName.getText(), username.getText(), pass);
            switch (result) {
                case -2: JOptionPane.showMessageDialog(getContentPane(), "Couldn't connect to database", "Blad", JOptionPane.ERROR_MESSAGE); break;
                case -1: JOptionPane.showMessageDialog(getContentPane(), "Couldn't register database driver", "Blad", JOptionPane.ERROR_MESSAGE); break;
                case 0: JOptionPane.showMessageDialog(getContentPane(), "Error connecting to database", "Blad", JOptionPane.ERROR_MESSAGE); break;
                case 1: JOptionPane.showMessageDialog(getContentPane(), "Connection succesfull", "Sukces", JOptionPane.INFORMATION_MESSAGE); break;
            }
        } else {
        }

        tables.clear();
        columns.clear();
    }

    private void query() {

        if(connection == null){
            JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
        }
        else{
            gui.removeAll();
            Vector <String> Tabele = new Vector<>();
            try{
                DatabaseMetaData md = connection.getMetaData();
                String[] types = {"TABLE"};
                rSet = md.getTables(dbName.getText(), null, "%", types);
                while(rSet.next()){
                    tables.add(new String(rSet.getString(3)));
                }
                Object[] tableNames = tables.toArray(new Object[tables.size()]);
                selectedTable = (String) JOptionPane.showInputDialog(getContentPane(), "Wybierz tabele", "Wybor tabeli", JOptionPane.QUESTION_MESSAGE, null, tableNames, tableNames[0]);
                statement = "SELECT * FROM " + selectedTable;
                stmt = connection.createStatement();
                rSet = stmt.executeQuery(statement);
                ResultSetMetaData rsmd = rSet.getMetaData();
                columns.add("*");
                for(int i = 1; i <= rsmd.getColumnCount(); i++)
                    columns.add(rsmd.getColumnName(i));
                Object[] columnNames = columns.toArray(new Object[columns.size()]);
                String selectedColumn = (String) JOptionPane.showInputDialog(getContentPane(), "Wybierz kolumny", "Wybor kolumny", JOptionPane.QUESTION_MESSAGE, null, columnNames, columnNames[0]);
                String sortParam = (String) JOptionPane.showInputDialog(getContentPane(), "Wybierz kolumne do sortowania", "Wybor sortowanie", JOptionPane.QUESTION_MESSAGE, null, columnNames, columnNames[1]);
                statement = "select " + selectedColumn + " from " + selectedTable + " order by " + sortParam + " asc";
                rSet = stmt.executeQuery(statement);
                table = new JTable(buildTableModel(rSet));
                table.setShowVerticalLines(false);
                button.addActionListener(e -> Click());
                removeRecord.addActionListener(e -> Remove());
                GridBagConstraints c = new GridBagConstraints();
                table.setSize(gui.getWidth(), 500);
                resizeColumnWidth(table);
                c.gridx = 0;
                c.gridy = 0;
                c.gridwidth = 2;
                gui.add(new JScrollPane(table), c);
                c.gridwidth = 1;
                c.gridy = 1;
                gui.add(button, c);
                c.gridx = 1;
                gui.add(removeRecord, c);
                getContentPane().add(gui);
                gui.updateUI();
                columns.clear();
                tables.clear();
                rSet.close();
                stmt.close();
            }
            catch (SQLException e){}
            }

        tables.clear();
        columns.clear();
        }

    private void Remove() {
        String selectedRow = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
        int a = JOptionPane.showConfirmDialog(getContentPane(),"Czy na pewno chcesz usunac ten rekord?", "Potwierdz", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if(a == JOptionPane.CANCEL_OPTION)
            return;
        statement = "delete from " + selectedTable + " where id = " + selectedRow;
        try{
            stmt = connection.createStatement();
            stmt.execute(statement);
        }
        catch(SQLException e){}

    }

    private void Click() {
        String selectedRow = String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
        try{
            statement = "select * from " + selectedTable;
            stmt = connection.createStatement();
            rSet = stmt.executeQuery(statement);
            ResultSetMetaData rsmd = rSet.getMetaData();
            statement = "update " + selectedTable + " set ";
            columns.add("*");
            for(int i = 1; i <= rsmd.getColumnCount(); i++){
                if(Objects.equals(rsmd.getColumnName(i), "id")){
                    statement += "id = " + selectedRow;
                    continue;
                }
                else{
                    String answer = (String) JOptionPane.showInputDialog(getContentPane(), rsmd.getColumnName(i), "Podaj wartosc", JOptionPane.INFORMATION_MESSAGE);
                    if(answer == null && answer.length() < 1)
                        return;
                    if(answer != ""){
                        if(rsmd.getColumnType(i) == 4){
                            if(answer.matches("[0-9]+")){
                                statement += ", " + rsmd.getColumnName(i) + " = " + answer;
                            }
                        }
                        else{
                            statement += ", " + rsmd.getColumnName(i) + " = '" + answer + "'";
                        }
                    }
                }
            }
            statement += " where id = " + selectedRow;
            PreparedStatement ps = connection.prepareStatement(statement);
            ps.executeUpdate();
            table.repaint();
        }
        catch(SQLException e){

        }

        tables.clear();
        columns.clear();
    }
    private void advQuery() {
        if(connection == null){
            JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
        }
        else{
            JOptionPane.showConfirmDialog(getContentPane(), advancedQuery, "Wprowadz zapytanie", JOptionPane.OK_CANCEL_OPTION);
            int answer  = JOptionPane.showConfirmDialog(getContentPane(), advQueryText.getText(), "Zapytanie", JOptionPane.OK_CANCEL_OPTION);
            if(advQueryText.getText() == "")
                JOptionPane.showMessageDialog(getContentPane(), "Zapytanie puste", "Blad zapytania", JOptionPane.ERROR_MESSAGE);
            else if((advQueryText.getText().toLowerCase().contains("drop") || advQueryText.getText().toLowerCase().contains("delete") || advQueryText.getText().toLowerCase().contains("insert")
                    || advQueryText.getText().toLowerCase().contains("update") || advQueryText.getText().toLowerCase().contains("create")) && !advQueryText.getText().startsWith("$")) {
                JOptionPane.showMessageDialog(getContentPane(), "Nie poprawne zapytanie", "Nie poprawne zapytanie", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if(answer == JOptionPane.CANCEL_OPTION)
                return;
            else if(answer == JOptionPane.YES_OPTION){
                if(advQueryText.getText().startsWith("$"))
                    statement = advQueryText.getText().substring(1);
                getContentPane().remove(gui);
                getContentPane().repaint();
                gui.removeAll();
                table = new JTable(0,0);
                try{
                    stmt = connection.createStatement();
                    rSet = stmt.executeQuery(statement);
                    table = new JTable(buildTableModel(rSet));
                    table.setShowHorizontalLines(false);
                    table.setSize(gui.getWidth(), 500);
                    resizeColumnWidth(table);
                    gui.add(new JScrollPane(table));
                    getContentPane().add(gui);
                    gui.updateUI();
                    rSet.close();
                    stmt.close();
                }catch (SQLException e){

                }
            }
            advQueryText.setText("Zapytanie");
        }

        tables.clear();
        columns.clear();
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


    public static void main(String[] args) {
        EventQueue.invokeLater(() ->{
            Baza ex = new Baza();
            ex.setVisible(true);

        });
    }

    private class MyClass {
    }
}
