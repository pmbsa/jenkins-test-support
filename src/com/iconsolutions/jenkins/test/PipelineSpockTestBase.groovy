package com.iconsolutions.jenkins.test

import spock.lang.Specification
import org.codehaus.groovy.runtime. InvokerHelper

/**

 * A base class for Spock testing using the pipeline helper

 */

class PipelineSpockTestBase extends Specification {
/**
* Delegate to the test helper
*/
    @Delegate
    PipelineTestHelper pipelineTestHelper = new PipelineTestHelper()

    @SuppressWarnings('UnnecessaryGetter')
    def script() {
        def Script = InvokerHelper.createScript( null, new Binding())
        nullScript.metaClass.invokeMethod = helper.getMethodInterceptor()
        nullScript.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        nullScript.metaClass.methodMissing helper.getMethodMissingInterceptor()
        nullScript
    }
}
