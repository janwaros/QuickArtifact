package com.github.janwaros.QuickArtifact.artifacts.packaging;

import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.compiler.ant.Generator;
import com.intellij.compiler.ant.Tag;
import com.intellij.compiler.ant.artifacts.DirectoryAntCopyInstructionCreator;
import com.intellij.compiler.ant.taskdefs.Copy;
import com.intellij.compiler.ant.taskdefs.FileSet;
import com.intellij.compiler.ant.taskdefs.Include;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.elements.*;
import com.intellij.packaging.impl.elements.FileOrDirectoryCopyPackagingElement;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingElementPresentation;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 25.03.2014
 * Time: 01:43
 */
public class ClassesCopyPackagingElement extends FileOrDirectoryCopyPackagingElement<ClassesCopyPackagingElement> {

    public static final ClassesCopyElementType CLASSES_COPY_ELEMENT_TYPE = new ClassesCopyElementType();

    public ClassesCopyPackagingElement() {
        super(CLASSES_COPY_ELEMENT_TYPE);
    }

    public ClassesCopyPackagingElement(String directoryWithFileName) {
        this();
        myFilePath = directoryWithFileName;
    }

    public PackagingElementPresentation createPresentation(@NotNull ArtifactEditorContext context) {
        return new PackagingElementPresentation() {
            @Override
            public String getPresentableName() {
                return "Classes copied from "+myFilePath+"[$*].class";
            }

            @Override
            public void render(@NotNull PresentationData presentationData, SimpleTextAttributes mainAttributes,
                               SimpleTextAttributes commentAttributes) {
                presentationData.setIcon(AllIcons.Nodes.CompiledClassesFolder);
                presentationData.addText(getPresentableName(), mainAttributes);
            }

            @Override
            public int getWeight() {
                return 0;
            }
        };
    }

    public ClassesCopyPackagingElement getState() {
        return this;
    }

    public void loadState(ClassesCopyPackagingElement state) {
        setFilePath(state.getFilePath());
    }



    public List<? extends Generator> computeAntInstructions(@NotNull PackagingElementResolvingContext resolvingContext, @NotNull AntCopyInstructionCreator creator,
                                                            @NotNull ArtifactAntGenerationContext generationContext,
                                                            @NotNull ArtifactType artifactType) {

        return Collections.emptyList();

        /*
        String fileOutputPath = Utils.getVirtualFileOutputPath(virtualFile, resolvingContext.getProject());

        return Collections.singletonList(this.createClassContentCopyInstruction(((DirectoryAntCopyInstructionCreator) creator).getOutputDirectory(), fileOutputPath, virtualFile.getNameWithoutExtension()));
        */
    }

    @Override
    public void computeIncrementalCompilerInstructions(@NotNull IncrementalCompilerInstructionCreator creator,
                                                       @NotNull PackagingElementResolvingContext resolvingContext,
                                                       @NotNull ArtifactIncrementalCompilerContext compilerContext, @NotNull ArtifactType artifactType) {

        /*
        VirtualFile fileOutputPath = LocalFileSystem.getInstance().findFileByIoFile(new File(Utils.getVirtualFileOutputPath(virtualFile, resolvingContext.getProject())));

        creator.addDirectoryCopyInstructions(fileOutputPath, new PackagingFileFilter() {
            @Override
            public boolean accept(@NotNull VirtualFile virtualFile, @NotNull CompileContext context) {

                    return (virtualFile.getExtension().equals("class") &&
                            (virtualFile.getNameWithoutExtension().equals(ClassesCopyPackagingElement.this.virtualFile.getNameWithoutExtension())
                                    || virtualFile.getNameWithoutExtension().startsWith(ClassesCopyPackagingElement.this.virtualFile.getNameWithoutExtension() + "$")));

            }
        });
        */

    }

    @Override
    public String toString() {
        return "<classes-copy>";
    }
}
