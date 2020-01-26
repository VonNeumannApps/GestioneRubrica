package it.vonneumannapps.gestionerubrica;

import android.content.Context;
import android.widget.Toast;

public class Utils {

    public static void showShortToast(Context context, int textStringId) {

        String stringToShow = context.getString(textStringId);
        Toast.makeText(context, stringToShow, Toast.LENGTH_SHORT).show();
    }
}
