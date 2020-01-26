package it.vonneumannapps.gestionerubrica;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "db";
    public static final int DATABASE_VERSION = 5;

    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String ID_COL = "id";
    public static final String NAME_COL = "name";
    public static final String SURNAME_COL = "surname";
    public static final String ADDRESS_COL = "postalAddress";
    public static final String PHONE_COL = "phone";
    public static final String EMAIL_COL = "email";
    public static final String PROFILE_PIC_COL = "immagine";
    public static final String SELECTED_FIELD_NAME = "selected";

    private static final String TAG = "DBManager";

    private final Context myContext;

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.myContext = context;
    }

    // NB onCreate è chiamata solo al primo accesso
    // anche se creo più istanza di DBManager
    // prima funzione, eseguita solo quando il db non esiste
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + CONTACTS_TABLE_NAME + " " +
                "(" + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME_COL + " TEXT, " +
                SURNAME_COL + " TEXT, " +
                ADDRESS_COL + " TEXT, " +
                PHONE_COL + " TEXT, " +
                EMAIL_COL + " TEXT, " +
                PROFILE_PIC_COL + " BLOB) ";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion);

        dropTables(sqLiteDatabase, myContext);
        onCreate(sqLiteDatabase);
    }

    void dropTables(SQLiteDatabase sqLiteDatabase, Context context){
        Log.w(TAG,"dropping DB tables..");

            String statement = "DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME;

            try {
                sqLiteDatabase.execSQL(statement);
            } catch (Exception e) {
                Log.e(TAG, statement);
                Log.e(TAG, e.toString());
                throw e;
            }
        Log.v(TAG, "Dropped DB schema and data.");
    }

    public void insertNewContact(Bundle newContact) {

        try(SQLiteDatabase db = getWritableDatabase()) {

            // tipo di dati "Bundle", sono chiave-valore
            // lo convertiamo in "ContentValues", che è il tipo di oggetto
            // che si aspetta la db insert

            ContentValues contentValues = new ContentValues();
            setContactMainFieldsToContentValues(contentValues, newContact);

            db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        }
    }

    public static void setContactMainFieldsToContentValues(
            ContentValues contentValues, Bundle newContact) {
        contentValues.put(NAME_COL, newContact.getString(NAME_COL));
        contentValues.put(SURNAME_COL, newContact.getString(SURNAME_COL));
        contentValues.put(ADDRESS_COL, newContact.getString(ADDRESS_COL));
        contentValues.put(PHONE_COL, newContact.getString(PHONE_COL));
        contentValues.put(EMAIL_COL, newContact.getString(EMAIL_COL));
        contentValues.put(PROFILE_PIC_COL, newContact.getByteArray(PROFILE_PIC_COL));
    }

    public void updateContact(Bundle contact) {

        try(SQLiteDatabase db = getWritableDatabase()) {

            ContentValues contentValues = new ContentValues();

            contentValues.put(ID_COL, contact.getInt(ID_COL));
            setContactMainFieldsToContentValues(contentValues, contact);

            String contactId = String.valueOf(contact.getInt(ID_COL));

            String[] args = new String[]{contactId};

            db.update(CONTACTS_TABLE_NAME, contentValues, ID_COL + "=?", args);
        }
    }

    public ArrayList<Bundle> getContacts() {

        ArrayList<Bundle> contacts = new ArrayList<>();
        String query = "SELECT * FROM " + CONTACTS_TABLE_NAME;

        try (SQLiteDatabase db = getReadableDatabase()) { //TODO refactor db e cur in one single try statement
            try(Cursor cur = db.rawQuery(query, null)) {

                cur.moveToFirst();

                while(!cur.isAfterLast()) {

                    Bundle contact = new Bundle();

                    putIntFromCursorIntoBundle(cur, contact, ID_COL);
                    putStringFromCursorIntoBundle(cur, contact, NAME_COL);
                    putStringFromCursorIntoBundle(cur, contact, SURNAME_COL);
                    putStringFromCursorIntoBundle(cur, contact, ADDRESS_COL);
                    putStringFromCursorIntoBundle(cur, contact, PHONE_COL);
                    putStringFromCursorIntoBundle(cur, contact, EMAIL_COL);
                    putByteArrFromCursorIntoBundle(cur, contact, PROFILE_PIC_COL);

                    contact.putBoolean(SELECTED_FIELD_NAME, false);

                    contacts.add(contact);
                    cur.moveToNext();
                }
            }
        }

        return contacts;
    }

    public static String getDeletionQuerySelectedContacts(ArrayList<Bundle> contacts) {

        String query = "DELETE FROM " + CONTACTS_TABLE_NAME
                + " WHERE " + ID_COL + " IN (";

        boolean isFirstIdToDelete = true;

        for(Bundle contact : contacts) {

            if(contact.getBoolean(SELECTED_FIELD_NAME))
            {
                int idToAdd = contact.getInt(ID_COL);

                if(isFirstIdToDelete) {

                    query = query + idToAdd;
                    isFirstIdToDelete = false; // abbiamo appena aggiunto il primo
                }
                else {
                    query = query + ", " + idToAdd;
                }
            }
        }

        query = query + ")";

        return query;
    }

    public void deleteSelectedContacts(ArrayList<Bundle> contacts) {

        String query = getDeletionQuerySelectedContacts(contacts);

        try(SQLiteDatabase db = getWritableDatabase()) {

            db.execSQL(query);
        }

        // TODO we need unit tests
    }

    static private void putStringFromCursorIntoBundle(Cursor cur, Bundle bundle, String columnName){

        int columnIndex = cur.getColumnIndex(columnName); // indice della colonna con nome columName
        String columnValue = cur.getString(columnIndex);
        bundle.putString(columnName, columnValue);
    }

    static private void putByteArrFromCursorIntoBundle(Cursor cur, Bundle bundle, String columnName){

        int columnIndex = cur.getColumnIndex(columnName);
        byte[] columnValue = cur.getBlob(columnIndex);
        bundle.putByteArray(columnName, columnValue);
    }

    static private void putIntFromCursorIntoBundle(Cursor cur, Bundle bundle, String columnName){

        int columnIndex = cur.getColumnIndex(columnName); // indice della colonna con nome columName
        int columnValue = cur.getInt(columnIndex);
        bundle.putInt(columnName, columnValue);
    }
}
