package com.solvitaire.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
      // 输出目录
      Path outputRoot = this.config.outputRoot.toAbsolutePath();
      //_work目录
      Path workRoot = outputRoot.resolve("_work");
      // 解决目录
      Path solvedRoot = outputRoot.resolve("solved");
      // spider1 or other
      Path solvedVariantDir = solvedRoot.resolve(this.config.variant.directoryName(this.config.parameter));
      // 创建
      Files.createDirectories(workRoot);
      // 创建解决目录
      Files.createDirectories(solvedVariantDir);
      //已经处理的 or 已经解决的
      List<ValidatedDeal> acceptedDeals = new ArrayList<>();
      // 开始随机数， 然后进行累加
      long seed = this.config.firstSeed;
      //最大尝试次数， 如果写一样，可能达不到目标个数
      int attempts = 0;
      // 循环遍历  处理的个数是否达到  最大尝试次数
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
      Path candidateReadableSolutionFile = null;
      try {
         // 关卡文件的路径
         candidateCardsFile = DealFileIO.writeGameStyleDeal(workRoot, this.config.variant, this.config.parameter, seed);
         SolveOutcome outcome = this.solve(candidateCardsFile);
         candidateSolutionFile = outcome.solutionFile;
         candidateReadableSolutionFile = outcome.readableSolutionFile;

         if (!outcome.solved) {
            this.deleteIfExists(candidateSolutionFile);
            this.deleteIfExists(candidateReadableSolutionFile);
            this.deleteIfExists(candidateCardsFile);
            return ValidationResult.rejected("solver found no solution");
         }

         Path finalCardsFile = solvedVariantDir.resolve(candidateCardsFile.getFileName().toString());
         Path finalSolutionFile = solvedVariantDir.resolve(candidateSolutionFile.getFileName().toString());
         Path finalReadableSolutionFile = solvedVariantDir.resolve(candidateReadableSolutionFile.getFileName().toString());
         Files.copy(candidateCardsFile, finalCardsFile, StandardCopyOption.REPLACE_EXISTING);
         Files.copy(candidateSolutionFile, finalSolutionFile, StandardCopyOption.REPLACE_EXISTING);
         Files.copy(candidateReadableSolutionFile, finalReadableSolutionFile, StandardCopyOption.REPLACE_EXISTING);
         Files.writeString(solvedVariantDir.resolve("chkpt.txt"), Long.toString(seed), StandardCharsets.US_ASCII);

         this.deleteIfExists(candidateSolutionFile);
         this.deleteIfExists(candidateReadableSolutionFile);
         this.deleteIfExists(candidateCardsFile);
         return ValidationResult.accepted(new ValidatedDeal(seed, outcome.moveCount, finalCardsFile, finalSolutionFile, finalReadableSolutionFile));
      } catch (Throwable throwable) {
         this.deleteIfExists(candidateSolutionFile);
         this.deleteIfExists(candidateReadableSolutionFile);
         this.deleteIfExists(candidateCardsFile);
         return ValidationResult.rejected(SolverContext.describeThrowable(throwable));
      }
   }

   /**
    * 开始处理文件
    * @param cardsFile
    * @return
    * @throws IOException
    */
   private SolveOutcome solve(Path cardsFile) throws IOException {
      SolverContext context = new SolverContext();
      context.logLevel = 0;
      // 纸牌类型  spider
      context.variantId = variantId(this.config.variant);
      // 要解决题的路径
      context.files = new SolverFileSet(cardsFile);
      context.files.b = 1;
      context.files.variantSlug = this.config.variant.slug(); //siper
      context.initialState = allocateState(context, this.config.variant);
      context.bestSolutionState = new GameState();  //最佳的状态
      context.playbackState = new GameState(); //玩的状态
      // 创建解析器
      BaseSolver solver = createSolver(context, this.config.variant);
      solver.C = false;
      context.bridge = new SolverBridge(solver) {
      };
      configureReader(context.bridge, this.config.variant);

      Path solutionFile = cardsFile.resolveSibling("solution_" + cardsFile.getFileName());
      Path readableSolutionFile = readableSolutionFileFor(solutionFile);
      Files.deleteIfExists(solutionFile);
      Files.deleteIfExists(readableSolutionFile);
      //解题
      solver.solve();
      // 读取步謯文件
      int[] moves = readMoves(solutionFile);
      if (moves.length > 0) {
         writeReadableSolution(context, moves, readableSolutionFile);
      }
      return new SolveOutcome(moves.length > 0, moves.length, solutionFile, readableSolutionFile);
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

   private static Path readableSolutionFileFor(Path solutionFile) {
      String fileName = solutionFile.getFileName().toString();
      int dotIndex = fileName.lastIndexOf('.');
      String readableName = dotIndex >= 0
              ? fileName.substring(0, dotIndex) + "_readable" + fileName.substring(dotIndex)
              : fileName + "_readable.txt";
      return solutionFile.resolveSibling(readableName);
   }

   private static void writeReadableSolution(SolverContext context, int[] moves, Path readableSolutionFile) throws IOException {
      String[] lines = Move.a(context.bridge, moves, 0, moves.length, false);
      StringBuilder builder = new StringBuilder();
      builder.append("variant=").append(context.files.variantSlug).append(System.lineSeparator());
      builder.append("moves=").append(moves.length).append(System.lineSeparator());
      builder.append(System.lineSeparator());
      for (String line : lines) {
         if (line != null && !line.isEmpty()) {
            builder.append(stripRawMoveCode(line)).append(System.lineSeparator());
         }
      }
      Files.writeString(readableSolutionFile, builder.toString(), StandardCharsets.UTF_8);
   }

   private static String stripRawMoveCode(String line) {
      int marker = line.lastIndexOf(" [");
      if (marker < 0 || !line.endsWith("]")) {
         return line;
      }
      return line.substring(0, marker);
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
                 .append(",readableSolution=").append(deal.readableSolutionFile.getFileName())
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

      /**
       *
       * @param variant 游戏类型
       * @param numberOfColors 花色
       * @param firstSeed 第一个的种子
       * @param desiredSolvedDeals 期望解决的个数
       * @param maxAttempts 尝试的次数
       * @param outputRoot 输出目录
       */
      BatchConfig(DealVariant variant, int numberOfColors, long firstSeed, int desiredSolvedDeals, int maxAttempts, Path outputRoot) {
         this.variant = variant;
         this.parameter = numberOfColors;
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
            1,
            1,
            Paths.get("batch_output")
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
      final Path readableSolutionFile;

      SolveOutcome(boolean solved, int moveCount, Path solutionFile, Path readableSolutionFile) {
         this.solved = solved;
         this.moveCount = moveCount;
         this.solutionFile = solutionFile;
         this.readableSolutionFile = readableSolutionFile;
      }
   }

   private static final class ValidatedDeal {
      final long seed;
      final int moveCount;
      final Path cardsFile;
      final Path solutionFile;
      final Path readableSolutionFile;

      ValidatedDeal(long seed, int moveCount, Path cardsFile, Path solutionFile, Path readableSolutionFile) {
         this.seed = seed;
         this.moveCount = moveCount;
         this.cardsFile = cardsFile;
         this.solutionFile = solutionFile;
         this.readableSolutionFile = readableSolutionFile;
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

