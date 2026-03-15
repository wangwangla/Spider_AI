package com.spider.action;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.manager.GameManager;
import com.spider.pocker.Pocker;
import com.solvitaire.app.DealCardCodec;
import com.solvitaire.app.DealVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理发牌与初始布局
 */
public class DealPocker extends Action {
    private int suitNum;
    private int seed;

    public DealPocker(int suitNum) {
        this.suitNum = suitNum;
        this.seed = (int) System.currentTimeMillis();
        NLog.e("seed is %s", seed);
    }

    /**
     * 发牌并生成牌局
     */
    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        poker.setSuitNum(suitNum);
        poker.setSeed(seed);
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();

        // 使用 SolverCard 的 Spider 生成器，确保与求解器保持一致
        String dealText = DealVariant.SPIDER.generate(suitNum, seed);
        poker.setDealString(dealText);
        ParsedDeal parsedDeal = parseDeal(dealText);
        if (parsedDeal.tableau.size() != 54 || parsedDeal.stock.size() != 50) {
            NLog.e("Deal parse error, tableau=%s stock=%s", parsedDeal.tableau.size(), parsedDeal.stock.size());
            return false;
        }

        // 桌面 10 列：前 5 行各 10 张，第 6 行前 4 列各 1 张
        int index = 0;
        for (int row = 0; row < 6; row++) {
            int columns = row == 5 ? 4 : 10;
            for (int col = 0; col < columns; col++) {
                if (poker.getDesk().size <= col) {
                    poker.getDesk().add(new Array<Card>());
                }
                Card card = toGameCard(parsedDeal.tableau.get(index++));
                card.setShow(false);
                poker.getDesk().get(col).add(card);
            }
        }
        for (Array<Card> cardArray : poker.getDesk()) {
            if (cardArray.size > 0) {
                cardArray.get(cardArray.size - 1).setShow(true);
            }
        }

        // 牌库 5 叠，每叠 10 张
        int stockIndex = 0;
        for (int pack = 0; pack < 5; ++pack) {
            Array<Card> cornerOne = new Array<Card>();
            for (int j = 0; j < 10; ++j) {
                Card card = toGameCard(parsedDeal.stock.get(stockIndex++));
                card.setShow(false);
                cornerOne.add(card);
            }
            poker.getCorner().add(cornerOne);
        }

        poker.setScore(500);
        poker.setOperation(0);
        return true;
    }

    private Card toGameCard(int solverCardId) {
        int solverSuit = solverCardId / 100;
        int rank = solverCardId % 100;
        // Solver: 1=spade,2=heart,3=diamond,4=club
        // Game:   1=club,2=diamond,3=heart,4=spade
        int gameSuit;
        switch (solverSuit) {
            case 1:
                gameSuit = 4;
                break;
            case 2:
                gameSuit = 3;
                break;
            case 3:
                gameSuit = 2;
                break;
            case 4:
            default:
                gameSuit = 1;
                break;
        }
        return new Card(gameSuit, rank);
    }

    private ParsedDeal parseDeal(String dealText) {
        List<Integer> tableau = new ArrayList<>();
        List<Integer> stock = new ArrayList<>();
        String[] lines = dealText.split("\\r?\\n");
        for (int i = 1; i < lines.length; i++) { // skip header
            if (lines[i].trim().isEmpty()) continue;
            String[] tokens = lines[i].split(",");
            for (String token : tokens) {
                String value = token.trim();
                if (value.isEmpty()) continue;
                int cardId = DealCardCodec.parse(value);
                if (tableau.size() < 54) {
                    tableau.add(cardId);
                } else {
                    stock.add(cardId);
                }
            }
        }
        return new ParsedDeal(tableau, stock);
    }

    private static class ParsedDeal {
        final List<Integer> tableau;
        final List<Integer> stock;

        ParsedDeal(List<Integer> tableau, List<Integer> stock) {
            this.tableau = tableau;
            this.stock = stock;
        }
    }

    public void startAnimation() {
        Array<Array<Card>> deskPocker = poker.getDesk();
        int indexX = 0;
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < deskPocker.size; i++) {
            int y = 0;
            Array<Card> cards = deskPocker.get(i);
            Image image = vecImageEmpty.get(i);
            for (Card card : cards) {
                card.addAction(Actions.delay(indexX * 0.1F + 1F * y,
                    Actions.moveTo(image.getX(), image.getY() - y * 20, 0.1F)));
                y++;
            }
            indexX++;
        }
    }

    public boolean redo(Pocker inpoker, Group deskGroup, Group finishGroup, Group coener) {
        poker = inpoker;
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();
        deskGroup.clearChildren();
        finishGroup.clearChildren();
        coener.clearChildren();
        return true;
    }

    public void initPos(Group sendCardGroup, Group cardGroup) {
        for (int i = 0; i < poker.getCorner().size; i++) {
            Array<Card> cards = poker.getCorner().get(i);
            for (int i1 = 0; i1 < cards.size; i1++) {
                Card card = cards.get(i1);
                card.setPosition(i * 10, 0);
            }
        }

        Array<Array<Card>> deskPocker = poker.getDesk();
        Vector2 pos = new Vector2(0, 0);
        sendCardGroup.localToStageCoordinates(pos);
        cardGroup.stageToLocalCoordinates(pos);
        for (Array<Card> array : deskPocker) {
            for (Card card : array) {
                card.setPosition(pos.x, pos.y);
            }
        }
    }
}
