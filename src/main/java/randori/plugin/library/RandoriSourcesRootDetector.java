/*
 * Copyright 2013 original Randori IntelliJ Plugin authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package randori.plugin.library;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import randori.plugin.projectStructure.detection.RandoriProjectStructureDetector;

import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.DistinctRootsCollection;

/**
 * @author Frédéric THOMAS Date: 17/06/13 Time: 16:37
 */
public class RandoriSourcesRootDetector extends RootDetector
{

    protected RandoriSourcesRootDetector()
    {
        super(OrderRootType.SOURCES, false, "RBL sources detector");
    }

    /**
     * Find suitable roots in {@code rootCandidate} or its descendants.
     * 
     * @param rootCandidate file selected in the file chooser by user
     * @param progressIndicator can be used to show information about the progress and to abort searching if process is
     * cancelled
     * @return suitable roots
     */
    @NotNull
    @Override
    public Collection<VirtualFile> detectRoots(@NotNull VirtualFile rootCandidate,
            @NotNull ProgressIndicator progressIndicator)
    {
        DistinctRootsCollection<VirtualFile> result = new DistinctRootsCollection<VirtualFile>() {
            @Override
            protected boolean isAncestor(@NotNull VirtualFile ancestor, @NotNull VirtualFile file)
            {
                return VfsUtilCore.isAncestor(ancestor, file, false);
            }
        };
        collectRoots(rootCandidate, result, rootCandidate, progressIndicator);

        return result;
    }

    private void collectRoots(final VirtualFile startFile, final Collection<VirtualFile> result, final VirtualFile topmostRoot,
            final ProgressIndicator progressIndicator)
    {

        VfsUtilCore.visitChildrenRecursively(startFile, new VirtualFileVisitor(VirtualFileVisitor.NO_FOLLOW_SYMLINKS) {

            @NotNull
            public VirtualFileVisitor.Result visitFileEx(@NotNull VirtualFile file)
            {
                progressIndicator.checkCanceled();
                progressIndicator.setText2(file.getPresentableUrl());
                String extension = file.getExtension();

                if (!file.isDirectory() && extension != null)
                {
                    if (JavaScriptSupportLoader.ECMA_SCRIPT_L4.equals(JavaScriptSupportLoader
                            .getLanguageDialect(extension)))
                    {
                        Pair<VirtualFile, String> root = CommonSourceRootDetectionUtil.VIRTUAL_FILE.suggestRootForFileWithPackageStatement(
                                file, topmostRoot, RandoriProjectStructureDetector.PACKAGE_NAME_FETCHER, false);
                        if (root != null)
                        {
                            VirtualFile detectedRoot;
                            detectedRoot = root.first;
                            result.add(detectedRoot);
                            if (VfsUtilCore.isAncestor(detectedRoot, startFile, false))
                            {
                                return skipTo(detectedRoot);
                            }
                        }
                    }
                }
                return CONTINUE;
            }
        });
    }
}
