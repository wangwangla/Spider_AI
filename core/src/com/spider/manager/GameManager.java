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
        this.record = new Array<Action>();
        this.autoSolveResult = new AutoSolveResult();
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
        //?????????????????????
        for (Array<Card> cards : pocker.getDesk()) {
            for (Card card : cards) {
                card.initCard();
                cardGroup.addActor(card);
            }
        }
        //?????????????????????
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
        //????????????????????????
        GetIndexFromPoint(target);
        //?????????
        if (clickPocker.i == -1)
            return false;
        origionTouchDownVector.set(x, y);
        target.stageToLocalCoordinates(origionTouchDownVector);
        int num = pocker.getDesk().get(clickPocker.i).size - clickPocker.j;
        //???????????????
        if (!pMove.canPick(pocker, clickPocker.i, num))
            return false;
        //??????????????????
        dragInfo.getVecCard().clear();
        for (int i = 0; i < num; ++i) {
            //???????????????z-index?????????????????????????????????
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
        //??????z-index
        for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
            arrayMap.getKeyAt(0).setZIndex(0);
        }
        dragInfo.getVecCard().clear();
        //?????????????????????
    }

    public boolean OnMouseMove(Vector2 pt) {
        if (dragInfo.isbOnDrag()) {
            //?????????????????????????????????
            //??????
            //???????????????
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
            //????????????????????????????????????
            Vector2 ptUpCard = dragInfo.getVecCard().get(0).getKeyAt(0).getPosition();
            //?????????????????????
            int dest = GetDestIndex(pocker, ptUpCard, dragInfo.getOrig(), dragInfo.getNum());
            //??????????????????
            dragInfo.setbOnDrag(false);
            for (ArrayMap<Card, Vector2> arrayMap : dragInfo.getVecCard()) {
                arrayMap.getKeyAt(0).setZIndex(0);
            }
            dragInfo.getVecCard().clear();
            //?????????????????????????????????
            Move(pocker,dragInfo.getOrig(),dest,dragInfo.getNum());
        }
        updateZIndex();
        return false;
    }


    public int GetDestIndex(Pocker pocker, Vector2 ptUpCard, int orig, int num){
        int dest = -1;
        float Smax = -1;
        for (int i = 0; i < 10; ++i) {
            if (i == orig)//??????????????????????????????????????????????????????????????????????????????????????????
                continue;
            Vector2 ptDest;//????????????
            if (pocker.getDesk().get(i).size <= 0)
                ptDest = GetCardEmptyPoint(i);
            else {
                Array<Card> array = pocker.getDesk().get(i);
                ptDest = array.get(array.size - 1).getPosition();
            }
            float dx = Math.abs(ptUpCard.x - ptDest.x);//????????????
            float dy = Math.abs(ptUpCard.y - ptDest.y);
            float S = (cardWidth - dx) * (cardHeight - dy);//?????????????????????????????????
            //????????????????????????????????????????????????????????????
            if (cardWidth - dx > 0 && S > Smax && pMove.canMove(pocker, orig, i, num)) {
                //???????????????????????????????????????????????????????????????????????????????????????????????????
                Smax = S;
                dest = i;
            }
        }
        return dest;
    }

    private Vector2 GetCardEmptyPoint(int index) {
        //?????????????????????
        int cardGap = (int) ((Constant.worldWidth - cardWidth * 10) / 11);
        //???????????????
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
    //?????????????????????????????????????????????????????????
    public void Win(){

    }

    //????????????????????????????????????
    public boolean PtInRelease(Vector2 pt){
        return false;
    }



    public void setGuiProperty() {
        //???????????????
        float v1 = (Constant.worldWidth - 71 * 10) / 11.0F;
        vecImageEmpty = new Array<Image>();
        for (int i = 0; i < 10; i++) {
            Image image = new Image(SpiderGame.getAssetUtil().loadTexture("Resource/cardempty.png"));
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
        //1????????????200????????????83/100??????500????????????90/100??????1000????????????92/100??????2000????????????93/100??????8000????????????98/100???
        //2????????????2000????????????23/100??????8000????????????32/100??????100000????????????47/100????????????100min
        //4????????????100000??????0/100????????????132min
        int calcLimited = 100000;
        //480?????????????????????????????????480?????????????????????????????????
        int stackLimited = 400;
        DFS(autoSolveResult.getCalc(),
                record,
                states,
                stackLimited,
                calcLimited,false);
        //???????????????
        if (autoSolveResult.isSuccess() == true) {
            //????????????
            NLog.e("Finished. Step = "+record.size);
            for (int i = 0; i < record.size; ++i)
                NLog.e("[" + i + "] " + record.get(i));
        } else {
            //??????????????????
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
                //??????????????????
                //??????????????????????????????????????????
                for (int orig = 0; orig < poker.getDesk().size; ++orig) {
                    Array<Card> cards = poker.getDesk().get(orig);
                    if (cards.size<=0)
                        continue;
                    //????????????????????????
                    //???????????????1
                    int num = 1;
                    while (true) {
                        if (num > cards.size) {
                            num--;
                            break;
                        }
                        if (pMove.canPick(poker, orig, num))
                        {
                            num++;
                        }
					else
                        {
                            num--;
                            break;
                        }
                    }

                    //??????????????????????????????
                    if (num == cards.size)
                        continue;
                    Pocker newPoker = new Pocker(poker);
                    Action action = new PMove(orig, dest, num,finishGroup,cardGroup);
                    action.doAction(newPoker);
//                    if (newPoker == states.getKeyAt(states.size-1)) {

                    if (!compare(states,newPoker)) {
                        actions.add(new Node(newPoker.GetValue(), newPoker, action));
                    }
                }
            } else//dest????????????
            {
                //???????????????
                Card pCardDest = destCards.get(destCards.size-1);
                //??????????????????
                for (int orig = 0; orig < poker.getDesk().size; ++orig) {
                    Array<Card> origCards = poker.getDesk().get(orig);
                    if (origCards.size<=0)
                        continue;
                    if (origCards.get(origCards.size-1).getPoint()
                            >= pCardDest.getPoint())
                        continue;
                    int num = 0;
                    //????????????????????????????????????
                    for (int i = origCards.size-1; i >= 0; i--) {
                        num++;
                        Card it = origCards.get(i);
                        //??????????????????????????????
                        if (it.getPoint() >= pCardDest.getPoint()) {
                            break;
                        }
                        //?????????????????????????????????
                        if (it.isShow() == false)
                            break;

                        //???????????????1???
                        if (it != origCards.get(origCards.size-1)) {
                            Card itDown = origCards.get(i+1);//????????????
                            if (itDown.getPoint() + 1 != it.getPoint())//?????????????????????????????????
                                break;
                        }

                        //it ----> pCard?????????pCard??????it???1
                        //????????????????????????????????????????????????
                        if (it.getPoint() + 1 == pCardDest.getPoint())//it->suit == pCard->suit &&
                        {
                            Pocker tempPoker = new Pocker(poker);
                            Action action = new PMove(orig, dest, num,finishGroup,cardGroup);

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

        //????????????????????????????????????
        //??????????????????
        if (emptyIndex.size<=0 && !(poker.getCorner().size<=0)){
            Pocker newPoker = new Pocker(poker);
            ReleaseCorner action = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
            action.doAction(newPoker);
            actions.add(new Node( poker.GetValue() - 100,newPoker,action ));
        }
        return actions;
    }


    private  int xx = 0;
    boolean DFS(int calc, Array<Action> record,
                      HashSet<Pocker> states, int stackLimited, int calcLimited,
                        boolean playAnimation) {

        if (pocker.isFinished()) {
            autoSolveResult.setSuccess(true);
            return true;
        }
        //????????????????????????????????????????????????
        if (calc >= calcLimited) {
            return true;
        }
        if (pocker.getOperation() >= stackLimited) {
            return false;
        }
        calc++;
        Array<Integer> emptyIndex = new Array<Integer>();
        Pocker tempPoker = new Pocker(pocker);
        Array<Node> actions = GetAllOperator(emptyIndex, tempPoker, states);
        //????????????
        if (emptyIndex.size<=0) {
            //????????????
            //????????????????????????????????????
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
            //?????????
            //?????????????????????????????????????????????????????????????????????????????????
            if (!(pocker.getCorner().size<=0)) {
                //???????????????????????????????????????????????????
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
                    //????????????????????????
                    ReleaseActions(actions);
                    int orig = 0;
                    int minPoint = 14;
                    //??????????????????
                    for (int i = 0; i < pocker.getDesk().size; ++i) {
                        Array<Card> cards = pocker.getDesk().get(i);
                        if (cards.size > 1 && cards.get(cards.size-1).getPoint() < minPoint)//??????>=2??????????????????????????????
                        {
                            minPoint = cards.get(cards.size-1).getPoint();
                            orig = i;
                        }
                    }

                    if (minPoint == 14) {//?????????????????????10??????????????????
                        Pocker newPoker = new Pocker(pocker);
                        ReleaseCorner action = new ReleaseCorner(sendCardGroup,cardGroup,finishGroup);
//                        shared_ptr<Action> action(new ReleaseCorner(config.enableSound, soundDeal));
                        action.doAction(newPoker);
                        actions.add(new Node(pocker.GetValue() - 100,newPoker,action));
                    }
                    else
                    {
                        //???????????????????????????????????????
                        Action action = new PMove(orig, emptyIndex.get(0), 1,finishGroup,cardGroup);
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
        //??????????????????????????????
//        sort(actions.begin(), actions.end(), [](const Node& n1, const Node& n2) {return n1.value > n2.value; });
        Array<Node> array = new Array<Node>();
        //????????????
        for (final Node it : actions) {
            //?????????????????????
            if (!compare(states,it.getPoker())) {
                it.getAction().setUpdateGroup(true);
                if (it.getAction() instanceof ReleaseCorner){
                    it.getAction().doAction(pocker);
                }else {
                    it.getAction().doAction(pocker);
                }
                xx++;
                System.out.println(xx);
                if (xx == 160)
                NLog.e("do ------------------ "+it.getAction());
//                it.getAction().startAnimation();
                setPos();
                //????????????
                states.add(it.getPoker());
                //push??????
                record.add(it.getAction());

                if (DFS(calc, record, states, stackLimited, calcLimited, playAnimation)) {
                    //????????????????????????true???????????????????????????true??????????????????????????????
                    ReleaseActions(actions);
                    return true;
                }

                it.getAction().redo(pocker);
//                it.getAction().redoAnimation();
                setPos();
                NLog.e("redo ------------------ "+it.getAction());
                it.getAction().setUpdateGroup(false);
                if (pocker.isHasGUI()) {
                    if (playAnimation) {

                    } else {

                    }
                }
                //pop??????
                record.removeIndex(record.size - 1);
            } else{//?????????????????????
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
