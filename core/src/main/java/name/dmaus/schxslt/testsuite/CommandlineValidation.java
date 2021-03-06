/*
 * Copyright (C) 2020 by David Maus <dmaus@dmaus.name>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package name.dmaus.schxslt.testsuite;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Implements validation with commandline applications like xsltproc.
 *
 */
public final class CommandlineValidation implements Validation
{
    private static final String NSSVRL = "http://purl.oclc.org/dsdl/svrl";

    private final List<Path> compilerSteps;
    private final Set<String> features = new HashSet<String>();
    private final CommandlineBuilder commandlineBuilder;
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    private Path schema;
    private Path document;
    private String phase;
    private Document report;

    public CommandlineValidation (final CommandlineBuilder commandlineBuilder, final String[] features, final List<Path> compilerSteps)
    {
        this.documentBuilderFactory.setNamespaceAware(true);
        this.commandlineBuilder = commandlineBuilder;
        this.compilerSteps = compilerSteps;
        for (int i = 0; i < features.length; i++) {
            this.features.add(features[i]);
        }
    }

    public void setSchema (final Path path)
    {
        schema = path;
    }

    public void setDocument (final Path path)
    {
        document = path;
    }

    public void setPhase (final String string)
    {
        phase = string;
    }

    public Set<String> getFeatures ()
    {
        return features;
    }

    public Document getReport ()
    {
        return report;
    }

    public boolean isValid ()
    {
        NodeList asserts = report.getElementsByTagNameNS(NSSVRL, "failed-assert");
        NodeList reports = report.getElementsByTagNameNS(NSSVRL, "successful-report");
        if (reports.getLength() + asserts.getLength() > 0) {
            return false;
        }
        return true;
    }

    public void execute () throws ValidationException
    {
        try {
            Path compiledSchematron = compileSchematron();
            CommandlineTransformer transformer = new CommandlineTransformer(compiledSchematron, commandlineBuilder);
            Path compiledReport = transformer.transform(document);

            report = documentBuilderFactory.newDocumentBuilder().parse(Files.newInputStream(compiledReport));
        } catch (Exception e) {
            throw new ValidationException(e);
        }
    }

    Path compileSchematron () throws IOException, InterruptedException
    {
        Path source = schema;
        for (Path step : compilerSteps) {
            CommandlineTransformer transformer = new CommandlineTransformer(step, commandlineBuilder);
            if (phase != null) {
                transformer.setParameter("phase", phase);
            }
            source = transformer.transform(source);
        }
        return source;
    }
}
