package cs5530.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import cs5530.Connector;

public class DatabaseModel {
	
	protected String table;
	protected HashSet<String> columns;
	protected HashMap<String, String> colValPairs;
	protected String keyVal;
	protected HashSet<String> primaryKeyColumns;
	protected HashMap<String, String> primaryKey;
	protected String keyColumn;
	
	public DatabaseModel(){
		this.colValPairs = new HashMap<String, String>();
		this.columns = new HashSet<String>();
	}
	
	public DatabaseModel(String table, String keyColumn, HashSet<String> columns, HashMap<String, String> colValPair) {
		this.columns = columns;
		this.table = table;
		this.colValPairs = colValPair;
		this.keyColumn = keyColumn;
	}

	public String Insert() {
		String sql = "insert into ";
		sql += table;
		sql += " (" + columnsToString(columns) + ")";
		sql += valuesToString();
		System.out.println("executing: " + sql);
		
		try{
			//prepare connection
	 		Connector con = new Connector();
	 		PreparedStatement prep = con.con.prepareStatement(sql);
	 		
	 		Iterator<String> iCols = columns.iterator();
	 		int count = 1;
	 		while(iCols.hasNext())
	 		{
	 			prep.setString(count++, colValPairs.get(iCols.next()));
	 		}
	 		
	 		prep.executeUpdate();
	 		
	 		//close connection
	 		prep.close();
	 		con.closeConnection();
	 		
	 		//return key column value
	 		return queryKey();
	 	}
	 	catch(Exception e)
	 	{
	 		System.err.println("Unable to open mysql jdbc connection. The error is as follows,\n");
    		System.err.println(e.getMessage());
    		return null;
	 	}
	}
	
	/**
	 * Queries the database for the value of the key column of the entity
	 * described by this class
	 * @return key column value as a string
	 */
	public String queryKey() {
		HashSet<String> cols = new HashSet<String>();
 		cols.add(keyColumn);
 		HashMap<String, String> result = Query(cols, buildWhereClause()).get(0);
 		return keyVal = result.get(keyColumn);
	}
	
	
	/**
	 * The private query method that does the brunt of the work.
	 * It takes a set of columns and a where clause as a string.
	 * It queries the table of the subclass that inherits this model on the 
	 * database columns described in the set.  It also appends the where
	 * clause.  It returns an ArrayList of HashMaps.  Each HashMap is a map of Column
	 * value pairs and each occurrence in the ArrayList is a row.
	 * 
	 * @param cols
	 * @param whereClause
	 * @return ArrayList of HashMaps, the result data of the query
	 */
	private ArrayList<HashMap<String, String>> Query_Work(HashSet<String> cols, String whereClause) {
		try{
			String sql = "select ";
			sql += columnsToString(cols);
			sql += " from " + table;
			sql += " " + whereClause;

			System.out.println("executing: " + sql);
			Connector con = new Connector();
			PreparedStatement prep = con.con.prepareStatement(sql);
			ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
			ResultSet rs = prep.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			Iterator<String> iCols = cols.iterator();
			int numCols = rsmd.getColumnCount();
			int count = 0;
			while (rs.next())
			{
				HashMap<String, String> colVal = new HashMap<String, String>();
				for (int i=1; i<=numCols;i++)
				{
					colVal.put(rsmd.getColumnLabel(i), rs.getString(i));
				}
				results.add(colVal);
				count++;
			}

			//close connection
			prep.close();
			con.closeConnection();

			return results;
		} catch (Exception e)
		{
			System.err.println("Unable to execute sql. The error is as follows,\n");
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * The basic Query function that only takes a where clause as a string.
	 * It will query the database on the core columns described by the class.
	 * It returns an ArrayList of HashMaps.  Each HashMap is a map of Column
	 * value pairs and each occurrence in the ArrayList is a row.
	 * @param whereClause
	 * @return ArrayList of HashMaps, the result data of the query
	 */
	public ArrayList<HashMap<String, String>> Query(String whereClause) {
		return Query_Work(columns, whereClause);
	}

	/**
	 * Query function that allows overriding of the columns to query on.
	 * The columns are to be passed as a HashSet of Strings. The Strings
	 * should match the column labels of the resulting table set of the query.
	 * Remember that the strings are case sensitive.
	 * It returns an ArrayList of HashMaps.  Each HashMap is a map of Column
	 * value pairs and each occurrence in the ArrayList is a row.
	 * @param whereClause
	 * @return ArrayList of HashMaps, the result data of the query
	 */
	public ArrayList<HashMap<String, String>> Query(HashSet<String> overrideColumns, String whereClause) {
		return Query_Work(overrideColumns, whereClause);
	}
	
	/**
	 * Query function that allows overriding of columns in the query.
	 * The columns are to be passed as a String each column separated
	 * by a comma. The Strings that describe each column
	 * should match the column labels of the resulting table set of the query.
	 * Remember that the strings are case sensitive.
	 * It returns an ArrayList of HashMaps.  Each HashMap is a map of Column
	 * value pairs and each occurrence in the ArrayList is a row.
	 * @param whereClause
	 * @return ArrayList of HashMaps, the result data of the query
	 */
	public ArrayList<HashMap<String, String>> Query(String simpleColumns, String whereClause) {
		HashSet<String> cols = new HashSet<String>();
		cols.add(simpleColumns);
		return Query_Work(cols, whereClause);
	}
	
	/**
	 * Takes a HashSet of Strings to represent the column labels.
	 * Mostly intended to take the column HashSet of the class, but
	 * is overridable.  If the set is empty, it returns an empty string;
	 * if the column set contains a string with an asterisk, it assumes
	 * that all columns are being requested.
	 * Otherwise it will append each column label to the next separated 
	 * by commas.
	 * 
	 * @param overrideColumns
	 * @return String sequence of comma separated column labels
	 */
	private String columnsToString(HashSet<String> overrideColumns) {
		String colString = " ";
		if(overrideColumns.isEmpty())
		{
			return "";
		}
		else if(columns.contains("*"))
		{
			return " * ";
		}
		Iterator<String> iCol = overrideColumns.iterator();
		while(iCol.hasNext())
		{
			colString += iCol.next().trim() + ",";
		}
		
		int i = colString.lastIndexOf(',');
		colString = colString.substring(0, i);
		return colString;
	}
	
	/**
	 * Takes each of the values in the set of Column Value Pairs
	 * and turns them into a comma separated list.
	 * @return comma separated values as string
	 */
	private String valuesToString() {
		Collection<String> values = colValPairs.values();
		String vals = " ";
		
		if(values.isEmpty())
		{
			return vals;
		}
		Iterator<String> iVal = values.iterator();
		vals += "VALUES(";
		while(iVal.hasNext())
		{
			vals += "?,";
			iVal.next();
		}
		
		int i = vals.lastIndexOf(',');
		vals = vals.substring(0, i);
		return vals += ")";
	}
	
	/**
	 * Builds the default where clause for the described class.
	 * It takes each of the columns from the column set described
	 * by the class and each value from the set of defined column
	 * value pairs and creates a where clause to simply identify the
	 * row described by the defined properties.
	 * @return string default where clause
	 */
	private String buildWhereClause() {
		if(columns == null || colValPairs == null || columns.isEmpty() || colValPairs.isEmpty())
		{
			return "";
		}
		String sql = "where ";
		Iterator<String> iCols = columns.iterator();
		while(iCols.hasNext())
		{
			String col = iCols.next();
			sql += col + "='" + colValPairs.get(col) + "' and ";
		}
		
		int i = sql.lastIndexOf(" and ");
		return sql.substring(0, i);
	}
}