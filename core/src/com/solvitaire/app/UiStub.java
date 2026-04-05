package com.solvitaire.app;

public final class UiStub {
   final SolverContext.RepaintStub repaintSurface = new SolverContext.RepaintStub();
   final LabelStub statusLabel = new LabelStub();
   final DialogStub dialog = new DialogStub();

   void showScreen(int code) {
   }

   void showStatus(int code, String message) {
   }

   boolean confirm(String title, String body) {
      return true;
   }

   void showError(String title, String body) {
      throw new IllegalStateException(title + ": " + body);
   }

   static final class LabelStub {
      void setText(String text) {
      }
   }

   static final class DialogStub {
      void reset() {
      }
   }
}




