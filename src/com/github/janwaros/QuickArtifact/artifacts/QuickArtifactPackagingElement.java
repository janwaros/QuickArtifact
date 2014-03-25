package com.github.janwaros.QuickArtifact.artifacts;

import com.intellij.compiler.ant.Generator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.elements.*;
import com.intellij.packaging.impl.elements.PackagingElementFactoryImpl;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingElementPresentation;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 25.03.2014
 * Time: 01:43
 */
public class QuickArtifactPackagingElement extends CompositePackagingElement<Object> {
    public QuickArtifactPackagingElement() {
        super(PackagingElementFactoryImpl.ARCHIVE_ELEMENT_TYPE);
    }

    public PackagingElementPresentation createPresentation(@NotNull ArtifactEditorContext context) {
        return new PackagingElementPresentation() {
            @Override
            public String getPresentableName() {
                return "QuickArtifact output";
            }

            @Override
            public void render(@NotNull PresentationData presentationData, SimpleTextAttributes mainAttributes,
                               SimpleTextAttributes commentAttributes) {
                presentationData.setIcon(AllIcons.Nodes.Artifact);
                presentationData.addText(getPresentableName(), mainAttributes);
            }

            @Override
            public int getWeight() {
                return 0;
            }
        };
    }

    public Object getState() {
        return null;
    }

    public void loadState(Object state) {
    }

    @Override
    public boolean canBeRenamed() {
        return false;
    }

    public void rename(@NotNull String newName) {
    }

    public List<? extends Generator> computeAntInstructions(@NotNull PackagingElementResolvingContext resolvingContext, @NotNull AntCopyInstructionCreator creator,
                                                            @NotNull ArtifactAntGenerationContext generationContext,
                                                            @NotNull ArtifactType artifactType) {
        return computeChildrenGenerators(resolvingContext, creator, generationContext, artifactType);
    }

    @Override
    public void computeIncrementalCompilerInstructions(@NotNull IncrementalCompilerInstructionCreator creator,
                                                       @NotNull PackagingElementResolvingContext resolvingContext,
                                                       @NotNull ArtifactIncrementalCompilerContext compilerContext, @NotNull ArtifactType artifactType) {
        computeChildrenInstructions(creator, resolvingContext, compilerContext, artifactType);
    }

    @Override
    public boolean isEqualTo(@NotNull PackagingElement<?> element) {
        return false;
    }

    public String getName() {
        return "";
    }

    @Override
    public String toString() {
        return "<root>";
    }
}
