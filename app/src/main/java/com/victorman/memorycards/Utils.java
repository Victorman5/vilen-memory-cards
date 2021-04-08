package com.victorman.memorycards;


import android.graphics.Color;

import com.victorman.memorycards.data.Card;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Utils
{
    public static <T> T getRandomElement(T[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }


    public static Integer[] getIntColors(String[] strColors) {
        Integer[] intColors = new Integer[strColors.length];

        for (int i = 0; i < strColors.length; i++) {
            intColors[i] = Color.parseColor(strColors[i]);
        }

        return intColors;
    }


    public static int binarySearchIndexOfMinGreaterElement(double[] list, double value) {
        int left = -1;
        int right = list.length;

        int middle;
        while (left < right - 1) {
            middle = (right + left) / 2;

            if (list[middle] < value) {
                left = middle;
            } else {
                right = middle;
            }
        }

        return right;
    }


    public static List<Card> getSmartCardsCompilation(List<Card> cards, int count) {
        double[] cardsWeightsSums = new double[cards.size()];
        cardsWeightsSums[0] = cards.get(0).getWeight();
        for (int i = 1; i < cards.size(); i++) {
            cardsWeightsSums[i] = cardsWeightsSums[i - 1] + cards.get(i).getWeight();
        }


        ArrayList<Card> cardsSmartCompilation = new ArrayList<>(count);


        while (count > 0) {
            cardsSmartCompilation.add(
                    cards.get(
                        binarySearchIndexOfMinGreaterElement(
                                cardsWeightsSums,
                                ThreadLocalRandom.current().nextDouble(cardsWeightsSums[cardsWeightsSums.length - 1]))
            ));

            count--;
        }

        return cardsSmartCompilation;
    }


    public static String cardsCollectionToText(String cardsCollectionName, List<Card> cards) {
        StringBuilder builder = new StringBuilder();
        builder.append(cardsCollectionName).append("\n");
        for (Card card : cards) {
            builder.append(card.getTerm()).append(" - ").append(card.getDefinition()).append("\n");
        }
        builder.append("\n");
        return builder.toString();
    }


    public static List<String> linesFromStream(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        for (String line; (line = reader.readLine()) != null; ) {
            lines.add(line);
        }

        reader.close();

        return lines;
    }


    public static void writeToStream(OutputStream outputStream, String text) throws IOException {
        outputStream.write(text.getBytes());
        outputStream.close();
    }
}
