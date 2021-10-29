package com.incognito.soundprofileconverter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.incognito.soundprofileconverter.DBHandler;
import com.incognito.soundprofileconverter.R;
import com.incognito.soundprofileconverter.WhitelistedContacts;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<WhitelistedContacts> whitelistedContactsList;

    public RecyclerViewAdapter(Context context, List<WhitelistedContacts> whitelistedContactsList) {
        this.context = context;
        this.whitelistedContactsList = whitelistedContactsList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        WhitelistedContacts contact = whitelistedContactsList.get(position);

        holder.contactName.setText(contact.getContactName());
        holder.contactNumber.setText(contact.getContactPhoneNumber());
        holder.deleteBtn.setOnClickListener(arg0 -> {
            removeContact(contact, position);
        });
    }

    @Override
    public int getItemCount() {
        return whitelistedContactsList.size();
    }

    public void addContact(WhitelistedContacts contact) {
        final DBHandler dbHandler = new DBHandler(context);
        if (dbHandler.addNewContact(contact) > 0) {
            whitelistedContactsList.add(contact);
            notifyItemInserted(whitelistedContactsList.size() - 1);
        } else {
            Toast.makeText(context,"Contact with this number is already whitelisted!",Toast.LENGTH_SHORT).show();
        }
    }

    public void removeContact(WhitelistedContacts contact, int position) {
        final DBHandler dbHandler = new DBHandler(context);
        dbHandler.removeContact(contact);
        whitelistedContactsList.remove(contact);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView contactName;
        public TextView contactNumber;
        public Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            deleteBtn = itemView.findViewById(R.id.button);
        }
    }
}
