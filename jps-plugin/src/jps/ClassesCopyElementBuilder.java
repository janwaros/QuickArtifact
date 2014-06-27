package jps;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.artifacts.builders.LayoutElementBuilderService;
import org.jetbrains.jps.incremental.artifacts.instructions.ArtifactCompilerInstructionCreator;
import org.jetbrains.jps.incremental.artifacts.instructions.ArtifactInstructionsBuilderContext;
import org.jetbrains.jps.incremental.artifacts.instructions.SourceFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 23.06.2014
 * Time: 00:34
 */
public class ClassesCopyElementBuilder extends LayoutElementBuilderService<JpsClassesCopyPackagingElement> {

    public ClassesCopyElementBuilder() {
        super(JpsClassesCopyPackagingElement.class);
    }

    @Override
    public void generateInstructions(JpsClassesCopyPackagingElement element, ArtifactCompilerInstructionCreator instructionCreator,
                                     ArtifactInstructionsBuilderContext builderContext) {
        final String dirPath = element.getDirectoryPath();
        if (dirPath != null) {

            final File abstractDir = new File(dirPath);
            final File directory = abstractDir.getParentFile();
            final String fileName = abstractDir.getName();
            instructionCreator.addDirectoryCopyInstructions(directory, new SourceFileFilter() {

                @Override
                public boolean accept(@NotNull String fullFilePath) {

                    File fullFile = new File(fullFilePath);

                    if(fullFile.isDirectory()) {
                        return directory.getPath().startsWith(fullFile.getPath());
                    }  else {
                        return fullFile.getName().equals(fileName) || (getExtension(fullFile).equals("class") && getNameWithoutExtension(fullFile).startsWith(getNameWithoutExtension(new File(fileName)) + "$"));
                    }
                }

                @Override
                public boolean shouldBeCopied(@NotNull String fullFilePath, ProjectDescriptor projectDescriptor) throws IOException {
                    File fullFile = new File(fullFilePath);

                    if(fullFile.isDirectory()) {
                        return directory.getPath().startsWith(fullFile.getPath());
                    }  else {
                        return fullFile.getName().equals(fileName) || (getExtension(fullFile).equals("class") && getNameWithoutExtension(fullFile).startsWith(getNameWithoutExtension(new File(fileName)) + "$"));
                    }
                }
            });
        }
    }

    @Override
    public Collection<? extends BuildTarget<?>> getDependencies(@NotNull JpsClassesCopyPackagingElement element,
                                                                TargetOutputIndex outputIndex) {
        String dirPath = element.getDirectoryPath();
        if (dirPath != null) {
            return outputIndex.getTargetsByOutputFile(new File(dirPath));
        }
        return Collections.emptyList();
    }

    @NonNls
    @NotNull
    public String getExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) return "";
        return name.substring(index + 1);
    }

    @NonNls
    @NotNull
    public String getNameWithoutExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index < 0) return name;
        return name.substring(0, index);
    }

}
