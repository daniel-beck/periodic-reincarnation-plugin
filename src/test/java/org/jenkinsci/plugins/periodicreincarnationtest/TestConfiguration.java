package org.jenkinsci.plugins.periodicreincarnationtest;

import java.io.IOException;
import antlr.ANTLRException;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Result;

import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnation;
import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnationBuildCause;
import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnationGlobalConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Test class.
 * 
 * @author yboev
 * 
 */
public class TestConfiguration extends HudsonTestCase {

    /**
     * The global configuration.
     */
    private PeriodicReincarnationGlobalConfiguration config;
    /**
     * The HTML form.
     */
    private HtmlForm form;

    /**
     * TestCase for Periodic Reincarnation. Populates the configuration with
     * values and submits it. Checks if the values have been populated right and
     * waits for the PeriodicReincarnation to start restarting jobs.
     * 
     * @throws Exception
     *             exception.
     */
    @LocalData
    @Test
    public void test1() throws Exception {
        final long reccurancePeriod = 60000;
        assertNotNull(PeriodicReincarnation.get());
        final String s = "reg ex hit";
        assertEquals("PeriodicReincarnation - " + s,
                new PeriodicReincarnationBuildCause(s).getShortDescription());
        assertEquals(reccurancePeriod, PeriodicReincarnation.get()
                .getRecurrencePeriod());

        checkJobsFromLocalData();

        this.getGlobalForm();

        populateValues();

        checkPopulatedValues();

        // checkLocalConfiguration();

        Thread.sleep(1000 * 60);
    }

    /**
     * Method should test local config page, but is never called due to an error
     * Error: EcmaError: lineNumber=[969] column=[0] lineSource=[null]
     * name=[TypeError]
     * sourceName=[http://localhost:50018/static/d01de31c/scripts
     * /hudson-behavior.js] message=[TypeError: Cannot call method
     * "hasClassName" of undefined
     * (http://localhost:50018/static/d01de31c/scripts/hudson-behavior.js#969)]
     * com.gargoylesoftware.htmlunit.ScriptException: TypeError: Cannot call
     * method "hasClassName" of undefined
     * (http://localhost:50018/static/d01de31c/scripts/hudson-behavior.js#969)
     * 
     * 
     * @throws IOException
     * @throws SAXException
     * @throws Exception
     */
//    private void checkLocalConfiguration() throws IOException, SAXException,
//            Exception {
//        final HtmlPage page = new WebClient()
//                .goTo("job/afterbuild_test/configure");
//        final String allElements = page.asText();
//        assertTrue(allElements
//                .contains("Configure PeriodicReincarnation locally"));
//        HtmlForm localForm = page.getFormByName("config");
//        assertNotNull(localForm);
//
//        final HtmlInput isLocallyConfigured = localForm
//                .getInputByName("isLocallyConfigured");
//        assertNotNull("isLocallyConfigured checkbox was null!",
//                isLocallyConfigured);
//        isLocallyConfigured.setChecked(true);
//
//        final HtmlInput isEnabled = localForm.getInputByName("_.isEnabled");
//        assertNotNull("isEnabled checkbox was null!", isEnabled);
//        isEnabled.setChecked(true);
//
//        final HtmlTextInput maxDepth = localForm.getInputByName("_.maxDepth");
//        assertNotNull("MaxDepth(local) field is null!", maxDepth);
//        maxDepth.setValueAttribute("2");
//        submit(localForm);
//    }

    private void populateValues() throws Exception {
        final HtmlTextInput cronTime = form.getInputByName("_.cronTime");
        assertNotNull("Cron Time is null!", cronTime);
        cronTime.setValueAttribute("* * * * *");

        final HtmlInput activeCron = form.getInputByName("_.activeCron");
        assertNotNull("EnableDisable cron field is null!", activeCron);
        activeCron.setChecked(true);

        final HtmlInput activeTrigger = form.getInputByName("_.activeTrigger");
        assertNotNull("EnableDisable trigger field is null!", activeTrigger);
        activeTrigger.setChecked(true);

        final HtmlTextInput maxDepth = form.getInputByName("_.maxDepth");
        assertNotNull("MaxDepth field is null!", maxDepth);
        maxDepth.setValueAttribute("2");

        final HtmlInput noChange = form.getInputByName("_.noChange");
        assertNotNull("NoChange checkbox was null!", noChange);
        noChange.setChecked(true);

        final HtmlElement regExprs = (HtmlElement) form.getByXPath(
                "//tr[td='Regular Expressions']").get(0);
        assertNotNull("RegExprs is null!", regExprs);
        assertNotNull("Add button not found",
                regExprs.getFirstByXPath(".//button"));
        ((HtmlButton) regExprs.getFirstByXPath(".//button")).click();
        final HtmlTextInput regEx1 = (HtmlTextInput) regExprs
                .getFirstByXPath("//input[@name='" + "regExprs.value" + "']");
        assertNotNull("regEx1 is null!", regEx1);
        regEx1.setValueAttribute("test");

        // submit all populated values
        submit(this.form);
    }

    private void checkJobsFromLocalData() {
        final Job<?, ?> job1 = (Job<?, ?>) Hudson.getInstance().getItem(
                "test_job");
        assertNotNull("job missing.. @LocalData problem?", job1);
        assertEquals(Result.FAILURE, job1.getLastBuild().getResult());
        System.out.println("JOB1 LOG:"
                + job1.getLastBuild().getLogFile().toString());

        final Job<?, ?> job2 = (Job<?, ?>) Hudson.getInstance().getItem(
                "no_change");
        assertNotNull("job missing.. @LocalData problem?", job2);
        assertEquals(Result.FAILURE, job2.getLastBuild().getResult());
        assertNotNull(job2.getLastSuccessfulBuild());
    }

    private void checkPopulatedValues() {
        config = PeriodicReincarnationGlobalConfiguration.get();
        assertNotNull(config);
        assertEquals("* * * * *", config.getCronTime());
        try {
            config.doCheckCronTime();
        } catch (ANTLRException e) {
            Assert.fail();
        } catch (NullPointerException e2) {
            Assert.fail();
        }

        assertEquals("true", config.getActiveCron());
        assertTrue(config.isCronActive());
        assertTrue(config.isTriggerActive());
        assertEquals("true", config.getActiveCron());
        assertEquals("true", config.getActiveTrigger());
        assertEquals(1, config.getRegExprs().size());
        assertEquals("test", config.getRegExprs().get(0).getValue());
        assertEquals("true", config.getNoChange());
        assertTrue(config.isRestartUnchangedJobsEnabled());
        assertEquals(2, config.getMaxDepth());
    }

    /**
     * Finds and sets the global form. Also makes a couple of test to see if
     * everything is correct.
     * 
     * @throws IOException
     *             IO error
     * @throws SAXException
     *             SAX error
     */
    private void getGlobalForm() throws IOException, SAXException {
        final HtmlPage page = new WebClient().goTo("configure");
        final String allElements = page.asText();

        assertTrue(allElements.contains("Cron Time"));
        assertTrue(allElements.contains("Periodic Reincarnation"));
        assertTrue(allElements.contains("Max restart depth"));
        assertTrue(allElements.contains("Enable afterbuild job reincarnation"));
        assertTrue(allElements.contains("Regular Expressions"));
        assertTrue(allElements.contains("Enable cron job reincarnation"));
        assertTrue(allElements
                .contains("Restart unchanged projects failing for the first time"));

        this.form = page.getFormByName("config");
        assertNotNull("Form is null!", this.form);
    }

}