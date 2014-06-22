package com.github.janwaros.QuickArtifact.artifacts.packaging;

import com.github.janwaros.QuickArtifact.exceptions.QuickArtifactException;
import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.compiler.ant.BuildProperties;
import com.intellij.compiler.ant.Generator;
import com.intellij.compiler.ant.Tag;
import com.intellij.compiler.ant.artifacts.DirectoryAntCopyInstructionCreator;
import com.intellij.compiler.ant.taskdefs.Copy;
import com.intellij.compiler.ant.taskdefs.FileSet;
import com.intellij.compiler.ant.taskdefs.Include;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.elements.*;
import com.intellij.packaging.impl.elements.PackagingElementFactoryImpl;
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
public class QuickArtifactPackagingElement extends PackagingElement<QuickArtifactPackagingElement> {

    private VirtualFile virtualFile;


    public QuickArtifactPackagingElement(VirtualFile virtualFile) {
        super(PackagingElementFactoryImpl.ARTIFACT_ROOT_ELEMENT_TYPE);
        this.virtualFile = virtualFile;
    }

    public PackagingElementPresentation createPresentation(@NotNull ArtifactEditorContext context) {
        return new PackagingElementPresentation() {
            @Override
            public String getPresentableName() {
                return "QuickArtifact output of "+virtualFile.toString();
            }

            @Override
            public void render(@NotNull PresentationData presentationData, SimpleTextAttributes mainAttributes,
                               SimpleTextAttributes commentAttributes) {
                presentationData.setIcon(AllIcons.Nodes.CopyOfFolder);
                presentationData.addText(getPresentableName(), mainAttributes);
            }

            @Override
            public int getWeight() {
                return 0;
            }
        };
    }

    public QuickArtifactPackagingElement getState() {
        return this;
    }

    public void loadState(QuickArtifactPackagingElement state) {
    }

    @NotNull
    public Tag createClassContentCopyInstruction(String myOutputDirectory, String classOutputDir, @NotNull String classFileName) {
        final Copy copy = new Copy(myOutputDirectory);
        final FileSet fileSet = new FileSet(classOutputDir);
        fileSet.add(new Include("/"+classFileName+".class"));
        fileSet.add(new Include("/"+classFileName+"$*.class"));
        copy.add(fileSet);
        return copy;
    }

    public List<? extends Generator> computeAntInstructions(@NotNull PackagingElementResolvingContext resolvingContext, @NotNull AntCopyInstructionCreator creator,
                                                            @NotNull ArtifactAntGenerationContext generationContext,
                                                            @NotNull ArtifactType artifactType) {

        String fileOutputPath = Utils.getVirtualFileOutputPath(virtualFile, resolvingContext.getProject());

        if(virtualFile.isDirectory()) {
            return Collections.singletonList(creator.createDirectoryContentCopyInstruction(fileOutputPath));
        } else {
            return Collections.singletonList(this.createClassContentCopyInstruction(((DirectoryAntCopyInstructionCreator) creator).getOutputDirectory(), fileOutputPath, virtualFile.getNameWithoutExtension()));
        }
    }

    @Override
    public void computeIncrementalCompilerInstructions(@NotNull IncrementalCompilerInstructionCreator creator,
                                                       @NotNull PackagingElementResolvingContext resolvingContext,
                                                       @NotNull ArtifactIncrementalCompilerContext compilerContext, @NotNull ArtifactType artifactType) {

        VirtualFile fileOutputPath = LocalFileSystem.getInstance().findFileByIoFile(new File(Utils.getVirtualFileOutputPath(virtualFile, resolvingContext.getProject())));


        if(virtualFile.isDirectory()) {
            creator.addDirectoryCopyInstructions(fileOutputPath, null);
        } else {
            creator.addDirectoryCopyInstructions(fileOutputPath, new PackagingFileFilter() {
                @Override
                public boolean accept(@NotNull VirtualFile virtualFile, @NotNull CompileContext context) {

                        return (virtualFile.getExtension().equals("class") &&
                                (virtualFile.getNameWithoutExtension().equals(QuickArtifactPackagingElement.this.virtualFile.getNameWithoutExtension())
                                        || virtualFile.getNameWithoutExtension().startsWith(QuickArtifactPackagingElement.this.virtualFile.getNameWithoutExtension() + "$")));

                }
            });
        }

    }

    @Override
    public boolean isEqualTo(@NotNull PackagingElement<?> element) {
        return false;
    }

    @Override
    public String toString() {
        return "<root>";
    }
}
