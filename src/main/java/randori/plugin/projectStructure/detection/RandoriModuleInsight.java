package randori.plugin.projectStructure.detection;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.jetbrains.annotations.NotNull;

import randori.compiler.bundle.IBundle;
import randori.compiler.bundle.IBundleLibrary;
import randori.compiler.bundle.io.BundleReader;
import randori.plugin.library.RandoriLibraryType;
import randori.plugin.module.RandoriModuleType;

import com.intellij.ide.util.DelegatingProgressIndicator;
import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ModuleInsight;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.xml.NanoXmlUtil;

/**
 * @author Frédéric THOMAS Date: 19/04/13 Time: 20:22
 */
class RandoriModuleInsight extends ModuleInsight
{
    public RandoriModuleInsight(DelegatingProgressIndicator progress, Set<String> existingModuleNames,
            Set<String> existingProjectLibraryNames)
    {
        super(progress, existingModuleNames, existingProjectLibraryNames);
    }

    @Override
    protected ModuleDescriptor createModuleDescriptor(File moduleContentRoot,
            Collection<DetectedProjectRoot> sourceRoots)
    {
        return new ModuleDescriptor(moduleContentRoot, RandoriModuleType.getInstance(), sourceRoots);
    }

    @Override
    public boolean isApplicableRoot(DetectedProjectRoot root)
    {
        return root instanceof RandoriModuleSourceRoot;
    }

    @Override
    protected boolean isSourceFile(File file)
    {
        return RandoriProjectStructureDetector.isRandoriSourceFile(file);
    }

    @Override
    protected void scanSourceFileForImportedPackages(CharSequence chars, Consumer<String> result)
    {
        Lexer lexer = LanguageParserDefinitions.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
                .createLexer(null);
        lexer.start(chars);

        if (RandoriProjectStructureDetector.readPackageName(chars, lexer) == null)
        {
            return;
        }
        while (true)
            if ((lexer.getTokenType() != null) && (lexer.getTokenType() != JSTokenTypes.IMPORT_KEYWORD))
            {
                lexer.advance();
            }
            else
            {
                if (lexer.getTokenType() == null)
                {
                    break;
                }
                lexer.advance();
                RandoriProjectStructureDetector.skipWhiteSpaceAndComments(lexer);
                String packageName = RandoriProjectStructureDetector.readQualifiedName(chars, lexer, true);
                if (packageName != null)
                {
                    String s = packageName.endsWith(".*") ? StringUtil.trimEnd(packageName, ".*") : StringUtil
                            .getPackageName(packageName);
                    if (!s.isEmpty())
                        result.consume(s);
                }
            }
    }

    @Override
    protected boolean isLibraryFile(String fileName)
    {
        return fileName.toLowerCase().endsWith(".swc")
                || fileName.toLowerCase().endsWith(RandoriLibraryType.LIBRARY_DOT_EXTENSION);
    }

    @Override
    protected void scanLibraryForDeclaredPackages(@NotNull final File file, final Consumer<String> result)
            throws IOException
    {
        if (file.getName().toLowerCase().endsWith(RandoriLibraryType.LIBRARY_DOT_EXTENSION))
        {
            final Consumer<ZipInputStream> extractedSwcList = new Consumer<ZipInputStream>() {
                @Override
                public void consume(ZipInputStream swcZipInputStream)
                {
                    try
                    {
                        scanSwcLibraryForDeclaredPackages(swcZipInputStream, result);
                    }
                    catch (IOException e)
                    {
                        //e.printStackTrace();
                    }
                }
            };
            extractSwcFromRbl(file, extractedSwcList);
        }
        else
        {
            FileInputStream swcInputStream = null;
            try
            {
                swcInputStream = new FileInputStream(file);
                ZipInputStream swcZipInputStream = new ZipInputStream(swcInputStream);
                scanSwcLibraryForDeclaredPackages(swcZipInputStream, result);
            }
            finally
            {
                if (swcInputStream != null)
                    try
                    {
                        swcInputStream.close();
                    }
                    catch (IOException ignored)
                    {
                    }
            }
        }
    }

    private void extractSwcFromRbl(File file, final Consumer<ZipInputStream> result) throws IOException
    {
        ZipFile zipFile = new ZipFile(file);
        IBundle bundle = new BundleReader(file.getPath()).getBundle();
        InputStream swcInputStream;

        for (IBundleLibrary library : bundle.getLibraries())
        {
            swcInputStream = getSwcInputStream(zipFile, library.getName());

            if (swcInputStream != null)
                result.consume(new ZipInputStream(swcInputStream));
        }
    }

    private static InputStream getSwcInputStream(ZipFile zipFile, String filename) throws IOException
    {
        ZipEntry zipEntry = null;
        InputStream inputStream = null;

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();

            if (entry.getName().endsWith(filename + ".swc"))
            {
                zipEntry = entry;
                break;
            }
        }

        if (zipEntry != null)
            inputStream = zipFile.getInputStream(zipEntry);

        return inputStream;
    }

    private void scanSwcLibraryForDeclaredPackages(ZipInputStream swc, final Consumer<String> result)
            throws IOException
    {
        ZipEntry e;
        while ((e = swc.getNextEntry()) != null)
        {
            if ((!e.isDirectory()) && ("catalog.xml".equals(e.getName())))
            {
                InputStreamReader reader = new InputStreamReader(swc, "UTF-8");
                NanoXmlUtil.parse(reader, new NanoXmlUtil.IXMLBuilderAdapter() {
                    private boolean processingDef;

                    public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr)
                            throws Exception
                    {
                        if (name.equals("def"))
                            this.processingDef = true;
                    }

                    public void endElement(String name, String nsPrefix, String nsURI) throws Exception
                    {
                        if (name.equals("def"))
                            this.processingDef = false;
                    }

                    public void addAttribute(String name, String nsPrefix, String nsURI, String value, String type)
                            throws Exception
                    {
                        if ((this.processingDef) && (name.equals("id")))
                        {
                            String fqn = value.replace(':', '.');
                            String packageName = StringUtil.getPackageName(fqn);
                            if (!packageName.isEmpty())
                                result.consume(packageName);
                        }
                    }
                });
            }
        }
    }
}
