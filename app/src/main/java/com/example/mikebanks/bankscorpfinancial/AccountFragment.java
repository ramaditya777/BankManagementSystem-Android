package com.example.mikebanks.bankscorpfinancial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mikebanks.bankscorpfinancial.Adapters.PaymentAdapter;
import com.example.mikebanks.bankscorpfinancial.Adapters.TransferAdapter;
import com.example.mikebanks.bankscorpfinancial.Model.Profile;
import com.example.mikebanks.bankscorpfinancial.Model.Transaction;
import com.example.mikebanks.bankscorpfinancial.Model.db.ApplicationDB;
import com.google.gson.Gson;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AccountFragment extends Fragment {

    //TODO: Have the up button return to the previous activity, rather than open the drawer menu
    private TextView txtAccountName;
    private TextView txtAccountNo;
    private TextView txtAccountBalance;
    private TextView txtTransactionMsg;

    private Button btnPayments;
    private Button btnTransfers;

    private TextView txtNoTransfersMsg;
    private TextView txtNoPaymentsMsg;

    private ListView lstPayments;
    private ListView lstTransfers;

    private Button btnAddDeposit;
    private EditText edtDepositAmount;
    private Button btnMakeDeposit;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.btn_payments:
                    displayPayments();
                    break;
                case R.id.btn_transfers:
                    displayTransfers();
                    break;
                case R.id.btn_add_deposit:
                    showDepositViews();
                    break;
                case R.id.btn_make_deposit:
                    makeDeposit();
                    break;
            }
        }
    };

    private Gson gson;
    private String json;
    private SharedPreferences userPreferences;
    private Profile userProfile;

    //KEEP ME
    private int selectedAccountIndex;
    private boolean containsTransfers;
    private boolean containsPayments;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBackButton();
        //TODO: Change the title to be: selectedAccount.toString(), OR, Keep as Accounts
        //TODO: Try and get more screen space for transactions
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        txtAccountName = rootView.findViewById(R.id.txt_account_name);
        txtAccountNo = rootView.findViewById(R.id.txt_account_no);
        txtAccountBalance = rootView.findViewById(R.id.txt_to_acc);
        txtTransactionMsg = rootView.findViewById(R.id.txt_transactions_msg);

        btnPayments = rootView.findViewById(R.id.btn_payments);
        btnTransfers = rootView.findViewById(R.id.btn_transfers);

        txtNoPaymentsMsg = rootView.findViewById(R.id.txt_no_payments_msg);
        txtNoTransfersMsg = rootView.findViewById(R.id.txt_no_transfers_msg);

        lstPayments = rootView.findViewById(R.id.lst_payments);
        lstTransfers = rootView.findViewById(R.id.lst_transfers);

        btnAddDeposit = rootView.findViewById(R.id.btn_add_deposit);
        edtDepositAmount = rootView.findViewById(R.id.edt_deposit_amount);
        btnMakeDeposit = rootView.findViewById(R.id.btn_make_deposit);

        setValues();
        return rootView;
    }

    public void showBackButton() {
        if (getActivity() instanceof DrawerActivity) {
            ((DrawerActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * method used to setup the values for the views and fields
     */
    private void setValues() {

        userPreferences = getActivity().getSharedPreferences("LastProfileUsed", MODE_PRIVATE);
        gson = new Gson();
        json = userPreferences.getString("LastProfileUsed", "");
        userProfile = gson.fromJson(json, Profile.class);

        txtNoTransfersMsg.setVisibility(GONE);
        txtNoPaymentsMsg.setVisibility(GONE);
        edtDepositAmount.setVisibility(GONE);
        btnMakeDeposit.setVisibility(GONE);

        btnPayments.setOnClickListener(clickListener);
        btnTransfers.setOnClickListener(clickListener);
        btnAddDeposit.setOnClickListener(clickListener);
        btnMakeDeposit.setOnClickListener(clickListener);

        getTransactionTypes();
        checkTransactionHistory();
        setupAdapters();

        txtAccountName.setText("Name:" + " " + userProfile.getAccounts().get(selectedAccountIndex).getAccountName());
        txtAccountNo.setText("No:" + " " + userProfile.getAccounts().get(selectedAccountIndex).getAccountNo());
        txtAccountBalance.setText("Balance: $" + String.format("%.2f",userProfile.getAccounts().get(selectedAccountIndex).getAccountBalance()));
    }

    /**
     * method used to get the transaction types
     */
    private void getTransactionTypes() {
        for (int i = 0; i < userProfile.getAccounts().get(selectedAccountIndex).getTransactions().size(); i++) {
            if (userProfile.getAccounts().get(selectedAccountIndex).getTransactions().get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.TRANSFER) {
                containsTransfers = true;
            } else {
                containsPayments = true;
            }
        }
    }

    /**
     * method used to check the transaction history of the current account
     */
    private void checkTransactionHistory() {
        if (userProfile.getAccounts().get(selectedAccountIndex).getTransactions().size() != 0) {

            txtTransactionMsg.setVisibility(GONE);

            btnPayments.setVisibility(VISIBLE);
            btnTransfers.setVisibility(VISIBLE);

            if (containsPayments) {
                btnPayments.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                lstTransfers.setVisibility(GONE);
                lstPayments.setVisibility(VISIBLE);
            } else {
                btnTransfers.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                lstTransfers.setVisibility(VISIBLE);
                lstPayments.setVisibility(GONE);
            }

        } else {

            txtTransactionMsg.setVisibility(VISIBLE);

            btnPayments.setVisibility(GONE);
            btnTransfers.setVisibility(GONE);
            txtNoTransfersMsg.setVisibility(GONE);
            txtNoPaymentsMsg.setVisibility(GONE);
            lstPayments.setVisibility(GONE);
            lstTransfers.setVisibility(GONE);
        }
    }

    //TODO: Have all transactions under the same adapter and ListView? Tricky, do some research - dynamically add TextViews? - have filtering options to only see them of one type?
    /**
     * method used to setup the adapters
     */
    private void setupAdapters() {

        ArrayList<Transaction> transactions = userProfile.getAccounts().get(selectedAccountIndex).getTransactions();
        ArrayList<Transaction> transfers = new ArrayList<>();
        ArrayList<Transaction> payments = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.TRANSFER) {
                transfers.add(transactions.get(i));
            } else {
                payments.add(transactions.get(i));
            }
        }

        TransferAdapter transferAdapter = new TransferAdapter(getActivity(), R.layout.lst_transfers, transfers);
        lstTransfers.setAdapter(transferAdapter);

        PaymentAdapter paymentAdapter = new PaymentAdapter(getActivity(), R.layout.lst_payments, payments);
        lstPayments.setAdapter(paymentAdapter);
    }

    /**
     * method used to display the payments
     */
    private void displayPayments() {
        lstTransfers.setVisibility(GONE);
        txtNoTransfersMsg.setVisibility(GONE);
        btnPayments.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        btnTransfers.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        if (containsPayments) {
            txtNoPaymentsMsg.setVisibility(GONE);
            lstPayments.setVisibility(VISIBLE);
        } else {
            txtNoPaymentsMsg.setVisibility(VISIBLE);
            lstPayments.setVisibility(GONE);
        }
    }

    /**
     * method used to display the transfers
     */
    private void displayTransfers() {
        lstPayments.setVisibility(GONE);
        txtNoPaymentsMsg.setVisibility(GONE);
        btnPayments.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        btnTransfers.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

        if (containsTransfers) {
            txtNoTransfersMsg.setVisibility(GONE);
            lstTransfers.setVisibility(VISIBLE);
        } else {
            txtNoTransfersMsg.setVisibility(VISIBLE);
            lstTransfers.setVisibility(GONE);
        }

    }

    /**
     * method used to display the deposit views
     */
    private void showDepositViews() {
        btnAddDeposit.setVisibility(GONE);
        btnMakeDeposit.setVisibility(VISIBLE);
        edtDepositAmount.setVisibility(VISIBLE);
    }

    //TODO: Make separate fragment for deposits
    /**
     * method used to make a deposit
     */
    private void makeDeposit() {

        double depositAmount = Double.parseDouble(edtDepositAmount.getText().toString());

        if (edtDepositAmount.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Please enter an amount to deposit", Toast.LENGTH_SHORT).show();
        } else {
            if (depositAmount < 10) {
                Toast.makeText(getActivity(), R.string.balance_less_than_ten, Toast.LENGTH_SHORT).show();
            } else {

                userProfile.getAccounts().get(selectedAccountIndex).setAccountBalance(userProfile.getAccounts().get(selectedAccountIndex).getAccountBalance() + depositAmount);

                SharedPreferences.Editor prefsEditor = userPreferences.edit();
                gson = new Gson();
                json = gson.toJson(userProfile);
                prefsEditor.putString("LastProfileUsed", json).commit();

                ApplicationDB applicationDb = new ApplicationDB(getActivity().getApplicationContext());
                applicationDb.overwriteAccount(userProfile, userProfile.getAccounts().get(selectedAccountIndex));

                txtAccountBalance.setText("Balance: $" + String.format("%.2f",userProfile.getAccounts().get(selectedAccountIndex).getAccountBalance()));

                Toast.makeText(getActivity(), "Deposit of $" + String.format("%.2f",depositAmount) + " " + "made successfully", Toast.LENGTH_SHORT).show();

                btnAddDeposit.setVisibility(VISIBLE);
                btnMakeDeposit.setVisibility(GONE);

                edtDepositAmount.getText().clear();
                edtDepositAmount.setVisibility(GONE);
            }
        }


    }

}
