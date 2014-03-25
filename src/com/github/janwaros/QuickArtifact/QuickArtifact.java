package com.github.janwaros.QuickArtifact;

import com.github.janwaros.QuickArtifact.artifacts.BuildFinishedHandler;
import com.github.janwaros.QuickArtifact.artifacts.QuickArtifactTemplate;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.impl.compiler.ArtifactCompileScope;
import com.intellij.packaging.impl.compiler.ArtifactsWorkspaceSettings;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 17.03.2014
 * Time: 22:43
 */
public class QuickArtifact {

    private Artifact lastSavedArtifact;
    private QuickArtifactBuilder builder;
    private ModifiableArtifactModel modifiableModel;
    private Artifact artifact;
    private CompilerManager compilerManager;
    private Module[] affectedModules;

    private QuickArtifact(QuickArtifactBuilder builder) {
        this.builder = builder;
        modifiableModel = ArtifactManager.getInstance(builder.project).createModifiableModel();
        compilerManager = CompilerManager.getInstance(builder.project);

        CompileScope filesScope = compilerManager.createFilesCompileScope(builder.files);
        affectedModules = (Module[])ArrayUtils.addAll(builder.modules, filesScope.getAffectedModules());

        String name = affectedModules.length == 1 ? affectedModules[0].getName() : builder.project.getName();

        artifact = QuickArtifactTemplate.getTemplateFor(builder.project).buildArtifact(name+"-QuickArtifact", builder.outputFilePath, builder.modules, builder.files);
    }

    public static QuickArtifactBuilder withProject(Project project) {
        return new QuickArtifactBuilder(project);
    }

    public static class QuickArtifactBuilder {

        Module[] modules = new Module[0];
        VirtualFile[] files = new VirtualFile[0];

        private Project project;
        private String outputFilePath;

        private QuickArtifactBuilder(Project project) {
            this.project = project;
        }

        public QuickArtifactBuilder withFiles(VirtualFile ... files) {
            this.files = files;
            return this;
        }

        public QuickArtifactBuilder withModules(Module ... modules) {
            this.modules = modules;
            return this;
        }

        public QuickArtifactBuilder withOutputFilePath(String outputFilePath) {
            this.outputFilePath = outputFilePath;
            return this;
        }

        public QuickArtifact create() {
            return new QuickArtifact(this);
        }

     }

    public QuickArtifact save(String name) {

        lastSavedArtifact = modifiableModel.addArtifact(name, artifact.getArtifactType(), artifact.getRootElement());

        new WriteAction() {
            @Override
            protected void run(final Result result) {
                modifiableModel.commit();
            }
        }.execute();

        return this;
    }

    public void build() {

        CompileScope affectedModulesScope = compilerManager.createModulesCompileScope(affectedModules, false);

        // TODO: check if it is going to work without following line
        ArtifactsWorkspaceSettings.getInstance(builder.project).setArtifactsToBuild(Arrays.asList(artifact));

        CompileScope scopeWithArtifact = ArtifactCompileScope.createScopeWithArtifacts(affectedModulesScope, Arrays.asList(artifact), true, true) ;

        save(artifact.getName());

        CompilerManager.getInstance(builder.project).compile(scopeWithArtifact, new BuildFinishedHandler(lastSavedArtifact, modifiableModel));
    }


}
