package it.vonneumannapps.gestionerubrica;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

// Contacts phone book

public class MainActivity extends AppCompatActivity {

    ArrayList<Bundle> contacts = new ArrayList<>();
    BaseAdapter baseAdapter;
    DBManager dbManager;

    boolean toggleAll = false;
    Button selectAllBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this, DBManager.DATABASE_NAME,
                null, DBManager.DATABASE_VERSION);

        initContactList();
        initButtonsAddRemoveSelectedContacts();
    }

    void initContactList() {

        initAdapter();
        loadContacts();
        final ListView contactsLV = findViewById(R.id.contactsListView);
        contactsLV.setAdapter(baseAdapter);

        contactsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle account = contacts.get(i);
                openContactDetailActivity(account);
            }
        });

        contactsLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                // todo selezionare singolo account
                Bundle contact = contacts.get(i);
                selectCurrentContact(contact);

                return true;
            }
        });
    }

    void initButtonsAddRemoveSelectedContacts() {
        ImageView addContactBtn = findViewById(R.id.addContactButton);
        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openContactDetailActivity(new Bundle());// problema memory leak: se poi si annulla la creazione dell'account
                // andrebbe creato solo facendo "salva"
            }
        });

        Button deleteBtn = findViewById(R.id.deleteButton);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDeletionConfirmationDialog();
            }
        });

        selectAllBtn = findViewById(R.id.selectAllButton);
        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //todo spiegare questo

                selectAllContacts();
            }
        });

        ImageView settingsBtn = findViewById(R.id.settingsButton);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    void openContactDetailActivity(Bundle contact) {

        Intent intent = new Intent(MainActivity.this,
                ContactDetailActivity.class);

        // serve per dire all'altra activity che riceve dei dati,
        // in questo caso il bundle del contatto
        intent.putExtras(contact);

        // result: ADD_OR_EDIT_CODE, per dire che ho aggiunto o modificato un elemento
        startActivityForResult(intent, ContactDetailActivity.ADD_OR_EDIT_CODE);
    }

    // per sapere quando torna indietro dall'altra activity (dalla ContactDetail activity)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {

            case ContactDetailActivity.ADD_OR_EDIT_CODE: {

                if(resultCode == RESULT_OK) {

                    loadContacts();
                    // TODO un pò inefficiente ricaricare tutti i contatti dal db ogni volta
                    // che se ne aggiunge/modifica uno
                    // si potrebbe modificare solo quello, su contacts e sul db
                }
                break;
            }
            default: {

                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    void initAdapter() {

        baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return contacts.size();
            }

            @Override
            public Bundle getItem(int i) {
                return contacts.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                if(view == null){
                    // dobbiamo creare la view
                    // si usa il layout inflater
                    // e si carica dentro la view

                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    // NB: only  API >= 23
                    // getSystemServiceName(LAYOUT_INFLATER_SERVICE);

                    view = inflater.inflate(R.layout.contact_item_layout, viewGroup, false);
                }

                // recuperare elemento a questa postizione
                Bundle contact = getItem(i);

                // recupero testi
                TextView fullNameTV = view.findViewById(R.id.fullNameTextView);
                TextView mailTV = view.findViewById(R.id.mailTextView);
                fullNameTV.setText(Contact.getFullName(contact));
                mailTV.setText(Contact.getEmailAndPostal(contact));

                ImageView checkIV = view.findViewById(R.id.checkImageView);

                // controllo se elemento è selezionato
                if(Contact.isSelected(contact)) {
                    checkIV.setVisibility(View.VISIBLE);
                } else {
                    checkIV.setVisibility(View.GONE);
                }

                return view;
            }
        };
    }

    void loadContacts() {

        this.contacts.clear();
        this.contacts.addAll(dbManager.getContacts());

        this.baseAdapter.notifyDataSetChanged();
    }

    ArrayList<Bundle> getContactsToBeDeleted() {

        ArrayList<Bundle> contactsToBeDeleted = new ArrayList<>();

        // questo for sarebbe superfluo perché ho già messo il controllo nel metodo del db manager
        for(Bundle contact : contacts) {
            if(Contact.isSelected(contact)) {
                contactsToBeDeleted.add(contact);
            }
        }

        return contactsToBeDeleted;
    }

    void deleteContacts() {

        dbManager.deleteSelectedContacts(getContactsToBeDeleted());

        Utils.showShortToast(this, R.string.DELETE_SUCCESS_MESSAGE);

        //non facciamo "notify changed" perché in questo caso è cambiato il
        // numero dei contatti e dobbiamo ricaricarli tutti dal db
        loadContacts();
    }

    void selectCurrentContact(Bundle contact) {

        boolean isSelected = Contact.isSelected(contact);

        // contact.putBoolean(DBManager.SELECTED_FIELD_NAME, !isSelected);
        // cicliamo nei contatti perché per qualche motivo, su android, a differenza di iOS
        // contact non è lo stesso della lista di contact (forse param passato per valore
        // invece che per riferimento)
        for(Bundle tmpContact : contacts) {
            if(tmpContact.getInt(DBManager.ID_COL) == contact.getInt(DBManager.ID_COL)) {

                tmpContact.putBoolean(DBManager.SELECTED_FIELD_NAME, !isSelected);
                break;
            }
        }

        baseAdapter.notifyDataSetChanged();
    }

    void selectAllContacts() {

        if (!toggleAll) {
            selectAllBtn.setText(getString(R.string.DESELECT_ALL_BUTTON_LABEL));
            toggleAll = true;

        } else {
            selectAllBtn.setText(getString(R.string.SELECT_ALL_BUTTON_LABEL));
            toggleAll = false;
        }

        for (Bundle contact : contacts) {
            contact.putBoolean(DBManager.SELECTED_FIELD_NAME, toggleAll);
        }

        baseAdapter.notifyDataSetChanged();
    }

    void openDeletionConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.DELETE_CONFIRMATION_MESSAGE);

        builder.setCancelable(false);// utente deve scegliere o si o no
        builder.setPositiveButton(getString(R.string.YES), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                deleteContacts(); // procedi con cancellazione
                // TODO notificare dataset changed
            }
        });

        // la dialog viene chiusa automaticamente passando listener null
        builder.setNegativeButton(getString(R.string.NO), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

