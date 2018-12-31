package com.budziaszek.tabmate.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.budziaszek.tabmate.R;
import com.budziaszek.tabmate.data.Transaction;
import com.budziaszek.tabmate.view.listener.TransactionClickListener;

import java.util.List;



public class TransactionsItemsAdapter extends RecyclerView.Adapter<TransactionsItemsAdapter.MyViewHolder> {

    private TransactionClickListener transactionsClickListener;
    private List<Transaction> transactionsList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView transactionAmount;
        private TextView transactionName;
        private TextView transactionDescription;
        private TextView transactionDate;
        private RelativeLayout transactionItemLayout;

        private MyViewHolder(View view) {
            super(view);

            transactionItemLayout = view.findViewById(R.id.transaction_item_layout);
            transactionItemLayout.setOnClickListener(view12 -> transactionsClickListener.onClick(getAdapterPosition()));

            transactionAmount = view.findViewById(R.id.transaction_amount);
            transactionName = view.findViewById(R.id.transaction_title);
            transactionDescription = view.findViewById(R.id.transaction_description);
            transactionDate = view.findViewById(R.id.transaction_date);
        }
    }


    public TransactionsItemsAdapter(List<Transaction> groupsList, Context context, TransactionClickListener transactionsClickListener) {
        //this.transactionItemLayouts = new ArrayList<>();
        this.transactionsList = groupsList;
        this.transactionsClickListener = transactionsClickListener;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Transaction transaction = transactionsList.get(position);

        String amount = transaction.getAmount().toString();
        holder.transactionAmount.setText(amount);
        holder.transactionName.setText(transaction.getTitle());
        holder.transactionDate.setText(transaction.getDateString());

        String description = transaction.getDescription();
        if (!description.equals("")) {
            holder.transactionDescription.setText(description);
            holder.transactionDescription.setVisibility(View.VISIBLE);
        } else {
            holder.transactionDescription.setVisibility(View.GONE);
        }

        if(transaction.getAmount()<0)
            holder.transactionItemLayout.setBackground(context.getResources().getDrawable(R.drawable.ripple_effect_todo, context.getTheme()));
        else
            holder.transactionItemLayout.setBackground(context.getResources().getDrawable(R.drawable.ripple_effect_done, context.getTheme()));
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public void update(List<Transaction> data) {
        transactionsList = data;
    }

}