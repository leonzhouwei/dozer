/**
 * Copyright 2005-2017 Dozer Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dozer.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dozer.classmap.MappingFileData;
import org.dozer.config.BeanContainer;
import org.dozer.loader.MappingsSource;
import org.dozer.util.DozerClassLoader;
import org.dozer.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal class that reads and parses a single custom mapping xml file into raw ClassMap objects. Only intended for
 * internal use.
 *
 * @author tierney.matt
 * @author garsombke.franz
 */
public class MappingFileReader implements MappingsSource<URL> {

  private final Logger log = LoggerFactory.getLogger(MappingFileReader.class);

  private final MappingStreamReader streamReader;

  public MappingFileReader(XMLParserFactory parserFactory) {
    streamReader = new MappingStreamReader(parserFactory);
  }

  public MappingFileData read(String fileName) {
    DozerClassLoader classLoader = BeanContainer.getInstance().getClassLoader();
    URL url = classLoader.loadResource(fileName);
    return read(url);
  }

  public MappingFileData read(URL url) {
    MappingFileData result = null;
    InputStream stream = null;
    try {
      stream = url.openStream();
      result = streamReader.read(stream);
    } catch (IOException e) {
	      log.error("Error while loading dozer mapping file url: [" + url + "]", e);
	      MappingUtils.throwMappingException(e);
	} finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException e) {
        MappingUtils.throwMappingException(e);
      }
    }
    return result;
  }

}
