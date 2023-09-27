#!/usr/bin/env python3
import pika
import subprocess as p
import json
import sys
import os
import re

MQ_HOST = os.environ['MQ_HOST']
MQ_USER = os.environ['MQ_USER']
MQ_PASS = os.environ['MQ_PASS']
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
    print("PID already exists for this MQ consumer")
    exit(1)
else:
    pid = os.getpid()
    print("PID: " + str(pid))
    f = open(PID_FILE, "w")
    f.write(str(pid))
    f.close()

credentials = pika.PlainCredentials(MQ_USER, MQ_PASS)
connection = pika.BlockingConnection(
    pika.ConnectionParameters(host=MQ_HOST,
                              port=5672,
                              virtual_host='/',
                              credentials=credentials))
channel = connection.channel()

channel.exchange_declare(exchange='nginx_host', exchange_type='fanout')

result = channel.queue_declare(queue='', exclusive=True)
queue_name = result.method.queue

channel.queue_bind(exchange='nginx_host', queue=queue_name)

print(' [*] Waiting for commands. To exit press CTRL+C')


def reload_nginx():
    reload = p.run(['service', 'nginx', 'reload'],
                   universal_newlines=True,
                   capture_output=True).stdout
    status = p.run(['service', 'nginx', 'status'],
                   universal_newlines=True,
                   capture_output=True).stdout
    if re.match('.*nginx is running.*', status, flags=re.DOTALL):
        return (True, status)
    else:
        return (False, status)


def exec(task, data):
    if task == 'reload':
        return reload_nginx()


def callback(ch, method, properties, body):
    try:
        data = json.loads(body.decode())
        if data['task']:
            task_name = data['task']
            print(f' [x] performing task: {task_name}')
            r = exec(task_name, data)
            if (r[0]):
                print(f'  {r[1]}')
            else:
                print('  ERROR')
    except Exception as e:
        print(" [x] Unknown error")
        print(e)


channel.basic_consume(queue=queue_name,
                      on_message_callback=callback,
                      auto_ack=True)

channel.start_consuming()