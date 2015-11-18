package cs304jdbc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/*
 * Have to SSH to remote at UBC first, to be able to connect to the db. 
 * ssh -l r2l8 -L localhost:1522:dbhost.ugrad.cs.ubc.ca:1522 remote.ugrad.cs.ubc.ca
 * 
 * */

public class Main {		
	
	private static String[] dropTablesArray;
	private static String[] createTablesArray;
	private static String[] insertIntoTablesArray;
	
	private static Statement stmt;
	
	public static void main(String[] args) {			 				
		readTextFilesToArrays();		
		connectToDatabase();				
		
		/** 
		 * 
		 * TODO: COMMENT dropTables() OUT IF IT'S THROWING AN EXCEPTION. 
		 *  
		 **/
		dropTables(stmt); // Throws exception if there are no tables to drop.		
		
		// Part 1 - Create tables and Populate them.			
		createTables(stmt); 
		populateTables(stmt); 		
		
		// Part 2 - Insert and remove prompt. 
		executePartTwoPrompt(stmt);
		
		// Part 3 - Query
		executePartThree(stmt);
		
		// Part 4 - Query
		executePartFour(stmt);
		
		// At the end close the Statement object. 
		try {
			stmt.close();
		} catch (SQLException e) {			
			e.printStackTrace();
		}
    }
	
	// Method to read files and return the contents as a string. 
	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	// Method to split long strings by semicolon into an array of strings.
	private static String[] parseTextFileToStringArrayCommands(String file) {	
		String[] createTablesArr = null;
		
		try {
			String tablesString = readFile(file, Charset.defaultCharset());
			createTablesArr = tablesString.split(";");		
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
		
		return createTablesArr;
	}	
	
	// Method that updates the create tables and drop tables arrays with the contents of the text files.
	private static void readTextFilesToArrays() {
		createTablesArray = parseTextFileToStringArrayCommands("createTables.txt");
		dropTablesArray = parseTextFileToStringArrayCommands("dropTables.txt");
		insertIntoTablesArray = parseTextFileToStringArrayCommands("insertIntoTables.txt");
	}
	
	private static void connectToDatabase() {
        try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:ug", "ora_r2l8", "a14842124");
			System.out.println("Connection Successful.");	
			stmt = con.createStatement();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void dropTables(Statement s) {
		for (int i = 0; i < dropTablesArray.length; i++) {
			try {
				s.executeUpdate(dropTablesArray[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void createTables(Statement s) {		
		for (int i = 0; i < createTablesArray.length; i++) {
			try {
				s.executeUpdate(createTablesArray[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void populateTables(Statement s) {
		for (int i = 0; i < insertIntoTablesArray.length; i++) {
			try {
				s.executeUpdate(insertIntoTablesArray[i]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void executePartTwoPrompt(Statement s) {							
		executeExampleOneSuccess(s); 	// Part 2-a
		executeExampleTwoFailure(s); 	// Part 2-b
		executeExampleThreeSuccess(s);	// Part 2-c
		executeExampleFourFailure(s);	// Part 2-d
		
		// Clerk/Manager Client.
		Boolean promptLoop = true;
		Scanner reader = new Scanner(System.in);
		while (promptLoop) {			
			System.out.print("To INSERT an item type 'i', to REMOVE an item type 'r', to QUIT type 'q': ");			
			String operation = reader.next();
			String lowercaseOperation = operation.toLowerCase();
						
			if (lowercaseOperation.equals("i")) {
				insertOperation(s);
			} 
			else if (lowercaseOperation.equals("r")) {
				removeOperation(s);
			}
			else if (lowercaseOperation.equals("q")) {
				reader.close();
				promptLoop = false;				
			}
			else {
				System.out.println("Invalid input, try again.");
			}
		}	
	}
	
	private static void executePartThree(Statement s) {
		try {
			String part3queryString = readFile("part3query.txt", Charset.defaultCharset());
			String part3Query = part3queryString.replaceAll("\n", " ");			
			ResultSet books = s.executeQuery(part3Query);
		    while (books.next()) {		    	
				System.out.println(books.getString(1));	
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void executePartFour(Statement s) {
		try {
			String part4queryString = readFile("part4query.txt", Charset.defaultCharset());
			String part4Query = part4queryString.replaceAll("\n", " ");			
			ResultSet items = s.executeQuery(part4Query);			
		    for(int i = 1; i <= 3; i++) {
		    	items.next();
				System.out.println("Top " + i + ": Item - " + items.getString(1) + " sold " + items.getString(2) + " units this week.");	
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void executeExampleOneSuccess(Statement s) {	
		printItemTable(s, "part2-1a_insertSuccessBefore_ITEM.txt");
		insertStatement(s); // My example doesn't insert a book, so Item is the only table we need to view. 
		printItemTable(s, "part2-1b_insertSuccessAfter_ITEM.txt");
	}
	
	private static void executeExampleTwoFailure(Statement s) {		
		printItemTable(s, "part2-2a_insertFailureBefore_ITEM.txt");
		insertStatement(s); // My example doesn't insert a book, so Item is the only table we need to view.
		printItemTable(s, "part2-2b_insertFailureAfter_ITEM.txt");
	}
	
	private static void executeExampleThreeSuccess(Statement s) {
		printItemPurchaseTable(s, "part2-3a_removeSuccessBefore_ITEMPURCHASE.txt");
		printBookTable(s, "part2-3a_removeSuccessBefore_BOOK.txt");
		printItemTable(s, "part2-3a_removeSuccessBefore_ITEM.txt");
		removeStatement(s);
		printItemPurchaseTable(s, "part2-3b_removeSuccessAfter_ITEMPURCHASE.txt");
		printBookTable(s, "part2-3b_removeSuccessAfter_BOOK.txt");
		printItemTable(s, "part2-3b_removeSuccessAfter_ITEM.txt");
	}
	
	private static void executeExampleFourFailure(Statement s) {
		printItemPurchaseTable(s, "part2-4a_removeFailureBefore_ITEMPURCHASE.txt");
		printBookTable(s, "part2-4a_removeFailureBefore_BOOK.txt");
		printItemTable(s, "part2-4a_removeFailureBefore_ITEM.txt");
		removeStatement(s);
		printItemPurchaseTable(s, "part2-4b_removeFailureAfter_ITEMPURCHASE.txt");
		printBookTable(s, "part2-4b_removeFailureAfter_BOOK.txt");
		printItemTable(s, "part2-4b_removeFailureAfter_ITEM.txt");
	}
	
	private static void insertStatement(Statement s) {				
		try {
			ResultSet duplicate = s.executeQuery("SELECT * FROM item WHERE upc='a00444'");					
			if (duplicate.next()) { // If duplicate.next() is true, then this item exists. 
				System.out.println("Can't Execute: INSERT INTO ITEM VALUES('a00444',44,444,'y') this item already exists!");
			}
			else {
				System.out.println("Execute: INSERT INTO ITEM VALUES('a00444',44,444,'y')");
				s.executeUpdate("INSERT INTO item VALUES('a00444',44,444,'y')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	private static void insertOperation(Statement s) {		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String itemIsaBook = null;
		try {
			System.out.print("Is the item a book (y/n): ");
			itemIsaBook = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String upc = null;
		try {
			System.out.print("UPC char(6): ");
			upc = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String sellingPrice = null;
		try {
			System.out.print("Selling price (float): ");
			sellingPrice = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			
		String stock = null;
		try {
			System.out.print("Stock (int): ");
			stock = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String taxable = null;
		try {
			System.out.print("Taxable (y/n): ");
			taxable = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (itemIsaBook.equals("y")) {		
			String title = null;
	        try {
	        	System.out.print("Title (varchar(50)): ");
				title = br.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}						
	        
	        String publisher = null;
	        try {
	        	System.out.print("Publisher (varchar(50)): ");					
				publisher = br.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			

	        String isaTextbook = null;
	        try {
	        	System.out.print("Textbook (y/n): ");					
				isaTextbook = br.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
				
			try {
				ResultSet duplicate = s.executeQuery("SELECT * FROM item WHERE UPC='" + upc +"'");					
				if (duplicate.next()) { // If duplicate.next() is true, then this item exists. 
					System.out.print("This item already exists!");
				}
				else {
					s.executeUpdate("INSERT INTO item VALUES('" + upc + "'," + sellingPrice + "," + stock + ",'" + taxable + "')");					
					s.executeUpdate("INSERT INTO book VALUES((SELECT upc FROM ITEM WHERE upc='" + upc + "'),'" + title + "','" + publisher + "','" + isaTextbook + "')");					
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		else {
			try {
				ResultSet duplicate = s.executeQuery("SELECT * FROM item WHERE UPC='" + upc +"'");					
				if (duplicate.next()) { // If duplicate.next() is true, then this item exists. 
					System.out.println("This item already exists!");
				}
				else {
					s.executeUpdate("INSERT INTO item VALUES('" + upc + "'," + sellingPrice + "," + stock + ",'" + taxable + "')");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}			
	}
	
	private static void removeStatement(Statement s) {
		try {
			ResultSet itemExists = s.executeQuery("SELECT * FROM item WHERE upc='a00001'");
			if (itemExists.next()) { // If the item exists. Delete it 
				System.out.println("Execute: DELETE FROM itempurchase WHERE upc='a00001'");
				System.out.println("Execute: DELETE FROM book WHERE upc='a00001'");				
				System.out.println("Execute: DELETE FROM item WHERE upc='a00001'");
				s.executeUpdate("DELETE FROM itempurchase WHERE upc='a00001'");
				s.executeUpdate("DELETE FROM book WHERE upc='a00001'");				
				s.executeUpdate("DELETE FROM item WHERE upc='a00001'");
			} 
			else {
				System.out.println("Can't execute: DELETE FROM itempurchase WHERE upc='a00001' this item does not exist!");
				System.out.println("Can't execute: DELETE FROM book WHERE upc='a00001' this item does not exist!");				
				System.out.println("Can't execute: DELETE FROM item WHERE upc='a00001' this item does not exist!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void removeOperation(Statement s) {
		try {
			ResultSet items = s.executeQuery("SELECT * FROM item");					
			while (items.next()) {
				System.out.format("%-10s%-10s%-10s%-10s\n", 
						items.getString(1), items.getString(2),
						items.getString(3), items.getString(4));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));		
		String itemToRemove = null;
		try {
			System.out.print("By the UPC which Item would you like to remove: ");
			itemToRemove = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			ResultSet itemExists = s.executeQuery("SELECT * FROM item WHERE upc='" + itemToRemove + "'");
			if (itemExists.next()) { // If the item exists. Delete it 
				s.executeUpdate("DELETE FROM itempurchase WHERE upc='" + itemToRemove + "'");
				s.executeUpdate("DELETE FROM book WHERE upc='" + itemToRemove + "'");				
				s.executeUpdate("DELETE FROM item WHERE upc='" + itemToRemove + "'");
			} 
			else {
				System.out.println("This item does not exist!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void printItemTable(Statement s, String filename) {		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			ResultSet rs = s.executeQuery("SELECT * FROM item");
			while (rs.next()) {	
				writer.format("%-10s%-10s%-10s%-10s\n", 
						rs.getString(1), rs.getString(2),
						rs.getString(3), rs.getString(4));			
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		writer.close();
	}
	
	private static void printBookTable(Statement s, String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			ResultSet rs = s.executeQuery("SELECT * FROM book");
			while (rs.next()) {	
				writer.format("%-10s%-55s\n%-55s%-10s\n", 
						rs.getString(1), rs.getString(2),
						rs.getString(3), rs.getString(4));			
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		writer.close();		
	}
	
	private static void printPurchaseTable(Statement s, String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			ResultSet rs = s.executeQuery("SELECT * FROM purchase");
			while (rs.next()) {	
				writer.format("%-10s%-10s%-10s%-20s%-15s%-15s\n", 
						rs.getString(1), rs.getString(2), rs.getString(3),
						rs.getString(4), rs.getString(5), rs.getString(6));			
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		writer.close();			
	}
	
	private static void printItemPurchaseTable(Statement s, String filename) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			ResultSet rs = s.executeQuery("SELECT * FROM itempurchase");
			while (rs.next()) {	
				writer.format("%-10s%-10s%-10s\n", 
						rs.getString(1), rs.getString(2), rs.getString(3));		
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		writer.close();	
	}	
}
