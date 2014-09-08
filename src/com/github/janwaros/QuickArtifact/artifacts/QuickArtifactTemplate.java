package com.github.janwaros.QuickArtifact.artifacts;

import com.github.janwaros.QuickArtifact.artifacts.packaging.ClassesCopyPackagingElement;
import com.github.janwaros.QuickArtifact.exceptions.QuickArtifactException;
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
import com.intellij.packaging.impl.artifacts.ArtifactImpl;
import com.intellij.packaging.impl.artifacts.ArtifactUtil;
import com.intellij.packaging.impl.artifacts.JarArtifactType;
import com.intellij.packaging.impl.elements.DirectoryPackagingElement;
import com.intellij.packaging.impl.elements.ProductionModuleOutputElementType;
import com.intellij.packaging.impl.elements.TestModuleOutputElementType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Processor;

import java.io.File;
import java.util.Arrays;

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

    public Artifact buildArtifact(String name, String outputFilePath,  Module[] modulesToInclude, VirtualFile[] filesToInclude) throws QuickArtifactException {

        return new ArtifactImpl(name,
                JarArtifactType.getInstance(),
                false,
                buildPackage(name, modulesToInclude, filesToInclude),
                outputFilePath);
    }


    private CompositePackagingElement buildPackage(String name, final Module[] modulesToInclude, VirtualFile[] filesToInclude) throws QuickArtifactException {

        //maybe for future usage
        final boolean includeTests = false;

        final PackagingElementFactory factory = PackagingElementFactory.getInstance();
        final CompositePackagingElement<?> archive = factory.createArchive(ArtifactUtil.suggestArtifactFileName(name) + ".jar");

        OrderEnumerator orderEnumerator = ProjectRootManager.getInstance(project).orderEntries(Arrays.asList(modulesToInclude));

        if (!includeTests) {
            orderEnumerator = orderEnumerator.productionOnly();
        }
        final ModulesProvider modulesProvider = new IncludedModuleProvider(modulesToInclude);

//        final OrderEnumerator enumerator = orderEnumerator.using(modulesProvider).withoutSdk().runtimeOnly().recursively();
//        enumerator.forEachModule(new Processor<Module>() {
//            @Override
//            public boolean process(Module module) {
        for(Module module : modulesProvider.getModules()) {
                if (ProductionModuleOutputElementType.ELEMENT_TYPE.isSuitableModule(modulesProvider, module)) {
                    archive.addOrFindChild(factory.createModuleOutput(module));
                }
                if (includeTests && TestModuleOutputElementType.ELEMENT_TYPE.isSuitableModule(modulesProvider, module)) {
                    archive.addOrFindChild(factory.createTestModuleOutput(module));
                }
                //return true;

        }

        for(VirtualFile file : filesToInclude) {
            String relativePath = Utils.getVirtualFileRelativeOutputPath(file, project);
            String outputPath = Utils.getVirtualFileOutputPath(file, project);
            if(file.isDirectory()) {
                archive.addOrFindChild(factory.createDirectoryCopyWithParentDirectories(outputPath, relativePath));
            } else {
                DirectoryPackagingElement dir = new DirectoryPackagingElement(relativePath);
                if(Utils.isResourceFile(project, file)) {
                    dir.addOrFindChild(new ClassesCopyPackagingElement(outputPath + "/" + file.getName()));
                }
                if(Utils.isCompilableFile(project,file)) {
                    final PsiManager psiManager = PsiManager.getInstance(project);
                    PsiFile psiFile = psiManager.findFile(file);
                    if( psiFile != null && psiFile instanceof PsiClassOwner) {
                        PsiClassOwner psiClassOwner = (PsiClassOwner)psiFile;
                        for(PsiClass psiClass : psiClassOwner.getClasses()) {
                            dir.addOrFindChild(new ClassesCopyPackagingElement(outputPath + "/" + psiClass.getName() + ".class"));
                        }
                    } else {
                        dir.addOrFindChild(new ClassesCopyPackagingElement(outputPath + "/" + file.getNameWithoutExtension() + ".class"));
                    }

                }

                archive.addOrFindChild(dir);
            }

        }

        return archive;
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
