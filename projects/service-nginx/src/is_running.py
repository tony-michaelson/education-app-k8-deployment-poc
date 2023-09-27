#!/usr/bin/env python3
import subprocess as p
import os
import re

PID_FILE = os.environ['PID_FILE']


def pid_is_running(running_pid):
    procs = p.run(["ps", "-o", "pid,comm"],
                  capture_output=True,
                  universal_newlines=True)
    procs_list = procs.stdout
    for x in str(procs_list).split('\n'):
        proc = re.split('\s+', x)
        if 1 < len(proc):
            if proc[1] == running_pid:
                return True
    return False


def read_file(path):
    f = open(path, "r")
    return f.read()


def read_pidfile(path):
    if os.path.isfile(path):
        pid = read_file(path)
        return pid
    else:
        return 0


if (pid_is_running(read_pidfile(PID_FILE))):
    exit(0)
else:
    print("MQ consumer process is not running")
    exit(2)