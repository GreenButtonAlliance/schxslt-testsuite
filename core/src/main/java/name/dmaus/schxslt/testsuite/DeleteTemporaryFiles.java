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

import java.util.Set;
import java.util.HashSet;

import java.util.List;
import java.util.ArrayList;

import java.util.Comparator;

import java.nio.file.Path;
import java.nio.file.Files;

import java.io.IOException;

/**
 * Collect and delete temporary files on shutdown.
 *
 * <p>Serves as functional replacement for File.deleteOnExit().</p>
 *
 */
final class DeleteTemporaryFiles
{
    static final Set<Path> FILES = new HashSet<Path>();

    private DeleteTemporaryFiles ()
    {
    }

    // This is a static initializer. It is run when an instance of the class is instantiated, a static function or
    // member is accessed.
    static {
        Runtime
            .getRuntime()
            .addShutdownHook(new Thread(DeleteTemporaryFiles::onRuntimeShutdown));
    }

    static void add (final Path fileOrDirectory)
    {
        FILES.add(fileOrDirectory);
    }


    static void onRuntimeShutdown ()
    {
        List<Path> files = new ArrayList<Path>(FILES);
        files.sort(Comparator.comparing(Path::getNameCount).reversed());
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException | SecurityException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
