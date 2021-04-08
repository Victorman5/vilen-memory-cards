package com.victorman.memorycards.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.R;

public class EditCardDialog extends DialogFragment {
    private EditCardDialogListener editCardDialogListener;

    private EditText editTextTerm;
    private EditText editTextDefinition;

    private int cardPosition; // if equals -1 -> forCreateCard
    private Card card;

    public EditCardDialog(int cardPosition, Card card) {
        this.cardPosition = cardPosition;
        this.card = card;
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_card, null);

        builder.setView(view)
                .setTitle("Edit card")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                    }
                });

        editTextTerm = view.findViewById(R.id.dialogEditTextCardTerm);
        editTextDefinition = view.findViewById(R.id.dialogEditTextCardDefinition);

        editTextDefinition.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                            && event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    save();
                    return true;
                }

                return false;
            }
        });

        if (cardPosition != -1) {
            editTextTerm.setText(card.getTerm());
            editTextDefinition.setText(card.getDefinition());
        }

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            editCardDialogListener = (EditCardDialogListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() +
                    "must implement EditCardDialogListener!");
        }

    }

    private void save() {
        String term = editTextTerm.getText().toString().trim();
        String definition = editTextDefinition.getText().toString().trim();

        if (!(term.equals("") || definition.equals(""))) {
            card.setTerm(term);
            card.setDefinition(definition);

            if (cardPosition == -1) {
                editCardDialogListener.onCreateCard(card);
            } else {
                editCardDialogListener.onEditCard(cardPosition, card);
            }
        }

        this.dismiss();
    }

    public interface EditCardDialogListener {
        void onEditCard(int position, Card card);
        void onCreateCard(Card card);
    }

}
