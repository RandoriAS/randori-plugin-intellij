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

package randori.plugin.compiled;

import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.rof.ByteArrayReadOnlyFile;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import de.schlichtherle.truezip.zip.ZipEntry;
import de.schlichtherle.truezip.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import randori.compiler.bundle.Bundle;
import randori.compiler.bundle.IBundle;
import randori.compiler.bundle.IBundleLibrary;
import randori.compiler.bundle.io.BundleReader;
import randori.compiler.bundle.io.StAXManifestReader;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Frédéric THOMAS Date: 27/04/13 Time: 15:57
 */
public class RblFileDecompiler implements BinaryFileDecompiler
{
    private static final Logger LOG = Logger.getInstance(RblFileDecompiler.class);
    private static final String MANIFEST_XML = "manifest.xml";
    private static final String libSeparator = "\n";

    @NotNull
    public CharSequence decompile(VirtualFile file)
    {
        TConfig.get().setArchiveDetector(new TArchiveDetector("rbl|swc", new ZipDriver(IOPoolLocator.SINGLETON)));

        final StringBuilder libraryInterface = new StringBuilder(ArrayUtil.EMPTY_CHAR_SEQUENCE);
        Project project = findProject();

        if (project != null)
            try
            {
                final Consumer<InputStream> extractedLibraryConsumer = new Consumer<InputStream>() {

                    @Override
                    public void consume(InputStream libraryInputStream)
                    {
                        if (libraryInputStream != null)
                            libraryInterface.append(FlexImporter.buildInterfaceFromStream(libraryInputStream));
                    }
                };
                extractLibraries(file, extractedLibraryConsumer);
            }
            catch (Exception e)
            {
                LOG.warn(file.getPath(), e);
            }

        return libraryInterface.toString();
    }

    /**
     * Extract the library for each declared bundle in the RBL.
     *
     * @param rblFile The RBL file.
     * @param result The processor used to consume the resulting extracted library.
     * @throws Exception
     */
    public static void extractLibraries(VirtualFile rblFile, final Consumer<InputStream> result)
            throws Exception
    {
        IBundle bundle = new BundleReader(rblFile.getPath()).getBundle();

        for (IBundleLibrary iBundleLibrary : bundle.getLibraries())
        {
            File library = new TFile(rblFile.getPath() + File.separator + iBundleLibrary.getName() + File.separator + "bin"
                    + File.separator + "swc" + File.separator + iBundleLibrary.getName() + ".swc" + File.separator
                    + "library.swf");

            InputStream inputStream = new TFileInputStream(library);
            result.consume(inputStream);
        }
    }

    /**
     * Extract the library for each declared bundle in the RBL.
     * It doesn't extract it from the rblFile itself but from its content.
     *
     * @param rblFile The RBL file.
     * @param content The content of the RBL file.
     * @return An InputStream sequence of the extracted libraries.
     * @throws IOException
     * @throws XMLStreamException
     */
    public static InputStream extractLibraries(VirtualFile rblFile, byte[] content) throws IOException, XMLStreamException {
        final List<InputStream> libraries = new ArrayList<InputStream>();
        SequenceInputStream librariesInputStream = null;
        InputStream swcInputStream;
        byte[] swcBytes;
        ZipFile swc;
        InputStream library;
        ZipEntry entry;

        final Bundle bundle = new Bundle(new File(rblFile.getPath()));
        final ByteArrayReadOnlyFile rof = new ByteArrayReadOnlyFile(content);
        final ZipFile rbl = new ZipFile(rof);

        readCatalog(rbl, bundle);

        for (IBundleLibrary iBundleLibrary : bundle.getLibraries())
        {
            entry = rbl.getEntry(iBundleLibrary.getName() + "/bin/swc/" + iBundleLibrary.getName() + ".swc");
            swcInputStream = rbl.getInputStream(entry);

            if (swcInputStream != null)
            {
                swcBytes = IOUtils.toByteArray(swcInputStream);

                if (swcBytes != null && swcBytes.length > 0)
                {
                    swc = new ZipFile(new ByteArrayReadOnlyFile(swcBytes));
                    library = swc.getInputStream("library.swf");
                    libraries.add(library);
                    libraries.add(new ByteArrayInputStream(libSeparator.getBytes()));
                }
            }
        }

        if (libraries.isEmpty())
            librariesInputStream = new SequenceInputStream(Collections.enumeration(libraries));

        return librariesInputStream;
    }

    private static InputStream readCatalog(ZipFile rbl, Bundle bundle) throws XMLStreamException, IOException {
        InputStream catalogInputStream;
        StAXManifestReader catalogReader;

        catalogInputStream = getInputStream(rbl);
        if (catalogInputStream != null)
        {
            catalogReader = new StAXManifestReader(new BufferedInputStream(catalogInputStream), bundle);
            catalogReader.parse();
            catalogReader.close();
        }

        return catalogInputStream;
    }

    private static InputStream getInputStream(ZipFile zipFile) throws IOException
    {
        ZipEntry zipEntry = null;
        for (final Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();)
        {
            final ZipEntry entry = entryEnum.nextElement();
            if (entry.getName().equals(RblFileDecompiler.MANIFEST_XML))
            {
                zipEntry = entry;
                break;
            }
        }

        return (zipEntry == null) ? null : zipFile.getInputStream(zipEntry);
    }

    private static Project findProject()
    {
        Project projects[] = ProjectManager.getInstance().getOpenProjects();

        if (projects.length > 0)
            return projects[0];

        return null;
    }
}
