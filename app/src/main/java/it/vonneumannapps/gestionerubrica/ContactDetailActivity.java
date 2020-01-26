package it.vonneumannapps.gestionerubrica;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ContactDetailActivity extends AppCompatActivity {

    public static final int ADD_OR_EDIT_CODE = 1000; // numero arbitrario
    public static final int TAKE_PICTURE_CODE = 200;
    public static final int CAMERA_PERMISSION_CODE = 300;

    DBManager dbManager;
    Bundle contact;

    ImageView profilePictureIV;

    EditText nameET;
    EditText surnameET;
    EditText addressET;
    EditText phoneET;
    EditText emailET;

    String[] actions;
    BaseAdapter actionsAdapter;

    // carico i valori dell'account nell'EditText
    void loadContactData() {

        if (!contact.isEmpty()) {

            this.nameET.setText(contact.getString(DBManager.NAME_COL));
            this.surnameET.setText(contact.getString(DBManager.SURNAME_COL));
            this.addressET.setText(contact.getString(DBManager.ADDRESS_COL));
            this.phoneET.setText(contact.getString(DBManager.PHONE_COL));
            this.emailET.setText(contact.getString(DBManager.EMAIL_COL));

            byte[] imageBytes = contact.getByteArray(DBManager.PROFILE_PIC_COL);
            Bitmap bitmap = Utils.convertByteArrayToBitmap(imageBytes);
            profilePictureIV.setImageBitmap(bitmap);
        }
    }

    void takePicture() {

        // SCATTA FOTO

        // DOUBT: difference btw this and ContactDetailActivity.this
        Uri outputFileUri = Utils.getOutputFileUri(this);

        //crete intent to take a picture and return control
        // to the calling application
        // apre la fotocamera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // create a file to save the  image
        // extra con il percorso del file
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        //start the image capture intent
        startActivityForResult(intent, TAKE_PICTURE_CODE);

        /*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Do something for Oreo 26 and above versions
        } else{
            // do something for phones running an SDK before lollipop
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        this.actions = new String[] {
                getString(R.string.CAMERA_ACTION_TEXT),
                getString(R.string.GALLERY_ACTION_TEXT)};
        initActionsAdapter();

        // inizializzo campi EditText del contatto
        nameET = findViewById(R.id.nameEditText);
        surnameET = findViewById(R.id.surnameEditText);
        addressET = findViewById(R.id.addressEditText);
        phoneET = findViewById(R.id.phoneEditText);
        emailET = findViewById(R.id.emailEditText);

        profilePictureIV = findViewById(R.id.profilePicImageView);

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
            loadContactData();
        }

        this.dbManager = new DBManager(this);

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

        ImageView addProfilePicBtnIV = findViewById(R.id.addProfilePicButton);
        addProfilePicBtnIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActionSelectionDialog();//scelta se fotocamera o galleria
            }
        });
    }

    void saveContactData() {

        // controllare se id esiste già, se no si crea.
        // Usere db manager update o insert new contact
        Boolean isNewAccount = contact.isEmpty();

        contact.putString(DBManager.NAME_COL, nameET.getText().toString());
        contact.putString(DBManager.SURNAME_COL, surnameET.getText().toString());
        contact.putString(DBManager.ADDRESS_COL, addressET.getText().toString());
        contact.putString(DBManager.PHONE_COL, phoneET.getText().toString());
        contact.putString(DBManager.EMAIL_COL, emailET.getText().toString());

        BitmapDrawable bitmapDrawable
                = (BitmapDrawable) profilePictureIV.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        contact.putByteArray(DBManager.PROFILE_PIC_COL, Utils.convertBitmapToByteArray(bitmap));

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

    void openActionSelectionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.ACTION_SELECTION_TEXT));

        builder.setAdapter(actionsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final int FOTOCAMERA = 0;
                //final int GALLERIA = 1;

                if(i==FOTOCAMERA) {
                    //TODO
                    checkForCameraPermission();// controlla permessi e poi apre fotocamera
                }
                else { //GALLERIA
                    //TODO
                }
            }
        });

        builder.setCancelable(true);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void checkForCameraPermission() {

        int myCameraPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA);
        if(myCameraPermission != PackageManager.PERMISSION_GRANTED){

            String[] permissionsToRequest = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, permissionsToRequest,
                    CAMERA_PERMISSION_CODE);
        }

        takePicture();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_PERMISSION_CODE: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
                else {
                    //TODO messaggio all'utente: servono i permessi
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {
            case TAKE_PICTURE_CODE : {
                if(resultCode == RESULT_OK) {
                    String pathName = Utils.getMainDirectory(this).getPath()
                            + Utils.TMP_IMG_FILENAME;
                    Bitmap bitmap;

                    // TODO Autoclose anche per i file?
                    File tempFile = new File(pathName);
                    bitmap = BitmapFactory.decodeFile(tempFile.getPath());

                    profilePictureIV.setImageBitmap(bitmap);

                    // TODO saveLocation()

                    // int jpegQuality = 5;
                    /*
                    //File tempFile2 = new File(pathName + "");
                    try(OutputStream fOut = new FileOutputStream(tempFile)) {

                        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, fOut);
                    }*/

                    /*
                    final FileOutputStream ostream;

                    try {

                        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath());

                        ostream = new FileOutputStream(tempFile);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, ostream);

                        locationPictureIV.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                    }
                    */
                }

                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void initActionsAdapter() {
        actionsAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return actions.length;
            }

            @Override
            public String getItem(int i) {
                return actions[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                if(view == null) {
                    LayoutInflater layoutInflater
                            = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    view = layoutInflater.inflate(R.layout.action_item_layout,
                            viewGroup, false);
                }

                String action = getItem(i);
                TextView titleTV = view.findViewById(R.id.titleTextView);
                titleTV.setText(action);

                return view;
            }
        };
    }
}
