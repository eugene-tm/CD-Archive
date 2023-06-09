import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import java.net.*;
import java.io.*;


/*********************************************************
 PROGRAM: CD Archive
 AUTHOR: Eugene Moore
 DUE DATE: 2023

 FUNCTION: This program manages a collection of CDs. Users can add, edit, and remove CDs.

           This class receives requests from the MainWindow class. It handles them and facilitates
           the process being processed and sent back to the main window.

 INPUT:    Data is loaded from "data.txt" in the program files.

 OUTPUT:   This class does not output data. It only sends information in RAM back to the main window.

 NOTES:    This class contains code that creates the UI for the second screen, facilitates the
           processing of requests from the main window, and sends data back to the main window.


 ********************************************************/

public class SecondWindow extends JFrame implements WindowListener, ActionListener, MouseListener {

    //CHAT RELATED ---------------------------
    private Socket socket = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread2 client2 = null;
    private String serverName = "localhost";
    private int serverPort = 4444;
    //----------------------------------------



    // DECLARING ALL GLOBAL VARIABLES
    SpringLayout myLayout = new SpringLayout();
    // constructor of JTable model
    CDModel cdModel;
    // Used to store data from our ReadFile method
    ArrayList<Object[]> dataArrayOfObjects;
    String[] options = {"RETRIEVE", "REMOVE", "RETURN","ADD TO COLLECTION","RANDOM SORT", "REVERSE ORDER SORT", "MOSTLY SORTED SORT"};
    JComboBox<String> comboBox = new JComboBox<>(options);
    int itemCount;
    JTable myTable;
    JLabel lblHeading, lblCurrentRequestedAction, lblBarcodeOfSelected, lblSection, lblRandomCollection, lblId, lblMessage;
    JButton btnProcess, btnAddItem, btnExit;
    JTextField txtBarcodeOfSelected, txtSection;

    /**
     * Primary constructor for SecondWindow.
     * Builds and populates the frame with elements.
     */
    public SecondWindow() {
        this.setSize(700, 400);
        this.setLayout(myLayout);
        this.setTitle("Automation Console");
        this.setResizable(false);

        getContentPane().setBackground(new Color(229, 229, 255));

        LocateFrameElements(myLayout, myLayout, myLayout, myLayout);

        CDTable(myLayout);

        //CHAT RELATED ---------------------------
        getParameters();
        connect(serverName, serverPort);
        //----------------------------------------

        this.addWindowListener(this);
        this.setVisible(true);
    }


    // *************************  CONNECTION CODE *****************************

    /**
     * Connect method links SecondWindow to MainWindow
     * @param serverName
     * @param serverPort
     */
    public void connect(String serverName, int serverPort)
    {
        println("Establishing connection. Please wait ...");
        try
        {
            socket = new Socket(serverName, serverPort);
            println("Connected: " + socket);
            open();
        }
        catch (UnknownHostException uhe)
        {
            println("Host unknown: " + uhe.getMessage());
        }
        catch (IOException ioe)
        {
            println("Unexpected exception: " + ioe.getMessage());
        }
    }

    /**
     * Send method creates a string of data and sends it to the connected source.
     */
    private void send()
    {
        try
        {
            streamOut.writeUTF(
                    // Barcode
                    txtBarcodeOfSelected.getText() + ": " +

                    // ID value
                    lblId.getText() + ": " +

                    // Combo box value
                    comboBox.getItemAt(comboBox.getSelectedIndex()) + ": " +

                    // Section value
                    txtSection.getText() + ": " +

                    // string to help control the process log
                    "toFirst"
            );
        }
        catch (IOException ioe)
        {
            println("Sending error: " + ioe.getMessage());
            close();
        }
    }

    /**
     * This method receives messages from other sources and handles them in accordance to the message.
     * @param msg
     */
    public void handle(String msg)
    {
        if (msg.equals(".bye"))
        {
            println("Good bye. Press EXIT button to exit ...");
            close();
        }
        if (msg.contains("toFirst"));
        if (msg.contains("sorty"))
        {
            System.out.println("Handle: " + msg);
            println(msg);

            String[] temp = msg.split(": ");
            // requested action
            comboBox.setSelectedItem(temp[1]);
            lblRandomCollection.setText(temp[1]);
            // section
            txtSection.setText(temp[2]);
        }
        else
        {
            System.out.println("Handle: " + msg);
            println(msg);

            String[] temp = msg.split(": ");

            txtBarcodeOfSelected.setText(String.valueOf(temp[1]));
            lblId.setText(String.valueOf(temp[2]));
            comboBox.setSelectedItem(temp[3]);
            txtSection.setText(temp[4]);
        }
    }

    /**
     * This method is used in the connect method to establish connection.
     */
    public void open()
    {
        try
        {
            streamOut = new DataOutputStream(socket.getOutputStream());
            client2 = new ChatClientThread2(this, socket);
        }
        catch (IOException ioe)
        {
            println("Error opening output stream: " + ioe);
        }
    }

    /**
     * This method ends the data transfer connection.
     */
    public void close()
    {
        try
        {
            if (streamOut != null)
            {
                streamOut.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (IOException ioe)
        {
            println("Error closing ...");
        }
        client2.close();
        client2.stop();
    }

    /**
     * This method writes the current connection status or message to a label in the UI.
     * This helps the user see if the connection is good, or if there are issues.
     * @param msg
     */
    void println(String msg)
    {
        lblMessage.setText(msg);
    }

    /**
     * This method is called in the primary constructor to aid connection configuration.
     */
    public void getParameters()
    {
        serverName = "localhost";
        serverPort = 4444;
    }

    // *************************  CONNECTION CODE END *****************************

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myButtonLayout
     * @param myButton
     * @param caption
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public JButton LocateAButton(
            SpringLayout myButtonLayout, JButton myButton, String caption, int x, int y, int w, int h) {
        myButton = new JButton(caption);
        add(myButton);
        myButtonLayout.putConstraint(SpringLayout.WEST, myButton, x, SpringLayout.WEST, this);
        myButtonLayout.putConstraint(SpringLayout.NORTH, myButton, y, SpringLayout.NORTH, this);
        myButton.setPreferredSize(new Dimension(w, h));
        myButton.addActionListener(this);
        return myButton;
    }

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myTextFieldLayout
     * @param myTextField
     * @param width
     * @param x
     * @param y
     * @return
     */
    public JTextField LocateATextField(SpringLayout myTextFieldLayout, JTextField myTextField, int width, int x, int y) {
        myTextField = new JTextField(width);
        add(myTextField);
        myTextFieldLayout.putConstraint(SpringLayout.WEST, myTextField, x, SpringLayout.WEST, this);
        myTextFieldLayout.putConstraint(SpringLayout.NORTH, myTextField, y, SpringLayout.NORTH, this);
        return myTextField;
    }

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myLabelLayout
     * @param myLabel
     * @param caption
     * @param x
     * @param y
     * @return
     */
    public JLabel LocateALabel(SpringLayout myLabelLayout, JLabel myLabel, String caption, int x, int y) {
        myLabel = new JLabel(caption);
        add(myLabel);
        myLabelLayout.putConstraint(SpringLayout.WEST, myLabel, x, SpringLayout.WEST, this);
        myLabelLayout.putConstraint(SpringLayout.NORTH, myLabel, y, SpringLayout.NORTH, this);
        return myLabel;
    }

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param layout
     * @param options
     * @param x
     * @param y
     * @param width
     * @return
     */
    public JComboBox<String> LocateAComboBox(SpringLayout layout, String[] options, int x, int y, int width) {
        JComboBox<String> comboBox = new JComboBox<>(options);
        add(comboBox);
        layout.putConstraint(SpringLayout.WEST, comboBox, x, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, comboBox, y, SpringLayout.NORTH, this);
        comboBox.setPreferredSize(new Dimension(width, comboBox.getPreferredSize().height));
        return comboBox;
    }

    /**
     * This method builds the entire UI.
     * Labels, text fields, buttons, combo boxes for MainWindow are all created here.
     * @param myLabelLayout
     * @param myButtonLayout
     * @param myTextFieldLayout
     * @param myComboLayout
     */
    public void LocateFrameElements(SpringLayout myLabelLayout, SpringLayout myButtonLayout, SpringLayout myTextFieldLayout, SpringLayout myComboLayout)
    {
        // HEADING
        lblHeading = LocateALabel(myLabelLayout, lblHeading, " Automation Console ",40,5);
        lblHeading.setFont(new Font("Arial",Font.PLAIN,25));
        lblHeading.setBackground(Color.GRAY);
        lblHeading.setOpaque(true);
        lblHeading.setForeground(Color.WHITE);

        // LABELS
        lblCurrentRequestedAction = LocateALabel(myLabelLayout, lblCurrentRequestedAction, "Current Requested Action:",40,50);
        lblBarcodeOfSelected = LocateALabel(myLabelLayout, lblBarcodeOfSelected, "Barcode of Selected Item:",40,80);
        lblSection = LocateALabel(myLabelLayout, lblSection, "Section:",310,80);
        lblRandomCollection = LocateALabel(myLabelLayout, lblRandomCollection, "Random Collection",530,50);
        lblId = LocateALabel(myLabelLayout, lblId, "ID",490,50);
        lblMessage = LocateALabel(myLabelLayout, lblMessage, "---",50,320);

        // TEXT FIELDS
        txtBarcodeOfSelected = LocateATextField(myTextFieldLayout, txtBarcodeOfSelected,9,200,80);

        txtSection = LocateATextField(myTextFieldLayout, txtSection,2,360,80);

        // BUTTONS
        btnProcess = LocateAButton(myButtonLayout, btnProcess, "Process", 400,48,83,22);
        btnProcess.setBackground(Color.white);
        btnAddItem = LocateAButton(myButtonLayout, btnAddItem, "Add Item", 400,78,83,22);
        btnAddItem.setBackground(Color.white);
        btnExit = LocateAButton(myButtonLayout, btnExit, "Exit", 540,325,100,23);
        btnExit.setBackground(Color.white);

        // WOMBO COMBO
        comboBox = LocateAComboBox(myComboLayout,options,200,50, 182);

    }


    /**
     * CD Table method creates our data table inside a panel.
     * @param myPanelLayout
     */
    public void CDTable(SpringLayout myPanelLayout)
    {
        // Create a panel to hold all other components
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BorderLayout());
        add(myPanel);

        // Create column names
        String columnNames[] = { "Id","Title","Author","Section","X","Y","Barcode","Description","IsOnLoan"};

        // Create an instance of our data model and populate it with information from our file using ReadFile()
        dataArrayOfObjects = new ArrayList();
        dataArrayOfObjects = ReadFile(dataArrayOfObjects);

        // Creating a new instance of the CDModel class using the table building constructor
        cdModel = new CDModel(dataArrayOfObjects, columnNames);

        // Creates the table using the data from CDModel
        myTable = new JTable(cdModel);

        // Configure some of JTable's parameters
        myTable.isForegroundSet();
        myTable.setShowHorizontalLines(false);
        myTable.setRowSelectionAllowed(true);
        myTable.setColumnSelectionAllowed(true);
        add(myTable);


        // Add the table to a scrolling pane, size and locate
        JScrollPane scrollPane = myTable.createScrollPaneForTable(myTable);
        myPanel.add(scrollPane, BorderLayout.CENTER);
        myPanel.setPreferredSize(new Dimension(600, 200));
        myPanelLayout.putConstraint(SpringLayout.WEST, myPanel, 40, SpringLayout.WEST, this);
        myPanelLayout.putConstraint(SpringLayout.NORTH, myPanel, 110, SpringLayout.NORTH, this);
    }

    /**
     * This readfile method reads data.txt and returns an arraylist of object[] called tempArray.
     * Needed for acquiring data for the table.
     * @param tempArray
     * @return
     */
    public ArrayList<Object[]> ReadFile(ArrayList<Object[]> tempArray) {
        try {
            String line;
            String[] splitLines;
            itemCount = 0;
            tempArray.clear();

            FileReader fReader = new FileReader("data.txt");
            BufferedReader bReader = new BufferedReader(fReader);

            while ((line = bReader.readLine()) != null ){
                splitLines = line.split(";");
                tempArray.add(new Object[] {splitLines[0], splitLines[1], splitLines[2],
                        splitLines[3], splitLines[4], splitLines[5], splitLines[6],splitLines[7],Boolean.parseBoolean(splitLines[8])});
                itemCount++;
            }
            fReader.close();
            bReader.close();
            return tempArray;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    /**
     * All button events of SecondWindow are handled here.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { System.exit(0); }

        if (e.getSource() == btnAddItem) { comboBox.setSelectedIndex(3); send(); }

        if (e.getSource() == btnProcess) { send(); }


    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
