package italiaken.fantasticbeasts.chizpurfle.fuzz.generators.object;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.IValueGenerator;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorException;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.ValueGeneratorManager;
import italiaken.fantasticbeasts.chizpurfle.fuzz.generators.primitive.PrimitiveGenerator;

/**
 * Created by ken on 26/11/17 for fantastic_beasts
 */

class IntentGenerator implements IValueGenerator<Intent> {


    @Override
    public Intent random() {

        Intent result = new Intent();
        result.setAction(knownActions[ValueGeneratorManager.r.nextInt(knownActions.length)]);
        return result;

    }

    @Override
    public Intent mutate(Intent mutant) throws ValueGeneratorException {

        try {

            switch (ValueGeneratorManager.chooseMutation()){
                case KNOWN:
                    return random();
                case NEIGHBOR:

                    Intent result = (mutant==null)?new Intent():new Intent(mutant);
                    int intentP = ValueGeneratorManager.r.nextInt(8);

                    if (mutant == null || intentP == 0){

                        result.setAction(knownActions[ValueGeneratorManager.r.nextInt(knownActions.length)]);

                    }else if (intentP == 1){

                        switch(ValueGeneratorManager.r.nextInt(3)){
                            case 0:
                                result.setAction(knownActions[ValueGeneratorManager.r.nextInt(knownActions.length)]);
                                break;
                            case 1:
                                result.setAction(knownBroadcastActions[ValueGeneratorManager.r.nextInt(knownBroadcastActions.length)]);
                                break;
                            case 2:
                                result.setAction(new StringGenerator().mutate(mutant.getAction()));
                                break;

                        }

                    }else if (intentP == 2){

                        Uri uri = mutant.getData();
                        UriGenerator uriGenerator = new UriGenerator();

                        Uri newUri;
                        if (uri == null)
                            result.setData(uriGenerator.random());
                        else
                            result.setData(uriGenerator.mutate(uri));

                    }else if (intentP == 3){

                        Set<String> categories = result.getCategories();
                        int intentCategoryP = ValueGeneratorManager.r.nextInt(4);


                        if (categories == null || categories.isEmpty() || intentCategoryP == 0){
                            result.addCategory(knownCategories[ValueGeneratorManager.r.nextInt(knownCategories.length)]);
                        }else if (intentCategoryP == 1){
                            result.addCategory(new StringGenerator().random());
                        }else if (intentCategoryP == 2){
                            String category = (String) categories.toArray()[ValueGeneratorManager.r.nextInt(categories.size())];
                            result.addCategory(new StringGenerator().mutate(category));
                            result.removeCategory(category);
                        }else if (intentCategoryP == 3){
                            String category = (String) categories.toArray()[ValueGeneratorManager.r.nextInt(categories.size())];
                            result.removeCategory(category);
                        }

                    }else if (intentP == 4){

                        result.setComponent(new ComponentNameGenerator().mutate(mutant.getComponent()));

                    }else if (intentP == 5){

                        String type = result.getType();
                        int intentTypeP = ValueGeneratorManager.r.nextInt(2);

                        if (type == null || intentTypeP == 0){
                            result.setType(knownMimeType[ValueGeneratorManager.r.nextInt(knownMimeType.length)]);
                        }else if (intentTypeP == 1){
                            result.setType(new StringGenerator().mutate(type));
                        }

                    }else if (intentP == 6){

                        Bundle bundle = result.getExtras();
                        int intentExtraP = ValueGeneratorManager.r.nextInt(4);

                        if (bundle == null || bundle.isEmpty() || intentExtraP < 3){

                            String extra = knownExtras[ValueGeneratorManager.r.nextInt(knownExtras.length)];
                            if (intentExtraP == 0){
                                result.putExtra(extra, ValueGeneratorManager.r.nextBoolean());
                            }else if (intentExtraP == 1){
                                result.putExtra(extra, ValueGeneratorManager.r.nextInt());
                            }else if (intentExtraP == 2){
                                result.putExtra(extra, new StringGenerator().random());
                            }

                        }else if (intentExtraP == 3){

                            result.removeExtra(
                                    (String) bundle.keySet().toArray()
                                            [ValueGeneratorManager.r.nextInt(bundle.keySet().size())]
                            );

                        }

                    }else if (intentP == 7){

                        result.setFlags(
                                (int)
                                        new PrimitiveGenerator(Integer.TYPE)
                                                .mutate(result.getFlags()));

                    }
                case INVERSE:
                    return random();
            }
        } catch (Exception e) {
            throw new ValueGeneratorException("can't mutate "+ mutant, e);
        }


        return null;
    }

    @Override
    public Intent crossover(Intent parent1, Intent parent2) throws ValueGeneratorException {
        try {
            Intent result =  new Intent(parent1);

            switch (ValueGeneratorManager.chooseCrossOver()){
                case SINGLE_POINT:
                case TWO_POINT:
                case UNIFORM:
                case ARITHMETIC:
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setFlags(parent2.getFlags());
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setType(parent2.getType());
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setPackage(parent2.getPackage());
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setAction(parent2.getAction());
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setComponent(parent2.getComponent());
                    if (ValueGeneratorManager.r.nextBoolean())
                        result.setData(parent2.getData());
                    if (ValueGeneratorManager.r.nextBoolean()) {
                        Bundle bundle = parent2.getExtras();
                        if (bundle != null) result.putExtras(bundle);
                    }
            }

            return result;

        }catch (Exception e){
            if (parent1 != null && parent2 == null)
                return parent1;
            if (parent2 != null && parent1 == null)
                return parent2;
            if (parent1 == null && parent2 == null)
                return null;
            throw new ValueGeneratorException("can't crossover "+parent1+" and "+parent2, e);
        }

    }

    private final static String[] knownActions = {Intent.ACTION_MAIN, Intent.ACTION_VIEW,
            Intent.ACTION_ATTACH_DATA, Intent.ACTION_EDIT, Intent.ACTION_INSERT_OR_EDIT, Intent.ACTION_PICK,
            Intent.ACTION_CHOOSER, Intent.ACTION_GET_CONTENT, Intent.ACTION_DIAL, Intent.ACTION_CALL,
            "android.intent.action.CALL_EMERGENCY", "android.intent.action.CALL_PRIVILEGED",
            "android.intent.action.SIM_ACTIVATION_REQUEST",
            Intent.ACTION_SEND, Intent.ACTION_SENDTO, Intent.ACTION_SEND_MULTIPLE, Intent.ACTION_ANSWER,
            Intent.ACTION_INSERT, Intent.ACTION_PASTE, Intent.ACTION_DELETE, Intent.ACTION_RUN,
            Intent.ACTION_SYNC, Intent.ACTION_SEARCH, Intent.ACTION_SYSTEM_TUTORIAL,
            Intent.ACTION_PICK_ACTIVITY, Intent.ACTION_WEB_SEARCH, Intent.ACTION_ASSIST, "android.intent.action.VOICE_ASSIST",
            Intent.EXTRA_ASSIST_PACKAGE, "android.intent.extra.ASSIST_UID", Intent.EXTRA_ASSIST_CONTEXT,
            "android.intent.extra.ASSIST_INPUT_HINT_KEYBOARD", "android.intent.extra.ASSIST_INPUT_DEVICE_ID",
            Intent.ACTION_ALL_APPS, Intent.ACTION_SET_WALLPAPER, Intent.ACTION_BUG_REPORT,
            Intent.ACTION_FACTORY_TEST, Intent.ACTION_CALL_BUTTON, Intent.ACTION_VOICE_COMMAND,
            Intent.ACTION_SEARCH_LONG_PRESS, Intent.ACTION_APP_ERROR, Intent.ACTION_POWER_USAGE_SUMMARY,
            Intent.ACTION_CREATE_SHORTCUT, "android.intent.action.UPGRADE_SETUP", Intent.ACTION_MANAGE_NETWORK_USAGE,
            Intent.ACTION_INSTALL_PACKAGE, Intent.ACTION_UNINSTALL_PACKAGE, "android.intent.action.MANAGE_APP_PERMISSIONS",
            "android.intent.action.MANAGE_PERMISSIONS", "android.intent.action.GET_PERMISSIONS_COUNT",
            "android.intent.action.MANAGE_PERMISSION_APPS"};

    private final static String[] knownBroadcastActions = {Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON,
            Intent.ACTION_DREAMING_STARTED, Intent.ACTION_USER_PRESENT, Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, "android.intent.action.CLEAR_DNS_CACHE",
            "android.intent.action.ALARM_CHANGED", Intent.ACTION_CLOSE_SYSTEM_DIALOGS, "android.intent.action.PACKAGE_INSTALL",
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_CHANGED, Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED, "android.intent.action.QUERY_PACKAGE_RESTART",
            Intent.ACTION_PACKAGE_RESTARTED, Intent.ACTION_PACKAGE_DATA_CLEARED, Intent.ACTION_PACKAGE_FIRST_LAUNCH,
            Intent.ACTION_UID_REMOVED, Intent.ACTION_PACKAGE_NEEDS_VERIFICATION, Intent.ACTION_PACKAGE_VERIFIED,
            "android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION", Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE,
            Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE, "android.intent.action.WALLPAPER_CHANGED",
            Intent.ACTION_BATTERY_CHANGED, Intent.ACTION_BATTERY_LOW, Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_CONFIGURATION_CHANGED, Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_SHUTDOWN, "android.intent.action.ACTION_REQUEST_SHUTDOWN", Intent.ACTION_DEVICE_STORAGE_LOW,
            Intent.ACTION_DEVICE_STORAGE_OK, "android.intent.action.DEVICE_STORAGE_FULL",
            "android.intent.action.DEVICE_STORAGE_NOT_FULL", "android.intent.action.UMS_CONNECTED",
            "android.intent.action.UMS_DISCONNECTED", Intent.ACTION_MEDIA_REMOVED, Intent.ACTION_MEDIA_UNMOUNTED,
            Intent.ACTION_MEDIA_CHECKING, Intent.ACTION_MEDIA_NOFS, Intent.ACTION_MEDIA_MOUNTED,
            Intent.ACTION_MEDIA_SHARED, "android.intent.action.MEDIA_UNSHARED", Intent.ACTION_MEDIA_BAD_REMOVAL,
            Intent.ACTION_MEDIA_UNMOUNTABLE, Intent.ACTION_MEDIA_EJECT, Intent.ACTION_MEDIA_SCANNER_STARTED,
            Intent.ACTION_MEDIA_SCANNER_FINISHED, Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Intent.ACTION_MEDIA_BUTTON,
            Intent.ACTION_CAMERA_BUTTON, Intent.ACTION_GTALK_SERVICE_CONNECTED, Intent.ACTION_GTALK_SERVICE_DISCONNECTED,
            Intent.ACTION_INPUT_METHOD_CHANGED, Intent.ACTION_AIRPLANE_MODE_CHANGED, Intent.ACTION_PROVIDER_CHANGED,
            Intent.ACTION_HEADSET_PLUG, "android.intent.action.ADVANCED_SETTINGS", "android.intent.action.APPLICATION_RESTRICTIONS_CHANGED",
            Intent.ACTION_NEW_OUTGOING_CALL, Intent.ACTION_REBOOT, Intent.ACTION_DOCK_EVENT,
            "android.intent.action.ACTION_IDLE_MAINTENANCE_START", "android.intent.action.ACTION_IDLE_MAINTENANCE_END",
            "com.google.android.c2dm.intent.RECEIVE", "android.intent.action.PRE_BOOT_COMPLETED",
            Intent.ACTION_GET_RESTRICTION_ENTRIES, Intent.ACTION_USER_INITIALIZE, Intent.ACTION_USER_FOREGROUND,
            Intent.ACTION_USER_BACKGROUND, "android.intent.action.USER_ADDED", "android.intent.action.USER_STARTED",
            "android.intent.action.USER_STARTING", "android.intent.action.USER_STOPPING", "android.intent.action.USER_STOPPED",
            "android.intent.action.USER_REMOVED", "android.intent.action.USER_SWITCHED", "android.intent.action.USER_INFO_CHANGED",
            "android.intent.action.MANAGED_PROFILE_ADDED", "android.intent.action.MANAGED_PROFILE_REMOVED", Intent.ACTION_QUICK_CLOCK,
            "android.intent.action.SHOW_BRIGHTNESS_DIALOG", "android.intent.action.GLOBAL_BUTTON", Intent.ACTION_OPEN_DOCUMENT,
            Intent.ACTION_CREATE_DOCUMENT, "android.intent.action.OPEN_DOCUMENT_TREE", "android.intent.action.MASTER_CLEAR",
            "android.os.action.SETTING_RESTORED", "android.intent.action.PROCESS_TEXT"};

    private final static String[] knownCategories = {Intent.CATEGORY_DEFAULT, Intent.CATEGORY_BROWSABLE,
            "android.intent.category.VOICE", "android.intent.category.LEANBACK_LAUNCHER",
            Intent.CATEGORY_TAB, Intent.CATEGORY_ALTERNATIVE, "android.intent.category.LEANBACK_SETTINGS",
            Intent.CATEGORY_SELECTED_ALTERNATIVE, Intent.CATEGORY_INFO, "android.intent.category.SETUP_WIZARD",
            Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_INFO, Intent.CATEGORY_HOME, Intent.CATEGORY_PREFERENCE,
            Intent.CATEGORY_DEVELOPMENT_PREFERENCE, "android.intent.category.EMBED", Intent.CATEGORY_APP_MARKET,
            Intent.CATEGORY_MONKEY, Intent.CATEGORY_UNIT_TEST, Intent.CATEGORY_SAMPLE_CODE, Intent.CATEGORY_OPENABLE,
            "android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST", Intent.CATEGORY_APP_BROWSER,
            Intent.CATEGORY_APP_CALCULATOR, Intent.CATEGORY_APP_CALENDAR, Intent.CATEGORY_APP_CONTACTS,
            Intent.CATEGORY_APP_EMAIL, Intent.CATEGORY_APP_MAPS, Intent.CATEGORY_APP_MESSAGING,
            Intent.CATEGORY_APP_MUSIC,
            Intent.CATEGORY_TEST, Intent.CATEGORY_CAR_DOCK, Intent.CATEGORY_DESK_DOCK, Intent.CATEGORY_LE_DESK_DOCK,
            Intent.CATEGORY_HE_DESK_DOCK, Intent.CATEGORY_CAR_DOCK, Intent.CATEGORY_APP_MARKET};

    private final static String[] knownExtras = {Intent.EXTRA_ALARM_COUNT, Intent.EXTRA_BCC, Intent.EXTRA_CC,
            Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST, "android.intent.extra.changed_component_name", Intent.EXTRA_DATA_REMOVED,
            Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DONT_KILL_APP, Intent.EXTRA_EMAIL, Intent.EXTRA_INITIAL_INTENTS,
            Intent.EXTRA_KEY_EVENT, Intent.EXTRA_PHONE_NUMBER, Intent.EXTRA_REFERRER, Intent.EXTRA_TEMPLATE,
            Intent.EXTRA_HTML_TEXT, Intent.EXTRA_INTENT, "android.intent.extra.ALTERNATE_INTENTS",
            "android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER", "android.intent.extra.RESULT_RECEIVER",
            "android.intent.extra.REPLACEMENT_EXTRAS", "android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER",
            "android.intent.extra.CHOSEN_COMPONENT", "android.intent.extra.KEY_CONFIRM", "android.intent.extra.PACKAGES",
            "android.intent.extra.REMOVED_FOR_ALL_USERS", Intent.METADATA_DOCK_HOME, Intent.EXTRA_REMOTE_INTENT_TOKEN,
            Intent.EXTRA_CHANGED_PACKAGE_LIST, Intent.EXTRA_CHANGED_UID_LIST, "android.intent.extra.client_label",
            "android.intent.extra.client_intent", Intent.EXTRA_LOCAL_ONLY, Intent.EXTRA_ALLOW_MULTIPLE,
            "android.intent.extra.user_handle", Intent.EXTRA_RESTRICTIONS_LIST, Intent.EXTRA_RESTRICTIONS_BUNDLE,
            "android.intent.extra.REFERRER_NAME", Intent.EXTRA_REMOTE_INTENT_TOKEN, Intent.EXTRA_REPLACING,
            Intent.EXTRA_RESTRICTIONS_INTENT, Intent.EXTRA_MIME_TYPES, Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY,
            "android.intent.extra.TIME_PREF_24_HOUR_FORMAT", "android.intent.extra.REASON", "android.intent.extra.WIPE_EXTERNAL_STORAGE",
            "android.intent.extra.SIM_ACTIVATION_RESPONSE",

            /* Intent.ACTION_SHUTDOWN */
            Intent.EXTRA_SHORTCUT_ICON, Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.EXTRA_SHORTCUT_INTENT, Intent.EXTRA_SHORTCUT_NAME,

            Intent.EXTRA_STREAM, Intent.EXTRA_SUBJECT, Intent.EXTRA_TEMPLATE,
            Intent.EXTRA_TEMPLATE, Intent.EXTRA_TEXT, Intent.EXTRA_TITLE, Intent.EXTRA_UID,

            /* Intent.ACTION_INSTALL_PACKAGE and Intent.ACTION_UNINSTALL_PACKAGE */
            Intent.EXTRA_INSTALLER_PACKAGE_NAME, Intent.EXTRA_NOT_UNKNOWN_SOURCE,
            "android.intent.extra.ALLOW_REPLACE", Intent.EXTRA_RETURN_RESULT, Intent.EXTRA_ORIGINATING_URI,
            "android.intent.extra.ORIGINATING_UID", "android.intent.extra.INSTALL_RESULT",
            "android.intent.extra.UNINSTALL_ALL_USERS",

            /* "android.intent.action.UPGRADE_SETUP" */
            "android.SETUP_VERSION",

            /* "android.intent.action.GET_PERMISSIONS_COUNT" */
            "android.intent.extra.GET_PERMISSIONS_COUNT_RESULT",
            "android.intent.extra.GET_PERMISSIONS_GROUP_LIST_RESULT",
            "android.intent.extra.GET_PERMISSIONS_RESONSE_INTENT",

            /* "android.intent.action.MANAGE_PERMISSION_APPS" */
            "android.intent.extra.PERMISSION_NAME",

            "android.intent.extra.PACKAGE_NAME",

            /* "android.os.action.SETTING_RESTORED" */
            "setting_name", "previous_value, new_value",

            /* Intent.ACTION_PROCESS_TEXT */
            "android.intent.extra.PROCESS_TEXT", "android.intent.extra.PROCESS_TEXT_READONLY"};

    private final static String[] knownMimeType = {"x-world/x-3dmf",
            "x-world/x-3dmf",
            "application/octet-stream",
            "application/x-authorware-bin",
            "application/x-authorware-map",
            "application/x-authorware-seg",
            "text/vnd.abc",
            "text/html",
            "video/animaflex",
            "application/postscript",
            "audio/aiff",
            "audio/x-aiff",
            "audio/aiff",
            "audio/x-aiff",
            "audio/aiff",
            "audio/x-aiff",
            "application/x-aim",
            "text/x-audiosoft-intra",
            "application/x-navi-animation",
            "application/x-nokia-9000-communicator-add-on-software",
            "application/mime",
            "application/octet-stream",
            "application/arj",
            "application/octet-stream",
            "image/x-jg",
            "video/x-ms-asf",
            "text/x-asm",
            "text/asp",
            "application/x-mplayer2",
            "video/x-ms-asf",
            "video/x-ms-asf-plugin",
            "audio/basic",
            "audio/x-au",
            "application/x-troff-msvideo",
            "video/avi",
            "video/msvideo",
            "video/x-msvideo",
            "video/avs-video",
            "application/x-bcpio",
            "application/mac-binary",
            "application/macbinary",
            "application/octet-stream",
            "application/x-binary",
            "application/x-macbinary",
            "image/bmp",
            "image/bmp",
            "image/x-windows-bmp",
            "application/book",
            "application/book",
            "application/x-bzip2",
            "application/x-bsh",
            "application/x-bzip",
            "application/x-bzip2",
            "text/plain",
            "text/x-c",
            "text/plain",
            "application/vnd.ms-pki.seccat",
            "text/plain",
            "text/x-c",
            "application/clariscad",
            "application/x-cocoa",
            "application/cdf",
            "application/x-cdf",
            "application/x-netcdf",
            "application/pkix-cert",
            "application/x-x509-ca-cert",
            "application/x-chat",
            "application/x-chat",
            "application/java",
            "application/java-byte-code",
            "application/x-java-class",
            "application/octet-stream",
            "text/plain",
            "text/plain",
            "application/x-cpio",
            "text/x-c",
            "application/mac-compactpro",
            "application/x-compactpro",
            "application/x-cpt",
            "application/pkcs-crl",
            "application/pkix-crl",
            "application/pkix-cert",
            "application/x-x509-ca-cert",
            "application/x-x509-user-cert",
            "application/x-csh",
            "text/x-script.csh",
            "application/x-pointplus",
            "text/css",
            "text/plain",
            "application/x-director",
            "application/x-deepv",
            "text/plain",
            "application/x-x509-ca-cert",
            "video/x-dv",
            "application/x-director",
            "video/dl",
            "video/x-dl",
            "application/msword",
            "application/msword",
            "application/commonground",
            "application/drafting",
            "application/octet-stream",
            "video/x-dv",
            "application/x-dvi",
            "drawing/x-dwf",
            "model/vnd.dwf",
            "application/acad",
            "image/vnd.dwg",
            "image/x-dwg",
            "application/dxf",
            "image/vnd.dwg",
            "image/x-dwg",
            "application/x-director",
            "text/x-script.elisp",
            "application/x-bytecode.elisp",
            "application/x-elc",
            "application/x-envoy",
            "application/postscript",
            "application/x-esrehber",
            "text/x-setext",
            "application/envoy",
            "application/x-envoy",
            "application/octet-stream",
            "text/plain",
            "text/x-fortran",
            "text/x-fortran",
            "text/plain",
            "text/x-fortran",
            "application/vnd.fdf",
            "application/fractals",
            "image/fif",
            "video/fli",
            "video/x-fli",
            "image/florian",
            "text/vnd.fmi.flexstor",
            "video/x-atomic3d-feature",
            "text/plain",
            "text/x-fortran",
            "image/vnd.fpx",
            "image/vnd.net-fpx",
            "application/freeloader",
            "audio/make",
            "text/plain",
            "image/g3fax",
            "image/gif",
            "video/gl",
            "video/x-gl",
            "audio/x-gsm",
            "audio/x-gsm",
            "application/x-gsp",
            "application/x-gss",
            "application/x-gtar",
            "application/x-compressed",
            "application/x-gzip",
            "application/x-gzip",
            "multipart/x-gzip",
            "text/plain",
            "text/x-h",
            "application/x-hdf",
            "application/x-helpfile",
            "application/vnd.hp-hpgl",
            "text/plain",
            "text/x-h",
            "text/x-script",
            "application/hlp",
            "application/x-helpfile",
            "application/x-winhelp",
            "application/vnd.hp-hpgl",
            "application/vnd.hp-hpgl",
            "application/binhex",
            "application/binhex4",
            "application/mac-binhex",
            "application/mac-binhex40",
            "application/x-binhex40",
            "application/x-mac-binhex40",
            "application/hta",
            "text/x-component",
            "text/html",
            "text/html",
            "text/html",
            "text/webviewhtml",
            "text/html",
            "x-conference/x-cooltalk",
            "image/x-icon",
            "text/plain",
            "image/ief",
            "image/ief",
            "application/iges",
            "model/iges",
            "application/iges",
            "model/iges",
            "application/x-ima",
            "application/x-httpd-imap",
            "application/inf",
            "application/x-internett-signup",
            "application/x-ip2",
            "video/x-isvideo",
            "audio/it",
            "application/x-inventor",
            "i-world/i-vrml",
            "application/x-livescreen",
            "audio/x-jam",
            "text/plain",
            "text/x-java-source",
            "text/plain",
            "text/x-java-source",
            "application/x-java-commerce",
            "image/jpeg",
            "image/pjpeg",
            "image/jpeg",
            "image/jpeg",
            "image/pjpeg",
            "image/jpeg",
            "image/pjpeg",
            "image/jpeg",
            "image/pjpeg",
            "image/x-jps",
            "application/x-javascript",
            "application/javascript",
            "application/ecmascript",
            "text/javascript",
            "text/ecmascript",
            "image/jutvision",
            "audio/midi",
            "music/x-karaoke",
            "application/x-ksh",
            "text/x-script.ksh",
            "audio/nspaudio",
            "audio/x-nspaudio",
            "audio/x-liveaudio",
            "application/x-latex",
            "application/lha",
            "application/octet-stream",
            "application/x-lha",
            "application/octet-stream",
            "text/plain",
            "audio/nspaudio",
            "audio/x-nspaudio",
            "text/plain",
            "application/x-lisp",
            "text/x-script.lisp",
            "text/plain",
            "text/x-la-asf",
            "application/x-latex",
            "application/octet-stream",
            "application/x-lzh",
            "application/lzx",
            "application/octet-stream",
            "application/x-lzx",
            "text/plain",
            "text/x-m",
            "video/mpeg",
            "audio/mpeg",
            "video/mpeg",
            "audio/x-mpequrl",
            "application/x-troff-man",
            "application/x-navimap",
            "text/plain",
            "application/mbedlet",
            "application/x-magic-cap-package-1.0",
            "application/mcad",
            "application/x-mathcad",
            "image/vasa",
            "text/mcf",
            "application/netmc",
            "application/x-troff-me",
            "message/rfc822",
            "message/rfc822",
            "application/x-midi",
            "audio/midi",
            "audio/x-mid",
            "audio/x-midi",
            "music/crescendo",
            "x-music/x-midi",
            "application/x-midi",
            "audio/midi",
            "audio/x-mid",
            "audio/x-midi",
            "music/crescendo",
            "x-music/x-midi",
            "application/x-frame",
            "application/x-mif",
            "message/rfc822",
            "www/mime",
            "audio/x-vnd.audioexplosion.mjuicemediafile",
            "video/x-motion-jpeg",
            "application/base64",
            "application/x-meme",
            "application/base64",
            "audio/mod",
            "audio/x-mod",
            "video/quicktime",
            "video/quicktime",
            "video/x-sgi-movie",
            "audio/mpeg",
            "audio/x-mpeg",
            "video/mpeg",
            "video/x-mpeg",
            "video/x-mpeq2a",
            "audio/mpeg3",
            "audio/x-mpeg-3",
            "video/mpeg",
            "video/x-mpeg",
            "audio/mpeg",
            "video/mpeg",
            "application/x-project",
            "video/mpeg",
            "video/mpeg",
            "audio/mpeg",
            "video/mpeg",
            "audio/mpeg",
            "application/vnd.ms-project",
            "application/x-project",
            "application/x-project",
            "application/x-project",
            "application/marc",
            "application/x-troff-ms",
            "video/x-sgi-movie",
            "audio/make",
            "application/x-vnd.audioexplosion.mzz",
            "image/naplps",
            "image/naplps",
            "application/x-netcdf",
            "application/vnd.nokia.configuration-message",
            "image/x-niff",
            "image/x-niff",
            "application/x-mix-transfer",
            "application/x-conference",
            "application/x-navidoc",
            "application/octet-stream",
            "application/oda",
            "application/x-omc",
            "application/x-omcdatamaker",
            "application/x-omcregerator",
            "text/x-pascal",
            "application/pkcs10",
            "application/x-pkcs10",
            "application/pkcs-12",
            "application/x-pkcs12",
            "application/x-pkcs7-signature",
            "application/pkcs7-mime",
            "application/x-pkcs7-mime",
            "application/pkcs7-mime",
            "application/x-pkcs7-mime",
            "application/x-pkcs7-certreqresp",
            "application/pkcs7-signature",
            "application/pro_eng",
            "text/pascal",
            "image/x-portable-bitmap",
            "application/vnd.hp-pcl",
            "application/x-pcl",
            "image/x-pict",
            "image/x-pcx",
            "chemical/x-pdb",
            "application/pdf",
            "audio/make",
            "audio/make.my.funk",
            "image/x-portable-graymap",
            "image/x-portable-greymap",
            "image/pict",
            "image/pict",
            "application/x-newton-compatible-pkg",
            "application/vnd.ms-pki.pko",
            "text/plain",
            "text/x-script.perl",
            "application/x-pixclscript",
            "image/x-xpixmap",
            "text/x-script.perl-module",
            "application/x-pagemaker",
            "application/x-pagemaker",
            "image/png",
            "application/x-portable-anymap",
            "image/x-portable-anymap",
            "application/mspowerpoint",
            "application/vnd.ms-powerpoint",
            "model/x-pov",
            "application/vnd.ms-powerpoint",
            "image/x-portable-pixmap",
            "application/mspowerpoint",
            "application/vnd.ms-powerpoint",
            "application/mspowerpoint",
            "application/powerpoint",
            "application/vnd.ms-powerpoint",
            "application/x-mspowerpoint",
            "application/mspowerpoint",
            "application/x-freelance",
            "application/pro_eng",
            "application/postscript",
            "application/octet-stream",
            "paleovu/x-pv",
            "application/vnd.ms-powerpoint",
            "text/x-script.phyton",
            "application/x-bytecode.python",
            "audio/vnd.qcelp",
            "x-world/x-3dmf",
            "x-world/x-3dmf",
            "image/x-quicktime",
            "video/quicktime",
            "video/x-qtc",
            "image/x-quicktime",
            "image/x-quicktime",
            "audio/x-pn-realaudio",
            "audio/x-pn-realaudio-plugin",
            "audio/x-realaudio",
            "audio/x-pn-realaudio",
            "application/x-cmu-raster",
            "image/cmu-raster",
            "image/x-cmu-raster",
            "image/cmu-raster",
            "text/x-script.rexx",
            "image/vnd.rn-realflash",
            "image/x-rgb",
            "application/vnd.rn-realmedia",
            "audio/x-pn-realaudio",
            "audio/mid",
            "audio/x-pn-realaudio",
            "audio/x-pn-realaudio",
            "audio/x-pn-realaudio-plugin",
            "application/ringing-tones",
            "application/vnd.nokia.ringing-tone",
            "application/vnd.rn-realplayer",
            "application/x-troff",
            "image/vnd.rn-realpix",
            "audio/x-pn-realaudio-plugin",
            "text/richtext",
            "text/vnd.rn-realtext",
            "application/rtf",
            "application/x-rtf",
            "text/richtext",
            "application/rtf",
            "text/richtext",
            "video/vnd.rn-realvideo",
            "text/x-asm",
            "audio/s3m",
            "application/octet-stream",
            "application/x-tbook",
            "application/x-lotusscreencam",
            "text/x-script.guile",
            "text/x-script.scheme",
            "video/x-scm",
            "text/plain",
            "application/sdp",
            "application/x-sdp",
            "application/sounder",
            "application/sea",
            "application/x-sea",
            "application/set",
            "text/sgml",
            "text/x-sgml",
            "text/sgml",
            "text/x-sgml",
            "application/x-bsh",
            "application/x-sh",
            "application/x-shar",
            "text/x-script.sh",
            "application/x-bsh",
            "application/x-shar",
            "text/html",
            "text/x-server-parsed-html",
            "audio/x-psid",
            "application/x-sit",
            "application/x-stuffit",
            "application/x-koan",
            "application/x-koan",
            "application/x-koan",
            "application/x-koan",
            "application/x-seelogo",
            "application/smil",
            "application/smil",
            "audio/basic",
            "audio/x-adpcm",
            "application/solids",
            "application/x-pkcs7-certificates",
            "text/x-speech",
            "application/futuresplash",
            "application/x-sprite",
            "application/x-sprite",
            "application/x-wais-source",
            "text/x-server-parsed-html",
            "application/streamingmedia",
            "application/vnd.ms-pki.certstore",
            "application/step",
            "application/sla",
            "application/vnd.ms-pki.stl",
            "application/x-navistyle",
            "application/step",
            "application/x-sv4cpio",
            "application/x-sv4crc",
            "image/vnd.dwg",
            "image/x-dwg",
            "application/x-world",
            "x-world/x-svr",
            "application/x-shockwave-flash",
            "application/x-troff",
            "text/x-speech",
            "application/x-tar",
            "application/toolbook",
            "application/x-tbook",
            "application/x-tcl",
            "text/x-script.tcl",
            "text/x-script.tcsh",
            "application/x-tex",
            "application/x-texinfo",
            "application/x-texinfo",
            "application/plain",
            "text/plain",
            "application/gnutar",
            "application/x-compressed",
            "image/tiff",
            "image/x-tiff",
            "image/tiff",
            "image/x-tiff",
            "application/x-troff",
            "audio/tsp-audio",
            "application/dsptype",
            "audio/tsplayer",
            "text/tab-separated-values",
            "image/florian",
            "text/plain",
            "text/x-uil",
            "text/uri-list",
            "text/uri-list",
            "application/i-deas",
            "text/uri-list",
            "text/uri-list",
            "application/x-ustar",
            "multipart/x-ustar",
            "application/octet-stream",
            "text/x-uuencode",
            "text/x-uuencode",
            "application/x-cdlink",
            "text/x-vcalendar",
            "application/vda",
            "video/vdo",
            "application/groupwise",
            "video/vivo",
            "video/vnd.vivo",
            "video/vivo",
            "video/vnd.vivo",
            "application/vocaltec-media-desc",
            "application/vocaltec-media-file",
            "audio/voc",
            "audio/x-voc",
            "video/vosaic",
            "audio/voxware",
            "audio/x-twinvq-plugin",
            "audio/x-twinvq",
            "audio/x-twinvq-plugin",
            "application/x-vrml",
            "model/vrml",
            "x-world/x-vrml",
            "x-world/x-vrt",
            "application/x-visio",
            "application/x-visio",
            "application/x-visio",
            "application/wordperfect6.0",
            "application/wordperfect6.1",
            "application/msword",
            "audio/wav",
            "audio/x-wav",
            "application/x-qpro",
            "image/vnd.wap.wbmp",
            "application/vnd.xara",
            "application/msword",
            "application/x-123",
            "windows/metafile",
            "text/vnd.wap.wml",
            "application/vnd.wap.wmlc",
            "text/vnd.wap.wmlscript",
            "application/vnd.wap.wmlscriptc",
            "application/msword",
            "application/wordperfect",
            "application/wordperfect",
            "application/wordperfect6.0",
            "application/wordperfect",
            "application/wordperfect",
            "application/x-wpwin",
            "application/x-lotus",
            "application/mswrite",
            "application/x-wri",
            "application/x-world",
            "model/vrml",
            "x-world/x-vrml",
            "model/vrml",
            "x-world/x-vrml",
            "text/scriplet",
            "application/x-wais-source",
            "application/x-wintalk",
            "image/x-xbitmap",
            "image/x-xbm",
            "image/xbm",
            "video/x-amt-demorun",
            "xgl/drawing",
            "image/vnd.xiff",
            "application/excel",
            "application/excel",
            "application/x-excel",
            "application/x-msexcel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/excel",
            "application/x-excel",
            "application/excel",
            "application/x-excel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/x-msexcel",
            "application/excel",
            "application/x-excel",
            "application/excel",
            "application/x-excel",
            "application/excel",
            "application/vnd.ms-excel",
            "application/x-excel",
            "application/x-msexcel",
            "audio/xm",
            "application/xml",
            "text/xml",
            "xgl/movie",
            "application/x-vnd.ls-xpix",
            "image/x-xpixmap",
            "image/xpm",
            "image/png",
            "video/x-amt-showrun",
            "image/x-xwd",
            "image/x-xwindowdump",
            "chemical/x-pdb",
            "application/x-compress",
            "application/x-compressed",
            "application/x-compressed",
            "application/x-zip-compressed",
            "application/zip",
            "multipart/x-zip",
            "application/octet-stream",
            "text/x-script.zsh",
            "application/*",
            "audio/*",
            "chemical/*",
            "drawing/*",
            "i-world/*",
            "image/*",
            "message/*",
            "model/*",
            "multipart/*",
            "music/*",
            "paleovu/*",
            "text/*",
            "video/*",
            "windows/*",
            "www/*",
            "x-conference/*",
            "x-music/*",
            "x-world/*",
            "xgl/*",
            "*/*"};


}
