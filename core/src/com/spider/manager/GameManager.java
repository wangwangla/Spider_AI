package com.spider.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.spider.action.Action;
import com.spider.action.Deal;
import com.spider.card.Card;
import com.spider.config.Configuration;
import com.spider.pocker.Pocker;

public class GameManager {
    private Pocker pocker;
    private Array<Array<Action>> record;
    private Array<Image> vecImageEmpty;
    private boolean idCardEmpty;
    private boolean idCardBack;
    private boolean idCard1;
    private boolean idCardMask;
    private Configuration config;


    public void newGame(boolean isRandom,int suitNum){
        int seed = (int) System.currentTimeMillis();
        if (isRandom){
            suitNum = 1;
            seed = (int) (Math.random() * 100);
        }else {
            //使用传进来的值
        }
        record.clear();
        pocker = new Pocker();
        Action action = new Deal(suitNum,seed,false,1);
        action.Do(pocker);

        if (idCardEmpty && idCardBack && idCard1 && idCardMask){
            initialImage();
        }
        if (pocker.isHasGUI()){
            if (config.isEnableAnimation())
                action.startAnimation(bOnThread, bStopThread);
            else
            {
                RefreshPaint();
                bOnThread = false;
            }
        }
        }
    }


    void initialImage() {
        //每张牌加入图片
        for (Array<Card> cards : pocker.getDesk()) {
            for (Card card : cards) {
                int imageIndex = (card.getSuit() - 1) * 13 + card.getPoint() - 1;
                Image imgCard = new Image(idCard1 + imageIndex, idCardMask));
                Image imgCardBack = new Image(idCardBack, idCardMask));
                card.setImage(imgCard, imgCardBack);
            }
        }

        //角落牌加入图片
        for (Array<Card> cards : poker.getCorner()) {
            for (Card card : cards) {
                int imageIndex = (card.getSuit() - 1) * 13 + card.getPoint() - 1;
                Image imgCard = new TImage(GetModuleHandle(NULL), idCard1 + imageIndex, idCardMask);
                Image imgCardBack = new TImage(GetModuleHandle(NULL), idCardBack, idCardMask);
                card.setImage(imgCard, imgCardBack);
            }
        }
        pocker.setHasGUI(true);
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

    public void setGuiProperty() {
        //创建空牌位
        vecImageEmpty = new Array<Image>();
        for (int i = 0; i < 10; i++) {
            vecImageEmpty.add(new Image());
        }
    }
}
