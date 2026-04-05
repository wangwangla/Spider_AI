package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class SolvableDealBatchMain {
   private final BatchConfig config;

   public SolvableDealBatchMain() {
      this(BatchConfig.defaultSpiderConfig());
   }

   public SolvableDealBatchMain(BatchConfig config) {
      this.config = config;
   }

   public static void main(String[] args) throws IOException {
      new SolvableDealBatchMain().run();
   }

   public void run() throws IOException {
      this.config.validate();

      Path outputRoot = this.config.outputRoot.toAbsolutePath();
      Path workRoot = outputRoot.resolve("_work");
      Path solvedRoot = outputRoot.resolve("solved");
      Path solvedVariantDir = solvedRoot.resolve(this.config.variant.directoryName(this.config.parameter));

      Files.createDirectories(workRoot);
      Files.createDirectories(solvedVariantDir);

      List<ValidatedDeal> acceptedDeals = new ArrayList<>();
      long seed = this.config.firstSeed;
      int attempts = 0;

      while (acceptedDeals.size() < this.config.desiredSolvedDeals && attempts < this.config.maxAttempts) {
         attempts++;
         ValidationResult result = this.generateAndValidate(seed, workRoot, solvedVariantDir);
         if (result.acceptedDeal != null) {
            acceptedDeals.add(result.acceptedDeal);
            System.out.println(String.format("accepted seed=%d moves=%d saved=%s", result.acceptedDeal.seed, result.acceptedDeal.moveCount, result.acceptedDeal.cardsFile));
         } else {
            System.out.println(String.format("rejected seed=%d reason=%s", seed, result.reason));
         }
         seed++;
      }

      this.writeSummary(solvedVariantDir, acceptedDeals, attempts);
      System.out.println(String.format("run complete: accepted=%d attempts=%d output=%s", acceptedDeals.size(), attempts, solvedVariantDir));
   }

   private ValidationResult generateAndValidate(long seed, Path workRoot, Path solvedVariantDir) {
      Path candidateCardsFile = null;
      Path candidateSolutionFile = null;
      try {
         candidateCardsFile = DealFileIO.writeGameStyleDeal(workRoot, this.config.variant, this.config.parameter, seed);
         SolveOutcome outcome = this.solve(candidateCardsFile);
         candidateSolutionFile = outcome.solutionFile;

         if (!outcome.solved) {
            this.deleteIfExists(candidateSolutionFile);
            this.deleteIfExists(candidateCardsFile);
            return ValidationResult.rejected("solver found no solution");
         }

         Path finalCardsFile = solvedVariantDir.resolve(candidateCardsFile.getFileName().toString());
         Path finalSolutionFile = solvedVariantDir.resolve(candidateSolutionFile.getFileName().toString());
         Files.copy(candidateCardsFile, finalCardsFile, StandardCopyOption.REPLACE_EXISTING);
         Files.copy(candidateSolutionFile, finalSolutionFile, StandardCopyOption.REPLACE_EXISTING);
         Files.writeString(solvedVariantDir.resolve("chkpt.txt"), Long.toString(seed), StandardCharsets.US_ASCII);

         this.deleteIfExists(candidateSolutionFile);
         this.deleteIfExists(candidateCardsFile);
         return ValidationResult.accepted(new ValidatedDeal(seed, outcome.moveCount, finalCardsFile, finalSolutionFile));
      } catch (Throwable throwable) {
         this.deleteIfExists(candidateSolutionFile);
         this.deleteIfExists(candidateCardsFile);
         return ValidationResult.rejected(SolverContext.describeThrowable(throwable));
      }
   }

   private SolveOutcome solve(Path cardsFile) throws IOException {
      SolverContext context = new SolverContext();
      context.logLevel = 99;
      context.variantId = variantId(this.config.variant);
      context.files = new SolverFileSet(cardsFile);
      context.files.b = 1;
      context.files.variantSlug = this.config.variant.slug();
      context.initialState = allocateState(context, this.config.variant);
      context.bestSolutionState = new GameState();
      context.playbackState = new GameState();

      BaseSolver solver = createSolver(context, this.config.variant);
      solver.C = false;
      context.bridge = new SolverBridge(solver) {
      };
      configureReader(context.bridge, this.config.variant);

      Path solutionFile = cardsFile.resolveSibling("solution_" + cardsFile.getFileName());
      Files.deleteIfExists(solutionFile);

      solver.solve();

      int[] moves = readMoves(solutionFile);
      return new SolveOutcome(moves.length > 0, moves.length, solutionFile);
   }

   private static int variantId(DealVariant variant) {
      switch (variant) {
         case KLONDIKE:
            return 1;
         case SPIDER:
            return 2;
         case FREECELL:
            return 3;
         default:
            throw new IllegalArgumentException("Current batch validator only supports Klondike/Spider/FreeCell");
      }
   }

   private static BaseSolver createSolver(SolverContext context, DealVariant variant) {
      switch (variant) {
         case KLONDIKE:
            return new KlondikeSolver(context);
         case SPIDER:
            return new SpiderSolver(context);
         case FREECELL:
            return new FreeCellSolver(context);
         default:
            throw new IllegalArgumentException("No solver available for variant " + variant.slug());
      }
   }

   private static void configureReader(SolverBridge reader, DealVariant variant) {
      if (variant == DealVariant.KLONDIKE) {
         reader.specialSourceGroupIndex = 1;
         reader.specialDestinationGroupIndex = 2;
      } else if (variant == DealVariant.SPIDER) {
         reader.specialSourceGroupIndex = 1;
      }
   }

   private static GameState allocateState(SolverContext context, DealVariant variant) {
      switch (variant) {
         case KLONDIKE:
            return allocateKlondikeState(context);
         case SPIDER:
            return allocateSpiderState(context);
         case FREECELL:
            return allocateFreeCellState(context);
         default:
            throw new IllegalArgumentException("No state allocator for variant " + variant.slug());
      }
   }

   private static GameState allocateKlondikeState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 7, 1, 9);
      state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 258);
      state.stackGroups[2] = new StackGroup(context, "Pile", 2, 1, 1, 18);
      state.stackGroups[3] = new StackGroup(context, "Aces", 3, 4, 1, 2);
      for (int index = 0; index < 4; ++index) {
         state.stackGroups[3].stacks[index].foundationSuit = -1;
      }
      return state;
   }

   private static GameState allocateSpiderState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 10, 7, 1);
      state.stackGroups[1] = new StackGroup(context, "Feed", 1, 1, 1, 2);
      state.stackGroups[2] = new StackGroup(context, "Suits", 2, 8, 0, 66);
      return state;
   }

   private static GameState allocateFreeCellState(SolverContext context) {
      GameState state = new GameState();
      state.stackGroups[0] = new StackGroup(context, "Stack", 0, 8, 7, 9);
      state.stackGroups[1] = new StackGroup(context, "WorkArea", 1, 4, 1, 2);
      state.stackGroups[2] = new StackGroup(context, "Aces", 2, 4, 1, 2);
      state.stackGroups[2].stacks[0].foundationSuit = 2;
      state.stackGroups[2].stacks[1].foundationSuit = 4;
      state.stackGroups[2].stacks[2].foundationSuit = 3;
      state.stackGroups[2].stacks[3].foundationSuit = 1;
      return state;
   }

   private static int[] readMoves(Path solutionFile) throws IOException {
      if (!Files.exists(solutionFile)) {
         return new int[0];
      }

      String content = Files.readString(solutionFile, StandardCharsets.UTF_8).trim();
      if (content.isEmpty()) {
         return new int[0];
      }

      String[] parts = content.split(",");
      List<Integer> moves = new ArrayList<>(parts.length);
      for (String part : parts) {
         String trimmed = part.trim();
         if (trimmed.isEmpty()) {
            continue;
         }
         moves.add(Move.b(Integer.parseInt(trimmed)));
      }

      int[] values = new int[moves.size()];
      for (int index = 0; index < moves.size(); ++index) {
         values[index] = moves.get(index);
      }
      return values;
   }

   private void writeSummary(Path solvedVariantDir, List<ValidatedDeal> acceptedDeals, int attempts) throws IOException {
      StringBuilder builder = new StringBuilder();
      builder.append("variant=").append(this.config.variant.slug()).append(System.lineSeparator());
      builder.append("parameter=").append(this.config.parameter).append(System.lineSeparator());
      builder.append("requestedSolvedDeals=").append(this.config.desiredSolvedDeals).append(System.lineSeparator());
      builder.append("acceptedSolvedDeals=").append(acceptedDeals.size()).append(System.lineSeparator());
      builder.append("attempts=").append(attempts).append(System.lineSeparator());
      builder.append("firstSeed=").append(this.config.firstSeed).append(System.lineSeparator());
      builder.append(System.lineSeparator());

      for (ValidatedDeal deal : acceptedDeals) {
         builder.append("seed=").append(deal.seed)
            .append(",moves=").append(deal.moveCount)
            .append(",cards=").append(deal.cardsFile.getFileName())
            .append(",solution=").append(deal.solutionFile.getFileName())
            .append(System.lineSeparator());
      }

      Files.writeString(solvedVariantDir.resolve("summary.txt"), builder.toString(), StandardCharsets.UTF_8);
   }

   private void deleteIfExists(Path path) {
      if (path == null) {
         return;
      }
      try {
         Files.deleteIfExists(path);
      } catch (IOException ignored) {
      }
   }

   public static final class BatchConfig {
      final DealVariant variant;
      final int parameter;
      final long firstSeed;
      final int desiredSolvedDeals;
      final int maxAttempts;
      final Path outputRoot;

      BatchConfig(DealVariant variant, int parameter, long firstSeed, int desiredSolvedDeals, int maxAttempts, Path outputRoot) {
         this.variant = variant;
         this.parameter = parameter;
         this.firstSeed = firstSeed;
         this.desiredSolvedDeals = desiredSolvedDeals;
         this.maxAttempts = maxAttempts;
         this.outputRoot = outputRoot;
      }

       /**
        * 生成
        * @return
        */
      static BatchConfig defaultSpiderConfig() {
         return new BatchConfig(
            DealVariant.SPIDER,
            1,  //huas
            1L,
            20,
            20,
            Path.of("batch_output")
         );
      }

      void validate() {
         this.variant.validateParameter(this.parameter);
         if (this.variant != DealVariant.KLONDIKE && this.variant != DealVariant.SPIDER && this.variant != DealVariant.FREECELL) {
            throw new IllegalArgumentException("Only Klondike / Spider / FreeCell can be validated with the current solver set");
         }
         if (this.desiredSolvedDeals <= 0) {
            throw new IllegalArgumentException("desiredSolvedDeals must be > 0");
         }
         if (this.maxAttempts < this.desiredSolvedDeals) {
            throw new IllegalArgumentException("maxAttempts must be >= desiredSolvedDeals");
         }
      }
   }

   private static final class SolveOutcome {
      final boolean solved;
      final int moveCount;
      final Path solutionFile;

      SolveOutcome(boolean solved, int moveCount, Path solutionFile) {
         this.solved = solved;
         this.moveCount = moveCount;
         this.solutionFile = solutionFile;
      }
   }

   private static final class ValidatedDeal {
      final long seed;
      final int moveCount;
      final Path cardsFile;
      final Path solutionFile;

      ValidatedDeal(long seed, int moveCount, Path cardsFile, Path solutionFile) {
         this.seed = seed;
         this.moveCount = moveCount;
         this.cardsFile = cardsFile;
         this.solutionFile = solutionFile;
      }
   }

   private static final class ValidationResult {
      final ValidatedDeal acceptedDeal;
      final String reason;

      private ValidationResult(ValidatedDeal acceptedDeal, String reason) {
         this.acceptedDeal = acceptedDeal;
         this.reason = reason;
      }

      static ValidationResult accepted(ValidatedDeal acceptedDeal) {
         return new ValidationResult(acceptedDeal, null);
      }

      static ValidationResult rejected(String reason) {
         return new ValidationResult(null, reason);
      }
   }
}

