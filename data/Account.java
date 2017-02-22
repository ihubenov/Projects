/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.thoughtworks.xstream.XStream;

/**
 *
 * @author Ivo
 */
public class Account {
    private String name;
    private String password;
    private int priority;
    
    public Account (String name, String password, int priority) {
        this.name = name;
        this.password = password;
        this.priority = priority;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getPriority() {
        return priority;
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(this == null) {
            return false;
        }
        if(getClass() != o.getClass()) {
            return false;
        }
        Account p = (Account) o;
        return name.equals(p.getName()) &&
                password.equals(p.getPassword());
    }
    
    public static void main(String[] args) {
        XStream xstream = new XStream();
        xstream.alias("account", Account.class);
        Account acc = new Account("a", "b", 1);
        String s = xstream.toXML(acc);
        try {
            Token copy = (Token) xstream.fromXML(s);
        } catch (Exception e) {
            System.out.println("Not a token");
        }
        
    }
}
