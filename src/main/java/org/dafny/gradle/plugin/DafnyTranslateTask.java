package org.dafny.gradle.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class DafnyTranslateTask extends DafnyBaseTask {

  @OutputFile
  public abstract RegularFileProperty getDooFile();

  @OutputFile
  public abstract RegularFileProperty getTranslationRecordFile();

  @OutputDirectory
  public abstract DirectoryProperty getOutputPath();

  @TaskAction
  public void translateToJava() throws IOException, InterruptedException {
    var dooFile = getDooFile().get().getAsFile();
    var outputDir = getOutputPath().get().getAsFile();

    if (!dooFile.exists()) {
      return;
    }

    List<String> args = new ArrayList<>();
    args.add("translate");
    args.add("java");
    args.add(dooFile.getPath());
    args.addAll(Utils.getTranslationArguments(getClasspath().get(), getOptions().get()));

    args.add("--translation-record-output");
    args.add(getTranslationRecordFile().get().getAsFile().getPath());

    args.add("-o");
    args.add(outputDir.getPath());

    invokeDafnyCLI(args);
  }
}
