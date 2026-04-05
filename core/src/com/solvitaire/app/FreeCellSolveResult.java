package com.solvitaire.app;

import java.util.Collections;
import java.util.List;

public final class FreeCellSolveResult {
   private final boolean solved;
   private final List<FreeCellSolutionStep> steps;
   private final String summary;

   FreeCellSolveResult(boolean solved, List<FreeCellSolutionStep> steps, String summary) {
      this.solved = solved;
      this.steps = Collections.unmodifiableList(steps);
      this.summary = summary;
   }

   public boolean isSolved() {
      return this.solved;
   }

   public List<FreeCellSolutionStep> getSteps() {
      return this.steps;
   }

   public String getSummary() {
      return this.summary;
   }
}
