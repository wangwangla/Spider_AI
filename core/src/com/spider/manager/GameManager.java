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
import com.spider.bean.AutoSolveResult;
import com.spider.bean.DragInfo;
import com.spider.bean.Node;
import com.spider.card.Card;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.pMove.PMove;
import com.spider.pocker.Pocker;
import java.util.Comparator;
import java.util.HashSet;

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
    private AutoSolveResult autoSolveResult;
    private float cardWidth = 71;
    private float cardHeight = 96;
    private float border = 10;
    private Vector2 origionTouchDownVector;

    public GameManager(Group cardGroup, Group finishGroup, Group sendCardGroup){
        //记录
        this.record = new Array<Action>();
        //结束
        this.autoSolveResult = new AutoSolveResult();
        //
        this.dragInfo = new DragInfo();
        this.pMove = new PMove();
        this.corner = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
        this.corner.setUpdateGroup(true);
        this.origionTouchDownVector = new Vector2();
        this.cardGroup = cardGroup;
        this.finishGroup = finishGroup;
        this.sendCardGroup = sendCardGroup;
    }

    public void newGame(int suitNum){
        this.record.clear();
        this.pocker = new Pocker();
        DealPocker action = new DealPocker(suitNum);
        action.doAction(pocker);
        initialImage();
        //发牌
        action.initPos(sendCardGroup,cardGroup);
//        action.startAnimation();
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
        updateZIndex();
    }

    public void updateZIndex(){
        int zIndex=0;
        for (Array<Card> array : pocker.getDesk()) {
            for (Card card : array) {
                card.setZIndex(10+zIndex++);
            }
        }
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



    public boolean touchDown(Actor target,float x,float y) {
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
        //恢复位置并刷新
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
            for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
                arrayMap.getKeyAt(0).setZIndex(0);
            }
            dragInfo.getVecCard().clear();
            //有目标牌位，且可以移动
            Move(pocker,dragInfo.getOrig(),dest,dragInfo.getNum());
        }
        updateZIndex();
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
        corner.doAction(pocker);
        record.add(corner);
        corner.startAnimation();
    }

    public void recod() {
        if (record.size<=0)return;
        Array<Action> record = this.record;
        Action action = record.removeIndex(record.size - 1);
        action.setGroup(cardGroup);
        action.redo(pocker);
        action.redoAnimation();
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
    }

    private boolean Move(final Pocker poker,int orig,final int dest,int num) {
        if (orig!=dest && dest!=-1) {
            if (canMove(orig,dest,num)) {
                PMove action = new PMove(orig, dest, num,finishGroup,cardGroup);
                action.setUpdateGroup(true);
                if (action.doAction(poker)) {
                    record.add(action);
                }
                action.startAnimation();
            }else {
                PMove action = new PMove(orig, orig, num,finishGroup,cardGroup);
                action.setUpdateGroup(true);
                action.setPocker(poker);
                action.startAnimation();
            }
        }else {
            PMove action = new PMove(orig, orig, num,finishGroup,cardGroup);
            action.setUpdateGroup(true);
            action.setPocker(poker);
            action.startAnimation();
        }
        return false;
    }

    private boolean canMove(int orig, int dest, int num) {
        Array<Card> cards = pocker.getDesk().get(orig);
        if (cards.size<=num)return false;
        Card card = cards.get(cards.size - num);
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
            return false;
        }
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



    public void setGuiProperty() {
        //创建空牌位
        float v1 = (Constant.worldWidth - 71 * 10) / 11.0F;
        vecImageEmpty = new Array<Image>();
        for (int i = 0; i < 10; i++) {
            Image image = new Image(SpiderGame.getAssetUtil().loadTexture("Resource/cardempty.png"));
            image.setScale(0.01f);
            vecImageEmpty.add(image);
            cardGroup.addActor(image);
            image.setX(v1*(i+1)+71 * i);
        }
    }

    public boolean AutoSolve1() {
        HashSet<Pocker> states = new HashSet<Pocker>();
        autoSolveResult.setCalc(0);
        autoSolveResult.setSuccess(false);
        autoSolveResult.setSuit(pocker.getSuitNum());
        autoSolveResult.setSeed(pocker.getSeed());
        int calcLimited = 100000;
        int stackLimited = 400;

        DFS(autoSolveResult.getCalc(),
                record,
                states,
                stackLimited,
                calcLimited,false);
        //输出计算量
        if (autoSolveResult.isSuccess() == true) {
            //输出步骤
            NLog.e("Finished. Step = "+record.size);
            for (int i = 0; i < record.size; ++i)
                NLog.e("[" + i + "] " + record.get(i));
        } else {
            //输出失败原因
            if (autoSolveResult.getCalc() >= calcLimited) {
                NLog.e("Calculation number >= %s" ,calcLimited);
            } else {
                NLog.e("Call-stack depth >= %s", stackLimited);
            }
            NLog.e("Fail.");
        }
        return autoSolveResult.isSuccess();
    }

    private Array<Node> GetAllOperator(Array<Integer> emptyIndex,Pocker poker, HashSet<Pocker> states) {
        Array<Node> actions = new Array<Node>();
        for (int dest = 0; dest < poker.getDesk().size; ++dest) {
            Array<Card> destCards = poker.getDesk().get(dest);
            if (destCards.size<=0) {
                emptyIndex.add(dest);
                //当前牌堆为空
                //遍历所有牌堆，把非空的加进来
                for (int orig = 0; orig < poker.getDesk().size; ++orig) {
                    Array<Card> cards = poker.getDesk().get(orig);
                    if (cards.size<=0)
                        continue;
                    //得到可移动牌数量
                    //至少会返回1
                    int num = 1;
                    while (true) {
                        if (num > cards.size) {
                            num--;
                            break;
                        }
                        if (pMove.canPick(poker, orig, num)) {
                            num++;
                        } else {
                            num--;
                            break;
                        }
                    }

                    //全部移到空位没有意义
                    if (num == cards.size)
                        continue;
                    Pocker newPoker = new Pocker(poker);
                    Action action = new PMove(orig, dest, num,null,null);
                    action.doAction(newPoker);
//                    if (newPoker == states.getKeyAt(states.size-1)) {
                    if (!compare(states,newPoker)) {
                        actions.add(new Node(newPoker.GetValue(), newPoker, action));
                    }
                }
            } else {//dest牌堆非空
                //最面上的牌
                Card pCardDest = destCards.get(destCards.size-1);
                //逐个牌堆遍历
                for (int orig = 0; orig < poker.getDesk().size; ++orig) {
                    Array<Card> origCards = poker.getDesk().get(orig);
                    if (origCards.size<=0)
                        continue;
                    if (origCards.get(origCards.size-1).getPoint()
                            >= pCardDest.getPoint())
                        continue;
                    int num = 0;
                    //从最底下的牌遍历到最顶上
                    for (int i = origCards.size-1; i >= 0; i--) {
                        num++;
                        Card it = origCards.get(i);
                        //点数不符合，不能移动
                        if (it.getPoint() >= pCardDest.getPoint()) {
                            break;
                        }
                        //没有显示的牌，不能移动
                        if (it.isShow() == false)
                            break;

                        //不是倒数第1个
                        if (it != origCards.get(origCards.size-1)) {
                            Card itDown = origCards.get(i+1);//上一张牌
                            if (itDown.getPoint() + 1 != it.getPoint())//不连续则跳出，不能移动
                                break;
                        }

                        //it ----> pCard，目标pCard比源it大1
                        //不考虑花色，花色留给估值函数计算
                        if (it.getPoint() + 1 == pCardDest.getPoint())//it->suit == pCard->suit &&
                        {
                            Pocker tempPoker = new Pocker(poker);
                            Action action = new PMove(orig, dest, num,null,null);

//                            boolean b = tempList.size() > 0 && tempPoker == tempList.get(tempList.size() - 1);
//                            boolean b1 = stateLast(tempList, tempPoker);
                            if (action.doAction(tempPoker) &&!compare(states,tempPoker))
                            actions.add(new Node(tempPoker.GetValue(),tempPoker,action));
                            break;
                        }
                    }
                }
            }
        }

        //没有空位，且待发区还有牌
        //加入发牌操作
        if (emptyIndex.size<=0 && !(poker.getCorner().size<=0)){
            Pocker newPoker = new Pocker(poker);
            ReleaseCorner action = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
            action.doAction(newPoker);
            actions.add(new Node( poker.GetValue() - 100,newPoker,action ));
        }
        return actions;
    }


    private  int xx = 0;

    /**
     * @param calc 计算次数
     * @param record 历史记录
     * @param states 状态
     * @param stackLimited 深度限制
     * @param calcLimited 计算总数限制
     * @param playAnimation 是否有动画
     * @return
     */
    boolean DFS(int calc,
                Array<Action> record,
                HashSet<Pocker> states,
                int stackLimited,
                int calcLimited,
                boolean playAnimation) {
        if (pocker.isFinished()) {
            autoSolveResult.setSuccess(true);
            return true;
        }
        //操作次数超出限制，计算量超出限制
        if (calc >= calcLimited) {
            return false;
        }
        if (pocker.getOperation() >= stackLimited) {
            return false;
        }
        calc++;
        Array<Integer> emptyIndex = new Array<Integer>();
        //new
        Pocker tempPoker = new Pocker(pocker);
        Array<Node> actions = GetAllOperator(emptyIndex, tempPoker, states);
        //优化操作
        if (emptyIndex.size<=0) {
            //没有空位
            //去掉比当前评分还低的移牌
            Array<Node> array = new Array<Node>();
            for (Node action : actions) {
                if (action.getAction() instanceof PMove && action.getValue() <= pocker.GetValue()) {
                    array.add(action);
                }
            }
            for (Node node : array) {
                actions.removeValue(node,true);
            }
        } else {
            //有空位
            //如果待发区还有牌，则移牌到空位补空，因为有空位不能发牌
            if (!(pocker.getCorner().size<=0)) {
                //如果全是顺牌，则找一张最小的移过去
                boolean AllIsOrdered = true;
                Array<Array<Card>> desk = pocker.getDesk();
                for (int i = 0; i < desk.size; i++) {
                    for (int i1 = 1; i1 < desk.get(i).size; i1++) {
                        if (desk.get(i).get(i1-1).getSuit() !=
                                desk.get(i).get(i1).getSuit() ||
                                desk.get(i).get(i1 - 1).getPoint() - 1
                                        != desk.get(i).get(i1).getPoint())
                        {
                            AllIsOrdered = false;
                            break;
                        }

                    }
                }

                if (AllIsOrdered) {
                    //清空当前所有操作
                    ReleaseActions(actions);
                    int orig = 0;
                    int minPoint = 14;
                    //寻找最小的牌
                    for (int i = 0; i < pocker.getDesk().size; ++i) {
                        Array<Card> cards = pocker.getDesk().get(i);
                        if (cards.size > 1 && cards.get(cards.size-1).getPoint() < minPoint)//牌数>=2，把上面的挪到旁边去
                        {
                            minPoint = cards.get(cards.size-1).getPoint();
                            orig = i;
                        }
                    }

                    if (minPoint == 14) {//说明总牌数小于10张，强行发牌
                        Pocker newPoker = new Pocker(pocker);
                        ReleaseCorner action = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
//                        shared_ptr<Action> action(new ReleaseCorner(config.enableSound, soundDeal));
                        action.doAction(newPoker);
                        actions.add(new Node(pocker.GetValue() - 100,newPoker,action));
                    }
                    else
                    {
                        //只添加一个移牌补空位的操作
                        Action action = new PMove(orig, emptyIndex.get(0), 1,null,null);
                        Pocker newPoker = new Pocker(pocker);
                        action.doAction(newPoker);
                        actions.add(new Node(newPoker.GetValue(),newPoker,action));
                    }
                }
            }
        }

        actions.sort(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o2.getValue()-o1.getValue();
            }
        });
        //按照评估分大到小排序
//        sort(actions.begin(), actions.end(), [](const Node& n1, const Node& n2) {return n1.value > n2.value; });
        Array<Node> array = new Array<Node>();
        //开始递归
        for (final Node it : actions) {
            //没出现过的状态
            if (!compare(states,it.getPoker())) {
                it.getAction().setUpdateGroup(true);
                if (it.getAction() instanceof ReleaseCorner){
                    it.getAction().doAction(pocker);
                }else {
                    it.getAction().doAction(pocker);
                }
                xx++;

                if (xx == 160)
//                it.getAction().startAnimation();
                setPos();
                //加入状态
                states.add(it.getPoker());
                //push记录
                record.add(it.getAction());

                if (DFS(calc, record, states, stackLimited, calcLimited, playAnimation)) {
                    //只有终止才会返回true，如果任意位置返回true，此处将逐级终止递归
                    ReleaseActions(actions);
                    return true;
                }

                it.getAction().redo(pocker);
//                it.getAction().redoAnimation();
                setPos();
                it.getAction().setUpdateGroup(false);
                if (pocker.isHasGUI()) {
                    if (playAnimation) {

                    } else {

                    }
                }
                //pop记录
                record.removeIndex(record.size - 1);
            } else{//已出现过的状态
                array.add(it);
		    }
        }
        for (Node node : array) {
            actions.removeValue(node,true);
        }
        ReleaseActions(actions);
        return false;
    }

    private void ReleaseActions(Array<Node> actions) {
        actions.clear();
    };

    public boolean compare(HashSet<Pocker> arrayMap,Pocker pocker){

        return arrayMap.contains(pocker);
    }
}
