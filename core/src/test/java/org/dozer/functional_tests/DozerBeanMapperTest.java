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
package org.dozer.functional_tests;

import java.util.ArrayList;
import java.util.List;

import org.dozer.AbstractDozerTest;
import org.dozer.DozerBeanMapper;
import org.dozer.DozerInitializer;
import org.dozer.Mapper;
import org.dozer.MappingException;
import org.dozer.functional_tests.runner.NoProxyDataObjectInstantiator;
import org.dozer.functional_tests.support.SampleCustomBeanFactory;
import org.dozer.functional_tests.support.SampleCustomBeanFactory2;
import org.dozer.functional_tests.support.SampleDefaultBeanFactory;
import org.dozer.functional_tests.support.TestDataFactory;
import org.dozer.vo.MetalThingyIF;
import org.dozer.vo.TestObject;
import org.dozer.vo.TestObjectPrime;
import org.dozer.vo.Van;
import org.junit.Before;
import org.junit.Test;

/**
 * Very high level tests of the DozerBeanMapper. This test class is not intended to provide in-depth testing of all the
 * possible mapping use cases. The more in-depth unit tests of the DozerBeanMapper and MappingProcessor can be found in
 * other test classes within this same package. i.e) GranularDozerBeanMapperTest, MapperTest, IndexMappingTest, etc
 *
 * @author tierney.matt
 * @author garsombke.franz
 */
public class DozerBeanMapperTest extends AbstractDozerTest {

  private static Mapper mapper;
  private TestDataFactory testDataFactory = new TestDataFactory(NoProxyDataObjectInstantiator.INSTANCE);

  @Override
  @Before
  public void setUp() throws Exception {
    if (mapper == null) {
      mapper = getNewMapper(new String[]{"dozerBeanMapping.xml"});
    }
  }

  @Test(expected=MappingException.class)
  public void testNoSourceObject() throws Exception {
    mapper.map(null, TestObjectPrime.class);
    fail("should have thrown exception");
  }

  @Test(expected=MappingException.class)
  public void testNoDestinationClass() throws Exception {
    mapper.map(new TestObjectPrime(), null);
    fail("should have thrown exception");
  }

  @Test(expected=MappingException.class)
  public void testNullDestObj() throws Exception {
    Object destObj = null;
    mapper.map(new TestObject(), destObj);
    fail("should have thrown mapping exception");
  }

  @Test(expected=MappingException.class)
  public void testMapIdDoesNotExist() {
    mapper.map(new TestObject(), TestObjectPrime.class, "thisMapIdDoesNotExist");
    fail("should have thrown exception");
  }

  @Test
  public void testGeneralMapping() throws Exception {
    assertCommon(mapper);
  }

  @Test
  public void testNoMappingFilesSpecified() throws Exception {
    // Mapper can be used without specifying any mapping files. Fields that have the same name will be mapped
    // automatically.
    Mapper mapper = new DozerBeanMapper();

    assertCommon(mapper);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testDetectDuplicateMapping() throws Exception {
    Mapper myMapper = null;
    List<String> mappingFiles = new ArrayList<String>();
    mappingFiles.add("duplicateMapping.xml");
    myMapper = new DozerBeanMapper(mappingFiles);

    myMapper.map(new org.dozer.vo.SuperSuperSuperClass(), org.dozer.vo.SuperSuperSuperClassPrime.class);
    fail("should have thrown exception");
  }

  @Test
  public void testCustomBeanFactory() throws Exception {
    // -----------------------------------------------------------
    // Test that java beans get created with explicitly specified
    // custom bean factory
    // -----------------------------------------------------------

    Mapper mapper = getNewMapper(new String[]{"customfactorymapping.xml"});

    TestObjectPrime prime = mapper.map(testDataFactory.getInputGeneralMappingTestObject(), TestObjectPrime.class);
    TestObject source = mapper.map(prime, TestObject.class);

    // The following asserts will verify that the ClassMap beanFactory attr gets applied to both classes
    assertNotNull("created by factory name should not be null", prime.getCreatedByFactoryName());
    assertNotNull("created by factory name should not be null", source.getCreatedByFactoryName());
    assertEquals(SampleCustomBeanFactory.class.getName(), prime.getCreatedByFactoryName());
    assertEquals(SampleCustomBeanFactory.class.getName(), source.getCreatedByFactoryName());

    // The following asserts will verify that default configuration is being applied
    assertNotNull("created by factory name should not be null", source.getThree().getCreatedByFactoryName());
    assertEquals(SampleDefaultBeanFactory.class.getName(), source.getThree().getCreatedByFactoryName());

    // The following asserts will verify that dest or src class level attr's override classMap and default config attr's
    assertNotNull("created by factory name should not be null", prime.getThreePrime().getCreatedByFactoryName());
    assertEquals(SampleCustomBeanFactory2.class.getName(), prime.getThreePrime().getCreatedByFactoryName());

    // test returning an Interface
    Van van = new Van();
    van.setName("testName");
    MetalThingyIF car = mapper.map(van, MetalThingyIF.class);
    assertEquals("testName", car.getName());
  }

  @Test
  public void testDestroy() throws Exception {
    DozerBeanMapper mapper = new DozerBeanMapper();
    assertTrue(DozerInitializer.getInstance().isInitialized());
    mapper.destroy();
    assertFalse(DozerInitializer.getInstance().isInitialized());
  }

  @Test
  public void testGlobalNullAndEmptyString() throws Exception {
    DozerBeanMapper mapperMapNull = new DozerBeanMapper();
    DozerBeanMapper mapperNotMapNull = (DozerBeanMapper) getNewMapper(new String[]{"customGlobalConfigWithNullAndEmptyStringTest.xml"});
    Van src = new Van();
	Van dest = new Van();
	dest.setName("not null or empty");

	mapperNotMapNull.map(src, dest);
	assertEquals("not null or empty", dest.getName());

	mapperMapNull.map(src, dest);
	assertNull(dest.getName());
  }

  private void assertCommon(Mapper mapper) throws Exception {
    TestObjectPrime prime = mapper.map(testDataFactory.getInputGeneralMappingTestObject(), TestObjectPrime.class);
    TestObject source = mapper.map(prime, TestObject.class);
    TestObjectPrime prime2 = mapper.map(source, TestObjectPrime.class);

    assertEquals(prime2, prime);
  }

  private Mapper getNewMapper(String[] mappingFiles) {
    List<String> list = new ArrayList<String>();
    if (mappingFiles != null) {
      for (int i = 0; i < mappingFiles.length; i++) {
        list.add(mappingFiles[i]);
      }
    }
    Mapper mapper = new DozerBeanMapper();
    ((DozerBeanMapper) mapper).setMappingFiles(list);
    return mapper;
  }

}
