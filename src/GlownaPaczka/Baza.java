package GlownaPaczka;

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
import java.util.List;
import java.util.regex.Pattern;

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
    private String statement = new String("");
    private JPanel gui = new JPanel();
    Statement stmt = null;
    ResultSet rSet = null;
    Connection connection = null;
    private Vector<String> tables = new Vector<String>();
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
        JMenuItem newDB = new JMenuItem("Utworz nowa baze");
        JMenuItem newTable = new JMenuItem("Stworz nowa tabele");
        JMenuItem newQuery = new JMenuItem("Nowe zapytanie select");
        JMenuItem newQueryAdv = new JMenuItem("Nowe zaawansowane zapytanie");
        JMenuItem deleteDB = new JMenuItem("Usun baze");
        JMenuItem deleteTable = new JMenuItem("Usun tabele");
        JMenuItem editDatabase = new JMenuItem("Edytuj baze");
        JMenuItem editTable = new JMenuItem("Edytuj tabele");
        newQueryAdv.addActionListener(new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt){
                if(connection == null){
                    JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    JOptionPane.showConfirmDialog(getContentPane(), advancedQuery, "Wprowadz zapytanie", JOptionPane.OK_CANCEL_OPTION);
                    int answer  = JOptionPane.showConfirmDialog(getContentPane(), advQueryText.getText(), "Zapytanie", JOptionPane.OK_CANCEL_OPTION);
                    if(advQueryText.getText() == "")
                        JOptionPane.showMessageDialog(getContentPane(), "Zapytanie puste", "Blad zapytania", JOptionPane.ERROR_MESSAGE);
                    else if(answer == JOptionPane.YES_OPTION){
                        getContentPane().remove(gui);
                        getContentPane().repaint();
                        gui.removeAll();
                        table = new JTable(0,0);
                        try{
                            stmt = connection.createStatement();
                            rSet = stmt.executeQuery(advQueryText.getText());
                            table = new JTable(buildTableModel(rSet));
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
            }
        });
        newQuery.addActionListener(new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt){
                if(connection == null){
                    JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    int answer = JOptionPane.showConfirmDialog(getContentPane(), selectQuery, "Enter query", JOptionPane.OK_CANCEL_OPTION);
                    if(answer == JOptionPane.YES_OPTION) {
                        getContentPane().remove(gui);
                        getContentPane().repaint();
                        gui.removeAll();
                        table = new JTable(0,0);
                        try {
                            getContentPane().remove(gui);
                            getContentPane().repaint();
                            gui.removeAll();
                            stmt = connection.createStatement();
                            statement += "SELECT " + queryStart.getText();
                            statement += " FROM " + queryEnd.getText();
                            rSet = stmt.executeQuery(statement);
                            if(!rSet.next()){
                                JOptionPane.showMessageDialog(getContentPane(), "Odpowiedz pusta lub blad zapytania", "Blad", JOptionPane.OK_OPTION);
                            }
                            else {
                                JTable table = new JTable(buildTableModel(rSet));
                                resizeColumnWidth(table);
                                gui.add(new JScrollPane(table));
                                getContentPane().add(gui);
                                gui.updateUI();
                                statement = " ";
                            }
                            rSet.close();
                            stmt.close();
                        } catch (SQLException e) {

                        }
                    }
                }
            }
        });
        connectDB.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

            }
        });
        editTable.addActionListener(new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt){
                if(connection == null){
                    JOptionPane.showMessageDialog(getContentPane(), "Nie podlaczono do bazy", "Blad polaczenia", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    try{
                        DatabaseMetaData md = connection.getMetaData();
                        String[] types = {"TABLE"};
                        rSet = md.getTables(dbName.getText(), null, "%", types);
                        while(rSet.next()){
                            tables.add(new String(rSet.getString(3)));
                        }
                        Object[] tableNames = tables.toArray(new Object[tables.size()]);
                        selectedTable = (String) JOptionPane.showInputDialog(getContentPane(), "Wybierz tabele", "Wybor tabeli", JOptionPane.QUESTION_MESSAGE, null, tableNames, tableNames[0]);
                        JOptionPane.showConfirmDialog(getContentPane(),selectedTable,"Wybrana tabela", JOptionPane.OK_OPTION);
                    }
                    catch(SQLException e){

                    }
                }
            }
        });
        databaseMenu.add(newTable);
        databaseMenu.addSeparator();
        databaseMenu.add(newQuery);
        databaseMenu.add(newQueryAdv);
        databaseMenu.addSeparator();
        databaseMenu.add(deleteDB);
        databaseMenu.add(deleteTable);
        databaseMenu.addSeparator();
        databaseMenu.add(editDatabase);
        databaseMenu.add(editTable);

        JMenuItem saveMi = new JMenuItem("Save");
        JMenuItem exitMi = new JMenuItem("Exit");
        connectDB.setToolTipText("Polacz z juz istniejaca baza");
        newDB.setToolTipText("Stworz nowa baze");
        exitMi.setToolTipText("Exit application");
        exitMi.addActionListener((ActionEvent) -> {
            System.exit(0);
        });
        exitMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        fileMenu.add(newDB);
        fileMenu.add(connectDB);
        fileMenu.add(saveMi);
        fileMenu.addSeparator();
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


    public static void main(String[] args) {
        EventQueue.invokeLater(() ->{
            Baza ex = new Baza();
            ex.setVisible(true);

        });
    }
}
