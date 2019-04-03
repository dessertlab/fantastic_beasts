#!/bin/bash

if [ $# -ne 2 ]
then
  echo "usage $0 ps_output service_pid_map"
  exit -1
fi

cat $1 | tail -n +2 | awk '{print $2 " " $9}' > .pid_process_map.tmp

cat $2 | tail -n +2 | sed -e 's/,/ /g' > .service_pid_map.tmp

if [ -a service_process_name.map ]
then
  echo "creating service_process_name_map.config.old"
  mv service_process_name.map service_process_name.map.old
fi

while read line; do
  service=$(echo $line | awk '{print $1}')
  pid=$(echo $line | awk '{print $2}')
  process=$(cat .pid_process_map.tmp | grep $pid | awk '{print $2}')
  echo $service,$process >> service_process_name.map
done < .service_pid_map.tmp

echo "DON'T FORGET TO COPY service_process_name.map IN /data/local/tmp"

rm -rf .pid_process_map.tmp
rm -rf .service_pid_map.tmp
