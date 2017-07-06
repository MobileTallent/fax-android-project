package co.faxapp.dialogs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;
import com.parse.ui.Countries;
import com.parse.ui.Country;
import com.parse.ui.CountryAdapter;

import java.util.List;

import co.faxapp.App;
import co.faxapp.NetworkService;
import co.faxapp.R;

public class Dialogs {

    public interface CountryListener {
        void updateCountry();
    }

    public static void overLimitMessage(Context context, int mess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(R.string.dialog_overLimit_title)
                .setMessage(mess);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showRateDialog(final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_rate_title);
        builder.setMessage(R.string.dialog_rate_message);
        builder.setPositiveButton(R.string.put_five, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putBoolean("rated", true);
                prefEditor.apply();
//                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=co.faxapp")));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=co.faxapp"));
                if (!isActivityStarted(intent, context)) {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=co.faxapp"));
                    if (!isActivityStarted(intent, context)) {
                        Toast.makeText(context, "Could not open Android market, please check if the market app installed or not. Try again later", Toast.LENGTH_SHORT).show();
                    }
                }
                ParseAnalytics.trackEventInBackground("rateYesClick");
            }
        });
        builder.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParseAnalytics.trackEventInBackground("rateNoClick");
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putLong("lastAsk", System.currentTimeMillis());
                prefEditor.apply();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    private static boolean isActivityStarted(Intent aIntent,Context context) {
        try {
            context.startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }


    public static void showResultDialog(final Activity context, Intent intent, final boolean closeActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_phaxio_result_title);
//        builder.setMessage(intent.getStringExtra(NetworkService.RESULT_STRING));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (closeActivity) {
                    context.finish();
                }
            }
        });
        boolean ok = intent.getBooleanExtra(NetworkService.SEND_RESULT, false);
        if (ok) {
            builder.setIcon(R.drawable.ic_icon_fax_blue);
            builder.setMessage(R.string.dialog_phaxio_result_message);
        } else {
            builder.setIcon(R.drawable.ic_icon_fax_red);
            builder.setMessage(intent.getStringExtra(NetworkService.RESULT_STRING));
        }
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public static void lockDialog(final Activity mainActivity) {
        SpannableString s = new SpannableString(mainActivity.getText(R.string.blocked_message));
        Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder
                .setTitle(R.string.blocked_title)
                .setMessage(s)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainActivity.finish();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
        ((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }


    public static void setUserCountry(Activity activity, int position, final CountryListener listener) {
        final Spinner spinner = new Spinner(activity);
        List<Country> countries = Countries.get().getCountries();
        CountryAdapter countryAdapter = new CountryAdapter(activity, R.layout.item_country_title, countries);
        countryAdapter.setDropDownViewResource(R.layout.item_country_title);
        spinner.setAdapter(countryAdapter);

        if (position>-1) {
            spinner.setSelection(position);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
                .setTitle(R.string.country_select_title)
                .setView(spinner)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Country country = (Country) spinner.getSelectedItem();
                        ParseUser.getCurrentUser().put("country", country.getName());
                        ParseUser.getCurrentUser().saveInBackground();
                        App.setUserCountry(country.getName());
                        if (listener != null) {
                            listener.updateCountry();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public static void confirmEmailDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_verify_title);
        builder.setMessage(R.string.dialog_verify_message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public static void setEmailForFaxesDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.congratulations);
        builder.setMessage(R.string.set_email_for_fax);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public static void congratulationsFaxesDialog(Context context, String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.congratulations);
        builder.setMessage(context.getString(R.string.show_number_dialog) + "\n" + phone + "\n" + context.getString(R.string.show_number_dialog_always));
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }
}
