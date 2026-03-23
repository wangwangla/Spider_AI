# 纸牌自定解题

Libgdx spider自动解题


修正发牌堆出牌顺序：D:\work\addAi\Spider_AI\core\src\com\spider\cardAction\ReleaseCorner.java 现在从 corner.first() 取牌并 
removeIndex(0)，撤销时用 insert(0, …) 放回，确保发牌使用生成牌堆的正确顶部顺序（之前从末尾取导致发牌后点数错乱、无法出牌）。
与顺序一致地更新了撤销动画：取第一个发牌包并移动到起始位置。
后续可验证：

启动游戏新开一局，连续点击发牌 5 次，确认每次发出的 10 张牌点数顺序正常、可继续出牌。

## 下一步加入其他卡牌的玩法

……