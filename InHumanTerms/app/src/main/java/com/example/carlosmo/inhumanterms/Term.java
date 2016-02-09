package com.example.carlosmo.inhumanterms;

/**
 * Created by Carlos Mo on 23/11/2015.
 */

public class Term {
    int id, favourited;
    String term, subject, definition, dictionary, synonyms;

    public Term() {}

    public Term(int id, String dictionary, String term, String subject, String definition, String synonyms, int favourited) {
        this.id = id;
        this.dictionary = dictionary;
        this.term = term;
        this.subject = subject;
        this.definition = definition;
        this.synonyms = synonyms;
        this.favourited = favourited;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDictionary() { return dictionary; }
    public void setDictionary(String dictionary) { this.dictionary = dictionary; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
    public String getSynonyms() { return synonyms; }
    public void setSynonyms(String synonyms) { this.synonyms = synonyms; }
    public int getFavourited() { return favourited; }
    public void setFavourited(int favourited) { this.favourited = favourited; }

    public String toString() { return term; }
}

