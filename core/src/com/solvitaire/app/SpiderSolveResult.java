package com.solvitaire.app;

import java.util.Collections;
import java.util.List;

public final class SpiderSolveResult {
   private final boolean solved;
   private final List<SpiderSolutionStep> steps;
   private final String summary;

   SpiderSolveResult(boolean solved, List<SpiderSolutionStep> steps, String summary) {
      this.solved = solved;
      this.steps = Collections.unmodifiableList(steps);
      this.summary = summary;
   }

   public boolean isSolved() {
      return this.solved;
   }

   public List<SpiderSolutionStep> getSteps() {
      return this.steps;
   }

   public String getSummary() {
      return this.summary;
   }
}
