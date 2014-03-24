package com.github.janwaros.QuickArtifact.artifacts;

import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.DefaultModulesProvider;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.ArtifactRootElement;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.artifacts.*;
import com.intellij.packaging.impl.elements.ProductionModuleOutputElementType;
import com.intellij.packaging.impl.elements.TestModuleOutputElementType;
import com.intellij.util.Processor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 20.03.2014
 * Time: 00:59
 */
public class QuickArtifactTemplate {

    private Project project;

    private QuickArtifactTemplate(Project project) {
        this.project = project;
    }

    public static QuickArtifactTemplate getTemplateFor(Project project) {
        return new QuickArtifactTemplate(project);


    }

    public Artifact buildArtifact(String name, String outputFilePath,  Module[] modulesToInclude, VirtualFile[] filesToInclude) {

        return new ArtifactImpl(name,
                JarArtifactType.getInstance(),
                false,
                buildPackage(name, modulesToInclude, filesToInclude),
                outputFilePath);
    }


    private CompositePackagingElement buildPackage(String name, final Module[] modulesToInclude, VirtualFile[] filesToInclude) {

        //maybe for future usage
        final boolean includeTests = false;

        final PackagingElementFactory factory = PackagingElementFactory.getInstance();
        final CompositePackagingElement<?> archive = factory.createArchive(ArtifactUtil.suggestArtifactFileName(name) + ".jar");

        OrderEnumerator orderEnumerator = ProjectRootManager.getInstance(project).orderEntries(Arrays.asList(modulesToInclude));

        if (!includeTests) {
            orderEnumerator = orderEnumerator.productionOnly();
        }
        final ModulesProvider modulesProvider = new IncludedModuleProvider(modulesToInclude) {

        };
        final OrderEnumerator enumerator = orderEnumerator.using(modulesProvider).withoutSdk().runtimeOnly().recursively();
        enumerator.forEachModule(new Processor<Module>() {
            @Override
            public boolean process(Module module) {
                if (ProductionModuleOutputElementType.ELEMENT_TYPE.isSuitableModule(modulesProvider, module)) {
                    archive.addOrFindChild(factory.createModuleOutput(module));
                }
                if (includeTests && TestModuleOutputElementType.ELEMENT_TYPE.isSuitableModule(modulesProvider, module)) {
                    archive.addOrFindChild(factory.createTestModuleOutput(module));
                }
                return true;
            }
        });

        archive.addOrFindChild(Utils.getPackagingElementForCompilableFiles(project, filesToInclude));

        final ArtifactRootElement<?> root = factory.createArtifactRootElement();
        root.addOrFindChild(archive);

        return root;
    }

    private class IncludedModuleProvider extends DefaultModulesProvider {
        private Module[] modules;

        private IncludedModuleProvider(Module[] modules) {
            super(project);
            this.modules = modules;
        }

        public Module[] getModules() {
            return this.modules;
        }
    }

}
