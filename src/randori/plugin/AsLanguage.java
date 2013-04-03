package randori.plugin;

import com.intellij.lang.Language;

/**
 * @author Frédéric THOMAS
 */
public class AsLanguage extends Language {

    public AsLanguage() {
        super("as");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }
}
