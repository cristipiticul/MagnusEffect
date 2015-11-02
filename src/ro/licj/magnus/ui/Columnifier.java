package ro.licj.magnus.ui;

/**
 * Used for UI tables to transform an object of class T into a table row.
 * The fields become columns.
 * 
 * @author Cristi
 */
public interface Columnifier<T> {
    String[] getColumnNames();
    Class[] getColumnTypes();
    Object getColumnOfElement(T element, int columnIndex);
}
