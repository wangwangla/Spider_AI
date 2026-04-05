package com.solvitaire.gdx;

import com.badlogic.gdx.Screen;
import com.kw.gdx.BaseBaseGame;

public class FreeCellBaseGame extends BaseBaseGame {

   @Override
   protected void loadingView() {
      super.loadingView();
      this.switchScreen(new FreeCellScreen(this));
   }

   public void showSpider() {
      this.switchScreen(new SpiderScreen(this));
   }

   private void switchScreen(Screen nextScreen) {
      Screen previous = this.getScreen();
      this.setScreen(nextScreen);
      if (previous != null) {
         previous.dispose();
      }
   }
}
