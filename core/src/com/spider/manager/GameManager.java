package com.spider.manager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.spider.SpiderGame;
import com.spider.action.Action;
import com.spider.action.DealPocker;
import com.spider.action.ReleaseCorner;
import com.spider.bean.DragInfo;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.action.pMove.PMove;
import com.spider.pocker.Pocker;


public class GameManager {
    private Pocker pocker;
    private Array<Action> record;
    public static Array<Image> vecImageEmpty;
    private Group cardGroup;
    private Group finishGroup;
    private Group sendCardGroup;
    private PMove pMove;
    private DragInfo dragInfo;
    private ReleaseCorner corner;
    private float cardWidth = 71;
    private float cardHeight = 96;
    private float border = 10;
    private Vector2 origionTouchDownVector;

    public GameManager(Group cardGroup, Group finishGroup, Group sendCardGroup){
        //记录
        this.record = new Array<Action>();
        this.dragInfo = new DragInfo();
        this.pMove = new PMove();
        this.corner = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
        this.origionTouchDownVector = new Vector2();
        this.cardGroup = cardGroup;
        this.finishGroup = finishGroup;
        this.sendCardGroup = sendCardGroup;
    }

    /**
     * 开始游戏
     * @param suitNum
     */
    public void newGame(int suitNum){
        this.record.clear();
        this.pocker = new Pocker();
        DealPocker action = new DealPocker(suitNum);
        action.doAction(pocker);
        initialImage();
        //发牌
        action.initPos(sendCardGroup,cardGroup);
        setPos();
    }

    public void setPos(){
        int index = 0;
        for (int i = 0; i < pocker.getDesk().size; i++) {
            Array<Card> cards = pocker.getDesk().get(i);
            index ++;
            float offSetY = 0;
            for (Card card : cards) {
                card.setPosition(vecImageEmpty.get(i).getX(),offSetY);
                offSetY -= 20;
            }
        }
        index=0;
        for (Array<Card> cards : pocker.getCorner()) {
            index ++;
            for (Card card : cards) {
                card.setPosition((index-1)*10,0,Align.bottom);
            }
        }
        index = 0;
        for (Array<Card> cards : pocker.getFinished()) {
            index++;
            for (Card card : cards) {
                card.setPosition((index-1)*10,0,Align.bottom);
            }
        }

    }

    public void updateZIndex(){
//        int zIndex=0;
//        for (Array<Card> array : pocker.getDesk()) {
//            for (Card card : array) {
//                card.setZIndex(10+zIndex++);
//            }
//        }
    }

    public void initialImage() {
        //每张牌加入图片
        for (Array<Card> cards : pocker.getDesk()) {
            for (Card card : cards) {
                card.initCard();
                cardGroup.addActor(card);
            }
        }
        //角落牌加入图片
        for (Array<Card> cards : pocker.getCorner()) {
            for (Card card : cards) {
                card.initCard();
                sendCardGroup.addActor(card);
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
        clickPocker.setI(-1);
        clickPocker.setJ(-1);
        //取得按下的牌编号
        GetIndexFromPoint(target);
        //没有牌
        if (clickPocker.i == -1)
            return false;
        origionTouchDownVector.set(x, y);
        target.stageToLocalCoordinates(origionTouchDownVector);
        int num = pocker.getDesk().get(clickPocker.i).size - clickPocker.j;
        //不能够拾取
        if (!pMove.canPick(pocker, clickPocker.i, num))
            return false;
        //开始拖动设置
        dragInfo.getVecCard().clear();
        for (int i = 0; i < num; ++i) {
            //拖动组设置z-index，加入牌指针及相对坐标
            Card card = pocker.getDesk().get(clickPocker.i).get(clickPocker.j + i);
            card.toFront();
            ArrayMap<Card,Vector2> arrayMap = new ArrayMap<Card, Vector2>();
            arrayMap.put(card,card.getPosition());
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
        //恢复z-index
        for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
            arrayMap.getKeyAt(0).setZIndex(0);
        }
        dragInfo.getVecCard().clear();
    }

    public boolean OnMouseMove(Vector2 pt) {
        if (dragInfo.isbOnDrag()) {
            //没有按下左键，释放拖动
            //抬起
            //移动拖动组
            int index = 0;
            for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
                Vector2 position = arrayMap.getKeyAt(0).getPosition();
                position.add(pt);
                arrayMap.getKeyAt(0).setPosition(pt.x - origionTouchDownVector.x,
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
            //取得拖动牌最顶上一张坐标
            Vector2 ptUpCard = dragInfo.getVecCard().get(0).getKeyAt(0).getPosition();
            //取得目标牌位号
            int dest = GetDestIndex(pocker, ptUpCard, dragInfo.getOrig(), dragInfo.getNum());
            //恢复拖动设置
            dragInfo.setbOnDrag(false);

            dragInfo.getVecCard().clear();
            //有目标牌位，且可以移动
            Move(pocker,dragInfo.getOrig(),dest,dragInfo.getNum());
        }else {
            updateZIndex();
        }
        return false;
    }


    public int GetDestIndex(Pocker pocker, Vector2 ptUpCard, int orig, int num){
        int dest = -1;
        float Smax = -1;
        for (int i = 0; i < 10; ++i) {
            if (i == orig)//由于计算面积，自己和自己的面积必然最大，所以需要排除掉拖动源
                continue;
            Vector2 ptDest;//目标坐标
            if (pocker.getDesk().get(i).size <= 0)
                ptDest = GetCardEmptyPoint(i);
            else {
                Array<Card> array = pocker.getDesk().get(i);
                ptDest = array.get(array.size - 1).getPosition();
            }
            float dx = Math.abs(ptUpCard.x - ptDest.x);//坐标之差
            float dy = Math.abs(ptUpCard.y - ptDest.y);
            float S = (cardWidth - dx) * (cardHeight - dy);//计算两个矩形的重合面积
            //第一个判断条件是为了排除掉负负得正的情形
            if (cardWidth - dx > 0 && S > Smax && pMove.canMove(pocker, orig, i, num)) {
                //在被覆盖牌中，取得可以放置的，且被覆盖面积最大的一张作为拖动目的地
                Smax = S;
                dest = i;
            }
        }
        return dest;
    }

    private Vector2 GetCardEmptyPoint(int index) {
        //计算空牌位坐标
        int cardGap = (int) ((Constant.worldWidth - cardWidth * 10) / 11);
        //空牌位位置
        float x = (int) (cardGap + index * (cardWidth + cardGap));
        float y = (int) border;
        return new Vector2(x,y);
    }

    public void faPai() {
        if (corner.doAction(pocker)) {
            record.add(corner);
            corner.startAnimation();
        }
    }

    public void recod() {
        if (record.size<=0)return;
        Array<Action> record = this.record;
        Action action = record.removeIndex(record.size - 1);
        action.redo(pocker);
        action.redoAnimation();
    }

//    public void test() {
//        Array<Card> cards = pocker.getDesk().get(1);
//        for (Card card : cards) {
//            Vector2 temp = new Vector2(card.getX(Align.center),card.getY(Align.center));
//            cardGroup.localToStageCoordinates(temp);
//            finishGroup.stageToLocalCoordinates(temp);
//            card.setPosition(temp.x,temp.y,Align.center);
//            finishGroup.addActor(card);
//        }
//    }

    class ClickPocker{
        private int i;

        private int j;

        public void setI(int i) {
            this.i = i;
        }

        public void setJ(int j) {
            this.j = j;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }
    }

    private ClickPocker clickPocker = new ClickPocker();

    public void GetIndexFromPoint(Object object) {
        if (pocker == null)
            return;
        for (int i = 0; i < pocker.getDesk().size; ++i) {
            for (int j = 0; j < pocker.getDesk().get(i).size; ++j) {
                Card card = pocker.getDesk().get(i).get(j);
                if (card == object) {
                    clickPocker.setI(i);
                    clickPocker.setJ(j);
                }
            }
        }
    }

    private boolean Move(final Pocker poker,int orig,final int dest,int num) {
        if (orig!=dest && dest!=-1) {
            if (canMove(orig,dest,num)) {
                PMove action = new PMove(orig, dest, num,finishGroup,cardGroup);
                if (action.doAction(poker)) {
                    record.add(action);
                }
                action.startAnimation();

            }else {
                PMove action = new PMove(orig, orig, num,finishGroup,cardGroup);
                action.setPocker(poker);
                action.startAnimation();
            }
        }else {
            PMove action = new PMove(orig, orig, num,finishGroup,cardGroup);
            action.setPocker(poker);
            action.startAnimation();
        }
        return false;
    }

    /**
     * 目标列没用牌可以放置
     * @param orig
     * @param dest
     * @param num
     * @return
     */
    private boolean canMove(int orig, int dest, int num) {
        Array<Card> cards = pocker.getDesk().get(orig);
        if (cards.size<num)return false;
        Card card = cards.get(cards.size - num);
        // 目标列  没用牌到时候
        Array<Card> cards1 = pocker.getDesk().get(dest);
        if (cards1.size > 0){
            Card card1 = cards1.get(cards1.size-1);
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

    public void setGuiProperty() {
        //创建空牌位
        float v1 = (Constant.worldWidth - 71 * 10) / 11.0F;
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
