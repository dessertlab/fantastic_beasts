# The Fantastic Beasts Framework for the Android OS

The Fantastic Beasts Framework is a collection of tools for fuzzing the Android OS.

The main, and for now only, tool is called **Chizpurfle**, a *gray-box* fuzzer designed to run on actual devices, with a focus on testing vendor-specific system services of Android OS. It was presented at the IEEE ISSRE 2017 conference, and received the best research paper award.  

If you want to mention Chizpurfle in a research paper, please cite the following references:

```
@article{cotroneo2019evolutionary,
  title={Evolutionary Fuzzing of {Android} {OS} Vendor System Services},
  author={Cotroneo, Domenico and Iannillo, Antonio Ken and Natella, Roberto},
  journal={Empirical Software Engineering},
  year={2019},
  month={May},
  doi={10.1007/s10664-019-09725-6}
}
```
[(arXiv.org)](https://arxiv.org/abs/1906.00621) [(Springer Nature SharedIt)](https://rdcu.be/bFj5F)

```
@inproceedings{iannillo2017chizpurfle,
  title={Chizpurfle: A gray-box {Android} fuzzer for vendor service customizations},
  author={Iannillo, Antonio Ken and Natella, Roberto and Cotroneo, Domenico and Nita-Rotaru, Cristina},
  booktitle={Software Reliability Engineering (ISSRE), 2017 IEEE 28th International Symposium on},
  pages={1--11},
  year={2017},
  organization={IEEE}
}
```

The framework includes scripts for initializing a fuzzing campaign, by creating the configuration files used by Chizpurfle.

# CHIZPURFLE

**Chizpurfle** is a *gray-box* fuzzer that targets the Binder interfaces of Android system services.
It includes an instrumentation module that traces the running service, and provides fine-grained coverage info.  


Chizpufle uses a genetic algorithm for generating inputs, and for evolving inputs over time based on coverage info from previous executions. The tool also provide a *black-box* fuzzing mode (blind random inputs). For more information about the design of the tool, please see check out our research paper.

The tool does not require to recompile the source code of the Android OS. Therefore, it can be adopted for testing any vendor (proprietary, closed-source) customization of the Android OS.

*Chizpurfle* runs on rooted devices. There is no need to modify the original Android OS.  
It supports Android versions 6 and 7, and can target Java-implemented Android services. 



## INSTALL

Once you cloned the project, you need to attach your Android device to the workstation, and to activate debug options on the device.

To build and install chizpurfle, run:
```bash
cd fantastic_beasts
bunzip2 ./chizpurfle/libs/frida-core/libfrida-core.a.bz2
./gradlew installAppProcess
```

This installs all the necessary files on your device. To check it, run:
```bash
adb shell
ls /data/local/tmp
> chizpurfle
> chizpurfle.jar
> cli.jar
> libstalker-server.so
> libchizpurfle-native.so
```

## INIT

*Chizpurfle* needs a "service-to-process map" file, which depends on the target device.  
This file maps each Android system service running in the device with the name of the hosting process.  
Some known maps are in `chizpurfle/config` project folder.

If your device is not present in this folder, you should create your own map file.  

First and former, you must install [FRIDA](https://www.frida.re/) both on your workstation (client) and device (server).

Run the frida server as root:
```bash
adb shell
su
cd /data/local/tmp
./frida-server &
```


Then, run the following command on your workstation:
```bash
python init_scripts/create_service_pid_map.py
```
(If there are errors at locating the device, you can place the device ID from `adb devices` in lines 81 and 85).


This command will instrument the Service Manager on your device, and will stay on hold.

Before pressing *enter*, you should kill the Zygote process to force the re-inizialization of system services:
```bash
adb shell
su
ps | grep zygote
>root      570  1     2132104 78644 poll_sched 7496f24b94 S zygote64
>root      571  1     1568616 67888 poll_sched 00ed08f664 S zygote
kill -9 570 571
```

Finally, press *enter* to terminate the `create_service_pid_map.py` python script.

Afterwards, you should save the output of the `ps` command on the device.
```bash
adb shell
su
ps >> /data/local/tmp/ps.out
chmod 666 /data/local/tmp/ps.out
exit
adb pull /data/local/tmp/ps.out
```
Now, you should have both the files *service_pid.map* and *ps.out*. Thus, run:
```
bash init_scripts/create_process_service_map.sh ps.out service_pid.map
```
This returns the *service_process_name.map* file for your device.

Once you have the map file, you have to push it on the device. Run:
```bash
adb push service_process_name.map /data/local/tmp
adb shell
su
chmod 666 /data/local/tmp/service_process_name.map
```

If you want a full model of your device service interfaces, run:
```bash
adb shell
su
cd /data/local/tmp
./chizpurfle -e
chmod 666 all_services_interfaces_in_binder.json
exit
adb pull /data/local/tmp/all_services_interfaces_in_binder.json
```
`all_services_interfaces_in_binder.json` is a json file that provides all the service interfaces of your devices.


## USAGE

*Chizpurfle* entry-point is `/data/local/tmp/chizpufle`, and should be executed as root user.  
All the output from chizpurfle is not printed on the standard output, but it is instead written on `/data/local/tmp/chizpufle.shell`.  
To print the help from chizpurfle, run:
```bash
adb shell
su
cd /data/local/tmp
./chizpurfle -h
cat chizpurfle.shell
>usage: chizpurfle
> -bb,--black-box                             uses a blackbox approach
> -e,--extract                                Extract the model from the smartphone
> -f1,--blocks-counter-fitness-evaluator      uses the blocks counter fitness evaluator (default)
> -f2,--branch-execution-fitness-evaluator    uses the blocks branch execution evaluator
> -f3,--coarse-branch-hit-fitness-evaluator   uses the blocks coarse branch hit evaluator
> -h,--help                                   show help
> -n,--max-generation <arg>                   the number of generations the populations should pass through (default is 20)
> -process,--process-name <arg>               the name of the process to trace
> -s1,--fitness-proportionate-selection       uses a fitness proportionate selection algorithm (default)
> -s2,--ranking-selection                     uses a ranking selection algorithm
> -s3,--tournament-selection                  uses a tournament selection algorithm
> -service,--service-name <arg>               the name of the service under test
> -method,--method-name <arg>                 the name of the method under test
>
>Thank you for feeding me!
```

To test a service with default fitness evaluator and default selection algorithm, run:
```bash
adb shell
su
cd /data/local/tmp
./chizpurfle -service <SERVICE_NAME>
```

To test a service with *black-box* fuzzing, run instead:
```bash
adb shell
su
cd /data/local/tmp
./chizpurfle -bb -n 200 -service <SERVICE_NAME>
```
The *max-generation* argument becomes the number of the iterations of the fuzzer.

If you want to trace a process different from the one specified in the *service_process_name.map*, run:
```bash
adb shell
su
cd /data/local/tmp
./chizpurfle -service <SERVICE_NAME> -process <PROCESS_NAME>
```
