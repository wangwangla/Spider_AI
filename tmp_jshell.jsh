/env -class-path core/build/classes/java/main
import com.solvitaire.app.DealShuffler;
import com.solvitaire.app.SpiderSolverService;
import com.solvitaire.app.SpiderSolveResult;
import java.util.*;
int cols=10;int[] deck=DealShuffler.shuffleSpiderDeck(12345L,1);
List<List<Integer>> stacks=new ArrayList<>();
int idx=0;
for(int c=0;c<cols;c++){
  int cards=c<4?6:5;
  List<Integer> s=new ArrayList<>();
  for(int i=0;i<cards;i++) s.add(deck[idx++]);
  stacks.add(s);
}
Deque<Integer> stock=new ArrayDeque<>();
while(idx<deck.length) stock.add(deck[idx++]);
int[] faceDown=new int[cols];int[] totals=new int[cols];int max=0;
for(int c=0;c<cols;c++){totals[c]=stacks.get(c).size();faceDown[c]=totals[c]-1;max=Math.max(max, totals[c]);}
StringBuilder sb=new StringBuilder();
sb.append("Spider,").append(stock.size());
for(int c=0;c<cols;c++) sb.append(":"+(faceDown[c]*100+totals[c]));
sb.append("\n");
for(int row=0; row<max; row++){
  for(int col=0; col<cols; col++){
    List<Integer> st=stacks.get(col); String code="";
    if(row < st.size()){
      int codeInt=st.get(row);int rank=codeInt%100;int suit=codeInt/100;String r=(rank==1?"a":rank==10?"10":rank==11?"j":rank==12?"q":rank==13?"k":String.valueOf(rank));char s="0shdc".charAt(suit);code=r+""+s;}
    sb.append(code).append(",");
  }
  sb.append("\n");
}
List<Integer> stockList=new ArrayList<>(stock);
for(int i=0;i<50;i++){
  String token = i<stockList.size()? ( (()->{int codeInt=stockList.get(i);int rank=codeInt%100;int suit=codeInt/100;String r=(rank==1?"a":rank==10?"10":rank==11?"j":rank==12?"q":rank==13?"k":String.valueOf(rank));char s="0shdc".charAt(suit);return r+""+s;}).get() ) : "";
  sb.append(token).append(",");
}
sb.append("\n");
for(int i=0;i<8;i++){sb.append(",");}
String board=sb.toString();
System.out.println(board.substring(0, Math.min(board.length(), 200)));
SpiderSolverService svc=new SpiderSolverService();
SpiderSolveResult res=svc.solveBoard(board);
System.out.println(res.isSolved()+" steps="+res.getSteps().size()+" summary="+res.getSummary());
/exit
