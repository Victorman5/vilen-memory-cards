package com.victorman.memorycards.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.R;
import com.victorman.memorycards.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardInListAdapter extends RecyclerView.Adapter<CardInListAdapter.ViewHolder> implements Filterable {
    private OnItemClickListener onItemClickListener;
    private Integer[] cardColors;

    private List<Item> items;
    private List<Item> itemsAll;


    public CardInListAdapter(String[] cardColors) {
        this.items = new ArrayList<>();
        this.itemsAll = new ArrayList<>();
        this.cardColors = Utils.getIntColors(cardColors);
    }

    public CardInListAdapter(List<Card> cards, String[] cardColors) {
        this.cardColors = Utils.getIntColors(cardColors);

        this.items = Item.itemsFrom(cards, this.cardColors);
        this.itemsAll = new ArrayList<>(items);
    }


    public void insertCard(int position, Card card) {
        Item insertedItem = Item.itemFrom(card, cardColors);
        items.add(position, insertedItem);
        itemsAll.add(position, insertedItem);
        notifyItemInserted(position);
    }


    public void insertItem(int position, Item item) {
        items.add(position, item);
        itemsAll.add(position, item);
        notifyItemInserted(position);
    }


    public void deleteCard(int position) {
        itemsAll.remove(items.get(position));
        items.remove(position);
        notifyItemRemoved(position);
    }


    public void editCard(int position, Card card) {
        items.get(position).card = card;
        itemsAll.get(itemsAll.indexOf(items.get(position))).card = card;
        notifyItemChanged(position);
    }


    public void swapItems(int pos1, int pos2) {
        Collections.swap(items, pos1, pos2);
        Collections.swap(itemsAll, itemsAll.indexOf(items.get(pos1)), itemsAll.indexOf(items.get(pos2)));
        notifyItemMoved(pos1, pos2);
    }


    public List<Item> getItems() {
        return items;
    }


    public List<Card> getCardsAll() {
        List<Card> cards = new ArrayList<>(itemsAll.size());
        for (Item item : itemsAll) {
            cards.add(item.card);
        }
        return cards;
    }


    public List<Item> getItemsAll() {
        return itemsAll;
    }


    public void setCards(List<Card> cards) {
        this.items = Item.itemsFrom(cards, this.cardColors);
        this.itemsAll = new ArrayList<>(items);
        notifyDataSetChanged();
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_card_in_list, parent, false),
                onItemClickListener
        );
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item currentItem = items.get(position);

        holder.constraintLayoutCardBackground.setBackgroundColor(currentItem.intColor);
        holder.textViewTerm.setText(currentItem.card.getTerm());
        holder.textViewDefinition.setText(currentItem.card.getDefinition());
        holder.textViewScore.setText(currentItem.card.getPositiveCount()+ "/" + currentItem.card.getNegativeCount());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout constraintLayoutCardBackground;
        public TextView textViewTerm;
        public TextView textViewDefinition;
        public TextView textViewScore;

        public ViewHolder(@NonNull final View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);

            constraintLayoutCardBackground = itemView.findViewById(R.id.listConstraintLayoutCardBackground);
            textViewTerm = itemView.findViewById(R.id.itemCardInListTextViewTerm);
            textViewDefinition = itemView.findViewById(R.id.itemCardInListTextViewDefinition);
            textViewScore = itemView.findViewById(R.id.itemCardInListTextViewScore);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }


    public static class Item {
        public Card card;
        public int intColor;

        private Item(Card card, int intColor) {
            this.card = card;
            this.intColor = intColor;
        }

        public static List<Item> itemsFrom(List<Card> cards, Integer[] colors) {
            List<Item> items = new ArrayList<>(cards.size());

            for (Card card : cards) {
                items.add(new Item(card, Utils.getRandomElement(colors)));
            }

            return items;
        }

        public static Item itemFrom(Card card, Integer[] colors) {
            return new Item(card, Utils.getRandomElement(colors));
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Item> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(itemsAll);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Item item : itemsAll) {
                    if (item.card.getTerm().toLowerCase().contains(filterPattern)
                        || item.card.getDefinition().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items.clear();
            items.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
