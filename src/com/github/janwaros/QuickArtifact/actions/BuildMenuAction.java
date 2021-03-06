package com.github.janwaros.QuickArtifact.actions;

import com.github.janwaros.QuickArtifact.artifacts.QuickArtifact;
import com.github.janwaros.QuickArtifact.exceptions.QuickArtifactException;
import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.actions.CompileActionBase;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 13.03.2014
 * Time: 22:09
 */
public class BuildMenuAction extends CompileActionBase {


    protected void doAction(DataContext dataContext, Project project) {
        try {
            QuickArtifact.QuickArtifactBuilder quickArtifactBuilder = QuickArtifact.withProject(project);

            Module[] modules = LangDataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext);

            if (modules != null) {
                quickArtifactBuilder.withModules(modules);
            }

            VirtualFile[] files = Utils.getCompilableFiles(project, CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext));
            if (files.length > 0) {
                quickArtifactBuilder.withFiles(files);
            }

            quickArtifactBuilder.create().build();
        } catch (QuickArtifactException ce) {
            throw new RuntimeException(ce);
        }

    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (!presentation.isEnabled()) {
            return;
        }
        DataContext dataContext = event.getDataContext();

        presentation.setText(ActionsBundle.actionText("Build Quick Artifact"));
        presentation.setVisible(true);

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        CompilerConfiguration compilerConfiguration = CompilerConfiguration.getInstance(project);

        Module[] modules = LangDataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext);

        final VirtualFile[] files = Utils.getCompilableFiles(project, CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext));
        if (files.length == 0 && (modules==null || modules.length==0)) {
            presentation.setEnabled(false);
            presentation.setVisible(!ActionPlaces.isPopupPlace(event.getPlace()));
            return;
        }

        String elementDescription = null;
        if (modules != null && modules.length > 0 && files.length == 0) {
            if(modules.length==1) {
                elementDescription = "Module " + "'" + modules[0].getName() + "'";
            } else {
                elementDescription = "Selected Modules";
            }
        }
        else {
            PsiPackage aPackage = null;
            if (files.length == 1) {
                final PsiDirectory directory = PsiManager.getInstance(project).findDirectory(files[0]);
                if (directory != null) {
                    aPackage = JavaDirectoryService.getInstance().getPackage(directory);
                }
            }
            else {
                PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
                if (element instanceof PsiPackage) {
                    aPackage = (PsiPackage)element;
                }
            }

            if (aPackage != null && !(modules != null && modules.length > 0)) {
                String name = aPackage.getQualifiedName();
                if (name.length() == 0) {
                    name = "<default>";
                }
                elementDescription = "'" + name + "'";
            }
            else if (files.length == 1 && !(modules != null && modules.length > 0)) {
                final VirtualFile file = files[0];
                FileType fileType = file.getFileType();
                if (CompilerManager.getInstance(project).isCompilableFileType(fileType) || compilerConfiguration.isResourceFile(file)) {
                    elementDescription = "'" + file.getName() + "'";
                }
                else {
                    if (!ActionPlaces.MAIN_MENU.equals(event.getPlace())) {
                        // the action should be invisible in popups for non-java files
                        presentation.setEnabled(false);
                        presentation.setVisible(false);
                        return;
                    }
                }
            }
            else {
                elementDescription = "Selected Files";
            }
        }

        if (elementDescription == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setText(Utils.createPresentationText(elementDescription), true);
        presentation.setEnabled(true);
    }


}

