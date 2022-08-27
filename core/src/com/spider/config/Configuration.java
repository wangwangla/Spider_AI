package com.spider.config;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class Configuration {
    class Record {
        float time;
        int seed;
        int highScore;
        char prefixDiff;
        int diff;
        boolean solved;

        Record(float t, int s, int hs, char pre, int diff, boolean sv) {
            this.time = t;
            this.seed = s;
            this.highScore = hs;
            this.prefixDiff = pre;
            this.diff = diff;
            this.solved = sv;
        }

        void SaveToFile(FileHandle fp) {

        }

        void ReadFromFile(FileHandle fp) {

        }
    }
    private boolean enableAnimation;
    private boolean enableSound;

    public boolean isEnableAnimation() {
        return enableAnimation;
    }

    public void setEnableAnimation(boolean enableAnimation) {
        this.enableAnimation = enableAnimation;
    }

    public boolean isEnableSound() {
        return enableSound;
    }

    public void setEnableSound(boolean enableSound) {
        this.enableSound = enableSound;
    }

    public Array<Array<Record>> getRecord() {
        return record;
    }

    public void setRecord(Array<Array<Record>> record) {
        this.record = record;
    }

    public Array<HashMap<Integer, Record>> getSeedMap() {
        return seedMap;
    }

    public void setSeedMap(Array<HashMap<Integer, Record>> seedMap) {
        this.seedMap = seedMap;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public int getPrefixDiff() {
        return prefixDiff;
    }

    public void setPrefixDiff(int prefixDiff) {
        this.prefixDiff = prefixDiff;
    }

    public float getHighScore() {
        return highScore;
    }

    public void setHighScore(float highScore) {
        this.highScore = highScore;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    //    using RecordType = std::vector<std::shared_ptr<Record>>;
    Array<Array<Record>> record;
//    using RecordMap = std::unordered_map<unsigned int, std::shared_ptr<Record>>;
//    std::vector<RecordMap> seedMap;
    Array<HashMap<Integer,Record>> seedMap;
    public Configuration(){
        this.enableAnimation = true;
        this.enableSound = true;
        record = new Array<Array<Record>>();
        seedMap = new Array<HashMap<Integer, Record>>();
    }

    public boolean readFromFile(String fileName){
        return false;
    }



    void UpdateRecord(int suitNum,int seed, int highScore){
        UpdateRecord(suitNum,seed,highScore,false,0,false);
    }



    private long time;
    private long seed;
    private int diff;
    private int prefixDiff;
    private float highScore;
    private boolean solved;

    public String time_tToString(long t) {
        char[] temp = new char[128];
//        String.format(temp, 128, "%Y年%m月%d日 %T",);
//        return string(temp);
        return "y m r";
    }

    public Array<String> ToVecString() {
        Array<String> ret = new Array<String>();
        ret.add(time_tToString(time));
        ret.add(seed+"");
        ret.add((diff == 0) ? "未评估" : (prefixDiff==0 ? ""+(prefixDiff + diff) : ""+diff));
        ret.add(""+highScore);
        ret.add(solved ? "已解决" : "未解决");
        return ret;
    }


/*
    public void SaveToFile(FileHandle fp) {
//        fwrite(&time, sizeof(time), 1, fp);
//        fwrite(&seed, sizeof(seed), 1, fp);
//        fwrite(&highScore, sizeof(highScore), 1, fp);
//        fwrite(&prefixDiff, sizeof(prefixDiff), 1, fp);
//        fwrite(&diff, sizeof(diff), 1, fp);
//        fwrite(&solved, sizeof(solved), 1, fp);
//
    }
*/

    void ReadFromFile(FileHandle fp){
//        fread(&time, sizeof(time), 1, fp);
//        fread(&seed, sizeof(seed), 1, fp);
//        fread(&highScore, sizeof(highScore), 1, fp);
//        fread(&prefixDiff, sizeof(prefixDiff), 1, fp);
//        fread(&diff, sizeof(diff), 1, fp);
//        fread(&solved, sizeof(solved), 1, fp);
    }

//    boolean ReadFromFile(String fileName) {
////        FILE* fp = fopen(fileName.c_str(), "rb");
////        if (fp)
////        {
////            fread(&enableAnimation, 1, 1, fp);
////            fread(&enableSound, 1, 1, fp);
////
////            ReadRecord(fp);
////
////            fclose(fp);
////            return true;
////        }
//        return false;
//    }

    boolean SaveToFile(String fileName) {
//        FILE* fp = fopen(fileName.c_str(), "wb");
//        if (fp)
//        {
//            fwrite(&enableAnimation, 1, 1, fp);
//            fwrite(&enableSound, 1, 1, fp);
//
//            SaveRecord(fp);
//
//            fclose(fp);
//            return true;
//        }
        return false;
    }

//    void ReadRecord(FILE* fp)
//    {
//
//        for (int i = 0; i < 3; ++i)
//        {
//            int sz;
//            fread(&sz, sizeof(sz), 1, fp);
//
//            for (int j = 0; j < sz; ++j)
//            {
//                shared_ptr<Record> temp = make_shared<Record>();
//                temp->ReadFromFile(fp);
//                record[i].push_back(temp);
//                seedMap[i][temp->seed] = temp;
//            }
//        }
//    }

//    void Configuration::SaveRecord(FILE* fp)
//    {
//
//        for (int i = 0; i < 3; ++i)
//        {
//            int sz = record[i].size();
//            fwrite(&sz, sizeof(sz), 1, fp);
//
//            for (int j = 0; j < sz; ++j)
//            {
//                record[i][j]->SaveToFile(fp);
//            }
//        }
//    }

    void UpdateRecord(int suitNum, int seed, int highScore, boolean solved, int calc,boolean hasPrefix)
    {
        int index = -1;
        switch (suitNum)
        {
            case 1:index = 0; break;
            case 2:index = 1; break;
            case 4:index = 2; break;
        }

        Record it = seedMap.get(index).get(seed);
        if (it == seedMap.get(index).end())
        {
            record.get(index).add(new Record(0, seed, highScore, '0', calc, false));
            seedMap[index][seed] = record[index].back();
        }
        else
        {
            if (highScore > it->second->highScore)
            {
                it->second->time = time(0);
                it->second->highScore = highScore;
            }
            if (calc)
            {
                //之前已经有评估值，未解决，新评估值小于原评估值
                if (it->second->diff && solved == false && calc <= it->second->diff)
                    ;
                else
                {
                    //更新评估值
                    //it->second->time = time(0);
                    it->second->diff = calc;
                    it->second->prefixDiff = hasPrefix ? '>' : 0;
                }
            }
            if (solved)
            {
                it->second->time = time(0);
                it->second->solved = solved;
            }
        }
    }

    public void ClearRecord() {
        for (int i = 0; i < 3; ++i) {
            record.get(i).clear();
            seedMap.get(i).clear();
        }
    }
}