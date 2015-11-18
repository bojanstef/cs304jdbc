# CS304 Assign 3 -- Bojan Stefanovic -- 14842124

NOTE: Please refresh project after executing to show 16 output files. 

NOTE: If dropTables() throws an exception the first time you execute, just comment it out "dropTables()" in Main, then execute for the first time. After that, you can uncomment dropTables() for all succeeding executions. 

#### Files included: 
        Main.java
        dropTables.txt
        createTables.txt
        insertIntoTables.txt
        part3query.txt
        part4query.txt
    
#### What happens when you run:
        Console prints "Connection Successful."
    
#### Part 1 - Executes in the background
        tables are dropped
        tables are created
        tables are populated
        
#### Part 2a - Examples are executed and printed in the console
        Insert successful printed to console
        Insert failure printed to console
        Remove successful printed to console from 3 tables
        Removes failure printed to console from 3 tables
        
#### Part 2b - Interactive Prompt
        This prompt will let you insert or remove items from the database. 
        Press 'q' to run parts 3 and 4 automatically.
        
#### Part 3 - Query 1
        This runs automatically after the interactive prompt quits. 
        It prints the 3 books that satisfy the query.

#### Part 4 - Query 2
        This runs automatically after the first query is finished.
        It prints the UPC and Quantity sold for the best 3 items this week.
