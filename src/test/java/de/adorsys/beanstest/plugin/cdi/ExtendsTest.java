package de.adorsys.beanstest.plugin.cdi;

import javax.enterprise.inject.Alternative;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
@Alternative
//TODO why does inheritance does not work?
public class ExtendsTest extends FirstCDIFacetTest {
	@Test 
	public void test() {
		
	}

}
