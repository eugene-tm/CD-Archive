import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

class CDModel extends AbstractTableModel
{
    ArrayList<Object[]> arrayList;

    // Create headings array variable
    String[] header;

    // Create the int to find the IsOnLoad column used for holding checkboxes
    int boxColumn;

    // Primary Constructor
    CDModel(ArrayList<Object[]> object, String[] heading)
    {
        // save the header
        this.header = heading;
        // and the data
        arrayList = object;
        // get the column index for the sent column
        boxColumn = this.findColumn("IsOnLoan");
    }

    // Finds the size of our arraylist/ number of entries.

    public int getRowCount()
    {
        return arrayList.size();
    }

    // Returns the number of headers.
    public int getColumnCount()
    {
        return header.length;
    }

    // Finds specific cells using row and column indexes
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return arrayList.get(rowIndex)[columnIndex];
    }

    // Returns the name of a column
    public String getColumnName(int index)
    {
        return header[index];
    }

    // Class used for changing the cells in the IsOnLoan column to hold checkboxes
    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == boxColumn)
        {
            return Boolean.class; // For every cell in column 7, set its class to Boolean.class
        }
        return super.getColumnClass(columnIndex); // Otherwise, set it to the default class
    }

    // Adds a new entry to the table
    void AddNewEntry(String id, String title, String author, String section, String x, String y, String barCode, String description, boolean isOnLoan)
    {
        // make it an array[3] as this is the way it is stored in the ArrayList
        // (not best design but we want simplicity)
        Object[] item = new Object[9];
        item[0] = id;
        item[1] = title;
        item[2] = author;
        item[3] = section;
        item[4] = x;
        item[5] = y;
        item[6] = barCode;
        item[7] = description;
        item[8] = isOnLoan;
        arrayList.add(item);
        // inform the GUI that I have change
        fireTableDataChanged();
    }
}

// row sorter
