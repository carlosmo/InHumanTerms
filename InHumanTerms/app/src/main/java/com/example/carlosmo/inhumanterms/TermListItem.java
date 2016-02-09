package com.example.carlosmo.inhumanterms;

/**
 * Created by Carlos Mo on 27/11/2015.
 */
public class TermListItem {
    int id;
    String term;

    public TermListItem() {}

    public TermListItem(int id, String term) {
        this.id = id;
        this.term = term;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String toString() { return term; }
}
