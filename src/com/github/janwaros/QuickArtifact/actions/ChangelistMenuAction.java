package com.github.janwaros.QuickArtifact.actions;

import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
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
        super(ActionsBundle.actionText("Build Quick Artifact from selected changes"),
                ActionsBundle.actionDescription("Build Quick Artifact from selected changes"),
                AllIcons.Actions.Compile);
    }

    public void update(AnActionEvent e) {
        final boolean isEnabled = isEnabled(e);
        if (ActionPlaces.isPopupPlace(e.getPlace())) {
            e.getPresentation().setVisible(isEnabled);
        }
        else {
            e.getPresentation().setEnabled(isEnabled);
        }
    }

    private static boolean isEnabled(final AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) return false;

        final ChangeList[] lists = e.getData(VcsDataKeys.CHANGE_LISTS);


        final List<VirtualFile> unversionedFiles = e.getData(ChangesListView.UNVERSIONED_FILES_DATA_KEY);
        if (unversionedFiles != null && (! unversionedFiles.isEmpty())) return true;

        final boolean hasChangedOrUnversionedFiles = SelectedFilesHelper.hasChangedOrUnversionedFiles(project, e);
        if (hasChangedOrUnversionedFiles) return true;
        Change[] changes = e.getData(VcsDataKeys.CHANGES);
        if (changes != null && changes.length > 0) {
            return true;
        }
        final VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        return files != null && files.length > 0;
    }

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) return;
        if (! ProjectLevelVcsManager.getInstance(project).hasActiveVcss()) return;
        Change[] changes = e.getData(VcsDataKeys.CHANGES);
        List<VirtualFile> unversionedFiles = e.getData(ChangesListView.UNVERSIONED_FILES_DATA_KEY);

        final List<VirtualFile> changedFiles = new ArrayList<VirtualFile>();
        boolean activateChangesView = false;
        unversionedFiles = new ArrayList<VirtualFile>();
        final List<Change> changesList = new ArrayList<Change>();
        if (changes != null) {
            changesList.addAll(Arrays.asList(changes));
        } else {
            changes = new Change[0];
        }
        //changesList.addAll(getChangesForSelectedFiles(project, unversionedFiles, changedFiles, e));
        activateChangesView = true;

        if (changesList.isEmpty() && unversionedFiles.isEmpty()) {
            VcsBalloonProblemNotifier.showOverChangesView(project, "Nothing is selected that can be moved", MessageType.INFO);
            return;
        }

        //if (!askAndMove(project, changesList, unversionedFiles)) return;
        if (activateChangesView) {
            ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(ChangesViewContentManager.TOOLWINDOW_ID);
            if (!window.isVisible()) {
                window.activate(new Runnable() {
                    public void run() {
                        if (changedFiles.size() > 0) {
                            ChangesViewManager.getInstance(project).selectFile(changedFiles.get(0));
                        }
                    }
                });
            }
        }
    }



}
