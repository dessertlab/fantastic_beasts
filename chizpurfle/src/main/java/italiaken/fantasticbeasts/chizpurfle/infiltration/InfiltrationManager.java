package italiaken.fantasticbeasts.chizpurfle.infiltration;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import italiaken.fantasticbeasts.chizpurfle.L;

import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_PROVIDERS;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;

/**
 * Created by ken on 27/11/17 for fantastic_beasts
 */

public class InfiltrationManager {


    private static List<ComponentInfo> componentInfos;

    public static List<ComponentInfo> getInstalledComponentInfoAgain() throws InfiltrationException{
        componentInfos = null;
        return getInstalledComponentInfo();
    }

    public static List<ComponentInfo> getInstalledComponentInfo() throws InfiltrationException {

        if (componentInfos != null)
            return componentInfos;

        Object packageService = new ServiceManagerWrapper()
                .getServiceObjectByName("package", null);

        MethodCaller methodCaller = new MethodCallerBuilder()
                .setCalledObject(packageService)
                .setMethodByName("getInstalledApplications")
                .createMethodCaller();

        Object o = methodCaller.callWithTimeout(60, 0,0);

        if (o != null){

            methodCaller = new MethodCallerBuilder()
                    .setCalledObject(o)
                    .setMethodByName("getList")
                    .createMethodCaller();

            List<ApplicationInfo> list =
                    (List<ApplicationInfo>) methodCaller.call(null);
            List<ComponentInfo> allComponentInfo = new ArrayList<>();

            methodCaller = new MethodCallerBuilder()
                    .setCalledObject(packageService)
                    .setMethodByName("getPackageInfo")
                    .createMethodCaller();

            for (ApplicationInfo applicationInfo : list){
                L.i(applicationInfo.packageName);
                int componentFlags =
                        GET_ACTIVITIES | GET_PROVIDERS | GET_RECEIVERS | GET_SERVICES;

                PackageInfo info = (PackageInfo)
                        methodCaller.call(
                                applicationInfo.packageName,
                                componentFlags,
                                0);

                if (info == null)
                    continue;
                if (info.activities != null) {
                    allComponentInfo.addAll(Arrays.asList(info.activities));
                }if (info.providers != null) {
                    allComponentInfo.addAll(Arrays.asList(info.providers));
                }if (info.receivers != null) {
                    allComponentInfo.addAll(Arrays.asList(info.receivers));
                }if (info.services!= null) {
                    allComponentInfo.addAll(Arrays.asList(info.services));
                }
            }

            componentInfos = allComponentInfo;
            return allComponentInfo;
        }


        throw new InfiltrationException("can't retrieve all components info");
    }


}
