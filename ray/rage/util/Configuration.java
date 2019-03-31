/**
 * Copyright (C) 2016 Raymond L. Rivera <ray.l.rivera@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ray.rage.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * A <i>configuration</i> is a set of {@link Properties properties} the
 * framework and clients rely on, providing clients with a single point of
 * access to communicate information that would otherwise need to be hard-coded
 * inside the framework, such as file system {@link Path paths} to specific
 * directories. The data is read from a <code>.properties</code> file containing
 * key-value pairs, which the framework then accesses by keys.
 * <p>
 * <i>All</i> specified {@link Path paths} must use the <i>Unix</i>-style
 * {@link Path path} separators (i.e. forward slash) at <i>all</i> times. This
 * class automatically replaces them for platform-specific separators to keep
 * the framework portable.
 * <p>
 * See <code>assets/config/README.md</code> for details.
 *
 * @author Raymond L. Rivera
 *
 */
public final class Configuration {

    private Properties properties = new Properties();

    /**
     * Loads the <code>.properties</code> file from the specified path.
     *
     * @param path
     *            The file system path for a <code>.properties</code> file.
     * @throws IOException
     *             If the file cannot be read.
     * @throws FileNotFoundException
     *             If the file is not found.
     * @throws IllegalArgumentException
     *             If the input stream contains malformed Unicode escape
     *             sequences.
     * @see #load()
     */
    public void load(String path) throws IOException, FileNotFoundException, IllegalArgumentException {
        String correctedPath = path.replace('/', File.separatorChar);
        InputStream stream = new FileInputStream(correctedPath);
        properties.load(stream);
        stream.close();
    }

    /**
     * Loads the default <code>rage.properties</code> file found in the
     * <code>assets/config/</code> directory.
     * <p>
     * If you want to provide your own custom <code>.properties</code> file
     * (e.g. for your own game), then see {@link #load(String)} method instead.
     *
     * @throws IOException
     *             If the file is not found or cannot be read.
     * @see #load(String)
     */
    public void load() throws IOException {
        load("assets/config/rage.properties");
    }

    /**
     * Gets the value associated with the specified key. The configuration must
     * be loaded before invoking this method.
     *
     * @param key
     *            The unique identifier of the configuration property to use
     *            when searching for a value.
     * @return The value associated with the specified key.
     * @throws RuntimeException
     *             If the given key does not exist.
     * @see #setKeyValuePair(String, String)
     */
    public String valueOf(String key) throws RuntimeException {
        try {
            // if an invalid key is provided, a null will be returned and crash
            // on replace, so make this failure more explicit/informative
            return properties.getProperty(key).replace('/', File.separatorChar);
        } catch (NullPointerException e) {
            throw new RuntimeException("Key does not exist: " + key, e);
        }
    }

    /**
     * Sets the value associated with the specified key to the one given.
     *
     * @param key
     *            The unique identifier of the configuration property to set to
     *            a new value.
     * @param value
     *            The value that will be associated by the given key.
     * @see #valueOf(String)
     */
    public void setKeyValuePair(String key, String value) {
        properties.setProperty(key, value.replace('/', File.separatorChar));
    }

}
