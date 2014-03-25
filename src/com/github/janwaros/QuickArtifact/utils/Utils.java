package com.github.janwaros.QuickArtifact.utils;

import com.github.janwaros.QuickArtifact.artifacts.QuickArtifactPackagingElement;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import java.util.ArrayList;
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
                if (!(compilerManager.isCompilableFileType(fileType) || compilerConfiguration.isResourceFile(file))) {
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

        final CompositePackagingElement compositePackagingElement = new QuickArtifactPackagingElement();

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

                if(compilerConfiguration.isResourceFile(file)) {

                    compositePackagingElement.addOrFindChild(factory.createFileCopyWithParentDirectories(outputParentDirectory.findChild(file.getName()).getPath(), relativePath));

                } else {

                    for (VirtualFile classFile : outputParentDirectory.getChildren()) {
                        if (classFile.getExtension().equals("class") && (classFile.getNameWithoutExtension().equals(file.getNameWithoutExtension()) || classFile.getNameWithoutExtension().startsWith(file.getNameWithoutExtension() + "$"))) {
                            compositePackagingElement.addOrFindChild(factory.createFileCopyWithParentDirectories(classFile.getPath(), relativePath));
                        }
                    }

                }
            }
        }

        return compositePackagingElement;
    }

}
