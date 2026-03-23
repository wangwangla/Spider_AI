package com.spider.manager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.spider.SpiderGame;
import com.spider.action.CardAction;
import com.spider.action.DealPocker;
import com.spider.action.ReleaseCorner;
import com.spider.action.restore.Restore;
import com.spider.bean.DragInfo;
import com.spider.card.Card;
import com.spider.card.CardViewProvider;
import com.spider.card.ClickCard;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.action.pMove.PMove;
import com.spider.model.CardModel;
import com.spider.pocker.Pocker;

public class GameManager implements CardViewProvider {
    private Pocker pocker;
    private Array<CardAction> record;
    public static Array<Image> vecImageEmpty;
    private final Group cardGroup;
    private final Group finishGroup;
    private final Group sendCardGroup;
    private DragInfo dragInfo;
    private ReleaseCorner corner;
    private float cardWidth = 71;
    private float cardHeight = 96;
    private float border = 10;
    private Vector2 origionTouchDownVector;

    private ClickCard clickPocker = new ClickCard();
    private final ObjectMap<CardModel, Card> cardViews = new ObjectMap<CardModel, Card>();
    private static final float STACK_GAP = 20f;

    public Pocker getPocker() {
        return pocker;
    }

    public GameManager(Group cardGroup, Group finishGroup, Group sendCardGroup){
        this.record = new Array<CardAction>();
        this.dragInfo = new DragInfo();
        this.corner = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup, this);
        this.origionTouchDownVector = new Vector2();
        this.cardGroup = cardGroup;
        this.finishGroup = finishGroup;
        this.sendCardGroup = sendCardGroup;
    }

    /**
     * 开始新游戏
     * @param suitNum
     */
    public void newGame(int suitNum){
        this.record.clear();
        this.pocker = new Pocker();
        DealPocker action = new DealPocker(suitNum, this);
        action.doAction(pocker);
        initialImage();
        //发牌
        action.initPos(sendCardGroup,cardGroup);
        setPos();
    }

    @Override
    public Card viewOf(CardModel model) {
        return cardViews.get(model);
    }

    private Card ensureCardView(CardModel model) {
        Card card = cardViews.get(model);
        if (card == null) {
            card = new Card(model);
            card.initCard();
            cardViews.put(model, card);
        }
        return card;
    }

    public void setPos(){
        int index = 0;
        for (int i = 0; i < pocker.getDesk().size; i++) {
            Array<CardModel> cards = pocker.getDesk().get(i);
            index ++;
            float offSetY = 0;
            for (CardModel cardModel : cards) {
                Card card = ensureCardView(cardModel);
                card.setPosition(vecImageEmpty.get(i).getX(),offSetY);
                offSetY -= 20;
            }
        }
        index=0;
        for (Array<CardModel> cards : pocker.getCorner()) {
            index ++;
            for (CardModel cardModel : cards) {
                Card card = ensureCardView(cardModel);
                card.setPosition((index-1)*10,0,Align.bottom);
            }
        }
        index = 0;
        for (Array<CardModel> cards : pocker.getFinished()) {
            index++;
            for (CardModel cardModel : cards) {
                Card card = ensureCardView(cardModel);
                card.setPosition((index-1)*10,0,Align.bottom);
            }
        }

    }

    public void initialImage() {
        for (Array<CardModel> cards : pocker.getDesk()) {
            for (CardModel cardModel : cards) {
                Card card = ensureCardView(cardModel);
                card.setShow(cardModel.isFaceUp());
            }
        }
        for (Array<CardModel> cards : pocker.getCorner()) {
            for (CardModel cardModel : cards) {
                Card card = ensureCardView(cardModel);
                card.setShow(cardModel.isFaceUp());
            }
        }
    }

    public boolean touchDown(Actor target,float x,float y) {
        if(target == null){
            return false;
        }
        if (!(target instanceof Card)){
            return false;
        }
        Card cardView = (Card) target;
        CardModel targetModel = cardView.getModel();
        //重置点击记录
        clickPocker.setI(-1);
        clickPocker.setJ(-1);
        //获取被点的卡索引
        GetIndexFromPoint(targetModel);
        if (clickPocker.i == -1) {
            return false;
        }
        origionTouchDownVector.set(x, y);
        target.stageToLocalCoordinates(origionTouchDownVector);
        int num = pocker.getDesk().get(clickPocker.i).size - clickPocker.j;
        PMove pMove = new PMove(clickPocker.i, clickPocker.i, num, finishGroup, cardGroup, this);
        if (!pMove.canPick(pocker, clickPocker.i, num)) {
            return false;
        }
        dragInfo.getVecCard().clear();
        for (int i = 0; i < num; ++i) {
            CardModel model = pocker.getDesk().get(clickPocker.i).get(clickPocker.j + i);
            Card card = ensureCardView(model);
            card.toFront();
            ArrayMap<CardModel,Vector2> arrayMap = new ArrayMap<CardModel, Vector2>();
            arrayMap.put(model,card.getPosition());
            dragInfo.getVecCard().add(arrayMap);
        }
        dragInfo.setbOnDrag(true);
        dragInfo.setOrig(clickPocker.i);
        dragInfo.setNum(num);
        dragInfo.setCardIndex(clickPocker.j);
        return true;
    }

    public void GiveUpDrag(){
        dragInfo.setbOnDrag(false);
        for (ArrayMap<CardModel, Vector2> arrayMap : dragInfo.getVecCard()) {
            CardModel model = arrayMap.getKeyAt(0);
            Card view = viewOf(model);
            if (view != null) {
                view.setZIndex(0);
            }
        }
        dragInfo.getVecCard().clear();
    }

    public boolean OnMouseMove(Vector2 pt) {
        if (dragInfo.isbOnDrag()) {
            int index = 0;
            for (ArrayMap<CardModel, Vector2> arrayMap : dragInfo.getVecCard()) {
                CardModel model = arrayMap.getKeyAt(0);
                Card view = ensureCardView(model);
                Vector2 position = arrayMap.getValueAt(0);
                position.add(pt);
                view.setPosition(pt.x - origionTouchDownVector.x,
                        pt.y-cardGroup.getY() - index*20 - origionTouchDownVector.y);
                index++;
                NLog.e(" x %s,y %s",pt.x-cardGroup.getX(),pt.y-cardGroup.getY());
            }
            return true;
        }
        return false;
    }

    public boolean OnLButtonUp() {
        if (dragInfo.isbOnDrag()) {
            CardModel first = dragInfo.getVecCard().get(0).getKeyAt(0);
            Card firstView = ensureCardView(first);
            Vector2 ptUpCard = firstView.getPosition();
            int dest = GetDestIndex(pocker, ptUpCard, dragInfo.getOrig(), dragInfo.getNum());
            dragInfo.setbOnDrag(false);
            dragInfo.getVecCard().clear();
            Move(pocker,dragInfo.getOrig(),dest,dragInfo.getNum());
        }
        return false;
    }


    public int GetDestIndex(Pocker pocker, Vector2 ptUpCard, int orig, int num){
        int dest = -1;
        float Smax = -1;
        for (int i = 0; i < 10; ++i) {
            if (i == orig)
                continue;
            Vector2 ptDest;
            if (pocker.getDesk().get(i).size <= 0)
                ptDest = GetCardEmptyPoint(i);
            else {
                int topIndex = pocker.getDesk().get(i).size - 1;
                ptDest = new Vector2(GetCardEmptyPoint(i).x, stackY(topIndex));
            }
            float dx = Math.abs(ptUpCard.x - ptDest.x);
            float dy = Math.abs(ptUpCard.y - ptDest.y);
            float S = (cardWidth - dx) * (cardHeight - dy);
            if (cardWidth - dx > 0 && S > Smax && new PMove(orig, i, num, finishGroup, cardGroup, this).canMove(pocker, orig, i, num)) {
                Smax = S;
                dest = i;
            }
        }
        return dest;
    }

    private Vector2 GetCardEmptyPoint(int index) {
        int cardGap = (int) ((Constant.worldWidth - cardWidth * 10) / 11);
        float x = (int) (cardGap + index * (cardWidth + cardGap));
        float y = (int) border;
        return new Vector2(x,y);
    }

    public static float stackY(int cardIndex) {
        return -STACK_GAP * cardIndex;
    }

    public void faPai() {
        if (corner.doAction(pocker)) {
            record.add(corner);
            corner.startAnimation();
        }
    }

    public void recod() {
        if (record.size<=0)return;
        Array<CardAction> record = this.record;
        CardAction cardAction = record.removeIndex(record.size - 1);
        cardAction.redo(pocker);
        cardAction.redoAnimation();
        if (cardAction instanceof Restore){
            cardGroup.addAction(Actions.delay(0.3f,Actions.run(()-> recod())));
        }
    }


    public void GetIndexFromPoint(CardModel targetModel) {
        if (pocker == null || targetModel == null) {
            return;
        }
        for (int i = 0; i < pocker.getDesk().size; ++i) {
            for (int j = 0; j < pocker.getDesk().get(i).size; ++j) {
                CardModel cardModel = pocker.getDesk().get(i).get(j);
                if (cardModel == targetModel) {
                    clickPocker.setI(i);
                    clickPocker.setJ(j);
                }
            }
        }
    }

    private boolean Move(final Pocker poker,int orig,final int dest,int num) {
        if (orig!=dest && dest!=-1) {
            PMove action = new PMove(orig, dest, num,finishGroup,cardGroup, this);
            if (action.doAction(poker)) {
                record.add(action);
            }
            action.startAnimation();
            Restore restore = action.restore();
            if (restore!=null){
                record.add(restore);
            }
        }else {
            PMove action = new PMove(orig, orig, num,finishGroup,cardGroup, this);
            action.doAction(poker);
            action.startAnimation();
        }
        return false;
    }

    /**
     * 目标列没有牌可以放
     */
    private boolean canMove(int orig, int dest, int num) {
        Array<CardModel> cards = pocker.getDesk().get(orig);
        if (cards.size<num)return false;
        CardModel card = cards.get(cards.size - num);
        Array<CardModel> cards1 = pocker.getDesk().get(dest);
        if (cards1.size > 0){
            CardModel card1 = cards1.get(cards1.size-1);
            if (card1.getSuit() != card.getSuit()) {
                return false;
            }else if (card.getPoint() + 1 == card1.getPoint()){
                return true;
            }else {
                return false;
            }
        }else {
            return true;
        }
    }

    public void applySolverMove(int rawMove) {
        int moveTypeFlags = rawMove >>> 24;
        if ((moveTypeFlags & 8) != 0) { // special -> deal
            System.out.println("fapia");
            return;
        }
        int num = (rawMove & 0xF0000) >> 16;
        int srcGroup = (rawMove >> 8 & 0xFF) / 10;
        int src = (rawMove >> 8 & 0xFF) % 10;
        int dstGroup = (rawMove & 0xFF) / 10;
        int dst = (rawMove & 0xFF) % 10;
        if (srcGroup != 0 || dstGroup != 0) {
            return; // 只处理行内移动
        }
        if (num <= 0) {
            num = 1;
        }
        System.out.println(src +"   "+dst);

    }
    public void applySolverMove1(int rawMove) {
        int moveTypeFlags = rawMove >>> 24;
        if ((moveTypeFlags & 8) != 0) { // special -> deal
            System.out.println("fapia");
            faPai();
            return;
        }
        int num = (rawMove & 0xF0000) >> 16;
        int srcGroup = (rawMove >> 8 & 0xFF) / 10;
        int src = (rawMove >> 8 & 0xFF) % 10;
        int dstGroup = (rawMove & 0xFF) / 10;
        int dst = (rawMove & 0xFF) % 10;
        if (srcGroup != 0 || dstGroup != 0) {
            return; // 只处理行内移动
        }
        if (num <= 0) {
            num = 1;
        }
        System.out.println(src +"   "+dst);
        performMove(src,dst,num);
    }

    public void performMove(int orig, int dest, int num) {
        if (orig == dest || dest == -1) {
            return;
        }
        PMove action = new PMove(orig, dest, num, finishGroup, cardGroup, this);
        if (!action.doAction(pocker)) {
            return;
        }
        record.add(action);
        action.startAnimation();
        Restore restore = action.restore();
        if (restore != null) {
            record.add(restore);
        }
    }

    public void setGuiProperty() {
        float v1 = (cardGroup.getWidth() - 71 * 10) / 11.0F;
        vecImageEmpty = new Array<Image>();
        for (int i = 0; i < 10; i++) {
            Image image = new Image(SpiderGame.getAssetUtil().loadTexture("Resource/cardempty.png"));
            image.setOrigin(Align.center);
            image.setScale(1);
            vecImageEmpty.add(image);
            cardGroup.addActor(image);
            image.setX(v1*(i+1)+71 * i);
        }
    }
}
