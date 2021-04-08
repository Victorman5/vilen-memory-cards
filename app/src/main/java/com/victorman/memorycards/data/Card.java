package com.victorman.memorycards.data;

import androidx.annotation.NonNull;


public class Card {
    private long id;
    private String term;
    private String definition;
    private int positiveCount;  // incremented if word guessed
    private int negativeCount;
    private long cardsCollectionId;

    public Card(long cardsCollectionId) {
        this.id = -1;
        this.term = null;
        this.term = null;
        this.positiveCount = 0;
        this.negativeCount = 0;
        this.cardsCollectionId = cardsCollectionId;
    }


    public Card(long id, String term, String definition, int positiveCount, int negativeCount, long cardsCollectionId) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.cardsCollectionId = cardsCollectionId;
    }


    public Card(String term, String definition, long cardsCollectionId) {
        this.id = -1;
        this.term = term;
        this.definition = definition;
        this.cardsCollectionId = cardsCollectionId;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getPositiveCount() {
        return positiveCount;
    }

    public void incrementPositiveCount() {
        this.positiveCount++;
    }

    public int getNegativeCount() {
        return negativeCount;
    }

    public void incrementNegativeCount() {
        this.negativeCount++;
    }

    public long getCardsCollectionId() {
        return cardsCollectionId;
    }

    public void discardCounters() {
        this.positiveCount = 0;
        this.negativeCount = 0;
    }

    final double POSITIVE_WEIGHT = -0.5;
    final double NEGATIVE_WEIGHT = 2;
    final double BIAS = 1;

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    // decides learned the card or not
    // learned if sigmoid >= .5
    // not learned if sigmoid < .5
    public double getWeight() {
        return 1 - sigmoid(POSITIVE_WEIGHT * this.positiveCount + NEGATIVE_WEIGHT * this.negativeCount + BIAS);
    }

    @NonNull
    @Override
    public String toString() {
        return "Card(id=" + this.id + ")(" + this.term + ", " + this.definition + " - " + positiveCount + "/" + negativeCount + ")";
    }


    public static void swapIds(Card card1, Card card2) {
        long card2Id = card2.getId();
        card2.setId(card1.getId());
        card1.setId(card2Id);
    }


    public static class CardContract {
        public static final String ID = "id";
        public static final String TERM = "TERM";
        public static final String DEFINITION = "DEFINITION";
        public static final String POSITIVE_COUNT = "POSITIVE_COUNT";
        public static final String NEGATIVE_COUNT = "NEGATIVE_COUNT";
        public static final String CARDS_COLLECTION_ID = "CARDS_COLLECTION_ID";
    }
}
