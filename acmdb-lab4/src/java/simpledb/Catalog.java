package simpledb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 *
 * @Threadsafe
 */
public class Catalog {
	
	public static class TableItem {
		public final int tableId;
		public DbFile file;
		public String name, primaryKeyField;
		
		TableItem(int tableId, DbFile file, String name, String primaryKeyField) {
			this.tableId = tableId;
			this.file = file;
			this.name = name;
			this.primaryKeyField = primaryKeyField;
		}
		
		TableItem(DbFile file, String name, String primaryKeyField) {
			this(file.getId(), file, name, primaryKeyField);
		}
		
		int getTableId() {
			return this.tableId;
		}
		
		DbFile getFile() {
			return this.file;
		}
		
		String getName() {
			return this.name;
		}
	}
	
	private HashMap<Integer, TableItem> tableById;
	private HashMap<String, Integer> idTableByName;	
	/**
	 * Constructor.
	 * Creates a new, empty catalog.
	 */
	public Catalog() {
		this.tableById = new HashMap<>();
		this.idTableByName = new HashMap<>();
	}	
	/**
	 * Add a new table to the catalog.
	 * This table's contents are stored in the specified DbFile.
	 * @param file the contents of the table to add;  file.getId() is the identifier of
	 *    this file/tupleDesc param for the calls getTupleDesc and getFile
	 * @param name the name of the table -- may be an empty string.  May not be null.  If a name
	 * conflict exists, use the last table to be added as the table for a given name.
	 * @param pkeyField the name of the primary key field
	 */
	public void addTable(DbFile file, String name, String pkeyField) {
		this.tableById.put(file.getId(), new TableItem(file, name, pkeyField));
		this.idTableByName.put(name, file.getId());
	}
	
	public void addTable(DbFile file, String name) {
		this.addTable(file, name, "");
	}	
	/**
	 * Add a new table to the catalog.
	 * This table has tuples formatted using the specified TupleDesc and its
	 * contents are stored in the specified DbFile.
	 * @param file the contents of the table to add;  file.getId() is the identifier of
	 *    this file/tupledesc param for the calls getTupleDesc and getFile
	 */
	public void addTable(DbFile file) {
		this.addTable(file, (UUID.randomUUID()).toString());
	}	
	/**
	 * Return the id of the table with a specified name,
	 * @throws NoSuchElementException if the table doesn't exist
	 */
	public int getTableId(String name) throws NoSuchElementException {
		if (!this.idTableByName.containsKey(name))
			throw new NoSuchElementException("Table with name \"" + name + "\" does not exist");
		
		return this.idTableByName.get(name);
	}	
	/**
	 * Returns the tuple descriptor (schema) of the specified table
	 * @param tableId The id of the table, as specified by the DbFile.getId()
	 *     function passed to addTable
	 * @throws NoSuchElementException if the table doesn't exist
	 */
	public TupleDesc getTupleDesc(int tableId) throws NoSuchElementException {
		if (!this.tableById.containsKey(tableId))
			throw new NoSuchElementException("Table with id " + tableId + " does not exist");
		
		return this.tableById.get(tableId).file.getTupleDesc();
	}	
	/**
	 * Returns the DbFile that can be used to read the contents of the
	 * specified table.
	 * @param tableId The id of the table, as specified by the DbFile.getId()
	 *     function passed to addTable
	 */
	public DbFile getDatabaseFile(int tableId) throws NoSuchElementException {
		if (!this.tableById.containsKey(tableId))
			throw new NoSuchElementException("Table with id " + tableId + " does not exist");
		
		return this.tableById.get(tableId).file;
	}
	
	public String getPrimaryKey(int tableId) {
		if (!this.tableById.containsKey(tableId))
			throw new NoSuchElementException("Table with id " + tableId + " does not exist");
		
		return this.tableById.get(tableId).primaryKeyField;
	}
	
	public Iterator<Integer> tableIdIterator() {
		return tableById.keySet().iterator();
	}
	
	public String getTableName(int id) {
		if (!this.tableById.containsKey(id))
			throw new NoSuchElementException("Table with id " + id + " does not exist");
		
		return this.tableById.get(id).name;
	}
	/** Delete all tables from the catalog */
	public void clear() {
		this.tableById.clear();
		this.idTableByName.clear();
	}	
	/**
	 * Reads the schema from a file and creates the appropriate tables in the database.
	 * @param catalogFile .
	 */
	public void loadSchema(String catalogFile) {
		String line = "";
		String baseFolder=new File(new File(catalogFile).getAbsolutePath()).getParent();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(catalogFile)));
			
			while ((line = br.readLine()) != null) {
				//assume line is of the format name (field type, field type, ...)
				String name = line.substring(0, line.indexOf("(")).trim();
				//System.out.println("TABLE NAME: " + name);
				String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
				String[] els = fields.split(",");
				ArrayList<String> names = new ArrayList<String>();
				ArrayList<Type> types = new ArrayList<Type>();
				String primaryKey = "";
				for (String e : els) {
					String[] els2 = e.trim().split(" ");
					names.add(els2[0].trim());
					if (els2[1].trim().toLowerCase().equals("int"))
						types.add(Type.INT_TYPE);
					else if (els2[1].trim().toLowerCase().equals("string"))
						types.add(Type.STRING_TYPE);
					else {
						System.out.println("Unknown type " + els2[1]);
						System.exit(0);
					}
					if (els2.length == 3) {
						if (els2[2].trim().equals("pk"))
							primaryKey = els2[0].trim();
						else {
							System.out.println("Unknown annotation " + els2[2]);
							System.exit(0);
						}
					}
				}
				Type[] typeAr = types.toArray(new Type[0]);
				String[] namesAr = names.toArray(new String[0]);
				TupleDesc t = new TupleDesc(typeAr, namesAr);
				HeapFile tabHf = new HeapFile(new File(baseFolder+"/"+name + ".dat"), t);
				addTable(tabHf,name,primaryKey);
				System.out.println("Added table : " + name + " with schema " + t);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IndexOutOfBoundsException e) {
			System.out.println ("Invalid catalog entry : " + line);
			System.exit(0);
		}
	}
}