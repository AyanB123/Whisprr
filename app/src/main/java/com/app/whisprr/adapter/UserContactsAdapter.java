package com.app.whisprr.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.whisprr.R;
import com.app.whisprr.databinding.CarditemContactrecyclerviewBinding;
import com.app.whisprr.model.Contact;
import com.app.whisprr.model.User;

import java.util.List;
import java.util.Objects;

public class UserContactsAdapter extends RecyclerView.Adapter<UserContactsAdapter.UserViewHolder> {

    List<Contact> contactList;
    private Context context;


    public UserContactsAdapter(List<Contact> contacts, Context context) {

        this.contactList = contacts;
        this.context = context;


    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CarditemContactrecyclerviewBinding carditemContactBinding = CarditemContactrecyclerviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(carditemContactBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(contactList.get(position));

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        CarditemContactrecyclerviewBinding binding;

        UserViewHolder(CarditemContactrecyclerviewBinding cardItemBinding) {
            super(cardItemBinding.getRoot());
            binding = cardItemBinding;
        }

        void setUserData(Contact contact) {
            if (contact.isOnWhisprr) {
                if (binding.imageProfile.getVisibility() != View.VISIBLE) {
                    binding.imageProfile.setVisibility(View.VISIBLE);
                }

               // if (binding.textDisplayName.getVisibility() != View.VISIBLE) {
                  //  binding.textDisplayName.setVisibility(View.VISIBLE);
              //  }
               //
                // binding.imageProfile.setImageBitmap(getUserImage(contact.uphoto));
                binding.textName.setText(contact.uName);
                binding.textNumber.setText(contact.unumber);
                binding.textUsername.setText(contact.uUsername);

                Drawable desiredDrawable = ContextCompat.getDrawable(context, R.drawable.ic_message);
                if (!Objects.equals(binding.iconMessage.getBackground(), desiredDrawable)) {
                    binding.iconMessage.setBackgroundResource(R.drawable.ic_message);
                }

                if (!binding.iconText.getText().equals("Message")) {
                    binding.iconText.setText(R.string.icon_messagetext);
                }
               // binding.textDisplayName.setText(contact.udisplayname);

                Log.d("TAG", "onwhisprr: " + contact.uName + " " + contact.udisplayname + " " + contact.unumber + " " + contact.uUsername + " " + contact.uphoto);
            } else {
                if (binding.imageProfile.getVisibility() != View.GONE) {
                    binding.imageProfile.setVisibility(View.GONE);
                }

              //  if (binding.textDisplayName.getVisibility() != View.GONE) {
               //     binding.textDisplayName.setVisibility(View.GONE);
               // }

                binding.textName.setText(contact.udisplayname);
                binding.textNumber.setText(contact.unumber);
                Drawable desiredDrawable = ContextCompat.getDrawable(context, R.drawable.ic_invite);
                if (!Objects.equals(binding.iconMessage.getBackground(), desiredDrawable)) {
                    binding.iconMessage.setBackgroundResource(R.drawable.ic_invite);
                }


                if (!binding.iconText.getText().equals("Invite")) {
                    binding.iconText.setText(R.string.icon_invitetext);
                }
                Log.d("TAG", "notonwhisprr: " +contact.udisplayname + " " + contact.unumber);
            }


        }

        private Bitmap getUserImage(String encodedImage) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }


    }

}
