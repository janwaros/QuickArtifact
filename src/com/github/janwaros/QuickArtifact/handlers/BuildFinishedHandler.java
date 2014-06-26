package com.github.janwaros.QuickArtifact.handlers;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.impl.artifacts.ArtifactUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 13.03.2014
 * Time: 22:55
 */
public class BuildFinishedHandler implements CompileStatusNotification {
    private final Artifact quickArtifact;
    private Artifact originalArtifact;
    private final ModifiableArtifactModel model;

    public BuildFinishedHandler(Artifact quickArtifact, @Nullable Artifact originalArtifact, ModifiableArtifactModel model) {
        this.quickArtifact = quickArtifact;
        this.originalArtifact = originalArtifact;
        this.model = model;
    }

    @Override
    public void finished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {

        model.removeArtifact(quickArtifact);
        if(originalArtifact!=null) {
            model.addArtifact(originalArtifact.getName(), originalArtifact.getArtifactType(), originalArtifact.getRootElement());
        }
        new WriteAction() {
            @Override
            protected void run(final Result result) {
                model.commit();
            }
        }.execute();

        Notification notification;

        if(aborted) {
            notification = new Notification("Quick Artifact","Quick Artifact Aborted", "creation has been aborted", NotificationType.WARNING);
        } else if(errors>0) {
            notification = new Notification("Quick Artifact", "Quick Artifact Aborted", "creation has been aborted because of compilation errors", NotificationType.WARNING);
        } else {
            String path = quickArtifact.getOutputFilePath()+"/"+ArtifactUtil.suggestArtifactFileName(quickArtifact.getName()) + ".jar";
            notification = new Notification("Quick Artifact", "Quick Artifact", "created successfully, written to: "+path, NotificationType.INFORMATION);
        }

        notification.setImportant(false);
        Notifications.Bus.notify(notification);
    }
}
