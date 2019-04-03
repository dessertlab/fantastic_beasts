import frida
import sys
import time

cur_name = ''
cur_pid = ''

file_name = "service_pid.map"

def process(cur):
  global cur_name
  global cur_pid
  if 'name' in cur['key']:
    cur_name = cur['value']
  elif 'pid' in cur['key']:
    cur_pid = cur['value']
  elif 'perm' in cur['key']:
    if 'add' in cur['value']:
      with open(file_name, 'a') as f:
        f.write(cur_name + ',' + str(cur_pid) + '\n')
    cur_name = ''
    cur_pid = ''

def on_message(message, data):
  if 'send' in  message['type']:
    process(message['payload'])

def stalk(target_process):
  js_code="""
function Discover()
{
  var f1;
  var f2;
  var f3;

  Module.enumerateExports("libselinux.so",{
    onMatch: function(exp){
      if (exp.name === "selabel_lookup"){
        f1 = exp;
      }
      if (exp.name === "getpidcon"){
        f2 = exp;
      }
      if (exp.name === "selinux_check_access"){
        f3 = exp;
      }
    },
    onComplete: function(){}
  });

  console.log(f1.name);
  console.log(f2.name);
  console.log(f3.name);

  Interceptor.attach(ptr(f1.address),{
    onEnter: function(args){
      send({'key': 'name', 'value': Memory.readCString(args[2])});
      recv('detach', function(value){
        console.log("detaching...");
        Interceptor.detachAll();
      });
    }
  });
  Interceptor.attach(ptr(f2.address),{
    onEnter: function(args){
      send({'key': 'pid', 'value': args[0].toInt32()});
    }
  });
  Interceptor.attach(ptr(f3.address),{
    onEnter: function(args){
      send({'key': 'perm', 'value': Memory.readCString(args[3])});
    }
  });
}
Discover();
"""
  device = None
  while device is None:
    try:
      device = frida.get_usb_device()
      #device = frida.get_device("00dbc3188d9648b6", 10)
    except frida.TimedOutError:
      pass
  process = device.attach(target_process)
  #process = frida.get_device("00dbc3188d9648b6", 10).attach(target_process)
  process.enable_jit()
  script = process.create_script(js_code)
  script.on('message', on_message)
  script.load()

  raw_input('[!] Press <Enter> at any time to detach from instrumented program.\n')
  script.post({"type": "detach"})
  time.sleep(1)
  process.detach()

if __name__ == "__main__":

  if len(sys.argv) == 1:
    file_name = "service_pid_map"
  else:
    file_name = sys.argv[1]

  with open(file_name, 'w') as f:
    f.write("service, pid\n")

  stalk("servicemanager")
