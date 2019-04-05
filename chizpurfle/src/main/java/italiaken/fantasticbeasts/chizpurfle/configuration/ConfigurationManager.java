package italiaken.fantasticbeasts.chizpurfle.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.Utils;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.IndividualAnalyzer;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.Population;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness.BlocksCounter;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness.BranchExecutionCount;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.fitness.CoarseBranchHitCounter;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection.FitnessProportionateSelector;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection.RankingSelector;
import italiaken.fantasticbeasts.chizpurfle.fuzz.evolutionary.selection.TournamentSelector;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class ConfigurationManager {

    private static final int MAX_GENERATION_DEFAULT = 20;

    private static String serviceName;
    private static String processName;
    private static int maxGeneration = MAX_GENERATION_DEFAULT;
    private static IndividualAnalyzer.IFitnessEvaluator fitnessEvaluator;
    private static Population.IIndividualSelector individualSelector;
    private static boolean blackBoxType;
    private static boolean singleCall;
    private static String methodName;
    private static Object[] args;
    private static boolean isExtraction;

    public static void initConfiguration(String[] args) throws ConfigurationException {

        Options options = new Options();
        options.addOption("h", "help", false, "show help");

        options.addOption(Option.builder("service")
                .required(false)//true)
                .hasArg()
                .type(String.class)
                .longOpt("service-name")
                .desc("the name of the service under test")
                .build());

        options.addOption(Option.builder("process")
                .required(false)
                .hasArg()
                .type(String.class)
                .longOpt("process-name")
                .desc("the name of the process to trace")
                .build());

        options.addOption(Option.builder("n")
                .required(false)
                .hasArg()
                .type(Number.class)
                .longOpt("max-generation")
                .desc("the number of generations the populations should pass through")
                .build());

        options.addOption(Option.builder("bb").longOpt("black-box").desc("uses a blackbox approach").build());

        options.addOptionGroup(new OptionGroup()
                .addOption(Option.builder("f1").longOpt("blocks-counter-fitness-evaluator").desc("uses the blocks counter fitness evaluator").build())
                .addOption(Option.builder("f2").longOpt("branch-execution-fitness-evaluator").desc("uses the blocks branch execution evaluator").build())
                .addOption(Option.builder("f3").longOpt("coarse-branch-hit-fitness-evaluator").desc("uses the blocks coarse branch hit evaluator").build()));

        options.addOptionGroup(new OptionGroup()
                .addOption(Option.builder("s1").longOpt("fitness-proportionate-selection").desc("uses a fitness proportionate selection algorithm").build())
                .addOption(Option.builder("s2").longOpt("ranking-selection").desc("uses a ranking selection algorithm").build())
                .addOption(Option.builder("s3").longOpt("tournament-selection").desc("uses a tournament selection algorithm").build()));

        options.addOption(Option.builder("single")
                .longOpt("single-call")
                .build());

        options.addOption(Option.builder("method")
                .required(false)//true)
                .hasArg()
                .type(String.class)
                .longOpt("method-name")
                .desc("the name of the method under test")
                .build());

        options.addOption(Option.builder("e")
                .required(false)
                .longOpt("extract")
                .desc("Extract the model from the smartphone")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            printHelper(options);
            throw new ConfigurationException("can't handle args: "+ Arrays.toString(args), e);
        }

        if (cmd.hasOption("help")){
            printHelper(options);
        }else if (cmd.hasOption("extract")){
            isExtraction = cmd.hasOption("extract");
        } else if (cmd.hasOption("service")){
            serviceName = cmd.getOptionValue("service");

            processName = cmd.getOptionValue("process", null);
            if (processName == null)
                processName = getProcessByServiceName(serviceName);

            if (cmd.hasOption("n"))
                try {
                    maxGeneration = ((Number)cmd.getParsedOptionValue("n")).intValue();
                } catch (ParseException e) {
                    printHelper(options);
                    throw new ConfigurationException("can't handle args: "+ Arrays.toString(args), e);
                }


            if (cmd.hasOption("method")){
           	methodName = cmd.getOptionValue("method");
            } 


            blackBoxType = cmd.hasOption("bb");

            if (cmd.hasOption("f3"))
                fitnessEvaluator = new CoarseBranchHitCounter();
            else if (cmd.hasOption("f2"))
                fitnessEvaluator = new BranchExecutionCount();
            else
                fitnessEvaluator = new BlocksCounter();

            if (cmd.hasOption("s3"))
                individualSelector = new TournamentSelector();
            else if (cmd.hasOption("s2"))
                individualSelector = new RankingSelector();
            else
                individualSelector = new FitnessProportionateSelector();

            singleCall = cmd.hasOption("single");

        }else{
            printHelper(options);
            throw new ConfigurationException("invalid sequence of args: "+ Arrays.toString(args));
        }




    }

    private static void printHelper(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, "chizpurfle",
                null, options,
                HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
                null);

        L.i(sw.toString());
    }

    public static JSONObject getThreadsList() throws ConfigurationException {


        try {
            JSONObject result = new JSONObject();

            JSONArray white = new JSONArray();
            JSONArray black = new JSONArray();

            if (serviceName.equalsIgnoreCase("ethernet"))
                white.put("ethernet");
            else if (serviceName.equalsIgnoreCase("ABTPersistenceService"))
                white.put("abt");
            else if (serviceName.equalsIgnoreCase("spengestureservice")){
                white.put("input");
                white.put("ui");
                white.put("binder");
            }else if (serviceName.equalsIgnoreCase("sensorservice"))
                white.put("sensor");
            else if (serviceName.equalsIgnoreCase("CocktailBarService"))
                white.put("bar");
            else if (serviceName.equalsIgnoreCase("knoxcustom"))
                white.put("knox");
            else if (serviceName.equalsIgnoreCase("hwAlarmService")){
                white.put("alarm");
                white.put("hw");
            }else if (serviceName.equalsIgnoreCase("hwConnectivityExService")){
                white.put("connectivity");
                white.put("hw");
            }else if (serviceName.equalsIgnoreCase("hwUsbExService")){
                white.put("usb");
                white.put("hw");
            }else if (serviceName.equalsIgnoreCase("BastetService")){
                //also /system/bin/bastetd process
                white.put("bastet");
            }else if (serviceName.equalsIgnoreCase("wifihs20")){
                //white.put("wifi");
                white.put("hs20");
            }else if (serviceName.equalsIgnoreCase("nonhardaccelpkgs")){
                white.put("nonhardaccelpk");
            }

            black.put("binder");
            black.put("signal");
            black.put("finalize");
            black.put("reference");
            black.put("heaptask");
            black.put("pool");

/*
            // system server stuff ???
            black.put("MARs");
            black.put("file");
            black.put("svc");
            black.put("SSRM");
            if (!serviceName.equalsIgnoreCase("batterystats")
                    || !serviceName.equalsIgnoreCase("battery"))
                black.put("batterystats");
            if (!serviceName.equalsIgnoreCase("power"))
                black.put("power");
            if (!serviceName.equalsIgnoreCase("display")
                    || serviceName.equalsIgnoreCase("DisplaySolution")
                    || serviceName.equalsIgnoreCase("SecExternalDisplayService"))
                black.put("display");
            if (!serviceName.equalsIgnoreCase("package"))
                black.put("package");
            if (!serviceName.equalsIgnoreCase("otadexopt"))
                black.put("dex");
            if (!serviceName.equalsIgnoreCase("activity"))
                black.put("activity");
            if (!serviceName.equalsIgnoreCase("cpuinfo"))
                black.put("cpu");
            if (!serviceName.equalsIgnoreCase("sensorservice"))
                black.put("sensor");
            if (!serviceName.equalsIgnoreCase("usagestats"))
                black.put("usage");
            if (!serviceName.equalsIgnoreCase("DeviceRootKeyService"))
                black.put("devicerootkey");
            if (!serviceName.equalsIgnoreCase("SatsService"))
                black.put("sat");
            if (!serviceName.equalsIgnoreCase("media.camera.proxy")
                    || !serviceName.equalsIgnoreCase("media.camera")
                    || !serviceName.equalsIgnoreCase("camera"))
                black.put("camera");
            if (!serviceName.equalsIgnoreCase("alarm"))
                black.put("alarm");
            if (!serviceName.equalsIgnoreCase("HqmManagerService"))
                black.put("hqm");
            if (!serviceName.equalsIgnoreCase("context_aware"))
                black.put("context");
            if (!serviceName.equalsIgnoreCase("tima"))
                black.put("tima");
            if (!serviceName.equalsIgnoreCase("knox_timakeystore_policy")
                    || !serviceName.equalsIgnoreCase("knoxcustom")
                    || !serviceName.equalsIgnoreCase("knox_ucsm_policy"))
                black.put("knox");
            if (!serviceName.equalsIgnoreCase("barbeam") ||
                    !serviceName.equalsIgnoreCase("CocktailBarService"))
                black.put("bar");
            if (!serviceName.equalsIgnoreCase("mDNIe"))
                black.put("mdnie");
            if (!serviceName.equalsIgnoreCase("cover"))
                black.put("cover");
            if (!serviceName.equalsIgnoreCase("mount"))
                black.put("mount");
            if (!serviceName.equalsIgnoreCase("sdp")
                    || !serviceName.equalsIgnoreCase("sdp_log"))
                black.put("sdp");
            if (!serviceName.equalsIgnoreCase("dlp"))
                black.put("dlp");
            if (!serviceName.equalsIgnoreCase("enterprise_license_policy"))
                black.put("enterprise");
            if (!serviceName.equalsIgnoreCase("wifi")
                    || !serviceName.equalsIgnoreCase("wifi_policy")
                    || !serviceName.equalsIgnoreCase("wifip2p")
                    || !serviceName.equalsIgnoreCase("wifihs20")
                    || !serviceName.equalsIgnoreCase("rttmanager")
                    || !serviceName.equalsIgnoreCase("wifiscanner")){
                black.put("wifi");
                black.put("wlan0");
            }
            if (!serviceName.equalsIgnoreCase("SEAMService"))
                black.put("SEAMService");
            if (!serviceName.equalsIgnoreCase("ABTPersistenceService"))
                black.put("abt");
            if (!serviceName.equalsIgnoreCase("network_management")
                    || !serviceName.equalsIgnoreCase("network_score")
                    || !serviceName.equalsIgnoreCase("netstats")
                    || !serviceName.equalsIgnoreCase("netpolicy")){
                black.put("network");
                black.put("netd");
            }
            if (!serviceName.equalsIgnoreCase("ethernet"))
                black.put("ethernet");
            if (!serviceName.equalsIgnoreCase("connectivity"))
                black.put("connectivity");
            if (!serviceName.equalsIgnoreCase("notification"))
                black.put("notifi");
            if (!serviceName.equalsIgnoreCase("location")
                    || !serviceName.equalsIgnoreCase("sec_location"))
                black.put("location");
            if (!serviceName.equalsIgnoreCase("audio"))
                black.put("audio");
            if (!serviceName.equalsIgnoreCase("usb")
                    || !serviceName.equalsIgnoreCase("kiesusb"))
                black.put("usb");
            if (!serviceName.equalsIgnoreCase("soundtrigger"))
                black.put("sound");
            if (!serviceName.equalsIgnoreCase("backup"))
                black.put("backup");
            if (!serviceName.equalsIgnoreCase("dns_listener"))
                black.put("dns");
            if (!serviceName.equalsIgnoreCase("mdm.remotedesktop"))
                black.put("mdm");
            if (!serviceName.equalsIgnoreCase("spengestureservice"))
                black.put("Accessory");
            if (!serviceName.equalsIgnoreCase("spengestureservice")
                    || !serviceName.equalsIgnoreCase("input")
                    || !serviceName.equalsIgnoreCase("input_method")
                    || !serviceName.equalsIgnoreCase("uimode")){
                black.put("input");
                black.put("ui");
            }
            if (!serviceName.equalsIgnoreCase("smartcard_access_policy")
                    || !serviceName.equalsIgnoreCase("smartcard_browser_policy")
                    || !serviceName.equalsIgnoreCase("smartcard_email_policy")
                    || !serviceName.equalsIgnoreCase("smartcard_vpn_policy")
                    || !serviceName.equalsIgnoreCase("smartcard_lockscreen_policy"))
                black.put("smart");
*/

            result.put("white", white);
            result.put("black", black);

            return result;

        } catch (JSONException e) {
            throw new ConfigurationException("TO BE DEFINED");
        }

    }

    public static int getPopulationInitialTargetSize() {
        return 10;
    }

    public static long getRandomSeed() {
        return 14021990;
    }

    public static int getStringMaxSize() {
        return 256;
    }

    public static int getArrayMaxSize() {
        /*
         * the smallest 'largest contiguous free bytes' observed is 4096 (64 is the squared root to
         * guarantee at most an array of array)
         */
        return 64;
    }

    public static int getCrossOverRate() {
        return 80; // 60 ~ 80
    }

    public static int getMutationRate() {
        return 5; //0.5 ~ 1
    }

    private static String getProcessByServiceName(String serviceName) throws ConfigurationException {

        String result = Utils.readCSVMapFromFile("/data/local/tmp/service_process_name.map")
                .get(serviceName);

        if (result != null)
            return result;

        throw new ConfigurationException(serviceName + " not found in service_process_name.map file");

    }

    public static long getRecoveryTimeInMilliseconds() {
        return 30 * 1000;
    }

    public static String getOutputRootDir() {
        return "/data/local/tmp/chizpurfle_outputs";
    }

    public static String getServiceName() {
        return serviceName;
    }

    public static int getMaxGeneration() {
        return maxGeneration;
    }

    public static String getProcessName() {
        return processName;
    }

    public static int getMaxIncubatingGeneration() {
        return 1;
    }

    public static int getUriMaxNodes() {
        return 3;
    }

    public static long getCallTimeoutInSeconds() {
        return 10;
    }

    public static int getRestoreMaxTrial() {
        return 5;
    }

    public static int getMaxConnectionTrials() {
        return 20;
    }

    public static IndividualAnalyzer.IFitnessEvaluator newFitnessEvaluator() {
        return fitnessEvaluator;
    }

    public static Population.IIndividualSelector getIndividualSelector() {
        return individualSelector;
    }

    public static boolean isBlackBoxType() {
        return blackBoxType;
    }

    public static boolean isSingleCall() {
        return singleCall;
    }

    public static boolean isExtraction() {
        return isExtraction;
    }

    public static int getMaxCommunitySize() {
        /*
         * per ridurre al caso di massimo 20 popolazioni anche senza estinzioni
         */
        return 20 * getPopulationInitialTargetSize();
    }

    public static String getMethodName() {
        return methodName;
    }

    public static Object[] getArgs() {
        return args;
    }
}
