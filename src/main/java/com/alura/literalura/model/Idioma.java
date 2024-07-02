package com.alura.literalura.model;

import java.util.HashMap;
import java.util.Map;

public enum Idioma {
    en("[en]", "Ingles"),
    es("[es]", "Español"),
    fr("[fr]", "Frances"),
    pt("[pt]", "Portugues");

    private final String idiomaGutendex;
    private final String idiomaEspanol;

    // Mapas estáticos para búsqueda rápida
    private static final Map<String, Idioma> GUTENDEX_MAP = new HashMap<>();
    private static final Map<String, Idioma> ESPANOL_MAP = new HashMap<>();

    static {
        for (Idioma idioma : Idioma.values()) {
            GUTENDEX_MAP.put(idioma.idiomaGutendex.toLowerCase(), idioma);
            ESPANOL_MAP.put(idioma.idiomaEspanol.toLowerCase(), idioma);
        }
    }

    Idioma(String idiomaGutendex, String idiomaEspanol) {
        this.idiomaGutendex = idiomaGutendex;
        this.idiomaEspanol = idiomaEspanol;
    }

    public static Idioma fromString(String text) {
        Idioma idioma = GUTENDEX_MAP.get(text.toLowerCase());
        if (idioma != null) {
            return idioma;
        }
        throw new IllegalArgumentException("Ningún idioma encontrado: " + text);
    }

    public static Idioma fromEspanol(String text) {
        Idioma idioma = ESPANOL_MAP.get(text.toLowerCase());
        if (idioma != null) {
            return idioma;
        }
        throw new IllegalArgumentException("Ningún idioma encontrado: " + text);
    }

    public String getIdiomaGutendex() {
        return idiomaGutendex;
    }

    public String getIdiomaEspanol() {
        return idiomaEspanol;
    }
}