package com.example.carlosmo.inhumanterms;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Carlos Mo on 26/11/2015.
 */

// Source: http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
public class DatabaseHelper extends SQLiteOpenHelper {
    // The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.example.carlosmo.inhumanterms/databases/";
    // Database name
    private static String DB_NAME = "inhumanterms.db";
    private static final int DATABASE_VERSION = 1;
    private final Context myContext;
    // Table names
    private static String TABLE_TERMS, TABLE_FAVOURITES, TABLE_RECENTS, TABLE_USER_DICTIONARIES;
    // Table column names
    // public static String KEY_ID, KEY_DICTIONARY, KEY_TERM, KEY_SUBJECT, KEY_DEFINITION, KEY_SYNONYMS;
    private static final String KEY_ID = "_id";
    private static final String KEY_DICTIONARY = "dictionary";
    private static final String KEY_TERM = "Term";
    private static final String KEY_SUBJECT = "Subject";
    private static final String KEY_DEFINITION = "Definition";
    private static final String KEY_SYNONYMS = "Synonyms";
    private static final String KEY_FAVOURITED = "Favourited";

    // Tag used for debugging purposes
    private final String TAG = "DatabaseHelper";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHelper (Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;

        TABLE_TERMS = context.getString(R.string.TABLE_TERMS);
        TABLE_FAVOURITES = context.getString(R.string.TABLE_FAVOURITES);
        TABLE_RECENTS = context.getString(R.string.TABLE_RECENTS);
        TABLE_USER_DICTIONARIES = context.getString(R.string.TABLE_USER_DICTIONARIES);

        /*
        KEY_ID = context.getString(R.string.KEY_ID);
        KEY_DICTIONARY = context.getString(R.string.KEY_DICTIONARY);
        KEY_TERM = context.getString(R.string.KEY_TERM);
        KEY_SUBJECT = context.getString(R.string.KEY_SUBJECT);
        KEY_DEFINITION = context.getString(R.string.KEY_DEFINITION);
        KEY_SYNONYMS = context.getString(R.string.KEY_SYNONYMS);
        TABLE_RECENTS = context.getString(R.string.TABLE_RECENTS);
        */
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TERMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TERMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + KEY_DICTIONARY + " TEXT,"
                + KEY_TERM + " TEXT," + KEY_SUBJECT + " TEXT," + KEY_DEFINITION + " TEXT,"
                + KEY_SYNONYMS + " TEXT," + KEY_FAVOURITED + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_TERMS_TABLE);

        String CREATE_FAVOURITES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVOURITES + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL," + KEY_DICTIONARY + " TEXT,"
                + KEY_TERM + " TEXT," + KEY_SUBJECT + " TEXT," + KEY_DEFINITION + " TEXT,"
                + KEY_SYNONYMS + " TEXT," + KEY_FAVOURITED + " INTEGER DEFAULT 1" + ")";
        db.execSQL(CREATE_FAVOURITES_TABLE);

        String CREATE_RECENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY NOT NULL," + KEY_DICTIONARY + " TEXT,"
                + KEY_TERM + " TEXT," + KEY_SUBJECT + " TEXT," + KEY_DEFINITION + " TEXT,"
                + KEY_SYNONYMS + " TEXT," + KEY_FAVOURITED + " INTEGER" + ")";
        db.execSQL(CREATE_RECENTS_TABLE);

        String CREATE_USER_DICTIONARIES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER_DICTIONARIES + "("
                + KEY_DICTIONARY + " TEXT PRIMARY KEY NOT NULL" + ") ";
        db.execSQL(CREATE_USER_DICTIONARIES_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TERMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DICTIONARIES);

        // Create tables again
        onCreate(db);
    }

    // Load all terms from CSV
    // Source: http://www.techrepublic.com/blog/software-engineer/turbocharge-your-sqlite-inserts-on-android/
    public void loadTermsFromCSV(String table, String dictionary) {
        // Get csv file
        String[] dictionaryList = myContext.getResources().getStringArray(R.array.dictionary_names);
        String[] csvList = myContext.getResources().getStringArray(R.array.dictionary_csv);
        int index = Arrays.asList(dictionaryList).indexOf(dictionary);
        String dictionaryCSV = csvList[index];

        // Get database object
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare sql statment
        String sql = "INSERT INTO " + table + " (" +
                KEY_DICTIONARY + ", " + KEY_TERM + ", " + KEY_SUBJECT + ", " + KEY_DEFINITION + ", " + KEY_SYNONYMS +
                ") VALUES (?, ?, ?, ?, ?)";

        // Compile sql statment
        SQLiteStatement statement = db.compileStatement(sql);

        // Start transaction
        db.beginTransaction();
        try {
            // Source: http://www.simplecodestuffs.com/read-write-csv-file-in-java-using-opencsv-library
            CSVReader reader;
            try {
                reader = new CSVReader(new InputStreamReader(myContext.getAssets().open(dictionaryCSV)));
                String[] row;

                while ((row = reader.readNext()) != null) {
                    // Clear statment bindings
                    statement.clearBindings();
                    // Bind corresponding values to sql statement
                    statement.bindString(1, dictionary);
                    statement.bindString(2, row[1]);
                    statement.bindString(3, row[0]);
                    statement.bindString(4, row[7] + ";" + row[8] + ";" + row[9]);
                    statement.bindString(5, row[5]);
                    // Execute sql statement
                    statement.execute();
                }
            }
            catch (FileNotFoundException e) { }
            catch (IOException e) { }

            statement.close();
            db.setTransactionSuccessful();
        } finally {
            // End transaction
            db.endTransaction();
        }
    }

    // Check if a table is empty
    public boolean checkEmptyTable(String table) {
        // Select query
        String selectQuery = "SELECT count(*) FROM " + table;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) { cursor.moveToFirst(); }

        int countRows = cursor.getInt(0);

        return !(countRows > 0);
    }

    // Adding new term (only used for table terms)
    public void addTerm(Term term, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DICTIONARY, term.getDictionary());
        values.put(KEY_TERM, term.getTerm());
        values.put(KEY_SUBJECT, term.getSubject());
        values.put(KEY_DEFINITION, term.getDefinition());
        values.put(KEY_SYNONYMS, term.getSynonyms());
        values.put(KEY_FAVOURITED, term.getFavourited());

        // Inserting Row
        db.insert(table, null, values);
        db.close(); // Closing database connection
    }

    // Add new term with id (only used for tables favourites and recents)
    public void addTermWithId(Term term, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, term.getId());
        values.put(KEY_DICTIONARY, term.getDictionary());
        values.put(KEY_TERM, term.getTerm());
        values.put(KEY_SUBJECT, term.getSubject());
        values.put(KEY_DEFINITION, term.getDefinition());
        values.put(KEY_SYNONYMS, term.getSynonyms());
        values.put(KEY_FAVOURITED, term.getFavourited());

        // Inserting Row
        db.insert(table, null, values);
        db.close(); // Closing database connection
    }

    // Add a term to the table favourites and update the favourited flag in the table terms
    public void addTermToFavourites(Term term) {
        // Set the favourited status of the term
        term.setFavourited(1);
        // Add the term to the table favourites
        addTermWithId(term, TABLE_FAVOURITES);
        // Update the favourite status of the term in the table terms
        updateTermFavouritedFlag(term, TABLE_TERMS);
    }

    // Update the favourited flag of a term
    public int updateTermFavouritedFlag (Term term, String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FAVOURITED, term.getFavourited());

        // updating row
        return db.update(table, values, KEY_ID + " = ?", new String[] { String.valueOf(term.getId())});
    }

    // Find a term and return a cursor
    public Cursor findTermCursor (String table, String constraint) {
        // Select All Query
        // Source: http://stackoverflow.com/questions/5186310/android-nearest-match-query-from-sqlite-db-if-input-string-value-is-bigger-tha
        String selectQuery = "SELECT " + KEY_ID + " FROM " + table
                + " WHERE " + KEY_TERM + " = '" + constraint + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return the cursor
        return cursor;
    }

    // Getting single term
    public Term getTerm(int id, String table) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(table, new String[]{KEY_ID, KEY_DICTIONARY, KEY_TERM, KEY_SUBJECT, KEY_DEFINITION, KEY_SYNONYMS, KEY_FAVOURITED},
                KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null) { cursor.moveToFirst(); }

        Term term = new Term(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));

        // return term
        return term;
    }

    // Get single random term
    public Term getRandomTerm (String table) {
        // Select Query
        String selectQuery = "SELECT  * FROM " + table + " WHERE " + KEY_TERM + " NOT NULL AND " +
                "LENGTH(" + KEY_TERM + ") <= 16 " + "ORDER BY RANDOM() LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) { cursor.moveToFirst(); }

        Term term = new Term(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));

        // return term
        return term;
    }

    // Get all terms
    public List<Term> getAllTerms(String table) {
        List<Term> termList = new ArrayList<Term>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + table + " ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(2).isEmpty()) {
                    /*
                    Term term = new Term();
                    term.setId(Integer.parseInt(cursor.getString(0)));
                    term.setDictionary(cursor.getString(1));
                    term.setTerm(cursor.getString(2));
                    term.setSubject(cursor.getString(3));
                    term.setDefinition(cursor.getString(4));
                    term.setSynonyms(cursor.getString(5));
                    */
                    Term term = new Term(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));
                    // Adding term to list
                    termList.add(term);
                }
            } while (cursor.moveToNext());
        }

        // return term list
        return termList;
    }

    // Get all terms by dictionary
    public List<Term> getAllTermsByDictionary(String table, String dictionary) {
        List<Term> termList = new ArrayList<Term>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + table + " WHERE " + KEY_DICTIONARY + " = '" + dictionary + "' ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(2).isEmpty()) {
                    /*
                    Term term = new Term();
                    term.setId(Integer.parseInt(cursor.getString(0)));
                    term.setDictionary(cursor.getString(1));
                    term.setTerm(cursor.getString(2));
                    term.setSubject(cursor.getString(3));
                    term.setDefinition(cursor.getString(4));
                    term.setSynonyms(cursor.getString(5));
                    */
                    Term term = new Term(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), Integer.parseInt(cursor.getString(6)));
                    // Adding term to list
                    termList.add(term);
                }
            } while (cursor.moveToNext());
        }

        // return term list
        return termList;
    }

    // Get all terms list items
    public List<TermListItem> getAllTermListItems(String table) {
        List<TermListItem> termListItems = new ArrayList<TermListItem>();

        // Select All Query
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table + " ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(1).isEmpty()) {
                    TermListItem termListItem = new TermListItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
                    // Adding term to list
                    termListItems.add(termListItem);
                }
            } while (cursor.moveToNext());
        }

        // return term list
        return termListItems;
    }

    // Get all term list items by dictionary
    public List<TermListItem> getAllTermListItemsByDictionary(String table, String dictionary) {
        List<TermListItem> termListItems = new ArrayList<TermListItem>();

        // Select All Query
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table + " WHERE " + KEY_DICTIONARY + " = '" + dictionary + "' ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(1).isEmpty()) {
                    TermListItem termListItem = new TermListItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
                    // Adding term to list
                    termListItems.add(termListItem);
                }
            } while (cursor.moveToNext());
        }

        // return term list items
        return termListItems;
    }

    // Get all terms cursor
    public Cursor getAllTermListItemsCursor(String table) {
        // Select All Query
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table + " ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return the cursor
        return cursor;
    }

    // Get all terms list items cursor with a filter
    public Cursor getAllTermListItemsCursorWithFilter(String table, String constraint) {
        // Select All Query
        // Source: http://stackoverflow.com/questions/5186310/android-nearest-match-query-from-sqlite-db-if-input-string-value-is-bigger-tha
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table
                + " WHERE " + KEY_TERM + " LIKE '%" + constraint + "%'"
                + " ORDER BY abs(length('" + constraint + "') - length(" + KEY_TERM + "))";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return the cursor
        return cursor;
    }

    // Get all terms list items with a filter
    public List<TermListItem> getAllTermListItemsWithFilter(String table, String constraint) {
        List<TermListItem> termListItems = new ArrayList<TermListItem>();

        // Select All Query
        // Source: http://stackoverflow.com/questions/5186310/android-nearest-match-query-from-sqlite-db-if-input-string-value-is-bigger-tha
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table
                + " WHERE " + KEY_TERM + " LIKE '%" + constraint + "%'"
                + " ORDER BY abs(length('" + constraint + "') - length(" + KEY_TERM + "))";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if (!cursor.getString(1).isEmpty()) {
                    TermListItem termListItem = new TermListItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
                    // Adding term to list
                    termListItems.add(termListItem);
                }
            } while (cursor.moveToNext());
        }

        // return a list of term list items
        return termListItems;
    }

    // Get all terms cursor
    public Cursor getAllTermsCursor(String table) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + table + " ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return the cursor
        return cursor;
    }

    // Get all terms by dictionary cursor
    public Cursor getAllTermListItemsByDictionaryCursor(String table, String dictionary) {
        // Select All Query
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TERM + " FROM " + table + " WHERE " + KEY_DICTIONARY + " = '" + dictionary + "' ORDER BY " + KEY_TERM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // return cursor
        return cursor;
    }

    // Delete single term
    public void deleteTerm(Term term, String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, KEY_ID + " = ?",
                new String[] { String.valueOf(term.getId()) });
        db.close();
    }

    // Delete single term from favourites
    public void deleteTermFromFavourites(int id) {
        // Get the term
        Term term = getTerm(id, TABLE_FAVOURITES);
        // Set the favourited status of the term
        term.setFavourited(0);
        // Update the favourite status of the term in the table terms
        updateTermFavouritedFlag(term, TABLE_TERMS);
        // Delete the term from the table favourites
        deleteTerm(term, TABLE_FAVOURITES);
        /*
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVOURITES, KEY_ID + " = ?",
                new String[] { String.valueOf(term.getId()) });
        db.close();
        */
    }

    // CHeck if user dictionary is installed
    public boolean isUserDictionaryInstalled(String dictionary) {
        List<String> userDictionaries = getAllUserDictionaries();
        return userDictionaries.contains(dictionary);
    }

    // Adding new user dictionary
    public void addUserDictionary(String dictionary) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DICTIONARY, dictionary);

        // Inserting Row
        db.insert(TABLE_USER_DICTIONARIES, null, values);
        db.close(); // Closing database connection
    }

    // Get all user dictionaries
    public List<String> getAllUserDictionaries() {
        List<String> userDictionariesList = new ArrayList<String>();
        String dictionary;

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_USER_DICTIONARIES + " ORDER BY " + KEY_DICTIONARY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                dictionary = cursor.getString(0);
                // Adding user dictionary to list
                userDictionariesList.add(dictionary);
            } while (cursor.moveToNext());
        }

        // return term list
        return userDictionariesList;
    }

    // Delete single user dictionary
    public void deleteUserDictionary(String dictionary) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TERMS, KEY_DICTIONARY + " = ?",
                new String[] { dictionary });
        db.delete(TABLE_FAVOURITES, KEY_DICTIONARY + " = ?",
                new String[] { dictionary });
        db.delete(TABLE_RECENTS, KEY_DICTIONARY + " = ?",
                new String[] { dictionary });
        db.delete(TABLE_USER_DICTIONARIES, KEY_DICTIONARY + " = ?",
                new String[] { dictionary });
        db.close();
    }
}
