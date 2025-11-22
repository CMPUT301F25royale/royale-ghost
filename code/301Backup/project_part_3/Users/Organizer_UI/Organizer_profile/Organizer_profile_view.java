package com.example.project_part_3.Users.Organizer_UI.Organizer_profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.project_part_3.Database_functions.Database;
import com.example.project_part_3.Users.Organizer;
import com.example.project_part_3.Users.User;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_part_3.R;

import java.util.concurrent.atomic.AtomicReference;

public class Organizer_profile_view extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Database db = new Database(FirebaseFirestore.getInstance());

        //get password
        SharedPreferences prefs = requireContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");

        //reset password here
        Button passwordReset = view.findViewById(R.id.Pass_Reset);
        passwordReset.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                //firebase change
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getPassword().equals(old)){
                        user.setPassword(_new);
                        db.setUser(user);
                    }
                });
                //change for shared prefs
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", _new);
                editor.apply();

            });
        });

        //reset Name
        Button nameRest = view.findViewById(R.id.name_change);
        nameRest.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                //firebase change
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getName().equals(old)){
                        user.setName(_new);
                        db.setUser(user);
                    }
                });
            });
        });

        //reset phone
        Button phone = view.findViewById(R.id.number_Change);
        phone.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                //firebase change
                db.fetchUser(username).addOnSuccessListener(user -> {
                    if(user.getPhone().equals(old)){
                        user.setPhone(_new);
                        db.setUser(user);
                    }
                });
            });
        });

        //reset email
        Button emailReset = view.findViewById(R.id.change_email);
        emailReset.setOnClickListener(v -> {
            InputDialog((old,_new) -> {
                db.fetchUser(username).addOnSuccessListener(user->{
                    String name = user.getName();
                    String password = user.getPassword();
                    String number = user.getPhone();
                    db.deleteUser(username);
                    Organizer new_user = new Organizer(name,password,_new,number);
                    db.addUser(new_user);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", _new);
                    editor.apply();
                });
            });
        });

        //delete user
        Button delete = view.findViewById(R.id.pass_Reset);
        delete.setOnClickListener(v -> {
            db.deleteUser(username).addOnSuccessListener(user -> {});
        });

    }

    private void InputDialog(InputDialogCallback callback){
        LayoutInflater inflator = LayoutInflater.from(requireContext());
        View dialogView = inflator.inflate(R.layout.profile_popup,null);

        EditText old = dialogView.findViewById(R.id.oldpass);
        EditText _new = dialogView.findViewById(R.id.newpass);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Enter Old value and New Value")
                .setView(dialogView) // attach your custom layout
                .setPositiveButton("OK", (d, which) -> {
                    String oldtext = old.getText().toString().trim();
                    String newtext = _new.getText().toString().trim();
                    callback.onInputSubmitted(oldtext,newtext);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }
}

