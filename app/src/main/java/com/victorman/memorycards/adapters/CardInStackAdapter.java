package com.victorman.memorycards.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.R;
import com.victorman.memorycards.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardInStackAdapter extends RecyclerView.Adapter<CardInStackAdapter.ViewHolder> {
    private Integer[] cardColors;
    private List<Card> cards;

    public CardInStackAdapter(List<Card> cards, String[] cardColors) {
        this.cards = cards;
        this.cardColors = Utils.getIntColors(cardColors);
    }

    public CardInStackAdapter(String[] cardColors) {
        this.cards = new ArrayList<>();
        this.cardColors = Utils.getIntColors(cardColors);
    }


    public List<Card> getCards() {
        return cards;
    }


    public void setCards(List<Card> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_card_in_stack, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Card currentCard = cards.get(position);

        holder.constraintLayoutCardBackground.setBackgroundColor(Utils.getRandomElement(cardColors));
        holder.textViewTerm.setText(currentCard.getTerm());
        holder.editTextDefinition.setText("");
        holder.buttonKeyboardInput.setVisibility(View.VISIBLE);
        holder.editTextDefinition.setFocusable(false);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.buttonKeyboardInput.setVisibility(View.INVISIBLE);
                holder.editTextDefinition.setText(currentCard.getDefinition());
                holder.editTextDefinition.setFocusable(false);
            }
        });

        holder.editTextDefinition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().equals(currentCard.getDefinition())) {
                    holder.editTextDefinition.setFocusable(false);
                    ((InputMethodManager) Objects.requireNonNull(holder.itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)))
                            .hideSoftInputFromWindow(holder.editTextDefinition.getWindowToken(), 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout constraintLayoutCardBackground;
        public TextView textViewTerm;
        public EditText editTextDefinition;
        public ImageButton buttonKeyboardInput;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            constraintLayoutCardBackground = itemView.findViewById(R.id.itemCardInStackConstraintLayoutBackground);
            textViewTerm = itemView.findViewById(R.id.itemCardInStackTextViewTerm);
            editTextDefinition = itemView.findViewById(R.id.itemCardInStackEditTextDefinition);
            buttonKeyboardInput = itemView.findViewById(R.id.itemCardInStackButtonKeyboardInput);

            buttonKeyboardInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextDefinition.setFocusable(true);
                    editTextDefinition.requestFocusFromTouch();
                    buttonKeyboardInput.setVisibility(View.INVISIBLE);
                    ((InputMethodManager) Objects.requireNonNull(itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)))
                            .showSoftInput(editTextDefinition, 0);
                }
            });
        }
    }
}
