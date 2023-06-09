import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import java.net.*;
import java.util.HashSet;

import org.apache.commons.io.FilenameUtils;

/*********************************************************
 PROGRAM: CD Archive
 AUTHOR: Eugene Moore
 DUE DATE: 2023

 FUNCTION: This program manages a collection of CDs. Users can add, edit, and remove CDs.

           This class contains code that communicates with a secondary class (SecondWindow).
           The secondary class behaves like a second program (a robot) which receives and processes
           requests sent from this class.

 INPUT:    Data is loaded from "data.txt" in the program files.

 OUTPUT:   CD data is saved back to "data.txt". Generated hash sets are saved to "hashSet.txt".

 NOTES:    This class contains code that creates the UI for the main screen, provides CRUD functions,
           sorts the table data, generates binary trees, creates hash sets, sends requests to the
           automation console, receives processed requests and handles them.


 ********************************************************/


public class MainWindow extends JFrame implements WindowListener,ActionListener, MouseListener {

    //CHAT RELATED ---------------------------
    private Socket socket = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread1 client = null;
    private String serverName = "localhost";

    HashSet<BTNode> hashSet = new HashSet<>();
    private int serverPort = 4444;

     // ---------------------------------------

    // DECLARING ALL GLOBAL VARIABLES
    SpringLayout myLayout = new SpringLayout();
    // constructor of JTable model
    CDModel cdModel;
    DoublyLinkedList dList = new DoublyLinkedList();

    // Used to store data from our ReadFile method
    ArrayList<Object[]> dataArrayOfObjects;

    BinaryTree bTree = new BinaryTree();

    int itemCount, selectedIndex;
    boolean isNewEntry = true;
    boolean isSortClicked = false;

    String requestedAction;

    JTable myTable;

    //  ** VARIABLES FOR UI: TOP HALF **

    // LABELS
    JLabel lblHeading, lblSort, lblSearchString, lblTitle, lblAuthor, lblSection, lblX, lblY, lblBarcode, lblDescription, lblIsOnLoan, lblMessage;
    // TEXT FIELDS & AREAS
    JTextField txtId, txtSearchString, txtTitle, txtAuthor, txtSection, txtX, txtY, txtBarcode;
    JTextArea txaDescription;
    // BUTTONS
    JButton btnSearch, btnTitleSort, btnAuthorSort, btnBarcodeSort, btnNewItem, btnSaveUpdate, btnExit;

    JCheckBox chkIsOnLoan;

    // ** ELEMENTS FOR UI: BOTTOM HALF **

    // LABELS
    JLabel lblProcessLog, lblBinaryTree, lblHashMap, lblAutomation, lblSortSection;
    // TEXT FIELDS & AREAS
    JTextField txtSortSection;
    JTextArea txaProcessLog;
    // BUTTONS
    JButton btnProcessLog, btnPreOrder, btnInOrder, btnPostOrder, btnGraphical, btnSave, btnDisplay,
            btnRetrieve, btnRemove, btnReturn, btnAddToCollection, btnRandomSort, btnMostlySort, btnReverseSort;

    /**
     * This is the primary constructor.
     * Frame is built, populated here + chat connection configuration.
     */
    public MainWindow()
    {
        this.setSize(1000,600);
        this.setLayout(myLayout);
        this.setTitle("CD Archive");
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        LocateFrameElements(myLayout,myLayout,myLayout,myLayout,myLayout);

        getContentPane().setBackground(new Color(229, 229, 255));

        CDTable(myLayout);

        //CHAT RELATED ---------------------------
        getParameters();
        connect(serverName, serverPort);

        //----------------------------------------

        this.addWindowListener(this);
        this.setVisible(true);
    }

    // **************  CONNECTION CODE ****************

    /**
     * Connect method links MainWindow to SecondWindow.
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
            streamOut.writeUTF(txtBarcode.getText() + ": " +
                    txtId.getText() + ": " +
                    requestedAction + ": " +
                    txtSection.getText() + ": " +
                    "toSecond"
            );

            streamOut.flush();
        }
        catch (IOException ioe)
        {
            println("Sending error: " + ioe.getMessage());
            close();
        }
    }

    /**
     * This is another send method. It works the same, but this one is specifically for sending sort requests.
     */
    private void sendSort()
    {
        try
        {
            streamOut.writeUTF(
                    requestedAction + ": " +
                    txtSortSection.getText() + ": " +
                    "toSecond" +
                    "sorty"
            );

            streamOut.flush();
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
        if (msg.contains("toSecond"));
        else
        {
            println(msg);

            String[] temp = msg.split(": ");

            String tempBarcode = String.valueOf(temp[1]);

            String tempAction = String.valueOf(temp[3]);


            if (tempBarcode != null){
                String processLine =
                        LocalDate.now() + " -- " +
                                LocalTime.now().withNano(0) + " -- RECEIVED -- " +  tempAction.toUpperCase() + " ITEM -- Barcode: " +
                                tempBarcode +
                                "\n";

                dList.head.Append(new Node(processLine));
                txaProcessLog.setText(dList.toString());

                if (tempAction.equalsIgnoreCase("RETRIEVE"))
                {
                    // CHECKS IsOnLoad / CHANGES BOOLEAN TO TRUE
                    chkIsOnLoan.setSelected(true);
                    AddOrUpdateEntry();
                    ClearFields();
                }
                if (tempAction.equalsIgnoreCase("RETURN"))
                {
                    // UNCHECKS IsOnLoan / CHANGES BOOLEAN TO FALSE
                    chkIsOnLoan.setSelected(false);
                    AddOrUpdateEntry();
                    ClearFields();

                }
                if (tempAction.equalsIgnoreCase("REMOVE"))
                {
                    // DELETES THE ROW FROM THE FILE
                    dataArrayOfObjects.remove(selectedIndex);
                    cdModel.fireTableDataChanged();
                    SaveToFile(dataArrayOfObjects);
                    ClearFields();
                }
                if (tempAction.equalsIgnoreCase("ADD TO COLLECTION"))
                {
                   // Facilitates the user completing the form to add an item to collection
                    ClearFields();
                    txaProcessLog.setText("ADD TO COLLECTION request being finalized." +
                            "\nPlease complete the form and click 'Save Update'.");
                    txtBarcode.setText(tempBarcode);
                }

            }
            else
            {
                txaProcessLog.setText("Retrieve failed: null input" + "\n");
            }
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
            client = new ChatClientThread1(this, socket);
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
        client.close();
        client.stop();
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
    // ******************************  CONNECTION CODE END  ******************************


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
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myLabelLayout
     * @param myLabel
     * @param caption
     * @param x
     * @param y
     * @return
     */
    public JLabel LocateALabel(SpringLayout myLabelLayout, JLabel myLabel, String caption, int x, int y)
    {
        myLabel = new JLabel(caption);
        add(myLabel);
        myLabelLayout.putConstraint(SpringLayout.WEST, myLabel, x, SpringLayout.WEST, this);
        myLabelLayout.putConstraint(SpringLayout.NORTH, myLabel, y, SpringLayout.NORTH, this);
        return myLabel;
    }

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myCheckBoxLayout
     * @param myCheckBox
     * @param x
     * @param y
     * @return
     */
    public JCheckBox LocateACheckBox(SpringLayout myCheckBoxLayout, JCheckBox myCheckBox, int x, int y)
    {
        myCheckBox = new JCheckBox();
        add(myCheckBox);
        myCheckBoxLayout.putConstraint(SpringLayout.WEST, myCheckBox, x, SpringLayout.WEST, this);
        myCheckBoxLayout.putConstraint(SpringLayout.NORTH, myCheckBox, y, SpringLayout.NORTH, this);
        return myCheckBox;
    }

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
            SpringLayout myButtonLayout, JButton myButton, String caption, int x, int y, int w, int h)
    {
        myButton = new JButton(caption);
        add(myButton);
        myButtonLayout.putConstraint(SpringLayout.WEST, myButton, x, SpringLayout.WEST, this);
        myButtonLayout.putConstraint(SpringLayout.NORTH, myButton, y, SpringLayout.NORTH, this);
        myButton.setPreferredSize(new Dimension(w,h));
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
    public JTextField LocateATextField(SpringLayout myTextFieldLayout, JTextField myTextField, int width, int x, int y)
    {
        myTextField = new JTextField(width);
        add(myTextField);
        myTextFieldLayout.putConstraint(SpringLayout.WEST, myTextField, x, SpringLayout.WEST, this);
        myTextFieldLayout.putConstraint(SpringLayout.NORTH, myTextField, y, SpringLayout.NORTH, this);
        return myTextField;
    }

    /**
     * This method is used in the LocateFrameElements method to configure UI elements.
     * @param myTextLayout
     * @param myTextArea
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public JTextArea LocateATextArea(SpringLayout myTextLayout, JTextArea myTextArea, int x, int y, int w, int h)
    {
        myTextArea = new JTextArea();
        add(myTextArea);
        myTextLayout.putConstraint(SpringLayout.WEST, myTextArea, x, SpringLayout.WEST, this);
        myTextLayout.putConstraint(SpringLayout.NORTH, myTextArea, y, SpringLayout.NORTH, this);
        myTextArea.setPreferredSize(new Dimension(w,h));
        return myTextArea;
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

        //
        myTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                isNewEntry = false;

                int row = myTable.rowAtPoint(e.getPoint());
                myTable.setRowSelectionInterval(row, row);
                selectedIndex = myTable.convertRowIndexToModel(row);

                PopulateFields();
            }
        });

        // Add the table to a scrolling pane, size and locate
        JScrollPane scrollPane = myTable.createScrollPaneForTable(myTable);
        myPanel.add(scrollPane, BorderLayout.CENTER);
        myPanel.setPreferredSize(new Dimension(600, 200));
        myPanelLayout.putConstraint(SpringLayout.WEST, myPanel, 10, SpringLayout.WEST, this);
        myPanelLayout.putConstraint(SpringLayout.NORTH, myPanel, 70, SpringLayout.NORTH, this);
    }

    /**
     * PopulateFields method is used to put correct data into fields based on user input (clicking the CDTable)
     */
    public void PopulateFields()
    {
        txtId.setText(dataArrayOfObjects.get(selectedIndex)[0].toString());
        txtTitle.setText(dataArrayOfObjects.get(selectedIndex)[1].toString());
        txtAuthor.setText(dataArrayOfObjects.get(selectedIndex)[2].toString());
        txtSection.setText(dataArrayOfObjects.get(selectedIndex)[3].toString());
        txtX.setText(dataArrayOfObjects.get(selectedIndex)[4].toString());
        txtY.setText(dataArrayOfObjects.get(selectedIndex)[5].toString());
        txtBarcode.setText(dataArrayOfObjects.get(selectedIndex)[6].toString());
        txaDescription.setText(dataArrayOfObjects.get(selectedIndex)[7].toString());

        boolean isChecked = Boolean.parseBoolean((dataArrayOfObjects.get(selectedIndex)[8]).toString());
        chkIsOnLoan.setSelected(isChecked);
    }

    /**
     * This method builds the entire UI.
     * Labels, text fields, text areas, buttons, checkboxes for MainWindow are all created here.
     * @param myLabelLayout
     * @param myButtonLayout
     * @param myTextFieldLayout
     * @param myCheckBoxLayout
     * @param myTextLayout
     */
    public void LocateFrameElements(SpringLayout myLabelLayout,
                                    SpringLayout myButtonLayout,
                                    SpringLayout myTextFieldLayout,
                                    SpringLayout myCheckBoxLayout,
                                    SpringLayout myTextLayout)
    {
        // ** ELEMENTS FOR UI: TOP HALF **

        // HEADING
        lblHeading = LocateALabel(myLabelLayout, lblHeading, " Archive Console ",10,5);
        lblHeading.setFont(new Font("Arial",Font.PLAIN,25));
        lblHeading.setBackground(Color.BLACK);
        lblHeading.setOpaque(true);
        lblHeading.setForeground(Color.WHITE);

        // CHECKBOX
        chkIsOnLoan = LocateACheckBox(myCheckBoxLayout, chkIsOnLoan, 915,164);

        // LABELS
        lblSearchString = LocateALabel(myLabelLayout,lblSearchString, "Search String: ",10,42);
        lblSort = LocateALabel(myLabelLayout,lblSearchString, "Sort: ",10,283);
        lblTitle = LocateALabel(myLabelLayout,lblTitle,"Title: ", 650,40);
        lblAuthor = LocateALabel(myLabelLayout,lblAuthor,"Author: ", 650,65);
        lblSection = LocateALabel(myLabelLayout,lblSection,"Section: ", 650,90);
        lblX = LocateALabel(myLabelLayout,lblX,"X: ", 650,115);
        lblY = LocateALabel(myLabelLayout,lblY,"Y: ", 650,140);
        lblBarcode = LocateALabel(myLabelLayout,lblBarcode,"Barcode: ", 650,165);
        lblDescription = LocateALabel(myLabelLayout,lblDescription,"Description: ", 650,190);
        lblIsOnLoan = LocateALabel(myLabelLayout,lblIsOnLoan,"On Loan:", 860,165);
        lblMessage = LocateALabel(myLabelLayout, lblMessage, "---",250,10);

        // TEXT FIELDS & AREAS
        txtId = LocateATextField(myTextFieldLayout, txtId,2,900,40);
        txtSearchString = LocateATextField(myTextFieldLayout, txtTitle,14,95,42);
        txtTitle = LocateATextField(myTextFieldLayout, txtTitle,12,730,40);
        txtAuthor = LocateATextField(myTextFieldLayout, txtAuthor,12,730,65);
        txtSection = LocateATextField(myTextFieldLayout, txtSection,12,730,90);
        txtX = LocateATextField(myTextFieldLayout, txtX,12,730,115);
        txtY = LocateATextField(myTextFieldLayout, txtY,12,730,140);
        txtBarcode = LocateATextField(myTextFieldLayout, txtBarcode,12,730,165);

        txaDescription = LocateATextArea(myTextFieldLayout, txaDescription,730,190,203,80);
        txaDescription.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // These lines below ensure that the text fills the text area in a uniform manner
        txaDescription.setLineWrap(true);
        txaDescription.setWrapStyleWord(true);

        // BUTTONS
        btnSearch = LocateAButton(myButtonLayout, btnSearch, "Search", 250,38,100,26);
        btnSearch.setBackground(Color.white);
        btnTitleSort = LocateAButton(myButtonLayout, btnTitleSort, "By Title", 50,280,100,26);
        btnTitleSort.setBackground(Color.white);
        btnAuthorSort = LocateAButton(myButtonLayout, btnAuthorSort, "By Author", 155,280,100,26);
        btnAuthorSort.setBackground(Color.white);
        btnBarcodeSort = LocateAButton(myButtonLayout, btnBarcodeSort, "By Barcode", 260,280,100,26);
        btnBarcodeSort.setBackground(Color.white);
        btnNewItem = LocateAButton(myButtonLayout, btnNewItem, "New Item", 650,280,100,26);
        btnNewItem.setBackground(Color.white);
        btnSaveUpdate = LocateAButton(myButtonLayout, btnSaveUpdate, "Save/Update", 825,280,110,26);
        btnSaveUpdate.setBackground(Color.white);

        // ** ELEMENTS FOR UI: BOTTOM HALF **

        // LABELS
        lblProcessLog = LocateALabel(myLabelLayout,lblProcessLog,"Process Log:",10,315);
        lblBinaryTree = LocateALabel(myLabelLayout,lblProcessLog,"Display Binary Tree:",10,500);
        lblHashMap = LocateALabel(myLabelLayout,lblProcessLog,"HashMap / Set:",10,520);
        lblAutomation = LocateALabel(myLabelLayout,lblAutomation,"Automation Action Request for the item above:",650,315);
        lblSortSection = LocateALabel(myLabelLayout,lblProcessLog,"Sort Section:",650,400);


        // TEXT FIELDS & AREAS
        txaProcessLog = LocateATextArea(myTextLayout,txaProcessLog, 10,340,600,150);
        txaProcessLog.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        txaProcessLog.setLineWrap(true);
        txaProcessLog.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(txaProcessLog);

        this.add(scrollPane);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        myTextLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, this);
        myTextLayout.putConstraint(SpringLayout.NORTH, scrollPane, 340, SpringLayout.NORTH, this);
        scrollPane.setPreferredSize(new Dimension(600,150));

        txtSortSection = LocateATextField(myTextFieldLayout,txtSortSection, 9,730,400);

        // BUTTONS
        btnProcessLog = LocateAButton(myButtonLayout, btnProcessLog, "Process Log", 480,310,120,23);
        btnProcessLog.setBackground(Color.white);

        btnRetrieve = LocateAButton(myButtonLayout, btnRetrieve, "Retrieve", 660,340,140,24);
        btnRetrieve.setBackground(Color.white);
        btnReturn = LocateAButton(myButtonLayout, btnReturn, "Return", 660,365,140,24);
        btnReturn.setBackground(Color.white);
        btnRemove = LocateAButton(myButtonLayout, btnRemove, "Remove", 801,340,140,24);
        btnRemove.setBackground(Color.white);
        btnAddToCollection = LocateAButton(myButtonLayout, btnAddToCollection, "Add to Collection", 801,365,140,24);
        btnAddToCollection.setBackground(Color.white);

        btnRandomSort = LocateAButton(myButtonLayout, btnRandomSort, "Random Collection Sort", 750,427,170,23);
        btnRandomSort.setBackground(Color.white);
        btnMostlySort = LocateAButton(myButtonLayout, btnMostlySort, "Mostly Sorted Sort", 750,452,170,23);
        btnMostlySort.setBackground(Color.white);
        btnReverseSort = LocateAButton(myButtonLayout, btnReverseSort, "Reverse Order Sort", 750,477,170,23);
        btnReverseSort.setBackground(Color.white);

        btnPreOrder = LocateAButton(myButtonLayout, btnPreOrder, "Pre-Order", 130,496,130,22);
        btnPreOrder.setBackground(Color.white);
        btnSave = LocateAButton(myButtonLayout, btnSave, "Save", 130,520,130,22);
        btnSave.setBackground(Color.white);
        btnInOrder = LocateAButton(myButtonLayout, btnInOrder, "In-Order", 261,496,130,22);
        btnInOrder.setBackground(Color.white);
        btnDisplay = LocateAButton(myButtonLayout, btnDisplay, "Display", 261,520,130,22);
        btnDisplay.setBackground(Color.white);
        btnPostOrder = LocateAButton(myButtonLayout, btnPostOrder, "Post-Order", 392,496,130,22);
        btnPostOrder.setBackground(Color.white);
        btnGraphical = LocateAButton(myButtonLayout, btnGraphical, "Graphical", 523,496,130,22);
        btnGraphical.setBackground(Color.white);

        btnExit = LocateAButton(myButtonLayout, btnExit, "Exit",850,520,100,26);
        btnExit.setBackground(Color.white);

    }

    /**
     * This method sets the text fields to empty strings + unchecks checkboxes
     */
    public void ClearFields()
    {
        txtId.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtSection.setText("");
        txtX.setText("");
        txtY.setText("");
        txtBarcode.setText("");
        txaDescription.setText("");

        isNewEntry = true;
    }

    /**
     * This method handles saving data to file.
     * It handles both new entries and updating existing entries.
     */
    public void AddOrUpdateEntry()
    {
       if (IsValidEntry()) {
           if (isNewEntry) {

               dataArrayOfObjects.add(new Object[]{
                       txtId.getText(),
                       txtTitle.getText(),
                       txtAuthor.getText(),
                       txtSection.getText(),
                       txtX.getText(),
                       txtY.getText(),
                       txtBarcode.getText(),
                       txaDescription.getText(),
                       false});
           }

           if (!isNewEntry) {
                Object[] updatedObject = dataArrayOfObjects.get(selectedIndex);
                        updatedObject[0] = txtId.getText();
                        updatedObject[1] = txtTitle.getText();
                        updatedObject[2] = txtAuthor.getText();
                        updatedObject[3] = txtSection.getText();
                        updatedObject[4] = txtX.getText();
                        updatedObject[5] = txtY.getText();
                        updatedObject[6] = txtBarcode.getText();
                        updatedObject[7] = txaDescription.getText();
                        updatedObject[8] = chkIsOnLoan.isSelected();

                        dataArrayOfObjects.set(selectedIndex, updatedObject);
           }

           cdModel.fireTableDataChanged();
           SaveToFile(dataArrayOfObjects);
       }
    }

    /**
     * This method is used in AddOrUpdateEntry() to validate user input before saving to file.
     * @return
     */
    public Boolean IsValidEntry()
    {
        // OLD UNOPTIMIZED
        /*  if (isNewEntry)
        {
            if (DoesIDMatch(txtId.getText()))
            {
                JOptionPane.showMessageDialog(null, "Cannot use duplicate ID values");
                return false;
            }
        }*/

        // NEW OPTIMIZED - USES A SINGLE IF STATEMENT RATHER THAN TWO
        if (isNewEntry && DoesIDMatch(txtId.getText()))
        {
            JOptionPane.showMessageDialog(null, "Cannot use duplicate ID values");
            return false;
        }

        if (    txtId.getText().isEmpty() ||
                txtTitle.getText().isEmpty() ||
                txtAuthor.getText().isEmpty() ||
                txtX.getText().isEmpty() ||
                txtY.getText().isEmpty() ||
                txtBarcode.getText().isEmpty() ||
                txaDescription.getText().isEmpty() )
        {
            JOptionPane.showMessageDialog(null, "Please check text fields and try again");
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * This method is used to ensure that users don't add duplicate ID values to the system.
     * @param currentID
     * @return
     */
    public boolean DoesIDMatch(String currentID) {
        for (Object id : GetIdValues()) {
            if (id.equals(currentID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used in conjunction with DoesIDMatch() to avoid duplicate ID values entering the system.
     * @return
     */
    public ArrayList<String> GetIdValues() {
        ArrayList<String> idList = new ArrayList<>();
        for (Object[] obj : dataArrayOfObjects) {
            idList.add((String) obj[0]);
        }
        return idList;
    }

    /**
     * Saves an arraylist of object arrays to file.
     * Takes data through the dataList parameter.
     * Called in the AddOrUpdateEntry() method.
     * @param dataList
     */
    public void SaveToFile(ArrayList<Object[]> dataList) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt"))) {
            for (Object[] row : dataList) {
                for (int i = 0; i < row.length; i++) {
                    writer.write(row[i].toString());
                    if (i != row.length - 1) {
                        writer.write(";");
                    }
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method filters data in the table based on the text input by a user.
     * @param list
     */
    public void SearchBar(ArrayList<Object[]> list) {
        ReadFile(dataArrayOfObjects);
        // OPTIMISATION ATTEMPT
        // According top optimisation documentation, having a dedicated list size variable is more efficient than
        // calling .size(), however, this broke the search function here. list.size() is required to avoid an
        // out of bounds exception.

        //int listSize = list.size();
        for (int i = 0; list.size() > i ; i++) {
            String data = (String) list.get(i)[1];
            if (!data.toLowerCase().contains(txtSearchString.getText().toLowerCase())) {
                list.remove(i);
                i--;
            }
        }
        cdModel.fireTableDataChanged();
    }

    /**
     * Sorts data into alphabetical order by Title.
     * @param array
     */
    public void SortTitlesUsingBubbleSort(ArrayList<Object[]> array) {
        // Loop through the array from 0 to array size - 1
        for (int k = 0; k < array.size(); k++) {
            // Loop through the array from k + 1 to array size - 1
            for (int i = k + 1; i < array.size(); i++) {
                // Compare the titles at index i and k, and swap them if i is less than k
                if ((array.get(i)[1]).toString().compareToIgnoreCase(array.get(k)[1].toString()) < 1) {
                    Object[] titles = array.get(k);
                    array.set(k, array.get(i));
                    array.set(i, titles);
                }
            }
            // Notify the table model that the data has changed
            cdModel.fireTableDataChanged();
        }
    }

    /**
     * Sorts the data into alphabetical order by Authors name.
     * @param array
     */
    public void SortAuthorsUsingInsertionSort(ArrayList<Object[]> array) {

        // Loop through the array.
        for (int i = 1; i < array.size(); i++) {

            // Set current object to the ith object in the array.
            Object[] current = array.get(i);

            // Set j to be one less than i.
            int j = i - 1;

            // Compare each element with the current element and swap if needed.
            while (j >= 0 && ((String) array.get(j)[2]).compareTo((String) current[2]) > 0) {
                array.set(j + 1, array.get(j));
                j--;
            }
            // Set the current element to its correct position.
            array.set(j + 1, current);
        }
        // Notify the table model that the data has changed.
        cdModel.fireTableDataChanged();
    }

    /**
     * Quick Sort method used for sorting barcodes of CDs from lowest to highest.
     * @param list
     * @param low
     * @param high
     */
    public void SortBarcodesUsingQuickSort(ArrayList<Object[]> list, int low, int high) {
        if (low < high) {
            // Choose a pivot index.
            int pivotIndex = FindPivotPoint(list, low, high);

            // Recursively sort the left and right partitions.
            SortBarcodesUsingQuickSort(list, low, pivotIndex - 1);
            SortBarcodesUsingQuickSort(list, pivotIndex + 1, high);

            // Update the table with the sorted data.
            cdModel.fireTableDataChanged();
        }
    }

    /**
     * Private method used exclusively by the QuickSortBarcodes() method.
     * It finds the pivot point used to compare objects in quick sort.
     * @param arrayList
     * @param low
     * @param high
     * @return
     */
    private int FindPivotPoint(ArrayList<Object[]> arrayList, int low, int high) {
        // Choose the pivot element.
        Object[] pivot = arrayList.get(high);

        // Initialize index of smaller element.
        int i = low - 1;

        // Traverse through all elements in the subarray.
        for (int j = low; j < high; j++) {
            // Compare the current element with the pivot.
            Object[] cdObject = arrayList.get(j);

            // The sixth element in the obj array will be the barcode
            int selectedBarcode = Integer.parseInt((String) cdObject[6]);
            int pivotBarcode = Integer.parseInt((String) pivot[6]);

            // if selected barcode is smaller than the pivot
            if (selectedBarcode < pivotBarcode) {
                i++;
                // Create a temporary array and swap the values.
                Object[] temp = arrayList.get(i);
                arrayList.set(i, cdObject);
                arrayList.set(j, temp);
            }
        }
        // Swap the pivot element with the element at the i+1 position.
        Object[] temp = arrayList.get(i + 1);
        arrayList.set(i + 1, pivot);
        arrayList.set(high, temp);

        // Return the partition index.
        return i + 1;
    }

    /**
     * This method creates a binary tree of the table data using nodes from the BinaryTree class
     */
    public void CreateBinaryTree()
    {
        for (Object[] item : dataArrayOfObjects)
        {
            bTree.addNode(Integer.parseInt((String) item[0]),item[1].toString());
        }
        System.out.println("Binary Tree Generated");

    }

    /**
     * This method creates a string to be viewed in the process log textarea.
     * It adds the created string to a doubly-linked list, which is what is displayed.
     * @param requestType
     */
    private void ConfigureProcessLog(String requestType) {
        if (!txtBarcode.getText().isEmpty()){
            String processLine =
                    LocalDate.now() + " -- " +
                            LocalTime.now().withNano(0) + " -- SENT -- " + requestType + " -- Barcode: " +
                            txtBarcode.getText() +
                            "\n";

            dList.head.Append(new Node(processLine));
            txaProcessLog.setText(dList.toString());
        }
        else
        {
            if(!isSortClicked)
            {
                txaProcessLog.setText("Request failed: invalid input" + "\n");
            }
        }
    }

    /**
     * This method is used to write the created hash set of table data to file (hashSet.txt)
     */
    public void WriteHashToFile()
    {
        String fileName = "hashSet.txt";
        try {
            FileWriter fWriter = new FileWriter(fileName);
            fWriter.write(hashSet.toString());

            fWriter.close();
            System.out.println("Writing to file: " + FilenameUtils.getBaseName(fileName) + "." + FilenameUtils.getExtension(fileName));

        } catch (Exception e) {
            System.out.println("Error writing: " + e);
        }
    }


    /**
     * All button events of MainWindow are handled here.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { System.exit(0); }

        if (e.getSource() == btnSearch) { SearchBar(dataArrayOfObjects); }

        if (e.getSource() == btnNewItem) { ClearFields(); }

        if (e.getSource() == btnSaveUpdate) { AddOrUpdateEntry(); }

        if (e.getSource() == btnTitleSort) { SortTitlesUsingBubbleSort(dataArrayOfObjects); CreateBinaryTree(); }

        if (e.getSource() == btnAuthorSort) { SortAuthorsUsingInsertionSort(dataArrayOfObjects);}

        if (e.getSource() == btnBarcodeSort) { SortBarcodesUsingQuickSort(dataArrayOfObjects, 0, dataArrayOfObjects.size() -1);}



        if (e.getSource() == btnRetrieve) { requestedAction = "RETRIEVE"; isSortClicked = false; ConfigureProcessLog(requestedAction); send();}

        if (e.getSource() == btnReturn) { requestedAction = "RETURN"; isSortClicked = false; ConfigureProcessLog(requestedAction); send(); }

        if (e.getSource() == btnRemove) { requestedAction = "REMOVE"; isSortClicked = false; ConfigureProcessLog(requestedAction); send(); }

        if (e.getSource() == btnAddToCollection) { requestedAction = "ADD TO COLLECTION"; isSortClicked = false;  ConfigureProcessLog(requestedAction); send(); }

        if (e.getSource() == btnRandomSort) { requestedAction = "RANDOM SORT"; isSortClicked = true; ConfigureProcessLog(requestedAction); sendSort(); }

        if (e.getSource() == btnReverseSort) { requestedAction = "REVERSE ORDER SORT"; isSortClicked = true; ConfigureProcessLog(requestedAction); sendSort(); }

        if (e.getSource() == btnMostlySort) { requestedAction = "MOSTLY SORTED SORT"; isSortClicked = true; ConfigureProcessLog(requestedAction); sendSort(); }


        if (e.getSource() == btnProcessLog) { txaProcessLog.setText(dList.toString());}

        if (e.getSource() == btnPreOrder) {

            // traverse the tree
            bTree.preorderTraverseTree(bTree.root);

            // set the tree output to the log
            txaProcessLog.setText(bTree.BTString);
            bTree.BTString = "";
        }

        if (e.getSource() == btnPostOrder) {

            bTree.postOrderTraverseTree(bTree.root);

            txaProcessLog.setText(bTree.BTString);
            bTree.BTString = "";
        }

        if (e.getSource() == btnInOrder)  {

            bTree.inOrderTraverseTree(bTree.root, hashSet);

            txaProcessLog.setText(bTree.BTString);
            bTree.BTString = "";
        }

        if (e.getSource() == btnDisplay) {
            txaProcessLog.setText(hashSet.toString());
        }

        if (e.getSource() == btnSave){ WriteHashToFile(); }

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
}
