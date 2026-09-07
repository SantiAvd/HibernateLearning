package org.example.mapper;

import org.example.model.CategoryCollors;

public class CollorMapper {

    public static String collorMapper(CategoryCollors collor) {
        String currentCollor = "";
        switch (collor) {
            case RED -> {
                currentCollor =  "\uD83D\uDD34";
            }
            case BLUE -> {
                currentCollor =   "\uD83D\uDFE3";
            }
            case ORANGE -> {
                currentCollor =   "\uD83D\uDFE0";
            }
            case YELLOW -> {
                currentCollor =   "\uD83D\uDFE1";
            }
            case GREEN -> {
                currentCollor =   "\uD83D\uDFE2";
            }
            case NON -> {
                currentCollor =   "";
            }
        }
        return currentCollor;
    }
}
