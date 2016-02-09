package com.example.carlosmo.inhumanterms;

/**
 * Created by Carlos Mo on 18/11/2015.
 */
// Source: http://www.tutecentral.com/android-custom-navigation-drawer/
public class DrawerItem {
    String name;
    int icon;

    public DrawerItem(String name, int icon) {
        super();
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public void setName() { this.name = name; }
    public int getIcon() { return icon; }
    public void setIcon() { this.icon = icon; }
}
