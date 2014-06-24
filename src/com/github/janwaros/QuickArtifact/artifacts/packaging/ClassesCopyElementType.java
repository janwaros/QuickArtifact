package com.github.janwaros.QuickArtifact.artifacts.packaging;

import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementType;
import com.intellij.packaging.ui.ArtifactEditorContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 22.06.2014
 * Time: 21:20
 */
public class ClassesCopyElementType extends PackagingElementType<ClassesCopyPackagingElement> {

    ClassesCopyElementType() {
        super("classes-copy", "Classes");
    }

    @Override
    public Icon getCreateElementIcon() {
        return AllIcons.FileTypes.JavaClass;
    }

    @Override
    public boolean canCreate(@NotNull ArtifactEditorContext context, @NotNull Artifact artifact) {
        return true;
    }

    @NotNull
    public List<? extends ClassesCopyPackagingElement> chooseAndCreate(@NotNull ArtifactEditorContext context, @NotNull Artifact artifact,
                                                                    @NotNull CompositePackagingElement<?> parent) {

        TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(context.getProject()).createFileChooser("Chose file to include in artifact", null, null, null);

        fileChooser.showDialog();

        if(fileChooser.getSelectedFile() != null) {
            VirtualFile file = fileChooser.getSelectedFile().getVirtualFile();
            String outputPath = Utils.getVirtualFileOutputPath(file, context.getProject());
            return Collections.singletonList(new ClassesCopyPackagingElement(outputPath+File.separator+file.getNameWithoutExtension()));
        }

        return Collections.emptyList();
    }

    @NotNull
    public ClassesCopyPackagingElement createEmpty(@NotNull Project project) {
        return new ClassesCopyPackagingElement();
    }

}
