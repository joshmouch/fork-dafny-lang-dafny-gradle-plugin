package org.dafny.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

public class Utils {

  public record DafnyArtifactInputs(String dooFile, String translationRecordFile) {}

  // TODO: Shape this as something Gradle can cache for us,
  // which will also ensure these are cleaned up as needed.
  public static List<DafnyArtifactInputs> dafnyArtifactsForClasspath(Collection<File> classpath)
      throws IOException {
    List<DafnyArtifactInputs> artifacts = new ArrayList<>();
    for (File classpathEntry : classpath) {
      if (classpathEntry.getName().endsWith(".jar")) {
        try (JarFile jarFile = new JarFile(classpathEntry)) {
          ZipEntry dooEntry = jarFile.getEntry("META-INF/" + DafnyPlugin.META_INF_DOO_FILE_NAME);
          ZipEntry translationRecordEntry =
              jarFile.getEntry("META-INF/" + DafnyPlugin.META_INF_TRANSLATION_RECORD_FILE_NAME);
          if (dooEntry == null && translationRecordEntry == null) {
            continue;
          }
          if (dooEntry == null || translationRecordEntry == null) {
            throw new IOException(
                "Dafny dependency "
                    + classpathEntry
                    + " must contain both META-INF/"
                    + DafnyPlugin.META_INF_DOO_FILE_NAME
                    + " and META-INF/"
                    + DafnyPlugin.META_INF_TRANSLATION_RECORD_FILE_NAME);
          }

          Path extractedDoo = extract(jarFile, dooEntry, classpathEntry.getName(), ".doo");
          Path extractedTranslationRecord =
              extract(jarFile, translationRecordEntry, classpathEntry.getName(), ".dtr");
          artifacts.add(
              new DafnyArtifactInputs(
                  extractedDoo.toString(), extractedTranslationRecord.toString()));
        }
      }
      // TODO: handle class directories too
    }

    return artifacts;
  }

  private static Path extract(JarFile jarFile, ZipEntry entry, String prefix, String suffix)
      throws IOException {
    Path extracted = Files.createTempFile(prefix, suffix);
    try (var input = jarFile.getInputStream(entry)) {
      Files.copy(input, extracted, StandardCopyOption.REPLACE_EXISTING);
    }
    return extracted;
  }

  public static List<String> getVerificationArguments(
      FileCollection classpath, Map<String, Object> options) throws IOException {
    List<String> args = new ArrayList<>();

    for (var artifact : Utils.dafnyArtifactsForClasspath(classpath.getFiles())) {
      args.add("--library");
      args.add(artifact.dooFile());
    }

    appendOptions(args, options);
    return args;
  }

  public static List<String> getTranslationArguments(
      FileCollection classpath, Map<String, Object> options) throws IOException {
    List<String> args = new ArrayList<>();

    for (var artifact : Utils.dafnyArtifactsForClasspath(classpath.getFiles())) {
      args.add("--library");
      args.add(artifact.dooFile());
      args.add("--translation-record");
      args.add(artifact.translationRecordFile());
    }

    appendOptions(args, options);
    return args;
  }

  private static void appendOptions(List<String> args, Map<String, Object> options) {
    for (var entry : options.entrySet()) {
      args.add("--" + entry.getKey() + ":" + entry.getValue());
    }
  }

  public static Configuration getCompileClasspath(Project project) {
    return project.getConfigurations().getByName("compileClasspath");
  }
}
