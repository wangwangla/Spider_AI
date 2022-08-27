package com.spider.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.action.Deal;
import com.spider.card.Card;
import com.spider.config.Configuration;
import com.spider.constant.Constant;
import com.spider.log.NLog;
import com.spider.pocker.Pocker;

public class GameManager {
    private Pocker pocker;
    private Array<Array<Action>> record = new Array<Array<Action>>();
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
//            cardGroup,finishGroup,sendCardGroup
    public GameManager(Group cardGroup, Group finishGroup, Group sendCardGroup){
        config = new Configuration();
        this.cardGroup = cardGroup;
        this.finishGroup = finishGroup;
        this.sendCardGroup = sendCardGroup;
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
                offSetY -= 10;
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
        if (canPick(poker, deskIndex, num))
        return false;

        //开始拖动设置

        dragInfo.vecCard.clear();
        for (int i = 0; i < num; ++i)
        {
            //拖动组设置z-index，加入牌指针及相对坐标
            auto& card = poker->desk[deskIndex][cardIndex + i];
            card.SetZIndex(999);
            dragInfo.vecCard.push_back({ &card,card.GetPos() - pt });
        }

        dragInfo.bOnDrag = true;
        dragInfo.orig = deskIndex;
        dragInfo.num = num;
        dragInfo.cardIndex = cardIndex;

        return true;
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
//    boolean Move(Pocker poker){
//
//    }
//    boolean CanPick(Poker* poker, std::istream& in);
//    void ReleaseRecord();
//
//    bool AutoSolve(bool playAnimation);
//
//#ifndef _CONSOLE
//    bool hasLoadImage;
//    HWND hWnd;
//	const RECT* pRcClient;
//    int idCardEmpty, idCardBack, idCard1,idCardMask;
//    std::vector<TImage*> vecImgCardEmpty;
//	const int border = 10;
//	const int xBorder = 15;//已完成牌堆的横向距离
//	const int cardWidth = 71;
//	const int cardHeight = 96;
//	const int cardGapH = 10;
//
//    struct DragInfo
//    {
//        bool bOnDrag;
//        int orig;
//        int cardIndex;
//        int num;
//        std::vector<std::pair<Card*, POINT>> vecCard;
//        DragInfo() :bOnDrag(false), orig(-1), cardIndex(-1), num(-1) {}
//    } dragInfo;
//    void GiveUpDrag();
//    POINT GetCardEmptyPoint(RECT rect, int index);
//    void GetIndexFromPoint(int& deskIndex, int& cardIndex, POINT pt);
//    void InitialImage();
//    void RefreshPaint();
//#else
//    void ShowHelpInfo() const;
//#endif
//    struct Node
//    {
//        int value;
//        std::shared_ptr<Poker> poker;
//        std::shared_ptr<Action> action;
//    };
//
//    //emptyIndex传入空数组即可，调用完成会将空牌位索引加入
//    //若某一操作后与states中已有的状态重合，则此操作不会加入actions
//    std::vector<Manager::Node> GetAllOperator(std::vector<int>& emptyIndex, std::shared_ptr<Poker> poker, const std::unordered_set<Poker>& states);
//    bool DFS(bool& success, int& calc, const std::string& origTitle, std::vector<std::shared_ptr<Action>>& record, std::unordered_set<Poker>& states, int stackLimited, int calcLimited, bool playAnimation);
//    public:
//    Manager();
//    Manager(int suitNum);
//    Manager(int suitNum, uint32_t seed);
//	~Manager();
//
//    int GetPokerSuitNum() { return poker->suitNum; };
//    int GetPokerOperation() { return poker->operation; };
//    int GetPokerScore() { return poker->score; };
//    uint32_t GetPokerSeed() { return poker->seed; };
//    bool HasPoker() { return poker; };
//    bool PokerCornerIsEmpty() { return poker->corner.empty(); }
//    //const Poker* GetPoker() { return poker; }
//
//    struct AutoSolveResult
//    {
//        bool success;
//        int calc;
//        int suit;
//        uint32_t seed;
//    };
//    AutoSolveResult autoSolveResult;
//
//    //命令：
//    //new suit seed
//    //newrandom suit
//    //auto 显示动画
//    bool Command(const std::string cmd);
//    bool ReadIn(std::istream& in);
//    bool CanRedo();
//
//    //
//    private boolean bOnThread;
//    private boolean bStopThread;
//    public void SetSoundId(int idTip,int idNoTip,int idWin,int idDeal){
//
//    }
//    public void SetTextOutputHWND(HWND hWnd);
//    void SetGUIProperty(HWND hWnd,const RECT *rcClient, int idCardEmpty, int idCardBack, int idCard1,int idCardMask);

    boolean GetIsWon(){
        return false;
    }

    boolean ShowOneHint(){
        return false;
    }

    boolean OnLButtonDown(Vector2 pt) {
        return false;
    }

    boolean OnLButtonUp(Vector2 pt){
        return false;
    }

    boolean OnMouseMove(Vector2 pt){
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
    }
}
