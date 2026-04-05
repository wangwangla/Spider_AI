package com.solvitaire.app;

import java.awt.Rectangle;

final class StackSnapshot {
   Rectangle captureBounds = new Rectangle();
   PixelSample[] pixelSamples;
   boolean snapshotEnabled = true;

   StackSnapshot(CardStack ignoredStack) {
      this.pixelSamples = new PixelSample[0];
   }
}




