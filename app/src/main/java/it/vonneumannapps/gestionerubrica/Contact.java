package it.vonneumannapps.gestionerubrica;

import android.os.Bundle;

public class Contact {

    public static String getFullName(Bundle contact) {
        String fullName = contact.getString(DBManager.NAME_COL)
                + " " + contact.getString(DBManager.SURNAME_COL);

        return fullName;
    }

    public static String getEmailAndPostal(Bundle contact) {
        String emailAndPostalAddressesConcat
                = contact.getString(DBManager.EMAIL_COL)
                + " - " + contact.getString(DBManager.ADDRESS_COL);

        return emailAndPostalAddressesConcat;
    }

    public static boolean isSelected(Bundle contact) {
        return contact.getBoolean(DBManager.SELECTED_FIELD_NAME);
    }
}
