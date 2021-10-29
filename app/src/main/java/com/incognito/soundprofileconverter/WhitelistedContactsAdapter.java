package com.incognito.soundprofileconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WhitelistedContactsAdapter extends RecyclerView.Adapter<WhitelistedContactsAdapter.ViewHolder> {

    private List<WhitelistedContacts> mContacts;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        WhitelistedContacts contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView contactName = holder.contactName;
        contactName.setText(contact.getContactName());
        TextView contactNumber = holder.contactNumber;
        contactNumber.setText(contact.getContactPhoneNumber());
//        Button button = holder.messageButton;
//        button.setText(contact.isOnline() ? "Message" : "Offline");
//        button.setEnabled(contact.isOnline());
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public WhitelistedContactsAdapter(List<WhitelistedContacts> contacts) {
        mContacts = contacts;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;

        public ViewHolder(View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
        }
    }
}
