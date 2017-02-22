/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

/**
 *
 * @author Ivo
 */
public class Token {
    private String token;
    
    public Token(String token) {
        this.token = token;
    }
    
    public String getToken() {
        return token;
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
        Token p = (Token) o;
        return token.equals(p.getToken());
    }
}
