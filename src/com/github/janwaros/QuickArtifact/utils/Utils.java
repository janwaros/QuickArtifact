package com.github.janwaros.QuickArtifact.utils;

import com.github.janwaros.QuickArtifact.artifacts.packaging.QuickArtifactPackagingElement;
import com.github.janwaros.QuickArtifact.exceptions.QuickArtifactException;
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
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.psi.*;

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

    public static String getVirtualFileOutputPath(VirtualFile file, Project project) {

        final PsiManager psiManager = PsiManager.getInstance(project);

        final Module module = ModuleUtil.findModuleForFile(file, project);
        final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);

        VirtualFile moduleOutputPath = extension.getCompilerOutputPath();

        if (file.isDirectory()) {
            final PsiDirectory directory = psiManager.findDirectory(file);
            return moduleOutputPath.getPath() + "/" + JavaDirectoryService.getInstance().getPackage(directory).getQualifiedName().replaceAll("\\.", "/");
        } else {
            final PsiFile psiFile = psiManager.findFile(file);
            return moduleOutputPath.getPath() + "/" + JavaDirectoryService.getInstance().getPackage(psiFile.getParent()).getQualifiedName().replaceAll("\\.", "/");
        }
    }

    /*
    public static CompositePackagingElement getPackagingElementForCompilableFiles(Project project, VirtualFile[] files) throws QuickArtifactException {
        try {
            final PsiManager psiManager = PsiManager.getInstance(project);
            final PackagingElementFactory factory = PackagingElementFactory.getInstance();
            final CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);

            final PackagingElement compositePackagingElement = new QuickArtifactPackagingElement();

            for (final VirtualFile file : files) {

                final Module module = ModuleUtil.findModuleForFile(file, project);
                final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);

                if (extension == null) {
                    throw new QuickArtifactException("CompilerModuleExtension for module "+module+" is null!");
                }

                VirtualFile moduleOutputPath = extension.getCompilerOutputPath();

                if (file.isDirectory()) {
                    final PsiDirectory directory = psiManager.findDirectory(file);
                    String relativePath = JavaDirectoryService.getInstance().getPackage(directory).getQualifiedName().replaceAll("\\.", "/");
                    compositePackagingElement.addOrFindChild(factory.createDirectoryCopyWithParentDirectories(moduleOutputPath.getPath() + "/" + relativePath, relativePath));
                } else {

                    final PsiFile psiFile = psiManager.findFile(file);
                    String relativePath = JavaDirectoryService.getInstance().getPackage(psiFile.getParent()).getQualifiedName().replaceAll("\\.", "/");

                    String outputParentDirectory = moduleOutputPath.getPath() + "/" + relativePath;

                    if (compilerConfiguration.isResourceFile(file)) {

                        compositePackagingElement.addOrFindChild(factory.createFileCopyWithParentDirectories(outputParentDirectory + "/" + file.getName(), relativePath));

                    } else {

                        if(psiFile instanceof PsiJavaFile ) {
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;

                            for (PsiClass psiClass : psiJavaFile.getClasses()) {
                                psiClass.getQualifiedName().replaceAll("\\.", "/");
                                //compositePackagingElement.addOrFindChild(factory.createFileCopyWithParentDirectories(classFile.getPath(), relativePath));

                            }
                        }
                    }
                }
            }

            return compositePackagingElement;
        } catch (QuickArtifactException ce) {
            throw ce;
        } catch (Exception e) {
            throw new QuickArtifactException(e);
        }
    }
    */

}
