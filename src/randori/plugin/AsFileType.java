package randori.plugin;

import javax.swing.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;

/**
 * @author Frédéric THOMAS
 */
public class AsFileType extends LanguageFileType {

    public static final AsFileType AS_FILE_TYPE = new AsFileType();
    public static final Language AS_LANGUAGE = AS_FILE_TYPE.getLanguage();
    @NonNls
    public static final String DEFAULT_EXTENSION = "as";

    private AsFileType() {
        super(new AsLanguage());
    }

    @NotNull
    @Override
    public String getName() {
        return "ActionScript";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "ActionScript File";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;  // TODO implement method
    }
}
