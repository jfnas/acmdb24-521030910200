package simpledb;

import java.io.Serializable;
import java.util.*;
//import java.util.*;
/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    //private static final long serialVersionUID = 1L;
    
    // List of TDItems
    private List<TDItem> fieldList;
    public TupleDesc(){
        fieldList = new ArrayList<TDItem>();
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    
    public Iterator<TDItem> iterator() {
        return fieldList.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
    	fieldList = new ArrayList<TDItem>();
        for(int i=0; i<typeAr.length; i++)
            fieldList.add(new TDItem(typeAr[i], fieldAr[i]));
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
    	fieldList = new ArrayList<TDItem>();
        for (Type typeAr1 : typeAr) 
            fieldList.add(new TDItem(typeAr1, ""));
    	
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return fieldList.size();
    }
    public void removeField(int i){
        fieldList.remove(i);
    }
    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
    	if(i<0 || i>=fieldList.size()) {
    		throw new NoSuchElementException();
    	}
    	if(fieldList.get(i).fieldName == null)
    		return "null";
    	
        return fieldList.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
    	if(i<0 || i>=fieldList.size()) {
    		throw new NoSuchElementException();
    	}
    	
        return fieldList.get(i).fieldType;
       
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
    	if(name == null) {
    		throw new NoSuchElementException("null is not a valid field name");
    	}
        if(name.equals("p.id")){
            for(int i = 0; i < fieldList.size(); i++)
                System.out.println("L: " + fieldList.get(i).fieldName + " " + fieldList.get(i).fieldName.equals(name));
        }
        for(int i = 0; i < fieldList.size(); i++)
            if(fieldList.get(i).fieldName.equals(name))
                return i;
        System.out.println(name);
        for(int i = 0; i < fieldList.size(); i++)
            System.out.println("N: " + fieldList.get(i).fieldName + " " + fieldList.get(i).fieldName.equals(name));
        // If not found
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
    	int sizeInBytes = 0;
        for(TDItem tdItem : fieldList)
            sizeInBytes += tdItem.fieldType.getLen();
        return sizeInBytes;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
    	TupleDesc td = new TupleDesc();
        td.fieldList.addAll(td1.fieldList);
        td.fieldList.addAll(td2.fieldList);
        return td;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
    	// Check for same class
        if(o == null || !o.getClass().equals(this.getClass()))
            return false;
        // Check for equal size
        if(this.numFields() != ((TupleDesc) o).numFields())
            return false;
        // Check for equal contents
        TupleDesc td = (TupleDesc) o;
        for(int i = 0; i < this.fieldList.size(); i++)
            if(!this.fieldList.get(i).fieldType.equals(td.fieldList.get(i).fieldType))
                return false;
        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
    //    throw new UnsupportedOperationException("unimplemented");
    	return fieldList.hashCode();
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
    	 StringBuilder sB = new StringBuilder();
         for(TDItem tdItem : fieldList)
             sB.append(tdItem.toString()).append(", ");
         return sB.toString();
    }
}