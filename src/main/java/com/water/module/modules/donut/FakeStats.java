package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class FakeStats extends Module {
   private static FakeStats INSTANCE;
   private static final String OBJECTIVE_NAME = "water_fake_stats";
   private static final int MAX_SIDEBAR_LINES = 15;
   private final Setting<String> money = new Setting<>("Money", "0");
   private final Setting<String> shards = new Setting<>("Shards", "0");
   private final Setting<String> kills = new Setting<>("Kills", "0");
   private final Setting<String> deaths = new Setting<>("Deaths", "0");
   private final Setting<String> playtime = new Setting<>("Playtime", "0m");
   private final Random randomSource = new Random();
   private ScoreboardObjective originalObjective;
   private String originalObjectiveName;
   private ScoreboardObjective customObjective;
   private FakeStats.AppliedStats appliedStats;
   private Object lastWorld;
   private String lastSnapshotSignature = "";
   private boolean needsRefresh;
   private long lastRebuildMs = 0L;
   private static final long REBUILD_COOLDOWN_MS = 500L;

   public static FakeStats getInstance() {
      return INSTANCE;
   }

   public FakeStats() {
      super("FakeStats", Category.d);
      INSTANCE = this;
      this.addSetting(this.money);
      this.addSetting(this.shards);
      this.addSetting(this.kills);
      this.addSetting(this.deaths);
      this.addSetting(this.playtime);
   }

   @Override
   public void onEnable() {
      this.lastWorld = mc.world;
      this.originalObjective = null;
      this.originalObjectiveName = null;
      this.customObjective = null;
      this.lastSnapshotSignature = "";
      this.randomizeSettings();
      this.applyCurrentValues();
      this.needsRefresh = true;
   }

   @Override
   public void onDisable() {
      this.lastWorld = null;
      this.lastSnapshotSignature = "";
      this.needsRefresh = false;
      ScoreboardObjective var1 = this.originalObjective;
      ScoreboardObjective var2 = this.customObjective;
      this.originalObjective = null;
      this.originalObjectiveName = null;
      this.customObjective = null;
      if (mc.world != null) {
         Scoreboard var3 = mc.world.getScoreboard();

         try {
            if (var2 != null) {
               var3.removeObjective(var2);
            }

            if (var1 != null && var3.getObjectives().contains(var1)) {
               var3.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, var1);
            }
         } catch (Exception var4) {
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.world == null) {
         this.originalObjective = null;
         this.originalObjectiveName = null;
         this.customObjective = null;
         this.lastWorld = null;
         this.lastSnapshotSignature = "";
      } else {
         if (mc.world != this.lastWorld) {
            this.originalObjective = null;
            this.originalObjectiveName = null;
            this.customObjective = null;
            this.lastSnapshotSignature = "";
            this.lastWorld = mc.world;
            this.needsRefresh = true;
         }

         this.captureOriginalObjective();
         if (this.originalObjective != null) {
            Scoreboard var1 = mc.world.getScoreboard();
            if (!var1.getObjectives().contains(this.originalObjective)) {
               this.originalObjective = null;
               this.captureOriginalObjective();
               if (this.originalObjective == null) {
                  return;
               }
            }

            FakeStats.Snapshot var2 = this.createSnapshot(var1, this.originalObjective);
            if (var2 != null) {
               this.syncAppliedStatsWithSettings();
               if (!var2.signature().equals(this.lastSnapshotSignature) || this.customObjective == null) {
                  this.needsRefresh = true;
               }

               this.rebuildIfPossible(var1, var2);
               if (this.customObjective != null && var1.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != this.customObjective) {
                  var1.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.customObjective);
               }
            }
         }
      }
   }

   private void randomizeSettings() {
      this.money.setValue(this.formatCompactNumber(this.randomBetweenLong(10000L, 5000000000L)));
      this.shards.setValue(this.formatCompactNumber(this.randomBetweenLong(0L, 2500000L)));
      this.kills.setValue(String.valueOf(this.randomBetweenLong(0L, 2000L)));
      this.deaths.setValue(String.valueOf(this.randomBetweenLong(0L, 1000L)));
      this.playtime.setValue(this.formatCompactPlaytime(this.randomBetweenLong(0L, 15552000L)));
   }

   private void applyCurrentValues() {
      this.appliedStats = new FakeStats.AppliedStats(
         this.sanitize(this.money.getValue(), "0"),
         this.sanitize(this.shards.getValue(), "0"),
         this.sanitize(this.kills.getValue(), "0"),
         this.sanitize(this.deaths.getValue(), "0"),
         this.sanitize(this.playtime.getValue(), "0m")
      );
      this.needsRefresh = true;
   }

   private void syncAppliedStatsWithSettings() {
      FakeStats.AppliedStats var1 = new FakeStats.AppliedStats(
         this.sanitize(this.money.getValue(), "0"),
         this.sanitize(this.shards.getValue(), "0"),
         this.sanitize(this.kills.getValue(), "0"),
         this.sanitize(this.deaths.getValue(), "0"),
         this.sanitize(this.playtime.getValue(), "0m")
      );
      if (this.appliedStats == null || !this.appliedStats.signature().equals(var1.signature())) {
         this.appliedStats = var1;
         this.needsRefresh = true;
      }
   }

   private void captureOriginalObjective() {
      if (mc.world != null) {
         Scoreboard var1 = mc.world.getScoreboard();
         ScoreboardObjective var2 = var1.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
         if (var2 != null && !"water_fake_stats".equals(var2.getName())) {
            this.originalObjective = var2;
            this.originalObjectiveName = var2.getName();
         } else if (this.originalObjective == null || !var1.getObjectives().contains(this.originalObjective)) {
            if (this.originalObjectiveName != null) {
               var2 = var1.getNullableObjective(this.originalObjectiveName);
               if (var2 != null && !"water_fake_stats".equals(var2.getName())) {
                  this.originalObjective = var2;
                  return;
               }
            }

            for (ScoreboardObjective var3 : var1.getObjectives()) {
               if (!"water_fake_stats".equals(var3.getName())) {
                  this.originalObjective = var3;
                  this.originalObjectiveName = var3.getName();
                  return;
               }
            }
         }
      }
   }

   private FakeStats.Snapshot createSnapshot(Scoreboard scoreboard, ScoreboardObjective objective) {
      ArrayList var3 = new ArrayList();
      ArrayList var4 = new ArrayList<>(scoreboard.getScoreboardEntries(objective));
      var4.removeIf(ScoreboardEntry::hidden);
      var4.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed());
      if (var4.size() > 15) {
         var4 = new ArrayList(var4.subList(0, 15));
      }

      for (ScoreboardEntry var5 : var4) {
         var3.add(new FakeStats.SourceLine(var5.value(), this.getVisibleLine(scoreboard, var5)));
      }

      MutableText var9 = objective.getDisplayName() != null ? objective.getDisplayName().copy() : Text.literal("Donut SMP");
      StringBuilder var10 = new StringBuilder(var9.getString());

      for (ScoreboardObjective var7 : var3) {
         var10.append('\n').append(var7.score()).append(':').append(var7.text().getString());
      }

      var10.append('\n').append(this.appliedStats != null ? this.appliedStats.signature() : "");
      return new FakeStats.Snapshot(var9, var3, var10.toString());
   }

   private Text getVisibleLine(Scoreboard scoreboard, ScoreboardEntry entry) {
      if (entry.display() != null) {
         return entry.display().copy();
      } else {
         MutableText var3 = entry.name() != null ? entry.name().copy() : Text.literal(entry.owner());
         Scoreboard var4 = scoreboard.getScoreHolderTeam(entry.owner());
         return Team.decorateName(var4, var3).copy();
      }
   }

   private void rebuildIfPossible(Scoreboard scoreboard, FakeStats.Snapshot snapshot) {
      if (this.needsRefresh && this.appliedStats != null) {
         long var3 = System.currentTimeMillis();
         if (var3 - this.lastRebuildMs >= 500L) {
            this.lastRebuildMs = var3;
            ScoreboardObjective var9 = scoreboard.getNullableObjective("water_fake_stats");
            if (var9 != null) {
               scoreboard.removeObjective(var9);
            }

            this.customObjective = scoreboard.addObjective(
               "water_fake_stats", ScoreboardCriterion.DUMMY, snapshot.title().copy(), RenderType.INTEGER, true, BlankNumberFormat.INSTANCE
            );
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.customObjective);
            List var10 = snapshot.lines();
            int var4 = 0;

            for (int var5 = 0; var5 < var10.size(); var5++) {
               FakeStats.SourceLine var6 = (FakeStats.SourceLine)var10.get(var5);
               ScoreHolder var7 = ScoreHolder.fromName("fake_stats_line_" + var5);
               ScoreAccess var11 = scoreboard.getOrCreateScore(var7, this.customObjective);
               var11.setScore(var10.size() - var5);
               String var8 = var6.text().getString().trim();
               boolean var12 = var8.matches(".*\\d.*");
               if (var12) {
                  var11.setDisplayText(this.replaceStatByIndex(var6.text(), var4));
                  var4++;
               } else {
                  var11.setDisplayText(var6.text().copy());
               }

               var11.setNumberFormat(BlankNumberFormat.INSTANCE);
            }

            this.lastSnapshotSignature = snapshot.signature();
            this.needsRefresh = false;
         }
      }
   }

   private Text replaceStatByIndex(Text originalText, int index) {
      String[] var3 = new String[]{
         this.appliedStats.money(), this.appliedStats.shards(), this.appliedStats.kills(), this.appliedStats.deaths(), this.appliedStats.playtime()
      };
      if (index >= var3.length) {
         return originalText.copy();
      } else {
         int var9 = var3[index];
         List var10 = this.collectSegments(originalText);
         Text var7 = originalText.getString();
         int var4 = -1;

         for (int var5 = 0; var5 < var7.length(); var5++) {
            char var6 = var7.charAt(var5);
            if (Character.isDigit(var6) || var6 == '-' && var5 + 1 < var7.length() && Character.isDigit(var7.charAt(var5 + 1))) {
               var4 = var5;
               break;
            }
         }

         if (var4 >= 0) {
            MutableText var12 = Text.empty();
            this.appendTextRange(var12, var10, var4);
            var12.append(Text.literal(var9).setStyle(this.findStyleAt(var10, var4)));
            return var12;
         } else {
            MutableText var11 = Text.empty();

            for (Text var8 : var10) {
               var11.append(Text.literal(var8.value()).setStyle(var8.style()));
            }

            return var11;
         }
      }
   }

   private List<FakeStats.TextSegment> collectSegments(Text text) {
      ArrayList var2 = new ArrayList();
      text.visit((style, string) -> {
         if (!string.isEmpty()) {
            var2.add(new FakeStats.TextSegment(string, style));
         }

         return Optional.empty();
      }, Style.EMPTY);
      return var2;
   }

   private void appendTextRange(MutableText result, List<FakeStats.TextSegment> segments, int endExclusive) {
      endExclusive = Math.max(0, endExclusive);

      for (FakeStats.TextSegment var4 : segments) {
         if (endExclusive <= 0) {
            return;
         }

         String var5 = var4.value();
         int var6 = Math.min(var5.length(), endExclusive);
         result.append(Text.literal(var5.substring(0, var6)).setStyle(var4.style()));
         endExclusive -= var6;
      }
   }

   private Style findStyleAt(List<FakeStats.TextSegment> segments, int charIndex) {
      charIndex = Math.max(0, charIndex);
      Style var3 = Style.EMPTY;

      for (FakeStats.TextSegment var4 : segments) {
         if (!var4.value().isEmpty()) {
            var3 = var4.style();
         }

         if (charIndex < var4.value().length()) {
            return var4.style();
         }

         charIndex -= var4.value().length();
      }

      return var3;
   }

   private void restoreOriginalScoreboard() {
      if (mc.world != null) {
         Scoreboard var1 = mc.world.getScoreboard();
         ScoreboardObjective var2 = var1.getNullableObjective("water_fake_stats");
         if (var2 != null) {
            var1.removeObjective(var2);
         }

         if (this.originalObjective != null && var1.getObjectives().contains(this.originalObjective)) {
            var1.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.originalObjective);
         }
      }
   }

   private long randomBetweenLong(long min, long max) {
      return min >= max ? min : min + (long)Math.floor(this.randomSource.nextDouble() * (max - min + 1L));
   }

   private String formatCompactNumber(long value) {
      long var3 = Math.abs(value);
      if (var3 < 1000L) {
         return Long.toString(value);
      } else if (var3 < 1000000L) {
         return this.formatCompactValue(value / 1000.0, "K");
      } else {
         return var3 < 1000000000L ? this.formatCompactValue(value / 1000000.0, "M") : this.formatCompactValue(value / 1.0E9, "B");
      }
   }

   private String formatCompactValue(double value, String suffix) {
      String var4 = value >= 100.0 ? "%.0f%s" : (value >= 10.0 ? "%.1f%s" : "%.2f%s");
      return String.format(Locale.US, var4, value, suffix);
   }

   private String formatCompactPlaytime(long totalSeconds) {
      long var3 = totalSeconds / 3600L;
      long var5 = var3 / 24L;
      long var7 = var3 % 24L;
      long var9 = totalSeconds % 3600L / 60L;
      if (var5 > 0L) {
         return String.format(Locale.US, "%dd %dh", var5, var7);
      } else {
         return var3 > 0L ? String.format(Locale.US, "%dh %dm", var3, var9) : String.format(Locale.US, "%dm", var9);
      }
   }

   private String sanitize(String value, String fallback) {
      if (value == null) {
         return fallback;
      } else {
         value = value.trim();
         return value.isEmpty() ? fallback : value;
      }
   }

   public Text fakeFooterText(Text footer) {
      if (this.appliedStats == null) {
         return footer;
      } else {
         String var2 = footer.getString();
         Pattern var3 = Pattern.compile("(\\$\\s*)([0-9][0-9.,]*[KkMmBbTt]?)");
         Matcher var7 = var3.matcher(var2);
         if (!var7.find()) {
            return footer;
         } else {
            var2 = var2.substring(0, var7.start(2)) + this.appliedStats.money() + var2.substring(var7.end(2));
            Text var4 = this.collectSegments(footer);
            Text var5 = var4.isEmpty() ? Style.EMPTY : ((FakeStats.TextSegment)var4.get(0)).style();
            return Text.literal(var2).setStyle(var5);
         }
      }
   }

   private record AppliedStats(String money, String shards, String kills, String deaths, String playtime) {
      private String signature() {
         return this.money + "|" + this.shards + "|" + this.kills + "|" + this.deaths + "|" + this.playtime;
      }
   }

   private record Snapshot(Text title, List<FakeStats.SourceLine> lines, String signature) {
   }

   private record SourceLine(int score, Text text) {
   }

   private record TextSegment(String value, Style style) {
   }
}
