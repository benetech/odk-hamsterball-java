package org.benetech.configuration.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.benetech.configuration.MvcConfiguration;
import org.benetech.configuration.SecurityConfiguration;
import org.benetech.configuration.WebClientConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Annotations used for general unit tests.
 * 
 * Remember, you can only combine *Spring* annotations into meta-annotations like this. Annotations
 * don't normally inherit so don't go sticking all your annotations in here..
 * 
 * @author Caden Howell <cadenh@benetech.org>
 */
@ContextConfiguration(
    classes = {MvcConfiguration.class, SecurityConfiguration.class, WebClientConfiguration.class})
@ActiveProfiles("unittest")
@Retention(RetentionPolicy.RUNTIME)
public @interface UnitTestConfig {

}
