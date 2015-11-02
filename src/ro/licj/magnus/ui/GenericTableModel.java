package ro.licj.magnus.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Cristi
 */
public class GenericTableModel<T> extends AbstractTableModel {
    
    private List<T> elements = new ArrayList<T>();
    private Columnifier<T> columnifier;
    
    public GenericTableModel(Columnifier<T> columnifier) {
        this.columnifier = columnifier;
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public int getRowCount() {
        return elements.size();
    }

    @Override
    public int getColumnCount() {
        String[] columnNames = columnifier.getColumnNames();
        return columnNames.length;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        Class[] columnTypes = columnifier.getColumnTypes();
        return columnTypes[columnIndex];
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        String[] columnNames = columnifier.getColumnNames();
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T item = elements.get(rowIndex);
        return columnifier.getColumnOfElement(item, columnIndex);
    }
    
    public void addElement(T item) {
        int index = elements.size();
        elements.add(item);
        fireTableRowsInserted(index, index);
    }
    
    public void removeAllElements() {
        int size = elements.size();
        if (size > 0) {
            elements.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    public T getElement(int rowIndex) {
        return elements.get(rowIndex);
    }

    public void refreshCell(int rowIndex, int columnIndex) {
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
}
