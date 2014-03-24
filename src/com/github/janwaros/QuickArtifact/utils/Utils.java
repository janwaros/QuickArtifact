package com.github.janwaros.QuickArtifact.utils;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.TranslatingCompilerFilesMonitor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.artifacts.ArtifactBySourceFileFinder;
import com.intellij.packaging.impl.elements.ArtifactRootElementImpl;
import com.intellij.packaging.impl.elements.DirectoryCopyPackagingElement;
import com.intellij.packaging.impl.elements.FileCopyPackagingElement;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 20.03.2014
 * Time: 00:28
 */
public class Utils {

    public static VirtualFile[] getCompilableFiles(Project project, VirtualFile[] files) {
        if (files == null || files.length == 0) {
            return VirtualFile.EMPTY_ARRAY;
        }
        final PsiManager psiManager = PsiManager.getInstance(project);
        final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        final CompilerManager compilerManager = CompilerManager.getInstance(project);
        final List<VirtualFile> filesToCompile = new ArrayList<VirtualFile>();
        for (final VirtualFile file : files) {
            if (!fileIndex.isInSourceContent(file)) {
                continue;
            }
            if (!file.isInLocalFileSystem()) {
                continue;
            }
            if (file.isDirectory()) {
                final PsiDirectory directory = psiManager.findDirectory(file);
                if (directory == null || JavaDirectoryService.getInstance().getPackage(directory) == null) {
                    continue;
                }
            }
            else {
                FileType fileType = file.getFileType();
                if (!(compilerManager.isCompilableFileType(fileType) || isCompilableResourceFile(project, compilerConfiguration, file))) {
                    continue;
                }
            }
            filesToCompile.add(file);
        }
        return VfsUtil.toVirtualFileArray(filesToCompile);
    }

    public static CompositePackagingElement getPackagingElementForCompilableFiles(Project project, VirtualFile[] files) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        final PackagingElementFactory factory = PackagingElementFactory.getInstance();
        final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        final CompilerManager compilerManager = CompilerManager.getInstance(project);
        final TranslatingCompilerFilesMonitor translatingCompilerFilesMonitor = ApplicationManager.getApplication().getComponent(TranslatingCompilerFilesMonitor.class);

        final CompositePackagingElement compositePackagingElement = new ArtifactRootElementImpl();

        for (final VirtualFile file : files) {

            final Module module = ModuleUtil.findModuleForFile(file, project);
            VirtualFile moduleOutputPath = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();

            if (file.isDirectory()) {
                final PsiDirectory directory = psiManager.findDirectory(file);
                String relativePath = JavaDirectoryService.getInstance().getPackage(directory).getQualifiedName().replaceAll("\\.","/");
                compositePackagingElement.addOrFindChild(factory.createDirectoryCopyWithParentDirectories(moduleOutputPath.findFileByRelativePath(relativePath).getPath(), relativePath));
            }
            else {
                final PsiDirectory directory = psiManager.findDirectory(file.getParent());
                String relativePath = JavaDirectoryService.getInstance().getPackage(directory).getQualifiedName().replaceAll("\\.","/");

                VirtualFile outputParentDirectory = moduleOutputPath.findFileByRelativePath(relativePath);

                for(VirtualFile classFile : outputParentDirectory.getChildren()) {
                    if(classFile.getNameWithoutExtension().equals(file.getNameWithoutExtension()) || classFile.getNameWithoutExtension().startsWith(file.getNameWithoutExtension()+"$")) {
                        compositePackagingElement.addOrFindChild(factory.createFileCopyWithParentDirectories(classFile.getPath(), relativePath));
                    }
                }
            }
        }

        return compositePackagingElement;
    }

    public static boolean isCompilableResourceFile(final Project project, final CompilerConfiguration compilerConfiguration, final VirtualFile file) {
        if (!compilerConfiguration.isResourceFile(file)) {
            return false;
        }
        final Collection<? extends Artifact> artifacts = ArtifactBySourceFileFinder.getInstance(project).findArtifacts(file);
        return artifacts.isEmpty();
    }

}
