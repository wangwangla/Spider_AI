package com.solvitaire.app;

import java.util.Collections;
import java.util.List;

public final class KlondikeSolveResult {
    private final boolean solved;
    private final List<KlondikeSolutionStep> steps;
    private final String summary;

    KlondikeSolveResult(boolean solved, List<KlondikeSolutionStep> steps, String summary) {
        this.solved = solved;
        this.steps = Collections.unmodifiableList(steps);
        this.summary = summary;
    }

    public boolean isSolved() { return solved; }
    public List<KlondikeSolutionStep> getSteps() { return steps; }
    public String getSummary() { return summary; }
}
