package com.victorman.memorycards.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.victorman.memorycards.data.CardsCollection;
import com.victorman.memorycards.R;

public class EditCardsCollectionDialog extends DialogFragment {
    private EditCardsCollectionDialogListener editCardsCollectionDialogListener;

    private EditText editTextName;

    private CardsCollection cardsCollection;

    public EditCardsCollectionDialog(CardsCollection cardsCollection) {
        this.cardsCollection = cardsCollection;
    }


    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) this.getDialog();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        positiveButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
        negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_cards_collection, null);

        builder.setView(view)
                .setTitle("Edit collection")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cardsCollection.setName(editTextName.getText().toString().trim());

                        editCardsCollectionDialogListener.onEditCardsCollection(cardsCollection);
                    }
                });

        editTextName = view.findViewById(R.id.dialogEditTextCardsCollectionName);

        editTextName.setText(cardsCollection.getName());

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            editCardsCollectionDialogListener = (EditCardsCollectionDialogListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() +
                    "must implement EditCardsCollectionDialogListener!");
        }
    }

    public interface EditCardsCollectionDialogListener {
        void onEditCardsCollection(CardsCollection cardsCollection);
    }
}
