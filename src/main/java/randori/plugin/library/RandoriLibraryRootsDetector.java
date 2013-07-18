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

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryRootType;
import com.intellij.openapi.roots.libraries.ui.DetectedLibraryRoot;
import com.intellij.openapi.roots.libraries.ui.impl.LibraryRootsDetectorImpl;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 06:37
 */
class RandoriLibraryRootsDetector extends LibraryRootsDetectorImpl
{

    private static final Condition<DetectedLibraryRoot> DETECTED_LIBRARY_ROOT_CONDITION = new Condition<DetectedLibraryRoot>() {
        @Override
        public boolean value(DetectedLibraryRoot root)
        {
            LibraryRootType libraryRootType = root.getTypes().get(0);
            return (libraryRootType.getType() == OrderRootType.CLASSES) && (libraryRootType.isJarDirectory()) || libraryRootType.getType() == OrderRootType.SOURCES;
        }
    };

    public RandoriLibraryRootsDetector()
    {
        super(Arrays.asList(new RandoriLibraryBinariesRootDetector(), new RandoriDocsRootDetector(),
                new RandoriSourcesRootDetector()));
    }

    public Collection<DetectedLibraryRoot> detectRoots(@NotNull VirtualFile rootCandidate,
            @NotNull ProgressIndicator progressIndicator)
    {
        Collection<DetectedLibraryRoot> roots = super.detectRoots(rootCandidate, progressIndicator);
        boolean libFoldersFound = ContainerUtil.find(roots, DETECTED_LIBRARY_ROOT_CONDITION) != null;
        final List<LibraryRootType> types = Arrays.asList(new LibraryRootType(OrderRootType.CLASSES, false),
                new LibraryRootType(OrderRootType.SOURCES, false));

        if (libFoldersFound)
        {
            Collections.reverse(types);
        }

        return ContainerUtil.map(roots, new Function<DetectedLibraryRoot, DetectedLibraryRoot>() {
            @Override
            public DetectedLibraryRoot fun(DetectedLibraryRoot root)
            {
                if (root.getTypes().get(0).getType() == OrderRootType.SOURCES)
                {
                    return new DetectedLibraryRoot(root.getFile(), types);
                }
                return root;
            }
        });
    }

    public String getRootTypeName(@NotNull LibraryRootType rootType)
    {
        if (rootType.getType() == OrderRootType.SOURCES)
        {
            return FlexBundle.message("sources.root.detector.name");
        }

        if (rootType.getType() == OrderRootType.CLASSES) {
            if (rootType.isJarDirectory()) {
                return "Folder with RBLs or SWCs";
            }

            return FlexBundle.message("as.libraries.root.detector.name");
        }

        if ((rootType.getType() instanceof JavadocOrderRootType))
        {
            return FlexBundle.message("docs.root.detector.name");
        }
        return null;
    }
}
