package it.vonneumannapps.gestionerubrica;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ContactDetailActivity extends AppCompatActivity {

    public static final int ADD_OR_EDIT_CODE = 1000; // numero arbitrario

    DBManager dbManager;
    Bundle contact;

    EditText nameET;
    EditText surnameET;
    EditText addressET;
    EditText phoneET;
    EditText emailET;

    // carico i valori dell'account nell'EditText
    void loadContactData() {

        if (!contact.isEmpty()) {

            this.nameET.setText(contact.getString(DBManager.NAME_COL));
            this.surnameET.setText(contact.getString(DBManager.SURNAME_COL));
            this.addressET.setText(contact.getString(DBManager.ADDRESS_COL));
            this.phoneET.setText(contact.getString(DBManager.PHONE_COL));
            this.emailET.setText(contact.getString(DBManager.EMAIL_COL));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        // inizializzo campi EditText del contatto
        nameET = findViewById(R.id.nameEditText);
        surnameET = findViewById(R.id.surnameEditText);
        addressET = findViewById(R.id.addressEditText);
        phoneET = findViewById(R.id.phoneEditText);
        emailET = findViewById(R.id.emailEditText);

        TextView titleTV = findViewById(R.id.titleTextView);

        // recupera l'intent con cui sono arrivato a questa activity
        // e ottiene gli extras passati all'intent
        this.contact = getIntent().getExtras();

        Boolean creatingANewContact = contact.isEmpty();
        if(creatingANewContact) {

            titleTV.setText(getString(R.string.NEW_CONTACT_TITLE));
        }
        else {
            titleTV.setText(getString(R.string.EDIT_ACCOUNT_TITLE,
                    Contact.getFullName(contact)));
        }

        this.dbManager = new DBManager(this, DBManager.DATABASE_NAME,
                null, DBManager.DATABASE_VERSION);

        Button saveBtn = findViewById(R.id.saveButton);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO controllare input utente

                // verificare se la chiamata all'inflater getsystemservice
                // funziona (cambiata per compatibilità API < 23)
                saveContactData();
            }
        });

        loadContactData();
    }

    void saveContactData() {

        // controllare se id esiste già, se no si crea. Usere db manager update o insert new contact
        Boolean isNewAccount = contact.isEmpty();

        contact.putString(DBManager.NAME_COL, nameET.getText().toString());
        contact.putString(DBManager.SURNAME_COL, surnameET.getText().toString());
        contact.putString(DBManager.ADDRESS_COL, addressET.getText().toString());
        contact.putString(DBManager.PHONE_COL, phoneET.getText().toString());
        contact.putString(DBManager.EMAIL_COL, emailET.getText().toString());

        if(isNewAccount) {

            dbManager.insertNewContact(contact);
        }
        else {
            dbManager.updateContact(contact);
        }

        Utils.showShortToast(this, R.string.SAVED_MESSAGE);

        setResult(RESULT_OK);

        finish();
    }
}
