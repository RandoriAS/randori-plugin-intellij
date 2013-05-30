package randori.plugin.components;

import com.intellij.ide.highlighter.ArchiveFileType;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import icons.RandoriIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Frédéric THOMAS Date: 30/04/13 Time: 15:38
 */
public class RandoriApplicationComponent extends FileTypeFactory implements ApplicationComponent
{
    private static final String RBL_EXTENSION = "rbl";
    private static final String SWC_EXTENSION = "swc";

    public static final Language DECOMPILED_RBL = new Language(JavaScriptSupportLoader.ECMA_SCRIPT_L4, "Decompiled RBL") {

    };

    @Override
    public void initComponent()
    {
        TConfig.get().setArchiveDetector(new TArchiveDetector("rbl|swc", new ZipDriver(IOPoolLocator.SINGLETON)));

    }

    @Override
    public void disposeComponent()
    {
        try {
            TVFS.umount();
        } catch (FsSyncException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer)
    {
        consumer.consume(ArchiveFileType.INSTANCE, RBL_EXTENSION);
        consumer.consume(RBL_FILE_TYPE, RBL_EXTENSION);

        consumer.consume(ArchiveFileType.INSTANCE, SWC_EXTENSION);
        consumer.consume(SWC_FILE_TYPE, SWC_EXTENSION);
    }

    @NotNull
    @Override
    public String getComponentName()
    {
        return "RandoriApplicationComponent";
    }

    public static final FileType RBL_FILE_TYPE = new FileType() {

        @NotNull
        @Override
        public String getName()
        {
            return "RBL";
        }

        @NotNull
        @Override
        public String getDescription()
        {
            return "Randori Resource Bundle Library";
        }

        @NotNull
        @Override
        public String getDefaultExtension()
        {
            return RBL_EXTENSION;
        }

        public Icon getIcon()
        {
            return RandoriIcons.Randori16;
        }

        @Override
        public boolean isBinary()
        {
            return true;
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }

        @Nullable
        @Override
        public String getCharset(@NotNull VirtualFile file, byte[] content)
        {
            return null;
        }

    };

    public static final FileType SWC_FILE_TYPE = new FileType() {

        @NotNull
        @Override
        public String getName()
        {
            return "SWC";
        }

        @NotNull
        @Override
        public String getDescription()
        {
            return "Action Script Library";
        }

        @NotNull
        @Override
        public String getDefaultExtension()
        {
            return SWC_EXTENSION;
        }

        public Icon getIcon()
        {
            return RandoriIcons.Randori16;
        }

        @Override
        public boolean isBinary()
        {
            return true;
        }

        @Override
        public boolean isReadOnly()
        {
            return true;
        }

        @Nullable
        @Override
        public String getCharset(@NotNull VirtualFile file, byte[] content)
        {
            return null;
        }

    };
}
