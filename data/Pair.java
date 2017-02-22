package data;

public class Pair {
    private CardNumber cNum;
    private Token token;
    
    public Pair(CardNumber cNum, Token token) {
        this.cNum = cNum;
        this.token = token;
    }
    
    public CardNumber getCardNumber() {
        return cNum;
    }
    
    public Token getToken() {
        return token;
    }
}
