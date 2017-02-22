/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokenization;
import javax.swing.*;

/**
 *
 * @author Ivo
 */
public class Tokenization {
    
    private static boolean checkLuhnAlg (char[] cardNum) {
        int sum = 0;
        
        for(int i = 0 ; i < 16 ; i++) {
            int current = Character.getNumericValue(cardNum[i]);
            if(i < 4) {
                if(current != 3 && current != 4 &&
                        current != 5 && current != 6) {
                    return false;
                }
            }
            if( i % 2 == 0 ) {
                current *= 2;
                sum += current > 9? current - 9 : current;
            } else {
                sum += current;
            }
        }
        return sum % 10 == 0;
    }

    public static boolean generateToken(String cardNum) {
        char[] cardNumChar = cardNum.toCharArray();
        char[] token = new char[16];
        byte rand;
        
        int sum = 0;
        
        for(int i = 0 ; i < 16 ; i++) {
            int current = Character.getNumericValue(cardNumChar[i]);
            if(i == 0) {
                if(current != 3 && current != 4 &&
                        current != 5 && current != 6) {
                    return false;
                } else {
                    do {
                        rand = (byte) (Math.random() * 10);
                    } while(rand != 3 && rand != 4 && rand != 5 &&
                            rand != 6);
                    
                    token[i] = (char)(rand + '0');
                }
            } else if(i < 12){
                do {
                        rand = (byte) (Math.random() * 10);
                    } while(rand == current);
                    
                    token[i] = (char)(rand + '0');
            } else {
                token[i] = cardNumChar[i];
            }
            if( i % 2 == 0 ) {
                current *= 2;
                sum += current > 9? current - 9 : current;
            } else {
                sum += current;
            }
        }
        System.out.println(token);
        return sum % 10 == 0;
    }
            
    public static void main(String[] args) {
        generateToken("4563960122019991");
    }
    
}
