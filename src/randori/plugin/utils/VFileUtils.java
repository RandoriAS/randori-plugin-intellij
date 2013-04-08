/***
 * Copyright 2013 Teoti Graphix, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * @author Michael Schmalle <mschmalle@teotigraphix.com>
 */

package randori.plugin.utils;

import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Michael Schmalle
 */
public class VFileUtils
{

    /**
     * Returns a VirtualFile from the absolute path given.
     * 
     * @param path The native file path.
     */
    public static VirtualFile getFile(String path)
    {
        VirtualFile virtualFile = LocalFileSystem.getInstance()
                .findFileByIoFile(new File(path));
        return virtualFile;
    }

    /**
     * @deprecated Use FileUtilRt.extensionEquals from IntelliJ 12.1
     *
     * @param fileName
     * @param extension
     * @return
     */
    public static boolean extensionEquals(@NotNull String fileName, @NotNull String extension) {
        int extLen = extension.length();
        if (extLen == 0) {
            return fileName.indexOf('.') == -1;
        }
        int extStart = fileName.length() - extLen;
        return extStart >= 1 && fileName.charAt(extStart-1) == '.'
                && fileName.regionMatches(!SystemInfoRt.isFileSystemCaseSensitive, extStart, extension, 0, extLen);
    }

}
