package com.solvitaire.app;

import java.nio.file.Path;

public final class SolverFileSet {
   int b = 0;
   int c = 0;
   int d = 0;
   int e = 0;
   int f = 0;
   int g = 0;
   int h = 0;
   int i = 0;
   int maxMoves = 999;
   int k = 0;
   int l = 0;
   int m = 0;
   int n = 0;
   boolean a = true;
   boolean q = false;
   boolean r = false;
   String outputDirectory;
   String variantSlug = "freecell";
   private final String inputFileName;

   SolverFileSet(Path inputFile) {
      Path absolute = inputFile.toAbsolutePath();
      Path parent = absolute.getParent();
      this.outputDirectory = parent == null ? "" : parent.toString() + java.io.File.separator;
      this.inputFileName = absolute.getFileName().toString();
   }

   void setOutputDirectory(String dir, boolean create) {
      this.outputDirectory = dir;
      if (create) {
         SolverContext.ensureDirectory(dir);
      }
   }

   int getCurrentFileIndex(boolean ignored) {
      return 0;
   }

   void setCurrentFileIndex(int value, boolean ignored) {
   }

   void setEndFileIndex(int value) {
   }

   int getEndFileIndex() {
      return 0;
   }

   boolean c() {
      return false;
   }

   String getInputFileName() {
      return this.inputFileName;
   }

   String getSolutionFileName() {
      return "solution_" + this.inputFileName;
   }

   String getPlaybackFileName() {
      return this.getSolutionFileName();
   }

   int g() {
      return 0;
   }

   int h() {
      return 0;
   }

   boolean i() {
      return false;
   }

   String getActiveInputFileName() {
      return this.inputFileName;
   }
}




