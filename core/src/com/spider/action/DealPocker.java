package com.spider.action;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.card.Card;
import com.spider.card.CardViewProvider;
import com.spider.log.NLog;
import com.spider.manager.GameManager;
import com.spider.model.CardModel;
import com.spider.pocker.Pocker;
import com.solvitaire.app.DealCardCodec;
import com.solvitaire.app.DealVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理发牌与初始摆放。
 */
public class DealPocker extends Action {
    private final int suitNum;
    private final int seed;
    private final CardViewProvider viewProvider;

    public DealPocker(int suitNum, CardViewProvider viewProvider) {
        this.suitNum = suitNum;
        this.viewProvider = viewProvider;
        this.seed = (int) System.currentTimeMillis();
        NLog.e("seed is %s", seed);
    }

    /**
     * 发牌并生成牌面
     */
    public boolean doAction(Pocker inpoker) {
        poker = inpoker;
        poker.setSuitNum(suitNum);
        poker.setSeed(seed);
        poker.getDesk().clear();
        poker.getCorner().clear();
        poker.getFinished().clear();

        // 使用 SolverCard 的 Spider 生成器，保证与求解器一致
        String dealText = DealVariant.SPIDER.generate(suitNum, seed);
        poker.setDealString(dealText);
        ParsedDeal parsedDeal = parseDeal(dealText);
        if (parsedDeal.tableau.size() != 54 || parsedDeal.stock.size() != 50) {
            NLog.e("Deal parse error, tableau=%s stock=%s", parsedDeal.tableau.size(), parsedDeal.stock.size());
            return false;
        }

        // 桌面 10 列：前 4 列 6 张，其余 5 张
        int index = 0;
        for (int row = 0; row < 6; row++) {
            int columns = row == 5 ? 4 : 10;
            for (int col = 0; col < columns; col++) {
                if (poker.getDesk().size <= col) {
                    poker.getDesk().add(new Array<CardModel>());
                }
                CardModel cardModel = toGameCard(parsedDeal.tableau.get(index++));
                cardModel.setFaceUp(false);
                poker.getDesk().get(col).add(cardModel);
            }
        }
        for (Array<CardModel> cardArray : poker.getDesk()) {
            if (cardArray.size > 0) {
                cardArray.get(cardArray.size - 1).setFaceUp(true);
            }
        }

        // 待发牌堆：5 叠 * 每叠 10 张
        int stockIndex = 0;
        for (int pack = 0; pack < 5; ++pack) {
            Array<CardModel> cornerOne = new Array<CardModel>();
            for (int j = 0; j < 10; ++j) {
                CardModel cardModel = toGameCard(parsedDeal.stock.get(stockIndex++));
                cardModel.setFaceUp(false);
                cornerOne.add(cardModel);
            }
            poker.getCorner().add(cornerOne);
        }

        poker.setScore(500);
        poker.setOperation(0);
        return true;
    }

    private CardModel toGameCard(int solverCardId) {
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
        return new CardModel(gameSuit, rank);
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
        Array<Array<CardModel>> deskPocker = poker.getDesk();
        int indexX = 0;
        Array<Image> vecImageEmpty = GameManager.vecImageEmpty;
        for (int i = 0; i < deskPocker.size; i++) {
            int y = 0;
            Array<CardModel> cards = deskPocker.get(i);
            Image image = vecImageEmpty.get(i);
            for (CardModel cardModel : cards) {
                Card card = viewProvider.viewOf(cardModel);
                if (card == null) continue;
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
            Array<CardModel> cards = poker.getCorner().get(i);
            for (int i1 = 0; i1 < cards.size; i1++) {
                CardModel cardModel = cards.get(i1);
                Card card = viewProvider.viewOf(cardModel);
                if (card != null) {
                    card.setPosition(i * 10, 0);
                    sendCardGroup.addActor(card);
                }
            }
        }

        Array<Array<CardModel>> deskPocker = poker.getDesk();
        Vector2 pos = new Vector2(0, 0);
        sendCardGroup.localToStageCoordinates(pos);
        cardGroup.stageToLocalCoordinates(pos);
        for (Array<CardModel> array : deskPocker) {
            for (CardModel cardModel : array) {
                Card card = viewProvider.viewOf(cardModel);
                if (card != null) {
                    card.setPosition(pos.x, pos.y);
                    cardGroup.addActor(card);
                }
            }
        }
    }
}
