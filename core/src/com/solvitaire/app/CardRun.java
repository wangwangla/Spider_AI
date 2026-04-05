/*
 * Decompiled with CFR 0.152.
 */
package com.solvitaire.app;

import com.solvitaire.app.Card;
import com.solvitaire.app.SolverContext;
import com.solvitaire.app.CardStack;
import java.util.Arrays;

/*
 * Renamed from com.solvitaire.app.ok
 */
final class CardRun {
    public int cardCount = 0;
    boolean faceDown;
    Card[] cards = new Card[13];
    CardStack stack;

    CardRun() {
        this.cardCount = 0;
    }

    CardRun(Card nT2) {
        this.cards[0] = nT2;
        this.cardCount = 1;
    }

    CardRun(CardRun ok_02) {
        this.cardCount = ok_02.cardCount;
        this.faceDown = ok_02.faceDown;
        this.cards = Arrays.copyOf(ok_02.cards, 13);
    }

    final void a(int n2) {
        if (this.cardCount != 1 || n2 <= 0 || this.cards[0].cardId != 0 && this.cards[0].cardId != n2) {
            String string = "setCardValue called on run existing length " + this.cardCount + " existing card " + this.cards[0].cardId + " new value " + n2;
            SolverContext context = this.stack.context;
            context.invalidInput(string, false);
        }
        this.cards[0].a(n2);
    }

    static boolean a(Card nT2, Card nT3) {
        return CardRun.a(nT2) ^ CardRun.a(nT3);
    }

    private static boolean a(Card nT2) {
        return nT2.suit == 1 || nT2.suit == 4;
    }

    final int a(Card nT2, Card nT3, int n2, boolean bl) {
        int n3;
        if (!bl) {
            if (!this.stack.alternatingColors && nT2.suit == nT3.suit) {
                return -1;
            }
            n3 = nT2.rank - nT3.rank;
        } else {
            n3 = nT2.cardId - nT3.cardId;
        }
        if (n3 <= 0 || n3 > n2) {
            n3 = -1;
        }
        return n3;
    }

    final int a(CardRun ok_02, int n2) {
        int n3 = 0;
        while (n3 < n2) {
            this.cards[this.cardCount + n3] = ok_02.cards[ok_02.cardCount - n2 + n3];
            ++n3;
        }
        this.cardCount += n2;
        if (n2 < ok_02.cardCount) {
            n2 += 20;
        }
        return n2;
    }

    final CardRun b(int n2) {
        CardRun ok_02 = new CardRun();
        ok_02.a(this, n2);
        this.cardCount -= n2;
        return ok_02;
    }

    public final String toString() {
        StringBuffer stringBuffer = new StringBuffer("Run:");
        boolean bl = true;
        Card[] nTArray = this.cards;
        int n2 = this.cards.length;
        int n3 = 0;
        while (n3 < n2) {
            Card nT2 = nTArray[n3];
            if (nT2 == null) break;
            if (!bl) {
                stringBuffer.append(",");
            }
            bl = false;
            stringBuffer.append(nT2.cardId);
            ++n3;
        }
        return stringBuffer.toString();
    }
}





