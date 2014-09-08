package com.github.janwaros.QuickArtifact.actions;

import com.github.janwaros.QuickArtifact.artifacts.QuickArtifact;
import com.github.janwaros.QuickArtifact.exceptions.QuickArtifactException;
import com.github.janwaros.QuickArtifact.utils.Utils;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.actions.SelectedFilesHelper;
import com.intellij.openapi.vcs.changes.ui.ChangeListChooser;
import com.intellij.openapi.vcs.changes.ui.ChangesListView;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 29.06.2014
 * Time: 22:04
 */
public class ChangelistMenuAction extends AnAction {


    public ChangelistMenuAction() {
        super(AllIcons.Actions.Compile);
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

        final VirtualFile[] files = Utils.getCompilableFiles(project, CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext));
        if (files.length == 0) {
            presentation.setEnabled(false);
            presentation.setVisible(!ActionPlaces.isPopupPlace(event.getPlace()));
            return;
        }

        String elementDescription = null;

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

        if (aPackage != null) {
            String name = aPackage.getQualifiedName();
            if (name.length() == 0) {
                name = "<default>";
            }
            elementDescription = "'" + name + "'";
        }
        else if (files.length == 1) {
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
            elementDescription = "selected changes";
        }


        if (elementDescription == null) {
            presentation.setEnabled(false);
            return;
        }

        presentation.setText(Utils.createPresentationText(elementDescription), true);
        presentation.setEnabled(true);

    }

    public void actionPerformed(AnActionEvent event) {
        try {
            DataContext dataContext = event.getDataContext();
            Project project = CommonDataKeys.PROJECT.getData(dataContext);
            QuickArtifact.QuickArtifactBuilder quickArtifactBuilder = QuickArtifact.withProject(project);

            VirtualFile[] files = Utils.getCompilableFiles(project, CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext));
            if (files.length > 0) {
                quickArtifactBuilder.withFiles(files);
            }

            quickArtifactBuilder.create().build();
        } catch (QuickArtifactException ce) {
            throw new RuntimeException(ce);
        }



//        final Project project = e.getData(CommonDataKeys.PROJECT);
//        if (project == null) return;
//        if (! ProjectLevelVcsManager.getInstance(project).hasActiveVcss()) return;
//        Change[] changes = e.getData(VcsDataKeys.CHANGES);
//        List<VirtualFile> unversionedFiles = e.getData(ChangesListView.UNVERSIONED_FILES_DATA_KEY);
//
//        final List<VirtualFile> changedFiles = new ArrayList<VirtualFile>();
//        boolean activateChangesView = false;
//        final List<Change> changesList = new ArrayList<Change>();
//        if (changes != null) {
//            changesList.addAll(Arrays.asList(changes));
//        }
//        if(unversionedFiles != null) {
//            changedFiles.addAll(unversionedFiles);
//        }
//        //changesList.addAll(getChangesForSelectedFiles(project, unversionedFiles, changedFiles, e));
//        activateChangesView = true;
//
//        if (changesList.isEmpty() && unversionedFiles.isEmpty()) {
//            VcsBalloonProblemNotifier.showOverChangesView(project, "Nothing is selected that can be moved", MessageType.INFO);
//            return;
//        }
        //if (!askAndMove(project, changesList, unversionedFiles)) return;
//        if (activateChangesView) {
//            ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(ChangesViewContentManager.TOOLWINDOW_ID);
//            if (!window.isVisible()) {
//                window.activate(new Runnable() {
//                    public void run() {
//                        if (changedFiles.size() > 0) {
//                            ChangesViewManager.getInstance(project).selectFile(changedFiles.get(0));
//                        }
//                    }
//                });
//            }
//        }
    }



}
