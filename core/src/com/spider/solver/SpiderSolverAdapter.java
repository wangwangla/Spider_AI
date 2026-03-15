package com.spider.solver;

import com.spider.log.NLog;
import com.spider.pocker.Pocker;
import com.solvitaire.app.SolverMain;

/**
 * 将 SolverCard 的 Spider 求解器封装为游戏可调用的工具。
 */
public class SpiderSolverAdapter {

    /** 返回求解的移动编码序列（SolverMain 格式）。 */
    public static int[] solveMoves(Pocker pocker) {
        if (pocker == null || pocker.getDealString() == null) {
            NLog.e("solve failed: dealString missing");
            return new int[0];
        }
        return SolverMain.solveDealText(pocker.getDealString());
    }
}
