package com.xyz.moneytrail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class ExpenseFragment extends Fragment {

    // Firebase database
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase; // Changed from mIncomeDatabase

    // Recyclerview
    private RecyclerView recyclerView;

    // TextView for the total
    private TextView expenseTotalSum; // Changed from incomeTotalSum

    //Edt data item...
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    //Button for update and delete
    private Button btnUpdate;
    private Button btnDelete;

    //Data variable...

    private String type;
    private String note;
    private int amount;

    private String post_key;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false); // Use fragment_expense layout
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        // Add a null check to prevent crashes if the user is not logged in
        if (mUser == null) {
            return myview;
        }
        String uid = mUser.getUid();

        // Point to the correct database URL and node
        String dbUrl = "https://moneytrail-c5cd8-default-rtdb.asia-southeast1.firebasedatabase.app/";
        mExpenseDatabase = FirebaseDatabase.getInstance(dbUrl).getReference().child("ExpenseDatabase").child(uid); // Use ExpenseDatabase

        // Find the correct TextView ID for the expense total
        expenseTotalSum = myview.findViewById(R.id.expense_txt_result);

        // Find the correct RecyclerView ID
        recyclerView = myview.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // This listener calculates the total sum of expenses
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalvalue = 0;
                for (DataSnapshot mysnapshot : dataSnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    // Ensure data is not null to prevent NullPointerException
                    if (data != null) {
                        totalvalue += data.getAmount();
                    }
                }
                // Set the text outside the loop
                String stTotalvalue = String.valueOf(totalvalue);
                expenseTotalSum.setText(stTotalvalue+".00");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors, e.g., show a Toast
            }
        });

        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDatabase, Data.class) // Query the expense database
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter =
                new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, @SuppressLint("RecyclerView") int position, @NonNull Data model) {
                        viewHolder.setAmount(model.getAmount());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setType(model.getType());
                        viewHolder.setNote(model.getNote());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                post_key = getRef(position).getKey();
                                type = model.getType();
                                note = model.getNote();
                                amount = model.getAmount();


                                updateDataItem();
                            }

                        });
                    }

                    @NonNull
                    @Override
                    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.expense_recycler_data, parent, false); // Use expense_recycler_data layout
                        return new MyViewHolder(view);
                    }
                };

        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        // Methods to set data in the ViewHolder's views
        void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_expense); // Use expense TextView ID
            mType.setText(type);
        }

        void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_expense); // Use expense TextView ID
            mNote.setText(note);
        }

        void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_expense); // Use expense TextView ID
            mDate.setText(date);
        }

        void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_expense); // Use expense TextView ID
            String stamount = String.valueOf(amount);
            mAmount.setText(stamount);
        }
    }

    private void updateDataItem() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.update_data_item, null);
        mydialog.setView(myview);

        edtAmount = myview.findViewById(R.id.amount_edt);
        edtType = myview.findViewById(R.id.type_edt);
        edtNote = myview.findViewById(R.id.note_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());


        btnUpdate = myview.findViewById(R.id.btn_upd_Update);
        btnDelete = myview.findViewById(R.id.btnuPD_Delete);

        final AlertDialog dialog = mydialog.create();
        dialog.show();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String stamount = String.valueOf(amount);
                stamount = edtAmount.getText().toString().trim();

                int intAmount = Integer.parseInt(stamount);

                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(intAmount, type, note, post_key, mDate);
                mExpenseDatabase.child(post_key).setValue(data);
                dialog.dismiss();





            }


        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpenseDatabase.child(post_key).removeValue();

                dialog.dismiss();

            }
        });

    }
}
