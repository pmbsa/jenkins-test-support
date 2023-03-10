package com.iconsolutions.jenkins.test

import com.lesfurets.jenkins.unit.BasePipelineTest
import net.sf.json.JSONObject
import org.yaml.snakeyaml.Yaml

class PipelineTestHelper extends BasePipelineTest {

/**
* Override the setup for our purposes
*/
    @Override
    void setUp () {
        helper.scriptRoots = ['']
        helper.scriptExtension = ''
        super.setUp ()
        registerDeclarativeMethods()
        registerScriptedMethods()
        setJobVariables()
    }

/**
* Declarative pipeline methods not in the base
*
* See here:
* https://www.cloudbees.com/sites/default/files/declarative-pipeline-refcard.pdf
*/
    void registerDeclarativeMethods () {
        // For execution of the pipeline
        helper.registerAllowedMethod('execute', [], {})
        helper.registerAllowedMethod('pipeline', [Closure.class], null)
        helper.registerAllowedMethod('options', [Closure.class], null)
        helper.registerAllowedMethod('environment', [Closure.class], {Closure c->
            def envBefore = [env: binding.getVariable('env')]
            println "Env section - original env vars: ${envBefore.toString()}"
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.delegate = envBefore
            c ()

            def envNew =  envBefore.env
            envBefore.each {String k, v->
                if (k != 'env') {
                    envNew ["$k"] = v
                }
            }
            println "Env section - env vars set to: ${envNew.toString ()}"
            binding.setVariable(' env', envNew)
        })

        helper.registerAllowedMethod('triggers', [Closure.class],null)
        helper.registerAllowedMethod('pollSCM', [String.class],null)
        helper.registerAllowedMethod('cron', [String.class],null)
        helper.registerAllowedMethod('parameters', [Closure.class],null)
        helper.registerAllowedMethod('string', [Map.class],null)
        helper.registerAllowedMethod('agent', [Closure.class],null)
        helper.registerAllowedMethod('Label', [String.class],null)
        helper.registerAllowedMethod('docker', [String.class],null)
        helper.registerAllowedMethod('image', [String.class],null)
        helper.registerAllowedMethod('args', [String.class],null)
        helper.registerAllowedMethod('dockerfile', [Closure.class],null)
        helper.registerAllowedMethod('dockerfile', [Boolean.class],null)
        helper.registerAllowedMethod('timestamps', [],null)

        helper.registerAllowedMethod("stages", [Closure.class], null)
        /**
        * Skip processing stage if abort/ fail set
        */
        helper.registerAllowedMethod('stage', [String.class, Closure.class], {String stgName, Closure body->
            def status = binding.getVariable('currentBuild').result
            switch (status) {
                case 'FAILURE':
                case 'ABORTED':
                    println "Stage ${stgName} skipped-job status: ${status}"
                    break
                default:
                    return body ()
            }
        })
        helper.registerAllowedMethod('steps', [Closure.class], null)
        helper.registerAllowedMethod('script', [Closure.class], null)
        helper.registerAllowedMethod('when', [Closure.class], null)
        helper.registerAllowedMethod('expression', [Closure.class], null)
        helper.registerAllowedMethod('disableResume', [], null)
        helper.registerAllowedMethod('echo', [String.class], null)
        helper.registerAllowedMethod('post', [Closure.class], null)
        /**
        * Handling the post sections
        */
        def postResultEmulator = {String section, Closure c->
            def currentBuild = binding.getVariable ('currentBuild')
            switch (section) {
                case 'always':
                case 'changed': // How to handle changed? It may happen so just run it ..
                    return c.call()
                    break
                case 'success':
                    if (currentBuild.result == 'SUCCESS') {return c.call ()}
                    else {println "post ${section} skipped as not SUCCESS"; return null}
                    break
                case 'unstable':
                    if (currentBuild.result == 'UNSTABLE') {return c.call ()}
                    else {println "post ${section} skipped as SUCCESS"; return null}
                    break
                case 'failure':
                    if (currentBuild.result == ' FAILURE') {return c.call ()}
                    else {println "post ${section} skipped as not FAILURE"; return null}
                    break
                case 'aborted':
                    if (current Build.result == ' ABORTED') {return c.call ()}
                    else {println "post ${section} skipped as not ABORTED"; return null}
                    break
                default:
                    assert false, "post section ${section} is not recognised. Check pipeline syntax"
                    break
            }
        }

        helper.registerAllowedMethod('always', [Closure.class], postResultEmulator.curry('always'))
        helper.registerAllowedMethod('changed', [Closure.class], postResultEmulator.curry('changed'))
        helper.registerAllowedMethod('success', [Closure.class], postResultEmulator.curry('success'))
        helper.registerAllowedMethod('unstable', [Closure.class], postResultEmulator.curry (argument: ' unstable'))
        helper.registerAllowedMethod('failure', [Closure.class], postResultEmulator.curry (argument: ' failure'))
        helper.registerAllowedMethod('aborted', [Closure.class], postResultEmulator.curry('aborted'))
    }

    /**
    * Scripted Pipeline Methods not in the base class
     */
    void registerScriptedMethods() {
        helper.registerAllowedMethod('timeout', [Integer.class, Closure.class], null)
        helper.registerAllowedMethod('waitUntil', [Closure.class], null)
        helper.registerAllowedMethod('writeFile', [Map.class], null)
        helper.registerAllowedMethod('build', [Map.class], null)
        helper.registerAllowedMethod('tool', [Map.class], { t -> "${t.name}_HOME" })
        helper.registerAllowedMethod('withCredentials', [Map.class, Closure.class], null)
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], null) I
        helper.registerAllowedMethod('username Password', [Map.class], { creds -> return creds })
        helper.registerAllowedMethod('deleteDir', [], null)
        helper.registerAllowedMethod('pwd', [], { 'workspaceDirMocked' })
        helper.registerAllowedMethod('stash', [Map.class], null)
        helper.registerAllowedMethod('unstash', [Map.class], null)
        helper.registerAllowedMethod('checkout', [Closure.class], null)

        helper.registerAllowedMethod('withEnv', [List.class, Closure.class], { List list, Closure c ->
            list.each {
                //def env = helper.get
                def item = it.split('=')
                assert item.size() == 2, "withEnv List does not Look right: ${list.toString()}"
                addEnvVar(item[0], item[1])
                c.delegate = binding
                c.call()
            }
        })

        helper.registerAllowedMethod("readYaml", [Map], { Map m ->
            if (m.text) {
                return new Yaml().load(m.text)
            } else if (m.file) {
                return new Yaml().load((m.file as File).text)
            } else {
                throw new IllegalArgumentException("Key 'text' is missing in map ${m}.")
            }
        })

        helper.registerAllowedMethod("readJSON", [Map], { Map m ->
            if (m.text) {
                return new JSONObject().fromObject(m.text)
            } else if (m.file) {
                return new JSONObject().fromObject(new File(m.file).getText())
            } else {
                throw new IllegalArgumentException("Key 'text or file' is missing in map ${m}.")
            }
        })

        helper.registerAllowedMethod( "readFile", [Map], {Maps ->
            def fileEncoding = (s.encoding? s.encoding: "UTF-8")
            return new File(s.file).getText(fileEncoding)
        })
        helper.registerAllowedMethod( 'milestone', [Map.class], { label -> return "milestone ($label)" })
        helper.registerAllowedMethod( 'lock', [Map.class, Closure], null)
    }

    /**
    * Variables that Jenkins Expects
    */
    void setJobVariables() {
     /**
     * Job params - may need to override in specific tests
     */
        binding.setVariable( 'params', [:])
    /**
     * Env passed in from Jenkins - may need to override in specific tests
     */
        binding.setVariable('env', [BUILD_NUMBER: '1234', PATH: '/some/path'])
    /**
     * The currentBuild in the job
     */
        binding.setVariable( 'currentBuild', new Expando (result: 'SUCCESS', displayName: 'Build #1234'))
     /**
     * agent any
     */
         binding.setVariable( 'any', value: {})
     /**
     * agent none
     */
        binding.setVariable('none', value: {})
     /**
     * checkout scm
     */
        binding.setVariable( name: 'scm', value: {})
     /**
     * PATH
     */
        binding.setVariable( name: 'PATH', value: '/some/path')
    }

/**
* Prettier print of call stack to whatever taste
*/

    @Override
    void printCallStack() {
        println '>>>>>> pipeline call stack --------------------------------------------'
        super.printCallStack()
        println ''
    }
    def getCallStackAsStrings() {
        def callStackAsString= []
        if (!Boolean.parseBoolean (System.getProperty("printstack.disabled"))) {
            helper.callStack.each {
                callStackAsString.add(new String (it.toString().trim()))

            }
        }
        return callStackAsString
    }
/**
 * Helper for adding a params value in tests
 */
    void addParam(String name, String val, Boolean overWrite = false) {
        Map params = binding.getVariable( 'params') as Map
        if (params == null) {
            params = [:]
            binding.setVariable('params', params)
        }
        if (params[name] == null || overWrite) {
            params[name] = val
        }
    }
/**
 * Helper for adding a environment value in tests
 */
    void addEnvVar(String name, String val) {
        Map env = binding.getVariable( 'env') as Map
        if (env == null) {
            env = [:]
            binding.setVariable( 'env', env)
        }
        env[name] = val
    }
    String getJobStatus() {
        return binding.getVariable( 'currentBuild').result
    }
}
