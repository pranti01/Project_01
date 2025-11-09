package com.xyz.moneytrail;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

public class DashBoardFragment extends Fragment {

    // Floating Action Buttons & Animations
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;
    private TextView fab_income_txt;
    private TextView fab_expense_txt;
    private boolean isOpen = false;
    private Animation FadOpen, FadeClose;

    // Dashboard Totals
    private TextView totalincomeResult;
    private TextView totalexpenseResult;
    private TextView totalBalanceResult;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    // RecyclerViews and Adapters
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;
    private FirebaseRecyclerAdapter<Data, IncomeViewHolder> incomeAdapter;
    private FirebaseRecyclerAdapter<Data, ExpenseViewHolder> expenseAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myview = inflater.inflate(R.layout.fragment_dash_board, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser == null) {
            // Handle user not logged in, maybe navigate to login screen
            return myview;
        }
        String uid = mUser.getUid();

        String dbUrl = "https://moneytrail-c5cd8-default-rtdb.asia-southeast1.firebasedatabase.app/";
        mIncomeDatabase = FirebaseDatabase.getInstance(dbUrl).getReference().child("IncomeData").child(uid);
        mExpenseDatabase = FirebaseDatabase.getInstance(dbUrl).getReference().child("ExpenseDatabase").child(uid);

        // Make sure Firebase keeps data synced for offline use
        mIncomeDatabase.keepSynced(true);
        mExpenseDatabase.keepSynced(true);

        // UI Connections
        connectUI(myview);

        // FAB Click Listeners
        setupFabListeners();

        // Listen for total income and expense changes
        calculateTotals();

        // Setup RecyclerViews
        setupRecyclerViews();

        return myview;
    }

    private void connectUI(View myview) {
        // FABs
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn);
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);

        // Animations
        FadOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fade_open);
        FadeClose = AnimationUtils.loadAnimation(getContext(), R.anim.fade_close);

        // Dashboard TextViews
        totalincomeResult = myview.findViewById(R.id.income_set_result);
        totalexpenseResult = myview.findViewById(R.id.expense_set_result);
        totalBalanceResult = myview.findViewById(R.id.balance_set_result);

        // RecyclerViews
        mRecyclerIncome = myview.findViewById(R.id.recycler_income);
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense);
    }

    private void setupFabListeners() {
        fab_main_btn.setOnClickListener(v -> toggleFabMenu());
        fab_income_btn.setOnClickListener(v -> incomeDataInsert());
        fab_expense_btn.setOnClickListener(v -> expenseDataInsert());
    }

    private void setupRecyclerViews() {
        // Income RecyclerView
        LinearLayoutManager layoutManagerIncome = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerIncome.setReverseLayout(true);
        layoutManagerIncome.setStackFromEnd(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        // Expense RecyclerView
        LinearLayoutManager layoutManagerExpense = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManagerExpense.setReverseLayout(true);
        layoutManagerExpense.setStackFromEnd(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);
    }

    private void toggleFabMenu() {
        if (isOpen) {
            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);
            isOpen = false;
        } else {
            fab_income_btn.startAnimation(FadOpen);
            fab_expense_btn.startAnimation(FadOpen);
            fab_income_txt.startAnimation(FadOpen);
            fab_expense_txt.startAnimation(FadOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);
            isOpen = true;
        }
    }

    private void calculateTotals() {
        // Calculate total income
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSum = 0;
                for (DataSnapshot mysnap : dataSnapshot.getChildren()) {
                    Data data = mysnap.getValue(Data.class);
                    if (data != null) {
                        totalSum += data.getAmount();
                    }
                }
                totalincomeResult.setText(String.valueOf(totalSum) + ".00");
                updateBalance();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Calculate total expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSum = 0;
                for (DataSnapshot mysnapshot : dataSnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    if (data != null) {
                        totalSum += data.getAmount();
                    }
                }
                totalexpenseResult.setText(String.valueOf(totalSum) + ".00");
                updateBalance();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateBalance() {
        int income = 0;
        int expense = 0;
        try {
            income = Integer.parseInt(totalincomeResult.getText().toString().replace(".00", ""));
        } catch (NumberFormatException e) { /* ignore */ }
        try {
            expense = Integer.parseInt(totalexpenseResult.getText().toString().replace(".00", ""));
        } catch (NumberFormatException e) { /* ignore */ }

        int balance = income - expense;
        totalBalanceResult.setText(String.valueOf(balance) + ".00");

        if (balance < 2000) {
            totalBalanceResult.setTextColor(getResources().getColor(R.color.red));
        } else {
            totalBalanceResult.setTextColor(getResources().getColor(R.color.greeny));
        }
    }

    public void incomeDataInsert() {
        insertDataDialog(mIncomeDatabase);
    }

    public void expenseDataInsert() {
        insertDataDialog(mExpenseDatabase);
    }

    public void insertDataDialog(final DatabaseReference databaseReference) {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final EditText edtAmount = myview.findViewById(R.id.amount_edt);
        final EditText edtType = myview.findViewById(R.id.type_edt);
        final EditText edtNote = myview.findViewById(R.id.note_edt);
        Button btnSave = myview.findViewById(R.id.btnSave);
        Button btnCancel = myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String type = edtType.getText().toString().trim();
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (TextUtils.isEmpty(type)) {
                edtType.setError("Required field...");
                return;
            }
            if (TextUtils.isEmpty(amountStr)) {
                edtAmount.setError("Required field...");
                return;
            }
            if (TextUtils.isEmpty(note)) {
                edtNote.setError("Required field...");
                return;
            }

            int amount = Integer.parseInt(amountStr);
            String id = databaseReference.push().getKey();
            String mDate = DateFormat.getDateInstance().format(new Date());

            Data data = new Data(amount, type, note, id, mDate);

            if (id != null) {
                databaseReference.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Data added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to add data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // *** START OF CORRECTED CODE ***
    @Override
    public void onStart() {
        super.onStart();

        // --- INCOME ADAPTER SETUP ---
        FirebaseRecyclerOptions<Data> incomeOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();

        incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(incomeOptions) {
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                holder.setIncomeType(model.getType());
                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());
            }

            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
                return new IncomeViewHolder(view);
            }
        };

        mRecyclerIncome.setAdapter(incomeAdapter);
        incomeAdapter.startListening();

        // --- EXPENSE ADAPTER SETUP ---
        FirebaseRecyclerOptions<Data> expenseOptions =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class)
                        .build();

        expenseAdapter = new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(expenseOptions) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                holder.setExpenseType(model.getType());
                holder.setExpenseAmount(model.getAmount());
                holder.setExpenseDate(model.getDate());
            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false);
                return new ExpenseViewHolder(view);
            }
        };

        mRecyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (incomeAdapter != null) {
            incomeAdapter.stopListening();
        }
        if (expenseAdapter != null) {
            expenseAdapter.stopListening();
        }
    }
    // *** END OF CORRECTED CODE ***


    // Static ViewHolder for Income
    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public IncomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setIncomeType(String type) {
            TextView mType = mView.findViewById(R.id.type_Income_ds);
            mType.setText(type);
        }

        public void setIncomeAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_Income_ds);
            mAmount.setText(String.valueOf(amount));
        }

        public void setIncomeDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_Income_ds);
            mDate.setText(date);
        }
    }

    // Static ViewHolder for Expense
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setExpenseType(String type) {
            TextView mType = mView.findViewById(R.id.type_Expense_ds);
            mType.setText(type);
        }

        public void setExpenseAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_Expense_ds);
            mAmount.setText(String.valueOf(amount));
        }

        public void setExpenseDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_Expense_ds);
            mDate.setText(date);
        }
    }
}

