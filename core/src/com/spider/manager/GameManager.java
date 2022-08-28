package com.spider.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.spider.action.Action;
import com.spider.action.Deal;
import com.spider.bean.DragInfo;
import com.spider.card.Card;
import com.spider.config.Configuration;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.pMove.PMove;
import com.spider.pocker.Pocker;

public class GameManager {
    private Pocker pocker;
    private Array<Action> record = new Array<Action>();
    private Array<Image> vecImageEmpty;
    private String idCardEmpty;
    private String idCardBack;
    private String idCard1;
    private String idCardMask;
    private Configuration config;
    private boolean bOnThread;
    private boolean bStopThread;
    private Group cardGroup;
    private Group finishGroup;
    private Group sendCardGroup;
    private PMove pMove;
    private DragInfo dragInfo;
    private ReleaseCorner corner ;
//            cardGroup,finishGroup,sendCardGroup
    public GameManager(Group cardGroup, Group finishGroup, Group sendCardGroup){
        config = new Configuration();
        this.cardGroup = cardGroup;
        this.finishGroup = finishGroup;
        this.sendCardGroup = sendCardGroup;
        this.dragInfo = new DragInfo();
        this.pMove = new PMove();
        this.corner = new ReleaseCorner();
    }

    public void newGame(int suitNum){
        int seed = (int) (Math.random() * 100);
        NLog.e("seed is %s",seed);
        record.clear();
        pocker = new Pocker();
        Deal action = new Deal(suitNum,seed,false,1);
        action.Do(pocker);
        if (idCardEmpty!=null && idCardBack!=null &&
                idCard1!=null && idCardMask!=null){
            initialImage();
        }
        setPos();
        if (pocker.isHasGUI()){
            if (config.isEnableAnimation()) {
                action.startAnimation(bOnThread, bStopThread);
            } else {
                bOnThread = false;
            }
        }
    }

    public void setPos(){
        int index = 0;
        float worldWidth = Constant.worldWidth;
        float v = worldWidth / 10.0F;
        for (Array<Card> cards : pocker.getDesk()) {
            index ++;
            float offSetY = 0;
            for (Card card : cards) {
                card.setPosition((index-1)* v,offSetY, Align.left);
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

        int zIndex=0;
        for (Array<Card> array : pocker.getDesk()) {
            for (Card card : array) {
//                card.setZIndex(zIndex++);
                card.setZIndex(zIndex++);
            }
        }

        System.out.println("-------------------");
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
        pocker.setHasGUI(true);
    }


    public boolean touchDown(Actor target) {
        if(target == null){
            return false;
        }
        if (!(target instanceof Card)){
            return false;
        }
        if (!pocker.isHasGUI())
            return false;

        clickPocker.setI(-1);
        clickPocker.setJ(-1);
        //取得按下的牌编号
        GetIndexFromPoint(target);
        //没有牌
        if (clickPocker.i == -1)
            return false;
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
//        for (auto& pr : dragInfo.vecCard)
//        {
//            pr.first->SetZIndex(0);
//        }
        dragInfo.getVecCard().clear();
        //恢复位置并刷新
//        OnSize(*pRcClient);
//        InvalidateRect(hWnd, pRcClient, false);
    }

    private boolean hasLoadImage;

    public boolean OnMouseMove(Vector2 pt) {
        if (hasLoadImage == false)
            return false;
        if (dragInfo.isbOnDrag()) {
            //没有按下左键，释放拖动
            //抬起
//            if (!(GetAsyncKeyState(VK_LBUTTON) & 0x8000)) {
            if (false){
                GiveUpDrag();
            } else {
                //移动拖动组
                int index = 0;
                for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
                    Vector2 position = arrayMap.getKeyAt(0).getPosition();
                    position.add(pt);
                    arrayMap.getKeyAt(0).setPosition(pt.x-cardGroup.getX(),pt.y-cardGroup.getY() - index*20);
                    index++;
                    NLog.e(" x %s,y %s",pt.x-cardGroup.getX(),pt.y-cardGroup.getY());
                }
//                for (auto& pr : dragInfo.vecCard)
//                {
//                    pr.first->SetPos(pt + pr.second);
//
//                }

                //刷新
//                InvalidateRect(hWnd, pRcClient, false);
                return true;
            }
        }
        return false;
    }

    public boolean OnLButtonUp(Vector2 pt) {
        if (dragInfo.isbOnDrag()) {
            //取得拖动牌最顶上一张坐标
            Vector2 ptUpCard = dragInfo.getVecCard().get(0).getKeyAt(0).getPosition();

            //取得目标牌位号
            int dest = GetDestIndex(pocker, ptUpCard, dragInfo.getOrig(), dragInfo.getNum());
            //恢复拖动设置
            dragInfo.setbOnDrag(false);
            for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
                arrayMap.getKeyAt(0).setZIndex(0);
            }
//            for (auto& pr : dragInfo.vecCard)
//            {
//                pr.first->SetZIndex(0);
//            }
            dragInfo.getVecCard().clear();

            //有目标牌位，且可以移动
            if (dest != -1 && dest != dragInfo.getOrig()) {
                Move(pocker,dragInfo.getOrig(),dest,dragInfo.getNum());
                //进行移动
//                Command("m " + to_string(dragInfo.orig) + " " + to_string(dest) + " " + to_string(dragInfo.num));

//                OnSize(*pRcClient);
//                InvalidateRect(hWnd, pRcClient, false);
//                return true;
            }else {
            }
        }

        setPos();
//        OnSize(*pRcClient);
//        InvalidateRect(hWnd, pRcClient, false);
        return false;
    }

    public int GetDestIndex(Pocker pocker, Vector2 ptUpCard, int orig, int num){
        //            auto GetDestIndex = [&](Poker* poker, RECT rectClient, POINT ptUpCard, int orig, int num)->int
//            {
        int dest = -1;
        float Smax = -1;
        for (int i = 0; i < 10; ++i) {
            if (i == orig)//由于计算面积，自己和自己的面积必然最大，所以需要排除掉拖动源
                continue;

            Vector2 ptDest = new Vector2();//目标坐标
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

    private float cardWidth = 71;
    private float cardHeight = 96;
    private float border = 10;

    private Vector2 GetCardEmptyPoint(int index) {
        //计算空牌位坐标
        int cardGap = (int) ((Constant.worldWidth - cardWidth * 10) / 11);
        //空牌位位置
        float x = (int) (cardGap + index * (cardWidth + cardGap));
        float y = (int) border;
        return new Vector2(x,y);
    }

    public void faPai() {
        corner.Do(pocker,cardGroup);
        setPos();
    }


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
//        for (int i = 0; i < pocker.getDesk().size; ++i) {
//            for (int j = 0; j < pocker.getDesk().get(i).size; ++j) {
//                Card card = pocker.getDesk().get(i).get(j);
//                Actor hit = card.hit(pt.x, pt.y, true);
//                if (hit!=null){
//                    clickPocker.setI(i);
//                    clickPocker.setJ(j);
//                }
//            }
//        }
    }

    public void NewGame(boolean isRandom){

    }

    void NewGameSolved(){}

    boolean Move(Pocker poker,int orig,int dest,int num) {
//
//        cout << "--move--" << endl << "input orig, dest, num: "; in >> orig >> dest >> num;
//        cout << endl;
//        cout << "Chose: "; poker->printCard(orig, num);
//        cout << endl;
//        cout << "canMove? ";
//#else
//        in >> orig >> dest >> num;
//#endif
        PMove action = new PMove(orig, dest, num);
        boolean success = false;
        if (success = action.Do(poker)) {
            record.add(action);
        }

//#ifndef _CONSOLE
//        if (success && poker->hasGUI)
//        {
//            if (config.enableAnimation)
//                action->StartAnimationQuick(hWnd, bOnThread, bStopThread);
//        }
//#else
//        cout << (success ? "success." : "failed.") << endl;
//        cout << *poker;
//#endif
        return false;
    }

    boolean GetIsWon(){
        return false;
    }

    boolean ShowOneHint(){
        return false;
    }

    boolean OnLButtonDown(Vector2 pt) {
        return false;
    }
    //播放胜利音乐，并开一个线程刷新烟花动画
    public void Win(){

    }

    //返回给定点是否位于发牌区
    public boolean PtInRelease(Vector2 pt){
        return false;
    }

    public void setSoundId() {

    }

    public void setGuiProperty(String idCardEmpty, String idCardBack,
                               String idCard1, String idCardMask) {
        this.idCardEmpty = idCardEmpty;
        this.idCardBack = idCardBack;
        this.idCard1 = idCard1;
        this.idCardMask = idCardMask;
        //创建空牌位
        vecImageEmpty = new Array<Image>();
        for (int i = 0; i < 10; i++) {
            vecImageEmpty.add(new Image());
        }
        hasLoadImage = true;
    }
}
